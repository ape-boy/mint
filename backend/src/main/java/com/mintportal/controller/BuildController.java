package com.mintportal.controller;

import com.mintportal.dto.BuildResponse;
import com.mintportal.dto.BuildTriggerRequest;
import com.mintportal.entity.Build;
import com.mintportal.entity.BuildStageResult;
import com.mintportal.scheduler.BuildSchedulerService;
import com.mintportal.service.BuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/builds")
@RequiredArgsConstructor
@Tag(name = "Builds", description = "빌드 API")
public class BuildController {

    private final BuildService buildService;
    private final BuildSchedulerService schedulerService;

    @GetMapping
    @Operation(summary = "빌드 목록 조회 (필터 지원)")
    public ResponseEntity<List<BuildResponse>> findAll(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String layerId,
            @RequestParam(required = false) String status) {

        List<Build> builds;
        if (projectId != null || layerId != null || status != null) {
            builds = buildService.findByFilters(projectId, layerId, status);
        } else {
            builds = buildService.findAll();
        }

        List<BuildResponse> responses = builds.stream()
                .map(BuildResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "빌드 상세 조회 (스테이지 결과 포함)")
    public ResponseEntity<BuildResponse> findById(@PathVariable String id) {
        Build build = buildService.findByIdWithStages(id);
        return ResponseEntity.ok(BuildResponse.fromEntity(build));
    }

    @GetMapping("/{id}/stages")
    @Operation(summary = "빌드 스테이지 결과 조회")
    public ResponseEntity<List<BuildStageResult>> getStages(@PathVariable String id) {
        return ResponseEntity.ok(buildService.getStageResults(id));
    }

    @GetMapping("/{id}/stages/{stageName}")
    @Operation(summary = "특정 스테이지 결과 조회")
    public ResponseEntity<BuildStageResult> getStage(
            @PathVariable String id,
            @PathVariable String stageName) {
        return buildService.getStageResult(id, stageName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    @Operation(summary = "진행 중인 빌드 조회")
    public ResponseEntity<List<BuildResponse>> findActiveBuilds() {
        List<BuildResponse> responses = buildService.findActiveBuilds().stream()
                .map(BuildResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/recent")
    @Operation(summary = "최근 빌드 조회")
    public ResponseEntity<List<BuildResponse>> findRecentBuilds(
            @RequestParam(defaultValue = "20") int limit) {
        List<BuildResponse> responses = buildService.findRecentBuilds(limit).stream()
                .map(BuildResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/project/{projectId}/recent")
    @Operation(summary = "프로젝트별 최근 빌드 조회")
    public ResponseEntity<List<BuildResponse>> findRecentByProject(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "10") int limit) {
        List<BuildResponse> responses = buildService.findRecentByProjectId(projectId, limit).stream()
                .map(BuildResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/layer/{layerId}/recent")
    @Operation(summary = "레이어별 최근 빌드 조회")
    public ResponseEntity<List<BuildResponse>> findRecentByLayer(
            @PathVariable String layerId,
            @RequestParam(defaultValue = "10") int limit) {
        List<BuildResponse> responses = buildService.findRecentByLayerId(layerId, limit).stream()
                .map(BuildResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/trigger")
    @Operation(summary = "빌드 트리거 (큐에 추가)")
    public ResponseEntity<Map<String, Object>> triggerBuild(@RequestBody BuildTriggerRequest request) {
        var queueItem = schedulerService.enqueue(
                request.getProjectId(),
                request.getLayerId(),
                request.getRequesterId(),
                request.getReqMethod(),
                request.getPriority() != null ? request.getPriority() : 0,
                request.getScmOverride(),
                request.getBuildOverride()
        );

        return ResponseEntity.ok(Map.of(
                "queueId", queueItem.getId(),
                "status", queueItem.getQueueStatus(),
                "message", "Build queued successfully"
        ));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "빌드 상태 업데이트")
    public ResponseEntity<BuildResponse> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        Build build = buildService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(BuildResponse.fromEntity(build));
    }

    @PatchMapping("/{id}/stages/{stageName}")
    @Operation(summary = "특정 스테이지 상태 업데이트")
    public ResponseEntity<BuildStageResult> updateStageStatus(
            @PathVariable String id,
            @PathVariable String stageName,
            @RequestBody Map<String, String> body) {
        BuildStageResult stage = buildService.updateStageStatus(id, stageName, body.get("status"));
        return ResponseEntity.ok(stage);
    }

    @PatchMapping("/{id}/release-status")
    @Operation(summary = "릴리즈 상태 업데이트")
    public ResponseEntity<BuildResponse> updateReleaseStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        Build build = buildService.updateReleaseStatus(id, body.get("releaseStatus"));
        return ResponseEntity.ok(BuildResponse.fromEntity(build));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "빌드 삭제")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        buildService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "빌드 통계")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(buildService.getStats());
    }

    @GetMapping("/stats/stages")
    @Operation(summary = "스테이지별 통계")
    public ResponseEntity<List<Map<String, Object>>> getStageStats() {
        return ResponseEntity.ok(buildService.getStageStats());
    }
}
