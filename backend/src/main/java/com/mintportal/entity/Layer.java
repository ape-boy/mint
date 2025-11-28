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
@Table(name = "layers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Layer {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;  // Release, HIL, FIL, FTL

    @Column(nullable = false, length = 20)
    private String type;  // release, layer, private

    // Layer별 빌드 경로 (Src/FTL/Build/ARM)
    @Column(name = "layer_path", length = 500)
    private String layerPath;

    // Layer별 파이프라인 활성화 (프로젝트 설정 오버라이드)
    @Column(name = "build_enabled")
    private Boolean buildEnabled;

    @Column(name = "sam_enabled")
    private Boolean samEnabled;

    @Column(name = "coverity_enabled")
    private Boolean coverityEnabled;

    // Layer별 상세 설정 (오버라이드용)
    @Type(JsonType.class)
    @Column(name = "layer_config", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> layerConfig = Map.of();

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "layer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Build> builds = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (buildEnabled == null) buildEnabled = true;
        if (samEnabled == null) samEnabled = true;
        if (coverityEnabled == null) coverityEnabled = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
