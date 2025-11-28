package com.mintportal.dto;

import lombok.Data;

import java.util.Map;

/**
 * 빌드 트리거 요청 DTO
 */
@Data
public class BuildTriggerRequest {
    private String projectId;
    private String layerId;
    private String requesterId;
    private String reqMethod;  // manual, scheduled, trigger
    private Integer priority;  // 기본값 0

    // Override 설정
    private Map<String, Object> scmOverride;
    private Map<String, Object> buildOverride;
}
