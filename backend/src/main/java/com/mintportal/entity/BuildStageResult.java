package com.mintportal.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 빌드 스테이지 결과 엔티티
 * Build, SAM, Coverity 각각의 결과
 * Bamboo에서 개별적으로 응답이 옴
 */
@Entity
@Table(name = "build_stage_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildStageResult {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id", nullable = false)
    private Build build;

    // Stage Info
    @Column(name = "stage_name", nullable = false, length = 50)
    private String stageName;  // Build, SAM, Coverity

    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;  // 실행 순서 (1, 2, 3)

    // Status
    @Column(nullable = false, length = 20)
    private String status;  // pending, running, success, failed, skipped

    // Bamboo Response (원본)
    @Type(JsonType.class)
    @Column(name = "bamboo_response", columnDefinition = "jsonb")
    private Map<String, Object> bambooResponse;

    // Parsed Results
    @Column(name = "error_count")
    @Builder.Default
    private Integer errorCount = 0;

    @Column(name = "warning_count")
    @Builder.Default
    private Integer warningCount = 0;

    // Stage-specific Results (JSONB)
    @Type(JsonType.class)
    @Column(name = "stage_result", columnDefinition = "jsonb")
    private Map<String, Object> stageResult;
    /*
        Build stage:
        {
            "compile_success": true,
            "binary_size": "2.3MB",
            "warnings": [...],
            "errors": [...]
        }

        SAM stage:
        {
            "score": 95,
            "issues": [...],
            "memory_leaks": 0
        }

        Coverity stage:
        {
            "defect_count": 3,
            "critical": 0,
            "high": 1,
            "medium": 2,
            "issues": [...]
        }
    */

    // Logs
    @Column(name = "log_url", length = 500)
    private String logUrl;

    @Column(name = "log_preview", columnDefinition = "TEXT")
    private String logPreview;  // 처음 몇 줄

    // Timing
    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // Bamboo 응답 수신 시간
    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        if (receivedAt == null) receivedAt = OffsetDateTime.now();
        if (status == null) status = "pending";
        if (errorCount == null) errorCount = 0;
        if (warningCount == null) warningCount = 0;
    }

    // 스테이지 시작
    public void start() {
        this.status = "running";
        this.startedAt = OffsetDateTime.now();
    }

    // 스테이지 완료
    public void complete(boolean success, Map<String, Object> result) {
        this.status = success ? "success" : "failed";
        this.finishedAt = OffsetDateTime.now();
        this.stageResult = result;
        if (this.startedAt != null) {
            this.durationSeconds = (int) java.time.Duration.between(startedAt, finishedAt).getSeconds();
        }
    }

    // 스테이지 스킵
    public void skip() {
        this.status = "skipped";
        this.finishedAt = OffsetDateTime.now();
    }

    // Bamboo 응답 처리
    public void handleBambooResponse(Map<String, Object> response) {
        this.bambooResponse = response;
        this.receivedAt = OffsetDateTime.now();

        // 응답에서 에러/경고 카운트 추출
        if (response != null) {
            if (response.containsKey("errorCount")) {
                this.errorCount = ((Number) response.get("errorCount")).intValue();
            }
            if (response.containsKey("warningCount")) {
                this.warningCount = ((Number) response.get("warningCount")).intValue();
            }
            if (response.containsKey("logUrl")) {
                this.logUrl = (String) response.get("logUrl");
            }
        }
    }
}
