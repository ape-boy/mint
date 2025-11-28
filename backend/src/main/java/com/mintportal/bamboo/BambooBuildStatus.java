package com.mintportal.bamboo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BambooBuildStatus {

    @JsonProperty("key")
    private String key;

    @JsonProperty("state")
    private String state;  // "Unknown", "Successful", "Failed", "In Progress"

    @JsonProperty("lifeCycleState")
    private String lifeCycleState;  // "Queued", "Pending", "InProgress", "Finished"

    @JsonProperty("buildState")
    private String buildState;  // "Unknown", "Successful", "Failed"

    @JsonProperty("buildNumber")
    private Integer buildNumber;

    @JsonProperty("buildStartedTime")
    private String buildStartedTime;

    @JsonProperty("buildCompletedTime")
    private String buildCompletedTime;

    @JsonProperty("buildDurationInSeconds")
    private Long buildDurationInSeconds;

    @JsonProperty("buildDuration")
    private Long buildDuration;

    @JsonProperty("buildDurationDescription")
    private String buildDurationDescription;

    @JsonProperty("stages")
    private StagesWrapper stages;

    @JsonProperty("successfulTestCount")
    private Integer successfulTestCount;

    @JsonProperty("failedTestCount")
    private Integer failedTestCount;

    @JsonProperty("quarantinedTestCount")
    private Integer quarantinedTestCount;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StagesWrapper {
        @JsonProperty("stage")
        private List<Stage> stage;

        @JsonProperty("size")
        private Integer size;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stage {
        private String name;
        private String state;
        private List<Result> results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String key;
        private String state;
        private String lifeCycleState;
    }

    // Helper methods
    public boolean isFinished() {
        return "Finished".equalsIgnoreCase(lifeCycleState);
    }

    public boolean isRunning() {
        return "InProgress".equalsIgnoreCase(lifeCycleState);
    }

    public boolean isQueued() {
        return "Queued".equalsIgnoreCase(lifeCycleState) || "Pending".equalsIgnoreCase(lifeCycleState);
    }

    public boolean isSuccessful() {
        return "Successful".equalsIgnoreCase(state) || "Successful".equalsIgnoreCase(buildState);
    }

    public boolean isFailed() {
        return "Failed".equalsIgnoreCase(state) || "Failed".equalsIgnoreCase(buildState);
    }
}
