package com.mintportal.bamboo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BambooBuildResult {

    @JsonProperty("buildResultKey")
    private String buildResultKey;

    @JsonProperty("planKey")
    private String planKey;

    @JsonProperty("buildNumber")
    private Integer buildNumber;

    @JsonProperty("triggerReason")
    private String triggerReason;

    @JsonProperty("link")
    private Link link;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Link {
        private String href;
        private String rel;
    }
}
