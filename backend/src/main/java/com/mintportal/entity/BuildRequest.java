package com.mintportal.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Bamboo 빌드 요청 엔티티
 * Bamboo로 전송된 실제 요청 기록 및 전체 파라미터 저장
 */
@Entity
@Table(name = "build_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildRequest {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id")
    private BuildQueue queue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "layer_id", nullable = false)
    private Layer layer;

    // BUILDREQUESTID: Bamboo에서 반환된 요청 ID
    @Column(name = "bamboo_request_id", length = 100)
    private String bambooRequestId;

    @Column(name = "bamboo_plan_key", length = 100)
    private String bambooPlanKey;

    // 전송된 전체 파라미터 (감사/디버깅용)
    // 모든 BAMBOO 파라미터가 여기 저장됨
    @Type(JsonType.class)
    @Column(name = "request_params", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> requestParams;
    /*
        request_params structure (Bamboo로 전송되는 실제 값):
        {
            "PROJECTID": "101",
            "PROJECTNAME": "SSD_Controller_FW",
            "PLANID": "PLAN-2025-Q1",
            "SWDPUSERNAME": "gildong.hong",
            "BUILDREQUESTID": null,  -- Bamboo가 채움

            "REPOPATH": "ssh://git@internal/repo.git",
            "GITBRANCHNAME": "develop",
            "BUILDREVISION": "a1b2c3d",
            "FASTCHECKOUTYN": "Y",
            "SOURCEZIPYN": "N",
            "autocommit_with_path": "/src/version.h",

            "BUILDTYPECD": "DAILY",
            "TARGET": "OA",
            "BUILDOSENV": "LINUX",
            "COMPILER": "ARMCC",
            "COMPILERDICT": "{\"ARM\":\"6.22\"}",
            "BUILDBATNAME": "build_start.sh",
            "BUILDBATOPTION": "--clean --all",
            "FASTBUILDYN": "N",

            "CICOVERITYYN": "Y",
            "SAMBATNAME": "run_sam.py",
            "SAMBATPATH": "/tools/analysis/sam",
            "CIBLACKDUCKYN": "N",
            "CICODINGRULEYN": "N",
            "TRIAGE": "HIGH",

            "ISCERTIFIEDYN": "N",
            "COMPILERLOGPATH": "/logs/2024/11/28/build.log"
        }
    */

    // Request Status
    @Column(name = "request_status", nullable = false, length = 20)
    private String requestStatus;  // sent, accepted, rejected, timeout, error

    // Timing
    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "response_at")
    private OffsetDateTime responseAt;

    // Error
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (sentAt == null) sentAt = OffsetDateTime.now();
        if (requestStatus == null) requestStatus = "sent";
    }

    // 요청 수락 처리
    public void markAccepted(String bambooRequestId) {
        this.bambooRequestId = bambooRequestId;
        this.requestStatus = "accepted";
        this.responseAt = OffsetDateTime.now();
    }

    // 요청 거부 처리
    public void markRejected(String errorMessage) {
        this.requestStatus = "rejected";
        this.errorMessage = errorMessage;
        this.responseAt = OffsetDateTime.now();
    }

    // 타임아웃 처리
    public void markTimeout() {
        this.requestStatus = "timeout";
        this.responseAt = OffsetDateTime.now();
    }

    // 에러 처리
    public void markError(String errorMessage) {
        this.requestStatus = "error";
        this.errorMessage = errorMessage;
        this.responseAt = OffsetDateTime.now();
    }
}
