package com.mintportal.dto;

import lombok.Data;

import java.util.Map;

@Data
public class BuildRequest {
    private String projectId;
    private String layerId;
    private Map<String, Object> triggeredBy;
    private String bambooPlanKey;  // Optional: specific Bamboo plan to use
}
