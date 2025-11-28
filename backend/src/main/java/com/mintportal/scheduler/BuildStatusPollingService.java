package com.mintportal.scheduler;

import com.mintportal.bamboo.BambooClient;
import com.mintportal.bamboo.BambooBuildStatus;
import com.mintportal.entity.Build;
import com.mintportal.entity.BuildStageResult;
import com.mintportal.repository.BuildRepository;
import com.mintportal.repository.BuildStageResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bamboo 빌드 상태 폴링 서비스
 * - 실행 중인 빌드의 상태를 주기적으로 확인
 * - 각 스테이지(Build, SAM, Coverity)의 결과를 개별 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BuildStatusPollingService {

    private final BuildRepository buildRepository;
    private final BuildStageResultRepository stageResultRepository;
    private final BambooClient bambooClient;

    @Value("${scheduler.enabled:true}")
    private boolean schedulerEnabled;

    /**
     * 빌드 상태 폴링 (30초마다 실행)
     */
    @Scheduled(fixedDelayString = "${bamboo.polling-interval:30000}")
    @Transactional
    public void pollBuildStatus() {
        if (!schedulerEnabled) {
            return;
        }

        List<Build> activeBuilds = buildRepository.findActiveBuilds();
        if (activeBuilds.isEmpty()) {
            return;
        }

        log.debug("Polling {} active builds", activeBuilds.size());

        for (Build build : activeBuilds) {
            if (build.getBambooBuildKey() == null) {
                continue;
            }

            pollSingleBuild(build);
        }
    }

    private void pollSingleBuild(Build build) {
        bambooClient.getBuildStatus(build.getBambooBuildKey())
                .subscribe(
                        status -> processBuildStatus(build, status),
                        error -> log.error("Failed to poll build {}: {}", build.getId(), error.getMessage())
                );
    }

    @Transactional
    public void processBuildStatus(Build build, BambooBuildStatus status) {
        try {
            // 스테이지별 상태 업데이트
            if (status.getStages() != null && status.getStages().getStage() != null) {
                for (BambooBuildStatus.Stage bambooStage : status.getStages().getStage()) {
                    updateStageFromBamboo(build, bambooStage);
                }
            }

            // 빌드 완료 처리
            if (status.isFinished()) {
                handleBuildComplete(build, status);
            }
        } catch (Exception e) {
            log.error("Error processing build status for {}: {}", build.getId(), e.getMessage(), e);
        }
    }

    private void updateStageFromBamboo(Build build, BambooBuildStatus.Stage bambooStage) {
        String stageName = mapBambooStageName(bambooStage.getName());
        if (stageName == null) {
            return;
        }

        stageResultRepository.findByBuildIdAndStageName(build.getId(), stageName)
                .ifPresent(stage -> {
                    String newStatus = mapBambooState(bambooStage.getState());

                    if (!newStatus.equals(stage.getStatus())) {
                        if ("running".equals(newStatus) && "pending".equals(stage.getStatus())) {
                            stage.start();
                        } else if ("success".equals(newStatus) || "failed".equals(newStatus)) {
                            Map<String, Object> result = extractStageResult(bambooStage, stageName);
                            stage.complete("success".equals(newStatus), result);
                        }
                        stageResultRepository.save(stage);
                        log.debug("Stage {} updated to {} for build {}", stageName, newStatus, build.getId());
                    }
                });
    }

    private String mapBambooStageName(String bamboostageName) {
        if (bamboostageName == null) return null;
        String normalized = bamboostageName.toLowerCase().replaceAll("[^a-z]", "");

        if (normalized.contains("build") || normalized.contains("compile")) {
            return "Build";
        } else if (normalized.contains("sam") || normalized.contains("static")) {
            return "SAM";
        } else if (normalized.contains("coverity") || normalized.contains("cov")) {
            return "Coverity";
        }
        return null;
    }

    private String mapBambooState(String bambooState) {
        if (bambooState == null) return "pending";

        return switch (bambooState.toLowerCase()) {
            case "successful", "success" -> "success";
            case "failed", "failure" -> "failed";
            case "in progress", "building", "running" -> "running";
            case "queued", "pending", "notbuilt" -> "pending";
            default -> "pending";
        };
    }

    private Map<String, Object> extractStageResult(BambooBuildStatus.Stage stage, String stageName) {
        Map<String, Object> result = new HashMap<>();

        switch (stageName) {
            case "Build" -> {
                result.put("compile_success", "Successful".equalsIgnoreCase(stage.getState()));
                // Bamboo에서 추가 정보 파싱
            }
            case "SAM" -> {
                result.put("score", 0);  // 실제 값은 Bamboo 응답에서 추출
                result.put("issues", List.of());
            }
            case "Coverity" -> {
                result.put("defect_count", 0);  // 실제 값은 Bamboo 응답에서 추출
                result.put("critical", 0);
                result.put("high", 0);
                result.put("medium", 0);
            }
        }

        return result;
    }

    @Transactional
    public void handleBuildComplete(Build build, BambooBuildStatus status) {
        // 모든 스테이지 완료 확인
        List<BuildStageResult> stages = stageResultRepository.findByBuildIdOrderByStageOrder(build.getId());

        // 미완료 스테이지 처리
        for (BuildStageResult stage : stages) {
            if ("pending".equals(stage.getStatus()) || "running".equals(stage.getStatus())) {
                // 빌드가 완료되었는데 스테이지가 미완료면 상태에 따라 처리
                String finalStatus = status.isSuccessful() ? "success" : "failed";
                stage.complete(status.isSuccessful(), Map.of("auto_completed", true));
                stageResultRepository.save(stage);
            }
        }

        // 최종 빌드 상태 결정
        String buildStatus = stageResultRepository.calculateBuildStatus(build.getId());

        // 빌드 완료 처리
        build.complete(buildStatus);

        // 품질 메트릭 추출
        Map<String, Object> artifacts = extractArtifacts(status);
        if (!artifacts.isEmpty()) {
            build.setArtifacts(artifacts);
        }

        // Release Layer의 경우 릴리즈 상태 설정
        if ("release".equals(build.getLayer().getType())) {
            updateReleaseStatus(build);
        }

        buildRepository.save(build);
        log.info("Build {} completed with status: {}", build.getId(), buildStatus);
    }

    private Map<String, Object> extractArtifacts(BambooBuildStatus status) {
        Map<String, Object> artifacts = new HashMap<>();
        // Bamboo 응답에서 아티팩트 URL 추출
        if (status.getArtifacts() != null) {
            artifacts.put("artifacts", status.getArtifacts());
        }
        return artifacts;
    }

    private void updateReleaseStatus(Build build) {
        // 모든 스테이지가 성공했는지 확인
        long failedCount = stageResultRepository.countFailedStages(build.getId());

        if (failedCount == 0 && "success".equals(build.getStatus())) {
            // 모든 스테이지 성공 → TR 가능
            build.setReleaseStatus("available");
        } else {
            // 일부 실패 → 승인 필요
            build.setReleaseStatus("pending_approval");
        }
    }

    /**
     * Webhook을 통한 스테이지 결과 수신 처리
     * Bamboo에서 각 스테이지 완료 시 호출됨
     */
    @Transactional
    public void handleStageWebhook(String bambooBuildKey, String stageName, Map<String, Object> payload) {
        Build build = buildRepository.findByBambooBuildKey(bambooBuildKey)
                .orElse(null);

        if (build == null) {
            log.warn("Received webhook for unknown build: {}", bambooBuildKey);
            return;
        }

        String mappedStageName = mapBambooStageName(stageName);
        if (mappedStageName == null) {
            log.warn("Unknown stage name: {}", stageName);
            return;
        }

        stageResultRepository.findByBuildIdAndStageName(build.getId(), mappedStageName)
                .ifPresent(stage -> {
                    stage.handleBambooResponse(payload);

                    boolean success = "success".equalsIgnoreCase((String) payload.get("status"));
                    stage.complete(success, payload);

                    stageResultRepository.save(stage);
                    log.info("Stage {} completed via webhook for build {}", mappedStageName, build.getId());

                    // 모든 스테이지 완료 확인
                    if (stageResultRepository.areAllStagesComplete(build.getId())) {
                        String finalStatus = stageResultRepository.calculateBuildStatus(build.getId());
                        build.complete(finalStatus);
                        buildRepository.save(build);
                    }
                });
    }
}
