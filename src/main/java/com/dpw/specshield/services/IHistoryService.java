package com.dpw.specshield.services;

import com.dpw.specshield.dto.TestHistoryResponse;
import java.util.List;

public interface IHistoryService {
    List<TestHistoryResponse> getTestHistory(String tenantId, String baseUrl);
    List<TestHistoryResponse> getTestHistoryByContractPath(String tenantId, String baseUrl, String contractPath);
}