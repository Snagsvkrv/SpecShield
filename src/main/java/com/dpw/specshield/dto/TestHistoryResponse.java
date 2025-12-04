package com.dpw.specshield.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestHistoryResponse {
    private String testKey;
    private TestDetails testDetails;
    private int totalRuns;
    private int successCount;
    private double successRate;
    private List<ExecutionHistory> recentTrend;
    private LocalDateTime lastExecution;

    @Data
    public static class TestDetails {
        private String contractPath;
        private String httpMethod;
        private String scenario;
        private int expectedStatus;
    }

    @Data
    public static class ExecutionHistory {
        private LocalDateTime timestamp;
        private String result;
        private Integer actualStatus;
        private String resultDetails;
    }
}