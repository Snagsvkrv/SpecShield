package com.dpw.specshield.controller;

import com.dpw.specshield.dto.TestReportResponse;
import com.dpw.specshield.model.TestSuite;
import com.dpw.specshield.services.IExecutorService;
import com.dpw.specshield.services.IReportCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final IExecutorService executorService;
    private final IReportCollector reportCollector;

    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeTestSuite(@RequestBody TestSuite testSuite) {
        log.info("Received test suite execution request: {}", testSuite.getTestSuiteName());

        CompletableFuture<String> future = executorService.executeTestSuite(testSuite);
        String reportId = future.join();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Test suite executed successfully", "reportId", reportId));
    }

    @GetMapping("/report/{id}")
    public ResponseEntity<TestReportResponse> getReportById(@PathVariable String id) {
        log.info("Received report request for ID: {}", id);

        try {
            TestReportResponse report = reportCollector.getReportById(id);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            log.error("Report not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
