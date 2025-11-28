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
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @Column(length = 50)
    private String id;  // PROJECTID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_group_id", nullable = false)
    private TaskGroup taskGroup;

    // Identity & Project
    @Column(name = "project_name", nullable = false, length = 200)
    private String projectName;  // PROJECTNAME: SSD_Controller_FW

    @Column(name = "project_code", nullable = false, length = 100)
    private String projectCode;

    @Column(name = "plan_id", length = 100)
    private String planId;  // PLANID: PLAN-2025-Q1

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 100)
    private String oem;  // Lenovo, Dell, HP

    // Team
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pl_id")
    private User pl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tl_id")
    private User tl;

    // SCM Configuration (Default values for this project)
    @Type(JsonType.class)
    @Column(name = "scm_config", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> scmConfig = Map.of();
    /*
        scm_config structure:
        {
            "repo_path": "ssh://git@internal/repo.git",    -- REPOPATH
            "branch": "develop",                            -- GITBRANCHNAME
            "revision": "HEAD",                             -- BUILDREVISION
            "fast_checkout": true,                          -- FASTCHECKOUTYN
            "source_zip": false,                            -- SOURCEZIPYN
            "auto_commit_path": "/src/version.h"           -- autocommit_with_path
        }
    */

    // Build Configuration (Default values)
    @Type(JsonType.class)
    @Column(name = "build_config", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> buildConfig = Map.of();
    /*
        build_config structure:
        {
            "type": "DAILY",                               -- BUILDTYPECD
            "target": "OA",                                -- TARGET (OA/VDI)
            "os_env": "LINUX",                             -- BUILDOSENV
            "compiler_main": "ARMCC",                      -- COMPILER
            "compiler_dict": {"ARM": "6.22", "GCC": "8.3"}, -- COMPILERDICT
            "script_name": "build_start.sh",               -- BUILDBATNAME
            "script_option": "--clean --all",              -- BUILDBATOPTION
            "fast_build": false                            -- FASTBUILDYN
        }
    */

    // Analysis Configuration (Default values)
    @Type(JsonType.class)
    @Column(name = "analysis_config", nullable = false, columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> analysisConfig = Map.of();
    /*
        analysis_config structure:
        {
            "coverity": {"enabled": true},                 -- CICOVERITYYN
            "sam": {
                "enabled": true,
                "script": "run_sam.py",                    -- SAMBATNAME
                "path": "/tools/analysis/sam"              -- SAMBATPATH
            },
            "blackduck": {"enabled": false},               -- CIBLACKDUCKYN (Skip)
            "coding_rule": {"enabled": false},             -- CICODINGRULEYN (Skip)
            "triage_level": "HIGH"                         -- TRIAGE
        }
    */

    // Control
    @Column(name = "is_certified")
    private Boolean isCertified;  // ISCERTIFIEDYN

    @Column(name = "log_path_template", length = 500)
    private String logPathTemplate;  // COMPILERLOGPATH template

    // Milestone
    @Column(name = "current_milestone", length = 10)
    private String currentMilestone;

    @Column(name = "kpi_score")
    private Integer kpiScore;

    // Timestamps
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Layer> layers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
        if (status == null) status = "active";
        if (isCertified == null) isCertified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
