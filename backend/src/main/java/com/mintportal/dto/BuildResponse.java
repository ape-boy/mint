package com.mintportal.dto;

import com.mintportal.entity.Build;
import com.mintportal.entity.BuildStageResult;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 빌드 응답 DTO (스테이지 결과 포함)
 */
@Data
@Builder
public class BuildResponse {
    private String id;
    private String projectId;
    private String projectName;
    private String layerId;
    private String layerName;
    private String layerType;

    private Integer round;
    private Integer buildNumber;
    private String status;
    private String releaseStatus;

    private String fwName;
    private String bambooBuildKey;
    private Integer bambooBuildNumber;

    private String triggerType;
    private String triggeredByName;

    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;
    private Integer durationSeconds;

    private Map<String, Object> buildSnapshot;
    private Map<String, Object> artifacts;

    private List<StageResultDto> stages;

    @Data
    @Builder
    public static class StageResultDto {
        private String id;
        private String stageName;
        private Integer stageOrder;
        private String status;
        private Integer errorCount;
        private Integer warningCount;
        private Integer durationSeconds;
        private OffsetDateTime startedAt;
        private OffsetDateTime finishedAt;
        private Map<String, Object> stageResult;
    }

    public static BuildResponse fromEntity(Build build) {
        BuildResponseBuilder builder = BuildResponse.builder()
                .id(build.getId())
                .projectId(build.getProject().getId())
                .projectName(build.getProject().getProjectName())
                .layerId(build.getLayer().getId())
                .layerName(build.getLayer().getName())
                .layerType(build.getLayer().getType())
                .round(build.getRound())
                .buildNumber(build.getBuildNumber())
                .status(build.getStatus())
                .releaseStatus(build.getReleaseStatus())
                .fwName(build.getFwName())
                .bambooBuildKey(build.getBambooBuildKey())
                .bambooBuildNumber(build.getBambooBuildNumber())
                .triggerType(build.getTriggerType())
                .startedAt(build.getStartedAt())
                .finishedAt(build.getFinishedAt())
                .durationSeconds(build.getDurationSeconds())
                .buildSnapshot(build.getBuildSnapshot())
                .artifacts(build.getArtifacts());

        if (build.getTriggeredBy() != null) {
            builder.triggeredByName(build.getTriggeredBy().getName());
        }

        if (build.getStageResults() != null) {
            builder.stages(build.getStageResults().stream()
                    .map(BuildResponse::toStageDto)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    private static StageResultDto toStageDto(BuildStageResult stage) {
        return StageResultDto.builder()
                .id(stage.getId())
                .stageName(stage.getStageName())
                .stageOrder(stage.getStageOrder())
                .status(stage.getStatus())
                .errorCount(stage.getErrorCount())
                .warningCount(stage.getWarningCount())
                .durationSeconds(stage.getDurationSeconds())
                .startedAt(stage.getStartedAt())
                .finishedAt(stage.getFinishedAt())
                .stageResult(stage.getStageResult())
                .build();
    }
}
