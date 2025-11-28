package com.mintportal.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 빌드 대기열 엔티티
 * 스케줄러가 처리하는 빌드 요청 대기열
 */
@Entity
@Table(name = "build_queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildQueue {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_id", nullable = false)
    private Layer layer;

    // Queue Status
    @Column(name = "queue_status", nullable = false, length = 20)
    private String queueStatus;  // waiting, processing, sent, completed, failed, cancelled

    @Column
    @Builder.Default
    private Integer priority = 0;  // 높을수록 우선

    // Requester
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private User requester;

    @Column(name = "req_method", length = 20)
    private String reqMethod;  // manual, scheduled, trigger

    // Override 설정 (빌드 시 기본값 오버라이드)
    @Type(JsonType.class)
    @Column(name = "scm_override", columnDefinition = "jsonb")
    private Map<String, Object> scmOverride;

    @Type(JsonType.class)
    @Column(name = "build_override", columnDefinition = "jsonb")
    private Map<String, Object> buildOverride;

    // Timing
    @Column(name = "queued_at")
    private OffsetDateTime queuedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    // Retry
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        if (queuedAt == null) queuedAt = OffsetDateTime.now();
        if (queueStatus == null) queueStatus = "waiting";
        if (priority == null) priority = 0;
        if (retryCount == null) retryCount = 0;
        if (maxRetries == null) maxRetries = 3;
    }

    // 처리 시작
    public void startProcessing() {
        this.queueStatus = "processing";
        this.processedAt = OffsetDateTime.now();
    }

    // 처리 완료
    public void markCompleted() {
        this.queueStatus = "completed";
    }

    // 실패 처리
    public void markFailed(String errorMessage) {
        this.lastError = errorMessage;
        this.retryCount++;
        if (this.retryCount >= this.maxRetries) {
            this.queueStatus = "failed";
        } else {
            this.queueStatus = "waiting";  // 재시도 대기
        }
    }

    // 재시도 가능 여부
    public boolean canRetry() {
        return this.retryCount < this.maxRetries;
    }
}
