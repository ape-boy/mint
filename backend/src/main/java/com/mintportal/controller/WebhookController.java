package com.mintportal.controller;

import com.mintportal.scheduler.BuildStatusPollingService;
import com.mintportal.service.BuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Bamboo Webhook 수신 컨트롤러
 * Bamboo에서 빌드/스테이지 완료 시 호출됨
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Bamboo Webhook 수신 API")
public class WebhookController {

    private final BuildService buildService;
    private final BuildStatusPollingService pollingService;

    /**
     * Bamboo 빌드 상태 Webhook
     * 빌드 시작/완료 시 Bamboo에서 호출
     */
    @PostMapping("/bamboo/build")
    @Operation(summary = "Bamboo 빌드 상태 Webhook")
    public ResponseEntity<Map<String, String>> handleBuildWebhook(
            @RequestBody Map<String, Object> payload) {
        log.info("Received Bamboo build webhook: {}", payload);

        try {
            String buildKey = extractString(payload, "buildResultKey");
            if (buildKey == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing buildResultKey"));
            }

            log.info("Build webhook received for: {}", buildKey);
            return ResponseEntity.ok(Map.of("status", "received"));
        } catch (Exception e) {
            log.error("Error processing build webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Bamboo 스테이지 완료 Webhook
     * Build, SAM, Coverity 각각의 스테이지 완료 시 Bamboo에서 호출
     */
    @PostMapping("/bamboo/stage")
    @Operation(summary = "Bamboo 스테이지 완료 Webhook")
    public ResponseEntity<Map<String, String>> handleStageWebhook(
            @RequestBody Map<String, Object> payload) {
        log.info("Received Bamboo stage webhook: {}", payload);

        try {
            String buildKey = extractString(payload, "buildResultKey");
            String stageName = extractString(payload, "stageName");

            if (buildKey == null || stageName == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing buildResultKey or stageName"));
            }

            pollingService.handleStageWebhook(buildKey, stageName, payload);

            return ResponseEntity.ok(Map.of("status", "processed"));
        } catch (Exception e) {
            log.error("Error processing stage webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Build 스테이지 결과 Webhook
     */
    @PostMapping("/bamboo/stage/build")
    @Operation(summary = "Build 스테이지 결과 Webhook")
    public ResponseEntity<Map<String, String>> handleBuildStageWebhook(
            @RequestBody Map<String, Object> payload) {
        return handleStageWebhookInternal("Build", payload);
    }

    /**
     * SAM 스테이지 결과 Webhook
     */
    @PostMapping("/bamboo/stage/sam")
    @Operation(summary = "SAM 스테이지 결과 Webhook")
    public ResponseEntity<Map<String, String>> handleSamStageWebhook(
            @RequestBody Map<String, Object> payload) {
        return handleStageWebhookInternal("SAM", payload);
    }

    /**
     * Coverity 스테이지 결과 Webhook
     */
    @PostMapping("/bamboo/stage/coverity")
    @Operation(summary = "Coverity 스테이지 결과 Webhook")
    public ResponseEntity<Map<String, String>> handleCoverityStageWebhook(
            @RequestBody Map<String, Object> payload) {
        return handleStageWebhookInternal("Coverity", payload);
    }

    private ResponseEntity<Map<String, String>> handleStageWebhookInternal(
            String stageName, Map<String, Object> payload) {
        log.info("Received {} stage webhook: {}", stageName, payload);

        try {
            String buildKey = extractString(payload, "buildResultKey");
            if (buildKey == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing buildResultKey"));
            }

            pollingService.handleStageWebhook(buildKey, stageName, payload);

            return ResponseEntity.ok(Map.of(
                    "status", "processed",
                    "stage", stageName
            ));
        } catch (Exception e) {
            log.error("Error processing {} stage webhook: {}", stageName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Legacy Bamboo webhook endpoint (backward compatibility)
     */
    @PostMapping("/bamboo")
    @Operation(summary = "Bamboo 빌드 웹훅 수신 (Legacy)")
    public ResponseEntity<Void> receiveBambooWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received Bamboo webhook (legacy): {}", payload);

        try {
            String buildResultKey = extractString(payload, "buildResultKey");
            String buildState = extractString(payload, "buildState");
            String lifeCycleState = extractString(payload, "lifeCycleState");

            log.info("Bamboo build {} - state: {}, lifecycle: {}",
                    buildResultKey, buildState, lifeCycleState);

            if ("Finished".equalsIgnoreCase(lifeCycleState)) {
                String status = "Successful".equalsIgnoreCase(buildState) ? "success" : "failed";
                log.info("Build {} completed with status: {}", buildResultKey, status);
            }

        } catch (Exception e) {
            log.error("Error processing Bamboo webhook: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Generic build notification webhook
     */
    @PostMapping("/build-notification")
    @Operation(summary = "빌드 알림 웹훅 (범용)")
    public ResponseEntity<Void> receiveBuildNotification(@RequestBody Map<String, Object> payload) {
        log.info("Received build notification: {}", payload);

        try {
            String buildId = extractString(payload, "buildId");
            String status = extractString(payload, "status");
            String stageName = extractString(payload, "stageName");

            if (buildId != null && status != null) {
                if (stageName != null) {
                    buildService.updateStageStatus(buildId, stageName, status);
                } else {
                    buildService.updateStatus(buildId, status);
                }
            }

        } catch (Exception e) {
            log.error("Error processing build notification: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Webhook 테스트 엔드포인트
     */
    @PostMapping("/test")
    @Operation(summary = "Webhook 테스트")
    public ResponseEntity<Map<String, Object>> testWebhook(
            @RequestBody Map<String, Object> payload) {
        log.info("Test webhook received: {}", payload);
        return ResponseEntity.ok(Map.of(
                "received", true,
                "payload", payload
        ));
    }

    @SuppressWarnings("unchecked")
    private String extractString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            if (nested.containsKey("value")) {
                return String.valueOf(nested.get("value"));
            }
        }
        return value != null ? String.valueOf(value) : null;
    }
}
