package com.dpw.specshield.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TestExecution {
    private String id;
    private String testKey;  // Hash-based unique identifier for tracking test history
    private LocalDateTime timestamp;
    private String scenario;
    private ExpectedResult expectedResult;
    private String result;
    private String resultDetails;
    private String contractPath;
    private String fullRequestPath;
    private String httpMethod;
    private RequestDetails requestDetails;
    private ResponseDetails responseDetails;

    @Data
    public static class RequestDetails {
        private Map<String, String> headers;
        private String payload;
        private String curl;
    }

    @Data
    public static class ResponseDetails {
        private Integer responseStatus;
        private String responseBody;
        private Map<String, String> responseHeaders;
    }

}
