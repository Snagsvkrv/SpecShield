package com.dpw.specshield.dto;

import lombok.Data;
import com.dpw.specshield.model.TestExecution;
import java.util.List;
import java.util.Map;

@Data
public class TestReportResponse {
    private String reportTimestamp;
    private Overview overview;
    private List<TestExecution> executionDetails;

    @Data
    public static class Overview {
        private String executionTime;
        private Integer total;
        private Integer errors;
        private Integer warnings;
        private Integer successful;
        private Map<String, PathStats> paths;
    }

    @Data
    public static class PathStats {
        private Integer total;
        private Integer successful;
        private Integer errors;
        private Integer warnings;
    }
}