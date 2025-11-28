package com.mintportal.service;

import com.mintportal.entity.Build;
import com.mintportal.entity.BuildStageResult;
import com.mintportal.entity.Layer;
import com.mintportal.entity.Project;
import com.mintportal.repository.BuildRepository;
import com.mintportal.repository.BuildStageResultRepository;
import com.mintportal.repository.LayerRepository;
import com.mintportal.repository.ProjectRepository;
import com.mintportal.repository.BuildQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BuildService {

    private final BuildRepository buildRepository;
    private final BuildStageResultRepository stageResultRepository;
    private final ProjectRepository projectRepository;
    private final LayerRepository layerRepository;
    private final BuildQueueRepository buildQueueRepository;

    public List<Build> findAll() {
        return buildRepository.findAll();
    }

    public Build findById(String id) {
        return buildRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Build not found: " + id));
    }

    public Optional<Build> findByBambooBuildKey(String bambooBuildKey) {
        return buildRepository.findByBambooBuildKey(bambooBuildKey);
    }

    public List<Build> findByProjectId(String projectId) {
        return buildRepository.findByProjectId(projectId);
    }

    public List<Build> findByLayerId(String layerId) {
        return buildRepository.findByLayerId(layerId);
    }

    public List<Build> findByFilters(String projectId, String layerId, String status) {
        return buildRepository.findByFilters(projectId, layerId, status);
    }

    public List<Build> findActiveBuilds() {
        return buildRepository.findActiveBuilds();
    }

    public List<Build> findRecentBuilds(int limit) {
        return buildRepository.findRecentBuilds(PageRequest.of(0, limit));
    }

    public List<Build> findRecentByProjectId(String projectId, int limit) {
        return buildRepository.findRecentByProjectId(projectId, PageRequest.of(0, limit)).getContent();
    }

    public List<Build> findRecentByLayerId(String layerId, int limit) {
        return buildRepository.findRecentByLayerId(layerId, PageRequest.of(0, limit));
    }

    /**
     * 빌드 상세 조회 (스테이지 결과 포함)
     */
    public Build findByIdWithStages(String id) {
        Build build = findById(id);
        // Lazy loading이므로 스테이지 결과를 명시적으로 로드
        List<BuildStageResult> stages = stageResultRepository.findByBuildIdOrderByStageOrder(id);
        build.setStageResults(stages);
        return build;
    }

    /**
     * 빌드 스테이지 결과 조회
     */
    public List<BuildStageResult> getStageResults(String buildId) {
        return stageResultRepository.findByBuildIdOrderByStageOrder(buildId);
    }

    /**
     * 특정 스테이지 결과 조회
     */
    public Optional<BuildStageResult> getStageResult(String buildId, String stageName) {
        return stageResultRepository.findByBuildIdAndStageName(buildId, stageName);
    }

    @Transactional
    public Build updateStatus(String id, String status) {
        Build build = findById(id);

        if ("success".equals(status) || "failed".equals(status) || "cancelled".equals(status)) {
            build.complete(status);
        } else if ("running".equals(status)) {
            build.start();
        } else {
            build.setStatus(status);
        }

        return buildRepository.save(build);
    }

    @Transactional
    public Build updateReleaseStatus(String id, String releaseStatus) {
        Build build = findById(id);
        build.setReleaseStatus(releaseStatus);
        return buildRepository.save(build);
    }

    @Transactional
    public BuildStageResult updateStageStatus(String buildId, String stageName, String status) {
        BuildStageResult stage = stageResultRepository.findByBuildIdAndStageName(buildId, stageName)
                .orElseThrow(() -> new RuntimeException(
                        "Stage not found: " + stageName + " for build " + buildId));

        if ("running".equals(status)) {
            stage.start();
        } else if ("success".equals(status)) {
            stage.complete(true, null);
        } else if ("failed".equals(status)) {
            stage.complete(false, null);
        } else if ("skipped".equals(status)) {
            stage.skip();
        } else {
            stage.setStatus(status);
        }

        stageResultRepository.save(stage);

        // 모든 스테이지 완료 확인 및 빌드 상태 업데이트
        if (stageResultRepository.areAllStagesComplete(buildId)) {
            String buildStatus = stageResultRepository.calculateBuildStatus(buildId);
            Build build = findById(buildId);
            build.complete(buildStatus);
            buildRepository.save(build);
        }

        return stage;
    }

    @Transactional
    public BuildStageResult updateStageResult(String buildId, String stageName,
                                               Map<String, Object> result,
                                               int errorCount, int warningCount) {
        BuildStageResult stage = stageResultRepository.findByBuildIdAndStageName(buildId, stageName)
                .orElseThrow(() -> new RuntimeException(
                        "Stage not found: " + stageName + " for build " + buildId));

        stage.setStageResult(result);
        stage.setErrorCount(errorCount);
        stage.setWarningCount(warningCount);

        return stageResultRepository.save(stage);
    }

    @Transactional
    public void delete(String id) {
        // 스테이지 결과는 cascade로 삭제됨
        buildRepository.deleteById(id);
    }

    /**
     * 대시보드 통계
     */
    public Map<String, Object> getStats() {
        long totalBuilds = buildRepository.countTotalBuilds();
        long successBuilds = buildRepository.countSuccessBuilds();
        long runningBuilds = buildRepository.countRunningBuilds();
        long failedBuilds = buildRepository.countFailedBuilds();
        long queuedBuilds = buildQueueRepository.countWaitingBuilds();
        long activeProjects = projectRepository.countActiveProjects();

        Double successRate = buildRepository.calculateSuccessRate();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBuilds", totalBuilds);
        stats.put("successBuilds", successBuilds);
        stats.put("runningBuilds", runningBuilds);
        stats.put("failedBuilds", failedBuilds);
        stats.put("queuedBuilds", queuedBuilds);
        stats.put("activeProjects", activeProjects);
        stats.put("successRate", successRate != null ? Math.round(successRate * 10) / 10.0 : 0);

        return stats;
    }

    /**
     * 스테이지별 통계
     */
    public List<Map<String, Object>> getStageStats() {
        List<Object[]> avgDurations = stageResultRepository.getAverageDurationByStage();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : avgDurations) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("stageName", row[0]);
            stat.put("avgDuration", row[1]);
            result.add(stat);
        }

        return result;
    }
}
