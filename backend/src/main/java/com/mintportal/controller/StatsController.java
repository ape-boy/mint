package com.mintportal.controller;

import com.mintportal.service.BuildService;
import com.mintportal.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Stats", description = "대시보드 통계 API")
public class StatsController {

    private final BuildService buildService;
    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "대시보드 통계 조회")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> buildStats = buildService.getStats();

        Map<String, Object> stats = new HashMap<>();
        stats.put("successRate", buildStats.get("successRate"));
        stats.put("totalBuilds", buildStats.get("totalBuilds"));
        stats.put("activeProjects", projectService.countActive());
        stats.put("runningBuilds", buildStats.get("runningBuilds"));
        stats.put("failedBuilds", buildStats.get("failedBuilds"));
        stats.put("successBuilds", buildStats.get("successBuilds"));

        // Quality overview (placeholder - would be calculated from actual data)
        Map<String, Object> qualityOverview = new HashMap<>();
        qualityOverview.put("coverityAvg", 94);
        qualityOverview.put("samAvg", 92);
        qualityOverview.put("passRate", buildStats.get("successRate"));
        stats.put("qualityOverview", qualityOverview);

        return ResponseEntity.ok(stats);
    }
}
