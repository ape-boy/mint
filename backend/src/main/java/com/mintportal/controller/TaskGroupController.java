package com.mintportal.controller;

import com.mintportal.entity.TaskGroup;
import com.mintportal.service.TaskGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-groups")
@RequiredArgsConstructor
@Tag(name = "Task Groups", description = "과제 그룹 (제품 라인) API")
public class TaskGroupController {

    private final TaskGroupService taskGroupService;

    @GetMapping
    @Operation(summary = "전체 과제 그룹 조회")
    public ResponseEntity<List<TaskGroup>> findAll() {
        return ResponseEntity.ok(taskGroupService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "과제 그룹 상세 조회")
    public ResponseEntity<TaskGroup> findById(@PathVariable String id) {
        return ResponseEntity.ok(taskGroupService.findById(id));
    }

    @PostMapping
    @Operation(summary = "과제 그룹 생성")
    public ResponseEntity<TaskGroup> create(@RequestBody TaskGroup taskGroup) {
        return ResponseEntity.ok(taskGroupService.create(taskGroup));
    }

    @PutMapping("/{id}")
    @Operation(summary = "과제 그룹 수정")
    public ResponseEntity<TaskGroup> update(@PathVariable String id, @RequestBody TaskGroup taskGroup) {
        return ResponseEntity.ok(taskGroupService.update(id, taskGroup));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "과제 그룹 삭제")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taskGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
