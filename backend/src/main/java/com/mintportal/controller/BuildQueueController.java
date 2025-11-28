package com.mintportal.controller;

import com.mintportal.entity.BuildQueue;
import com.mintportal.repository.BuildQueueRepository;
import com.mintportal.scheduler.BuildSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@Tag(name = "Build Queue", description = "빌드 큐 API")
public class BuildQueueController {

    private final BuildQueueRepository queueRepository;
    private final BuildSchedulerService schedulerService;

    @GetMapping
    @Operation(summary = "빌드 큐 목록 조회")
    public ResponseEntity<List<BuildQueue>> findAll(
            @RequestParam(required = false) String status) {
        if (status != null) {
            return ResponseEntity.ok(
                    queueRepository.findByQueueStatusOrderByPriorityDescQueuedAtAsc(status));
        }
        return ResponseEntity.ok(queueRepository.findAll());
    }

    @GetMapping("/waiting")
    @Operation(summary = "대기 중인 빌드 조회")
    public ResponseEntity<List<BuildQueue>> findWaiting() {
        return ResponseEntity.ok(queueRepository.findWaitingBuilds());
    }

    @GetMapping("/status")
    @Operation(summary = "큐 상태 조회")
    public ResponseEntity<Map<String, Object>> getQueueStatus() {
        return ResponseEntity.ok(schedulerService.getQueueStatus());
    }

    @GetMapping("/{id}")
    @Operation(summary = "큐 아이템 상세 조회")
    public ResponseEntity<BuildQueue> findById(@PathVariable String id) {
        return queueRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "큐 아이템 취소")
    public ResponseEntity<Map<String, String>> cancel(@PathVariable String id) {
        try {
            schedulerService.cancelQueueItem(id);
            return ResponseEntity.ok(Map.of("message", "Queue item cancelled successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/priority")
    @Operation(summary = "큐 아이템 우선순위 변경")
    public ResponseEntity<BuildQueue> updatePriority(
            @PathVariable String id,
            @RequestBody Map<String, Integer> body) {
        return queueRepository.findById(id)
                .map(item -> {
                    if (!"waiting".equals(item.getQueueStatus())) {
                        throw new IllegalStateException("Cannot change priority of non-waiting item");
                    }
                    item.setPriority(body.get("priority"));
                    return ResponseEntity.ok(queueRepository.save(item));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
