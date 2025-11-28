package com.mintportal.bamboo;

import com.mintportal.entity.Layer;
import com.mintportal.entity.Project;
import com.mintportal.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Bamboo 빌드 파라미터 생성기
 * DB 스키마의 generate_bamboo_params() 함수와 동일한 로직을 Java로 구현
 *
 * CI/CD Metadata Parameter Mapping Document 기반:
 * - Identity & Project: PROJECTID, PROJECTNAME, PLANID, SWDPUSERNAME, BUILDREQUESTID
 * - SCM: REPOPATH, GITBRANCHNAME, BUILDREVISION, FASTCHECKOUTYN, SOURCEZIPYN, autocommit_with_path
 * - Build Configuration: BUILDTYPECD, TARGET, BUILDOSENV, COMPILER, COMPILERDICT, BUILDBATNAME, BUILDBATOPTION, FASTBUILDYN
 * - Quality & Analysis: CICOVERITYYN, SAMBATNAME, SAMBATPATH, CIBLACKDUCKYN, CICODINGRULEYN, TRIAGE
 * - Result & Control: ISCERTIFIEDYN, COMPILERLOGPATH
 */
@Component
@Slf4j
public class BambooParamsGenerator {

    /**
     * 프로젝트/레이어 설정에서 Bamboo 파라미터 맵 생성
     *
     * @param project 프로젝트 엔티티
     * @param layer 레이어 엔티티
     * @param requester 요청자 (SWDPUSERNAME)
     * @param scmOverride SCM 설정 오버라이드 (optional)
     * @param buildOverride 빌드 설정 오버라이드 (optional)
     * @return Bamboo API로 전송할 파라미터 맵
     */
    public Map<String, Object> generateParams(
            Project project,
            Layer layer,
            User requester,
            Map<String, Object> scmOverride,
            Map<String, Object> buildOverride) {

        Map<String, Object> params = new HashMap<>();

        // === Identity & Project ===
        params.put("PROJECTID", project.getId());
        params.put("PROJECTNAME", project.getProjectName());
        params.put("PLANID", project.getPlanId());
        params.put("SWDPUSERNAME", requester != null ? requester.getSwdpUsername() : null);
        params.put("BUILDREQUESTID", null);  // Bamboo가 채움

        // === SCM Configuration ===
        Map<String, Object> scmConfig = mergeConfig(project.getScmConfig(), scmOverride);
        params.put("REPOPATH", getStringValue(scmConfig, "repo_path"));
        params.put("GITBRANCHNAME", getStringValue(scmConfig, "branch", "main"));
        params.put("BUILDREVISION", getStringValue(scmConfig, "revision", "HEAD"));
        params.put("FASTCHECKOUTYN", boolToYN(scmConfig, "fast_checkout"));
        params.put("SOURCEZIPYN", boolToYN(scmConfig, "source_zip"));
        params.put("autocommit_with_path", getStringValue(scmConfig, "auto_commit_path"));

        // === Build Configuration ===
        Map<String, Object> buildConfig = mergeConfig(project.getBuildConfig(), buildOverride);
        params.put("BUILDTYPECD", getStringValue(buildConfig, "type", "DAILY"));
        params.put("TARGET", getStringValue(buildConfig, "target", "OA"));
        params.put("BUILDOSENV", getStringValue(buildConfig, "os_env", "LINUX"));
        params.put("COMPILER", getStringValue(buildConfig, "compiler_main", "ARMCC"));
        params.put("COMPILERDICT", getObjectValue(buildConfig, "compiler_dict"));
        params.put("BUILDBATNAME", getStringValue(buildConfig, "script_name"));
        params.put("BUILDBATOPTION", getStringValue(buildConfig, "script_option"));
        params.put("FASTBUILDYN", boolToYN(buildConfig, "fast_build"));

        // === Analysis Configuration ===
        Map<String, Object> analysisConfig = project.getAnalysisConfig();
        if (analysisConfig == null) {
            analysisConfig = new HashMap<>();
        }

        // Coverity: layer.coverity_enabled && project.analysis_config.coverity.enabled
        boolean coverityEnabled = layer.getCoverityEnabled() != null && layer.getCoverityEnabled();
        if (coverityEnabled) {
            @SuppressWarnings("unchecked")
            Map<String, Object> coverity = (Map<String, Object>) analysisConfig.get("coverity");
            if (coverity != null) {
                coverityEnabled = getBoolValue(coverity, "enabled", true);
            }
        }
        params.put("CICOVERITYYN", coverityEnabled ? "Y" : "N");

        // SAM
        @SuppressWarnings("unchecked")
        Map<String, Object> samConfig = (Map<String, Object>) analysisConfig.get("sam");
        if (samConfig != null) {
            params.put("SAMBATNAME", getStringValue(samConfig, "script"));
            params.put("SAMBATPATH", getStringValue(samConfig, "path"));
        } else {
            params.put("SAMBATNAME", null);
            params.put("SAMBATPATH", null);
        }

        // BlackDuck & Coding Rule - 현재 Skip
        params.put("CIBLACKDUCKYN", "N");
        params.put("CICODINGRULEYN", "N");

        // TRIAGE
        params.put("TRIAGE", getStringValue(analysisConfig, "triage_level", "HIGH"));

        // === Control ===
        params.put("ISCERTIFIEDYN", project.getIsCertified() != null && project.getIsCertified() ? "Y" : "N");
        params.put("COMPILERLOGPATH", project.getLogPathTemplate());

        // === Layer specific ===
        params.put("LAYER_NAME", layer.getName());
        params.put("LAYER_PATH", layer.getLayerPath());

        log.debug("Generated Bamboo params for project {} layer {}: {}",
                project.getId(), layer.getId(), params);

        return params;
    }

