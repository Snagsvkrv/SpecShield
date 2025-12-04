package com.dpw.specshield.controller;

import com.dpw.specshield.dto.TestHistoryResponse;
import com.dpw.specshield.services.IHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class HistoryController {

    private final IHistoryService historyService;

    @GetMapping("/tests")
    public ResponseEntity<List<TestHistoryResponse>> getTestHistory(
            @RequestParam String tenantId,
            @RequestParam String baseUrl) {

        log.info("Received test history request for tenantId: {} and baseUrl: {}", tenantId, baseUrl);

        try {
            List<TestHistoryResponse> history = historyService.getTestHistory(tenantId, baseUrl);

            log.info("Returning {} test history records", history.size());
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching test history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tests/contractPath")
    public ResponseEntity<List<TestHistoryResponse>> getTestHistoryByContract(
            @RequestParam String tenantId,
            @RequestParam String baseUrl,
            @RequestParam String contractPath) {

        log.info("Received test history request for tenantId: {}, baseUrl: {}, contractPath: {}",
                tenantId, baseUrl, contractPath);

        try {
            // Decode the contractPath (handles URL encoding)
            String decodedPath = java.net.URLDecoder.decode(contractPath, "UTF-8");

            List<TestHistoryResponse> history = historyService.getTestHistoryByContractPath(
                    tenantId, baseUrl, decodedPath);

            log.info("Returning {} test history records for contractPath: {}", history.size(), decodedPath);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching test history by contract: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tests/summary")
    public ResponseEntity<?> getTestSummary(
            @RequestParam String tenantId,
            @RequestParam String baseUrl) {

        log.info("Received test summary request for tenantId: {} and baseUrl: {}", tenantId, baseUrl);

        try {
            List<TestHistoryResponse> history = historyService.getTestHistory(tenantId, baseUrl);

            // Calculate summary statistics
            int totalUniqueTests = history.size();
            int totalExecutions = history.stream().mapToInt(TestHistoryResponse::getTotalRuns).sum();
            int totalSuccesses = history.stream().mapToInt(TestHistoryResponse::getSuccessCount).sum();
            double overallSuccessRate = totalExecutions > 0 ? (totalSuccesses * 100.0) / totalExecutions : 0.0;

            var summary = java.util.Map.of(
                "tenantId", tenantId,
                "baseUrl", baseUrl,
                "totalUniqueTests", totalUniqueTests,
                "totalExecutions", totalExecutions,
                "totalSuccesses", totalSuccesses,
                "overallSuccessRate", Math.round(overallSuccessRate * 100.0) / 100.0,
                "timestamp", java.time.LocalDateTime.now()
            );

            log.info("Returning test summary: {} unique tests, {} total executions, {}% success rate",
                    totalUniqueTests, totalExecutions, Math.round(overallSuccessRate * 100.0) / 100.0);

            return ResponseEntity.ok(summary);

        } catch (Exception e) {
            log.error("Error fetching test summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}