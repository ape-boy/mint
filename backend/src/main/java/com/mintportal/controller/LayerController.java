package com.mintportal.controller;

import com.mintportal.entity.Layer;
import com.mintportal.service.LayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/layers")
@RequiredArgsConstructor
@Tag(name = "Layers", description = "빌드 계층 API")
public class LayerController {

    private final LayerService layerService;

    @GetMapping
    @Operation(summary = "레이어 목록 조회 (프로젝트별 필터 지원)")
    public ResponseEntity<List<Layer>> findAll(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String type) {

        if (projectId != null && type != null) {
            return ResponseEntity.ok(layerService.findByProjectIdAndType(projectId, type));
        }
        if (projectId != null) {
            return ResponseEntity.ok(layerService.findByProjectId(projectId));
        }
        return ResponseEntity.ok(layerService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "레이어 상세 조회")
    public ResponseEntity<Layer> findById(@PathVariable String id) {
        return ResponseEntity.ok(layerService.findById(id));
    }

    @PostMapping
    @Operation(summary = "레이어 생성")
    public ResponseEntity<Layer> create(
            @RequestBody Layer layer,
            @RequestParam String projectId) {
        return ResponseEntity.ok(layerService.create(layer, projectId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "레이어 수정")
    public ResponseEntity<Layer> update(@PathVariable String id, @RequestBody Layer layer) {
        return ResponseEntity.ok(layerService.update(id, layer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "레이어 삭제")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        layerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