    /**
     * 빌드 스냅샷 생성 (재현 가능하도록 설정 저장)
     */
    public Map<String, Object> createBuildSnapshot(
            Project project,
            Layer layer,
            Map<String, Object> scmOverride,
            Map<String, Object> buildOverride) {

        Map<String, Object> snapshot = new HashMap<>();

        snapshot.put("scm", mergeConfig(project.getScmConfig(), scmOverride));
        snapshot.put("build", mergeConfig(project.getBuildConfig(), buildOverride));
        snapshot.put("analysis", project.getAnalysisConfig());

        // Layer 설정
        Map<String, Object> layerSnapshot = new HashMap<>();
        layerSnapshot.put("id", layer.getId());
        layerSnapshot.put("name", layer.getName());
        layerSnapshot.put("type", layer.getType());
        layerSnapshot.put("path", layer.getLayerPath());
        layerSnapshot.put("buildEnabled", layer.getBuildEnabled());
        layerSnapshot.put("samEnabled", layer.getSamEnabled());
        layerSnapshot.put("coverityEnabled", layer.getCoverityEnabled());
        snapshot.put("layer", layerSnapshot);

        return snapshot;
    }

    /**
     * 두 설정 맵 병합 (override가 base보다 우선)
     */
    private Map<String, Object> mergeConfig(Map<String, Object> base, Map<String, Object> override) {
        Map<String, Object> result = new HashMap<>();
        if (base != null) {
            result.putAll(base);
        }
        if (override != null) {
            result.putAll(override);
        }
        return result;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        return getStringValue(map, key, null);
    }

    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map == null) return defaultValue;
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private Object getObjectValue(Map<String, Object> map, String key) {
        if (map == null) return null;
        return map.get(key);
    }

    private boolean getBoolValue(Map<String, Object> map, String key, boolean defaultValue) {
        if (map == null) return defaultValue;
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private String boolToYN(Map<String, Object> map, String key) {
        return getBoolValue(map, key, false) ? "Y" : "N";
    }
}
