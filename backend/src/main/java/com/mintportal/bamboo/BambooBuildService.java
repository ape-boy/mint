package com.mintportal.bamboo;

import com.mintportal.entity.Build;
import com.mintportal.entity.Layer;
import com.mintportal.service.BuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class BambooBuildService {

    private final BambooClient bambooClient;
    private final BuildService buildService;

    // Track Bamboo build keys to our build IDs
    private final Map<String, String> bambooBuildMapping = new ConcurrentHashMap<>();

    /**
     * Trigger a new build in Bamboo
     */
    public Build triggerBuild(String projectId, String layerId, Map<String, Object> triggeredBy, String bambooPlanKey) {
        // Create build record first
        Build build = buildService.create(projectId, layerId, triggeredBy);

        // Prepare build variables from project config
        Map<String, String> variables = new HashMap<>();
        if (build.getScmConfig() != null) {
            Map<String, Object> scm = build.getScmConfig();
            if (scm.get("branch") != null) {
                variables.put("branch", scm.get("branch").toString());
            }
            if (scm.get("revisionTag") != null) {
                variables.put("revisionTag", scm.get("revisionTag").toString());
            }
        }

        // Trigger Bamboo build
        bambooClient.triggerBuild(bambooPlanKey, variables)
                .subscribe(
                        result -> {
                            log.info("Bamboo build triggered: {} for build {}", result.getBuildResultKey(), build.getId());
                            bambooBuildMapping.put(result.getBuildResultKey(), build.getId());
                            buildService.updateStatus(build.getId(), "running");
                        },
                        error -> {
                            log.error("Failed to trigger Bamboo build for build {}: {}", build.getId(), error.getMessage());
                            buildService.updateStatus(build.getId(), "failed");
                        }
                );

        return build;
    }

    /**
     * Cancel a running build in Bamboo
     */
    public void cancelBuild(String buildId) {
        // Find Bamboo build key
        String bambooBuildKey = bambooBuildMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(buildId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (bambooBuildKey != null) {
            bambooClient.stopBuild(bambooBuildKey)
                    .subscribe(
                            v -> {
                                log.info("Bamboo build cancelled: {}", bambooBuildKey);
                                buildService.updateStatus(buildId, "cancelled");
                                bambooBuildMapping.remove(bambooBuildKey);
                            },
                            error -> log.error("Failed to cancel Bamboo build: {}", error.getMessage())
                    );
        } else {
            // No Bamboo mapping, just update status
            buildService.updateStatus(buildId, "cancelled");
        }
    }

    /**
     * Poll Bamboo for build status updates
     * Runs every 30 seconds by default
     */
    @Scheduled(fixedDelayString = "${bamboo.polling-interval:30000}")
    public void pollBuildStatus() {
        if (bambooBuildMapping.isEmpty()) {
            return;
        }

        log.debug("Polling {} active Bamboo builds", bambooBuildMapping.size());

        for (Map.Entry<String, String> entry : bambooBuildMapping.entrySet()) {
            String bambooBuildKey = entry.getKey();
            String buildId = entry.getValue();

            bambooClient.getBuildStatus(bambooBuildKey)
                    .subscribe(
                            status -> processBuildStatus(buildId, bambooBuildKey, status),
                            error -> log.error("Failed to get status for {}: {}", bambooBuildKey, error.getMessage())
                    );
        }
    }

    private void processBuildStatus(String buildId, String bambooBuildKey, BambooBuildStatus status) {
        try {
            Build build = buildService.findById(buildId);

            // Update stages based on Bamboo stages
            if (status.getStages() != null && status.getStages().getStage() != null) {
                updateBuildStages(build, status);
            }

            // Update overall build status
            if (status.isFinished()) {
                String newStatus = status.isSuccessful() ? "success" : "failed";
                buildService.updateStatus(buildId, newStatus);

                // Update quality metrics if available
                updateQualityMetrics(build, status);

                // Calculate release criteria for release layers
                if (isReleaseLayer(build)) {
                    updateReleaseCriteria(build);
                }

                // Remove from tracking
                bambooBuildMapping.remove(bambooBuildKey);
                log.info("Build {} finished with status: {}", buildId, newStatus);
            } else if (status.isRunning() && !"running".equals(build.getStatus())) {
                buildService.updateStatus(buildId, "running");
            }
        } catch (Exception e) {
            log.error("Error processing build status for {}: {}", buildId, e.getMessage());
        }
    }

    private void updateBuildStages(Build build, BambooBuildStatus status) {
        List<Map<String, Object>> buildStages = build.getStages();

        for (BambooBuildStatus.Stage bambooStage : status.getStages().getStage()) {
            // Find matching stage in build
            for (Map<String, Object> stage : buildStages) {
                if (matchStageName(stage.get("name").toString(), bambooStage.getName())) {
                    String stageStatus = mapBambooState(bambooStage.getState());
                    stage.put("status", stageStatus);

                    if ("success".equals(stageStatus) || "failed".equals(stageStatus)) {
                        stage.put("finishedAt", OffsetDateTime.now().toString());
                    }
                    break;
                }
            }
        }

        buildService.updateStages(build.getId(), buildStages);
    }

    private boolean matchStageName(String buildStageName, String bambooStageName) {
        // Normalize names for comparison
        String normalized1 = buildStageName.toLowerCase().replaceAll("[^a-z0-9]", "");
        String normalized2 = bambooStageName.toLowerCase().replaceAll("[^a-z0-9]", "");
        return normalized1.equals(normalized2) || normalized1.contains(normalized2) || normalized2.contains(normalized1);
    }

    private String mapBambooState(String bambooState) {
        if (bambooState == null) return "pending";

        return switch (bambooState.toLowerCase()) {
            case "successful" -> "success";
            case "failed" -> "failed";
            case "in progress", "building" -> "running";
            case "queued", "pending" -> "pending";
            default -> "pending";
        };
    }

    private void updateQualityMetrics(Build build, BambooBuildStatus status) {
        Map<String, Object> qualityMetrics = new HashMap<>();

        // Check test results
        if (status.getSuccessfulTestCount() != null || status.getFailedTestCount() != null) {
            int passed = status.getSuccessfulTestCount() != null ? status.getSuccessfulTestCount() : 0;
            int failed = status.getFailedTestCount() != null ? status.getFailedTestCount() : 0;
            int total = passed + failed;

            Map<String, Object> testMetrics = new HashMap<>();
            testMetrics.put("status", failed == 0 ? "pass" : "fail");
            testMetrics.put("passedTests", passed);
            testMetrics.put("failedTests", failed);
            if (total > 0) {
                testMetrics.put("score", (int) ((double) passed / total * 100));
            }

            qualityMetrics.put("onboardTest", testMetrics);
        }

        // Default metrics for other stages (would be populated from actual stage results)
        for (Map<String, Object> stage : build.getStages()) {
            String stageName = stage.get("name").toString().toLowerCase().replace(" ", "");
            String stageStatus = stage.get("status").toString();

            if (!qualityMetrics.containsKey(stageName) && !"build".equals(stageName)) {
                Map<String, Object> metric = new HashMap<>();
                metric.put("status", "success".equals(stageStatus) ? "pass" :
                        "failed".equals(stageStatus) ? "fail" : "skipped");
                qualityMetrics.put(stageName, metric);
            }
        }

        buildService.updateQualityMetrics(build.getId(), qualityMetrics);
    }

    private boolean isReleaseLayer(Build build) {
        Layer layer = build.getLayer();
        return layer != null && "release".equals(layer.getType());
    }

    private void updateReleaseCriteria(Build build) {
        Map<String, Object> qualityMetrics = build.getQualityMetrics();
        if (qualityMetrics == null) qualityMetrics = new HashMap<>();

        Map<String, Object> criteria = new HashMap<>();

        // Check required stages
        boolean coverityPassed = checkMetricPassed(qualityMetrics, "coverity");
        boolean samPassed = checkMetricPassed(qualityMetrics, "sam");
        boolean onboardTestPassed = checkMetricPassed(qualityMetrics, "onboardtest", "onboardTest");
        boolean blackduckPassed = checkMetricPassed(qualityMetrics, "blackduck");

        boolean allStagesPassed = build.getStages().stream()
                .allMatch(s -> "success".equals(s.get("status")) || "skipped".equals(s.get("status")));

        boolean overallPassed = allStagesPassed && coverityPassed && samPassed && onboardTestPassed && blackduckPassed;

        criteria.put("allStagesPassed", allStagesPassed);
        criteria.put("coverityPassed", coverityPassed);
        criteria.put("samPassed", samPassed);
        criteria.put("onboardTestPassed", onboardTestPassed);
        criteria.put("blackduckPassed", blackduckPassed);
        criteria.put("overallPassed", overallPassed);

        buildService.updateReleaseCriteria(build.getId(), criteria);
    }

    @SuppressWarnings("unchecked")
    private boolean checkMetricPassed(Map<String, Object> qualityMetrics, String... keys) {
        for (String key : keys) {
            Object metric = qualityMetrics.get(key);
            if (metric instanceof Map) {
                Object status = ((Map<String, Object>) metric).get("status");
                if ("pass".equals(status)) return true;
            }
        }
        // If metric doesn't exist, assume passed (stage was skipped)
        return true;
    }
}
