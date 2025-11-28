package com.mintportal.controller;

import com.mintportal.entity.Project;
import com.mintportal.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "과제 API")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "과제 목록 조회 (필터 지원)")
    public ResponseEntity<List<Project>> findAll(
            @RequestParam(required = false) String projectGroupId,
            @RequestParam(required = false) String taskGroupId,
            @RequestParam(required = false) String oem,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        // Support both projectGroupId and taskGroupId for backward compatibility
        String groupId = taskGroupId != null ? taskGroupId : projectGroupId;

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(projectService.search(search));
        }

        if (groupId != null || oem != null || status != null) {
            return ResponseEntity.ok(projectService.findByFilters(groupId, oem, status));
        }

        return ResponseEntity.ok(projectService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "과제 상세 조회")
    public ResponseEntity<Project> findById(@PathVariable String id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    @PostMapping
    @Operation(summary = "과제 생성")
    public ResponseEntity<Project> create(
            @RequestBody Project project,
            @RequestParam String taskGroupId) {
        return ResponseEntity.ok(projectService.create(project, taskGroupId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "과제 수정")
    public ResponseEntity<Project> update(@PathVariable String id, @RequestBody Project project) {
        return ResponseEntity.ok(projectService.update(id, project));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "과제 부분 수정")
    public ResponseEntity<Project> patch(@PathVariable String id, @RequestBody Project project) {
        return ResponseEntity.ok(projectService.update(id, project));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "과제 삭제")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
