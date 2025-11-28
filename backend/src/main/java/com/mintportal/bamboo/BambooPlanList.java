package com.mintportal.bamboo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BambooPlanList {

    @JsonProperty("plans")
    private Plans plans;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Plans {
        @JsonProperty("plan")
        private List<Plan> plan;

        @JsonProperty("size")
        private Integer size;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Plan {
        @JsonProperty("key")
        private String key;

        @JsonProperty("name")
        private String name;

        @JsonProperty("shortKey")
        private String shortKey;

        @JsonProperty("shortName")
        private String shortName;

        @JsonProperty("enabled")
        private Boolean enabled;
    }
}
