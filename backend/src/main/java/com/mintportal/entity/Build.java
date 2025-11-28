package com.mintportal.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "builds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Build {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private BuildRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_id", nullable = false)
    private Layer layer;

    // Build Numbers
    @Column(nullable = false)
    private Integer round;  // Layer 내 회차

    @Column(name = "build_number", nullable = false)
    private Integer buildNumber;  // 프로젝트 전체 빌드 번호

    // Status
    @Column(nullable = false, length = 20)
    private String status;  // pending, running, success, failed, cancelled

    // Bamboo Info
    @Column(name = "bamboo_build_key", length = 100)
    private String bambooBuildKey;

    @Column(name = "bamboo_build_number")
    private Integer bambooBuildNumber;

    // 빌드 시점의 설정 스냅샷 (재현 가능하도록)
    @Type(JsonType.class)
    @Column(name = "build_snapshot", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> buildSnapshot;
    /*
        build_snapshot: 빌드 시점의 전체 설정
        {
            "scm": { ... },
            "build": { ... },
            "analysis": { ... }
        }
    */

    // Result
    @Column(name = "fw_name", length = 200)
    private String fwName;  // 생성된 FW 이름

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> artifacts;
    /*
        artifacts:
        {
            "binary_url": "/path/to/fw.bin",
            "log_url": "/path/to/build.log",
            "source_zip_url": "/path/to/source.zip"
        }
    */

    // Release (Release Layer only)
    @Column(name = "release_status", length = 30)
    private String releaseStatus;  // available, pending_approval, approved, rejected, released

    // Trigger Info
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by")
    private User triggeredBy;

    @Column(name = "trigger_type", length = 20)
    private String triggerType;  // manual, scheduled, trigger

    // Timing
    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Build Stage Results (Build, SAM, Coverity)
    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BuildStageResult> stageResults = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (status == null) status = "pending";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // 빌드 완료 처리
    public void complete(String finalStatus) {
        this.status = finalStatus;
        this.finishedAt = OffsetDateTime.now();
        if (this.startedAt != null) {
            this.durationSeconds = (int) java.time.Duration.between(startedAt, finishedAt).getSeconds();
        }
    }

    // 빌드 시작 처리
    public void start() {
        this.status = "running";
        this.startedAt = OffsetDateTime.now();
    }
}
