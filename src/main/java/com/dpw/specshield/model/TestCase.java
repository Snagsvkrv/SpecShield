package com.dpw.specshield.model;

import lombok.Data;

@Data
public class TestCase {
    private String testCaseId;
    private String testType;
    private Endpoint endpoint;
    private Request request;
    private Expected expected;
}