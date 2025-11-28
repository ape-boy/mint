package com.mintportal.bamboo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BambooPlanDetails {

    @JsonProperty("key")
    private String key;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("isBuilding")
    private Boolean isBuilding;

    @JsonProperty("averageBuildTimeInSeconds")
    private Long averageBuildTimeInSeconds;

    @JsonProperty("stages")
    private StagesWrapper stages;

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
        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("plans")
        private PlansWrapper plans;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlansWrapper {
        @JsonProperty("plan")
        private List<StagePlan> plan;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StagePlan {
        @JsonProperty("key")
        private String key;

        @JsonProperty("name")
        private String name;
    }
}
