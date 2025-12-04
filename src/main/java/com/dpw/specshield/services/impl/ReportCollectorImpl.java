package com.dpw.specshield.services.impl;

import com.dpw.specshield.dto.TestReportResponse;
import com.dpw.specshield.model.TestResult;
import com.dpw.specshield.model.TestExecution;
import com.dpw.specshield.repository.TestResultRepository;
import com.dpw.specshield.services.IReportCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportCollectorImpl implements IReportCollector {

    private final TestResultRepository testResultRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss").withZone(java.time.ZoneId.systemDefault());

    @Override
    public TestReportResponse getReportById(String reportId) {
        log.info("Fetching report for ID: {}", reportId);

        TestResult testResult = testResultRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));

        TestReportResponse response = new TestReportResponse();
        if (testResult.getExecutionEndTime() != null) {
            response.setReportTimestamp(testResult.getExecutionEndTime().atZone(java.time.ZoneId.systemDefault()).format(FORMATTER) + " +0530");
        } else {
            response.setReportTimestamp(testResult.getExecutionStartTime().atZone(java.time.ZoneId.systemDefault()).format(FORMATTER) + " +0530");
        }

        TestReportResponse.Overview overview = new TestReportResponse.Overview();
        overview.setExecutionTime(testResult.getExecutionDuration() != null ? testResult.getExecutionDuration() : "In Progress");
        overview.setTotal(testResult.getTotalTests());
        overview.setErrors(testResult.getErrorTests());
        overview.setWarnings(testResult.getWarningTests());
        overview.setSuccessful(testResult.getSuccessfulTests());

        overview.setPaths(calculatePathStats(testResult.getExecutions()));

        response.setOverview(overview);
        response.setExecutionDetails(testResult.getExecutions());

        log.info("Report retrieved successfully for ID: {}", reportId);
        return response;
    }

    private Map<String, TestReportResponse.PathStats> calculatePathStats(List<TestExecution> executions) {
        if (executions == null || executions.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, List<TestExecution>> executionsByPath = executions.stream()
            .collect(Collectors.groupingBy(TestExecution::getContractPath));

        Map<String, TestReportResponse.PathStats> pathStatsMap = new HashMap<>();

        for (Map.Entry<String, List<TestExecution>> entry : executionsByPath.entrySet()) {
            String path = entry.getKey();
            List<TestExecution> pathExecutions = entry.getValue();

            TestReportResponse.PathStats pathStats = new TestReportResponse.PathStats();
            pathStats.setTotal(pathExecutions.size());

            long successCount = pathExecutions.stream()
                .filter(e -> "success".equals(e.getResult()))
                .count();

            long errorCount = pathExecutions.stream()
                .filter(e -> "error".equals(e.getResult()))
                .count();

            long warningCount = pathExecutions.stream()
                .filter(e -> "warning".equals(e.getResult()))
                .count();

            pathStats.setSuccessful((int) successCount);
            pathStats.setErrors((int) errorCount);
            pathStats.setWarnings((int) warningCount);

            pathStatsMap.put(path, pathStats);
        }

        return pathStatsMap;
    }
}