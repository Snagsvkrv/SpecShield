package com.dpw.specshield.services.impl;

import com.dpw.specshield.dto.TestHistoryResponse;
import com.dpw.specshield.services.IHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import com.mongodb.BasicDBObject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements IHistoryService {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<TestHistoryResponse> getTestHistory(String tenantId, String baseUrl) {
        log.info("Getting test history for tenantId: {} and baseUrl: {}", tenantId, baseUrl);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("tenantId").is(tenantId).and("baseUrl").is(baseUrl)),

                Aggregation.unwind("executions"),

                Aggregation.group("executions.testKey")
                        .first("executions.contractPath").as("contractPath")
                        .first("executions.httpMethod").as("httpMethod")
                        .first("executions.scenario").as("scenario")
                        .first("executions.expectedResult.statusCode").as("expectedStatus")
                        .push(
                                new BasicDBObject()
                                        .append("timestamp", "$executions.timestamp")
                                        .append("result", "$executions.result")
                                        .append("actualStatus", "$executions.responseDetails.responseStatus")
                                        .append("resultDetails", "$executions.resultDetails")
                        ).as("history"),

                Aggregation.project()
                        .and("_id").as("testKey")
                        .and("contractPath").as("testDetails.contractPath")
                        .and("httpMethod").as("testDetails.httpMethod")
                        .and("scenario").as("testDetails.scenario")
                        .and("expectedStatus").as("testDetails.expectedStatus")
                        .and(ArrayOperators.Size.lengthOfArray("history")).as("totalRuns")
                        .and(ArrayOperators.Size.lengthOfArray(
                                ArrayOperators.Filter.filter("history")
                                        .as("item")
                                        .by(ComparisonOperators.Eq.valueOf("$$item.result").equalToValue("success"))
                        )).as("successCount")
                        .and(ConditionalOperators.Cond.when(ComparisonOperators.Gt.valueOf("totalRuns").greaterThanValue(0))
                                .then(ArithmeticOperators.Multiply.valueOf(
                                        ArithmeticOperators.Divide.valueOf("successCount").divideBy("totalRuns")
                                ).multiplyBy(100))
                                .otherwise(0)).as("successRate")
                        .and(ArrayOperators.Slice.sliceArrayOf("history").itemCount(5)).as("recentTrend")
                        .andExpression("max(history.timestamp)").as("lastExecution"),

                Aggregation.sort(Sort.Direction.ASC, "successRate").and(Sort.Direction.DESC, "lastExecution")
        );

        AggregationResults<TestHistoryResponse> results = mongoTemplate.aggregate(
                aggregation, "test_results", TestHistoryResponse.class);

        List<TestHistoryResponse> historyList = results.getMappedResults();
        log.info("Found {} unique test histories for tenantId: {}", historyList.size(), tenantId);

        return historyList;
    }

    @Override
    public List<TestHistoryResponse> getTestHistoryByContractPath(String tenantId, String baseUrl, String contractPath) {
        log.info("Getting test history for tenantId: {}, baseUrl: {}, contractPath: {}", tenantId, baseUrl, contractPath);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("tenantId").is(tenantId)
                        .and("baseUrl").is(baseUrl)
                        .and("executions.contractPath").is(contractPath)),

                Aggregation.unwind("executions"),

                Aggregation.match(Criteria.where("executions.contractPath").is(contractPath)),

                Aggregation.group("executions.testKey")
                        .first("executions.contractPath").as("contractPath")
                        .first("executions.httpMethod").as("httpMethod")
                        .first("executions.scenario").as("scenario")
                        .first("executions.expectedResult.statusCode").as("expectedStatus")
                        .push(
                                new BasicDBObject()
                                        .append("timestamp", "$executions.timestamp")
                                        .append("result", "$executions.result")
                                        .append("actualStatus", "$executions.responseDetails.responseStatus")
                                        .append("resultDetails", "$executions.resultDetails")
                        ).as("history"),

                Aggregation.project()
                        .and("_id").as("testKey")
                        .and("contractPath").as("testDetails.contractPath")
                        .and("httpMethod").as("testDetails.httpMethod")
                        .and("scenario").as("testDetails.scenario")
                        .and("expectedStatus").as("testDetails.expectedStatus")
                        .and(ArrayOperators.Size.lengthOfArray("history")).as("totalRuns")
                        .and(ArrayOperators.Size.lengthOfArray(
                                ArrayOperators.Filter.filter("history")
                                        .as("item")
                                        .by(ComparisonOperators.Eq.valueOf("$$item.result").equalToValue("success"))
                        )).as("successCount")
                        .and(ConditionalOperators.Cond.when(ComparisonOperators.Gt.valueOf("totalRuns").greaterThanValue(0))
                                .then(ArithmeticOperators.Multiply.valueOf(
                                        ArithmeticOperators.Divide.valueOf("successCount").divideBy("totalRuns")
                                ).multiplyBy(100))
                                .otherwise(0)).as("successRate")
                        .and(ArrayOperators.Slice.sliceArrayOf("history").itemCount(10)).as("recentTrend")
                        .andExpression("max(history.timestamp)").as("lastExecution"),

                Aggregation.sort(Sort.Direction.DESC, "lastExecution")
        );

        AggregationResults<TestHistoryResponse> results = mongoTemplate.aggregate(
                aggregation, "test_results", TestHistoryResponse.class);

        List<TestHistoryResponse> historyList = results.getMappedResults();
        log.info("Found {} test histories for contractPath: {}", historyList.size(), contractPath);

        return historyList;
    }
}