package com.mintportal.scheduler;

import com.mintportal.bamboo.BambooClient;
import com.mintportal.bamboo.BambooParamsGenerator;
import com.mintportal.entity.*;
import com.mintportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * 빌드 스케줄러 서비스
 * - 빌드 큐에서 대기 중인 빌드를 처리
 * - 동시 빌드 수 제한
 * - 재시도 로직 처리
 * - Bamboo 빌드 상태 폴링
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BuildSchedulerService {

    private final BuildQueueRepository buildQueueRepository;
    private final BuildRequestRepository buildRequestRepository;
    private final BuildRepository buildRepository;
    private final BuildStageResultRepository stageResultRepository;
    private final ProjectRepository projectRepository;
    private final LayerRepository layerRepository;
    private final UserRepository userRepository;

    private final BambooClient bambooClient;
    private final BambooParamsGenerator paramsGenerator;

    @Value("${scheduler.max-concurrent-builds:5}")
    private int maxConcurrentBuilds;

    @Value("${scheduler.enabled:true}")
    private boolean schedulerEnabled;

    /**
     * 빌드 큐 처리 (10초마다 실행)
     */
    @Scheduled(fixedDelayString = "${scheduler.queue-poll-interval:10000}")
    @Transactional
    public void processQueue() {
        if (!schedulerEnabled) {
            return;
        }

        long runningCount = buildQueueRepository.countProcessingBuilds();
        if (runningCount >= maxConcurrentBuilds) {
            log.debug("Max concurrent builds reached ({}/{}), waiting...", runningCount, maxConcurrentBuilds);
            return;
        }

        int availableSlots = (int) (maxConcurrentBuilds - runningCount);
        List<BuildQueue> waitingBuilds = buildQueueRepository.findWaitingBuilds();

        for (int i = 0; i < Math.min(availableSlots, waitingBuilds.size()); i++) {
            BuildQueue queueItem = waitingBuilds.get(i);
            try {
                processQueueItem(queueItem);
            } catch (Exception e) {
                log.error("Error processing queue item {}: {}", queueItem.getId(), e.getMessage());
                queueItem.markFailed(e.getMessage());
                buildQueueRepository.save(queueItem);
            }
        }
    }

    /**
     * 큐 아이템 처리 - Bamboo로 빌드 요청 전송
     */
    @Transactional
    public void processQueueItem(BuildQueue queueItem) {
        log.info("Processing queue item: {} (project: {}, layer: {})",
                queueItem.getId(),
                queueItem.getProject().getId(),
                queueItem.getLayer().getId());

        queueItem.startProcessing();
        buildQueueRepository.save(queueItem);

        Project project = queueItem.getProject();
        Layer layer = queueItem.getLayer();
        User requester = queueItem.getRequester();

        // Bamboo 파라미터 생성
        Map<String, Object> bambooParams = paramsGenerator.generateParams(
                project,
                layer,
                requester,
                queueItem.getScmOverride(),
                queueItem.getBuildOverride()
        );

        // BuildRequest 생성 (전송 파라미터 기록)
        BuildRequest request = BuildRequest.builder()
                .queue(queueItem)
                .project(project)
                .layer(layer)
                .requestParams(bambooParams)
                .bambooPlanKey(project.getPlanId())
                .requestStatus("sent")
                .build();
        buildRequestRepository.save(request);

        // 빌드 스냅샷 생성
        Map<String, Object> buildSnapshot = paramsGenerator.createBuildSnapshot(
                project, layer,
                queueItem.getScmOverride(),
                queueItem.getBuildOverride()
        );

        // Build 엔티티 생성
        Integer nextRound = buildRepository.findMaxRoundByLayerId(layer.getId());
        nextRound = (nextRound == null ? 0 : nextRound) + 1;

        Integer nextBuildNumber = buildRepository.findMaxBuildNumberByProjectId(project.getId());
        nextBuildNumber = (nextBuildNumber == null ? 0 : nextBuildNumber) + 1;

        Build build = Build.builder()
                .request(request)
                .project(project)
                .layer(layer)
                .round(nextRound)
                .buildNumber(nextBuildNumber)
                .status("pending")
                .buildSnapshot(buildSnapshot)
                .triggeredBy(requester)
                .triggerType(queueItem.getReqMethod())
                .build();
        buildRepository.save(build);

        // Build Stage Results 생성 (Build, SAM, Coverity)
        createStageResults(build, layer);

        // Bamboo API 호출
        triggerBambooBuild(queueItem, request, build, bambooParams);
    }

    /**
     * 빌드 스테이지 결과 초기 생성
     */
    private void createStageResults(Build build, Layer layer) {
        List<BuildStageResult> stages = new ArrayList<>();

        // Build stage (항상 활성)
        stages.add(createStageResult(build, "Build", 1,
                layer.getBuildEnabled() != null && layer.getBuildEnabled()));

        // SAM stage
        stages.add(createStageResult(build, "SAM", 2,
                layer.getSamEnabled() != null && layer.getSamEnabled()));

        // Coverity stage
        stages.add(createStageResult(build, "Coverity", 3,
                layer.getCoverityEnabled() != null && layer.getCoverityEnabled()));

        stageResultRepository.saveAll(stages);
    }

    private BuildStageResult createStageResult(Build build, String stageName, int order, boolean enabled) {
        return BuildStageResult.builder()
                .build(build)
                .stageName(stageName)
                .stageOrder(order)
                .status(enabled ? "pending" : "skipped")
                .build();
    }

    /**
     * Bamboo 빌드 트리거
     */
    private void triggerBambooBuild(BuildQueue queueItem, BuildRequest request, Build build, Map<String, Object> params) {
        String planKey = request.getBambooPlanKey();

        // 파라미터를 String 맵으로 변환 (Bamboo API 요구사항)
        Map<String, String> stringParams = new HashMap<>();
        params.forEach((key, value) -> {
            if (value != null) {
                if (value instanceof Map || value instanceof List) {
                    // JSON으로 직렬화
                    try {
                        stringParams.put(key, new com.fasterxml.jackson.databind.ObjectMapper()
                                .writeValueAsString(value));
                    } catch (Exception e) {
                        stringParams.put(key, value.toString());
                    }
                } else {
                    stringParams.put(key, value.toString());
                }
            }
        });

        bambooClient.triggerBuild(planKey, stringParams)
                .subscribe(
                        result -> handleBuildTriggered(queueItem, request, build, result),
                        error -> handleBuildTriggerError(queueItem, request, build, error)
                );
    }

    private void handleBuildTriggered(BuildQueue queueItem, BuildRequest request, Build build,
                                       com.mintportal.bamboo.BambooBuildResult result) {
        log.info("Bamboo build triggered successfully: {} for build {}",
                result.getBuildResultKey(), build.getId());

        // Request 업데이트
        request.markAccepted(result.getBuildResultKey());
        buildRequestRepository.save(request);

        // Build 업데이트
        build.setBambooBuildKey(result.getBuildResultKey());
        build.setBambooBuildNumber(result.getBuildNumber());
        build.start();
        buildRepository.save(build);

        // Queue 완료 처리
        queueItem.markCompleted();
        buildQueueRepository.save(queueItem);

        // 첫 번째 스테이지 시작
        stageResultRepository.findByBuildIdAndStageName(build.getId(), "Build")
                .ifPresent(stage -> {
                    stage.start();
                    stageResultRepository.save(stage);
                });
    }

    private void handleBuildTriggerError(BuildQueue queueItem, BuildRequest request, Build build, Throwable error) {
        log.error("Failed to trigger Bamboo build for build {}: {}", build.getId(), error.getMessage());

        // Request 에러 처리
        request.markError(error.getMessage());
        buildRequestRepository.save(request);

        // Build 실패 처리
        build.complete("failed");
        buildRepository.save(build);

        // Queue 재시도 처리
        queueItem.markFailed(error.getMessage());
        buildQueueRepository.save(queueItem);
    }

    /**
     * 빌드를 큐에 추가
     */
    @Transactional
    public BuildQueue enqueue(String projectId, String layerId, String requesterId,
                              String reqMethod, int priority,
                              Map<String, Object> scmOverride,
                              Map<String, Object> buildOverride) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        Layer layer = layerRepository.findById(layerId)
                .orElseThrow(() -> new IllegalArgumentException("Layer not found: " + layerId));
        User requester = requesterId != null ?
                userRepository.findById(requesterId).orElse(null) : null;

        BuildQueue queueItem = BuildQueue.builder()
                .project(project)
                .layer(layer)
                .requester(requester)
                .reqMethod(reqMethod != null ? reqMethod : "manual")
                .priority(priority)
                .scmOverride(scmOverride)
                .buildOverride(buildOverride)
                .build();

        return buildQueueRepository.save(queueItem);
    }

    /**
     * 큐 아이템 취소
     */
    @Transactional
    public void cancelQueueItem(String queueId) {
        BuildQueue queueItem = buildQueueRepository.findById(queueId)
                .orElseThrow(() -> new IllegalArgumentException("Queue item not found: " + queueId));

        if ("waiting".equals(queueItem.getQueueStatus())) {
            queueItem.setQueueStatus("cancelled");
            buildQueueRepository.save(queueItem);
            log.info("Queue item {} cancelled", queueId);
        } else {
            throw new IllegalStateException("Cannot cancel queue item in status: " + queueItem.getQueueStatus());
        }
    }

    /**
     * 큐 상태 조회
     */
    public Map<String, Object> getQueueStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("waiting", buildQueueRepository.countWaitingBuilds());
        status.put("processing", buildQueueRepository.countProcessingBuilds());
        status.put("maxConcurrent", maxConcurrentBuilds);
        status.put("schedulerEnabled", schedulerEnabled);
        return status;
    }
}
