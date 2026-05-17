package com.automation.qa.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a test case with metadata and execution details.
 */
public class TestCase {
    private String testCaseId;
    private String testName;
    private String description;
    private String category;
    private TestPriority priority;
    private TestStatus status;
    private String endpoint;
    private String httpMethod;
    private Map<String, Object> testData;
    private Map<String, Object> expectedResult;
    private String actualResult;
    private String errorMessage;
    private long executionTime;
    private LocalDateTime createdDate;
    private LocalDateTime executedDate;
    private int retryCount;
    private boolean skipped;

    public TestCase(String testCaseId, String testName, String endpoint, String httpMethod) {
        this.testCaseId = testCaseId;
        this.testName = testName;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.status = TestStatus.PENDING;
        this.testData = new HashMap<>();
        this.expectedResult = new HashMap<>();
        this.createdDate = LocalDateTime.now();
        this.retryCount = 0;
        this.skipped = false;
    }

    // Getters and Setters
    public String getTestCaseId() {
        return testCaseId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TestPriority getPriority() {
        return priority;
    }

    public void setPriority(TestPriority priority) {
        this.priority = priority;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
        if (status == TestStatus.PASSED || status == TestStatus.FAILED) {
            this.executedDate = LocalDateTime.now();
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Map<String, Object> getTestData() {
        return testData;
    }

    public void setTestData(Map<String, Object> testData) {
        this.testData = testData;
    }

    public Map<String, Object> getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(Map<String, Object> expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getActualResult() {
        return actualResult;
    }

    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getExecutedDate() {
        return executedDate;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "testCaseId='" + testCaseId + '\'' +
                ", testName='" + testName + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", status=" + status +
                ", executionTime=" + executionTime + "ms" +
                '}';
    }

    // Enum for Test Status
    public enum TestStatus {
        PENDING, RUNNING, PASSED, FAILED, SKIPPED, BLOCKED
    }

    // Enum for Test Priority
    public enum TestPriority {
        CRITICAL, HIGH, MEDIUM, LOW
    }
}
