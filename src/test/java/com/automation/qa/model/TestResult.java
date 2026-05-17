package com.automation.qa.model;

import java.time.LocalDateTime;

/**
 * Represents the result of a test execution.
 */
public class TestResult {
    private TestCase testCase;
    private TestCase.TestStatus status;
    private int statusCode;
    private String responseBody;
    private String expectedOutput;
    private String actualOutput;
    private String errorMessage;
    private long executionTime;
    private LocalDateTime executionDateTime;
    private String executionEnvironment;
    private boolean retried;
    private int retryAttempt;
    private Throwable exception;

    public TestResult(TestCase testCase) {
        this.testCase = testCase;
        this.status = TestCase.TestStatus.PENDING;
        this.executionDateTime = LocalDateTime.now();
        this.retried = false;
        this.retryAttempt = 0;
    }

    /**
     * Mark result as passed.
     */
    public void markPassed(int statusCode, String responseBody) {
        this.status = TestCase.TestStatus.PASSED;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.testCase.setStatus(TestCase.TestStatus.PASSED);
    }

    /**
     * Mark result as failed.
     */
    public void markFailed(String errorMessage, String actualOutput) {
        this.status = TestCase.TestStatus.FAILED;
        this.errorMessage = errorMessage;
        this.actualOutput = actualOutput;
        this.testCase.setStatus(TestCase.TestStatus.FAILED);
    }

    /**
     * Mark result as skipped.
     */
    public void markSkipped(String reason) {
        this.status = TestCase.TestStatus.SKIPPED;
        this.errorMessage = reason;
        this.testCase.setStatus(TestCase.TestStatus.SKIPPED);
    }

    /**
     * Check if test passed.
     */
    public boolean isPassed() {
        return status == TestCase.TestStatus.PASSED;
    }

    /**
     * Check if test failed.
     */
    public boolean isFailed() {
        return status == TestCase.TestStatus.FAILED;
    }

    /**
     * Check if test was skipped.
     */
    public boolean isSkipped() {
        return status == TestCase.TestStatus.SKIPPED;
    }

    /**
     * Get detailed result summary.
     */
    public String getSummary() {
        return String.format(
            "Test: %s | Status: %s | Time: %dms | StatusCode: %d",
            testCase.getTestName(),
            status,
            executionTime,
            statusCode
        );
    }

    // Getters and Setters
    public TestCase getTestCase() {
        return testCase;
    }

    public TestCase.TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestCase.TestStatus status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public String getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(String actualOutput) {
        this.actualOutput = actualOutput;
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

    public LocalDateTime getExecutionDateTime() {
        return executionDateTime;
    }

    public String getExecutionEnvironment() {
        return executionEnvironment;
    }

    public void setExecutionEnvironment(String executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    public boolean isRetried() {
        return retried;
    }

    public void setRetried(boolean retried) {
        this.retried = retried;
    }

    public int getRetryAttempt() {
        return retryAttempt;
    }

    public void setRetryAttempt(int retryAttempt) {
        this.retryAttempt = retryAttempt;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
