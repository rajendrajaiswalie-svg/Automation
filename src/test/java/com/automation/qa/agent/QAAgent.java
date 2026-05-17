package com.automation.qa.agent;

import com.automation.api.ApiClient;
import com.automation.qa.model.TestCase;
import com.automation.qa.model.TestResult;
import com.automation.qa.model.TestSuite;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Main QA Agent for managing and executing test cases.
 * Provides comprehensive test management, execution, and reporting capabilities.
 */
public class QAAgent {
    private String agentName;
    private ApiClient apiClient;
    private List<TestSuite> testSuites;
    private List<TestResult> testResults;
    private ExecutorService executorService;
    private boolean isRunning;
    private int maxRetries;
    private long retryDelayMs;

    public QAAgent(String agentName, ApiClient apiClient) {
        this.agentName = agentName;
        this.apiClient = apiClient;
        this.testSuites = new ArrayList<>();
        this.testResults = new ArrayList<>();
        this.executorService = Executors.newFixedThreadPool(4);
        this.isRunning = false;
        this.maxRetries = 3;
        this.retryDelayMs = 1000;
    }

    // ==================== Test Suite Management ====================

    /**
     * Create a new test suite.
     */
    public TestSuite createTestSuite(String suiteName) {
        TestSuite suite = new TestSuite(suiteName);
        testSuites.add(suite);
        return suite;
    }

    /**
     * Get test suite by name.
     */
    public TestSuite getTestSuite(String suiteName) {
        for (TestSuite suite : testSuites) {
            if (suite.getSuiteName().equals(suiteName)) {
                return suite;
            }
        }
        return null;
    }

    /**
     * Add test case to suite.
     */
    public void addTestCaseToSuite(String suiteName, TestCase testCase) {
        TestSuite suite = getTestSuite(suiteName);
        if (suite != null) {
            suite.addTestCase(testCase);
        }
    }

    /**
     * Remove test suite.
     */
    public void removeTestSuite(String suiteName) {
        testSuites.removeIf(suite -> suite.getSuiteName().equals(suiteName));
    }

    /**
     * Get all test suites.
     */
    public List<TestSuite> getAllTestSuites() {
        return new ArrayList<>(testSuites);
    }

    // ==================== Test Execution ====================

    /**
     * Execute a single test case.
     */
    public TestResult executeTestCase(TestCase testCase) throws IOException {
        TestResult result = new TestResult(testCase);
        testCase.setStatus(TestCase.TestStatus.RUNNING);

        long startTime = System.currentTimeMillis();

        try {
            // Execute API request
            ApiClient.ApiResponse response = executeApiRequest(testCase);

            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTime(executionTime);
            testCase.setExecutionTime(executionTime);

            // Verify expected result
            if (verifyResponse(testCase, response)) {
                result.markPassed(response.statusCode, response.body);
            } else {
                result.markFailed(
                    "Expected result not matched",
                    response.body
                );
            }
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTime(executionTime);
            result.markFailed(e.getMessage(), "");
            result.setException(e);
            testCase.setErrorMessage(e.getMessage());
        }

        testResults.add(result);
        return result;
    }

    /**
     * Execute test case with automatic retry on failure.
     */
    public TestResult executeTestCaseWithRetry(TestCase testCase) throws IOException {
        TestResult result = null;
        int attemptCount = 0;

        while (attemptCount < maxRetries) {
            try {
                result = executeTestCase(testCase);
                if (result.isPassed()) {
                    return result;
                }
                attemptCount++;
                if (attemptCount < maxRetries) {
                    Thread.sleep(retryDelayMs);
                    testCase.incrementRetryCount();
                    result.setRetried(true);
                    result.setRetryAttempt(attemptCount);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return result;
    }

    /**
     * Execute an entire test suite.
     */
    public void executeTestSuite(String suiteName) {
        TestSuite suite = getTestSuite(suiteName);
        if (suite == null || suite.isPaused()) {
            return;
        }

        isRunning = true;
        long startTime = System.currentTimeMillis();

        for (TestCase testCase : suite.getTestCases()) {
            try {
                if (testCase.isSkipped()) {
                    testCase.setStatus(TestCase.TestStatus.SKIPPED);
                    continue;
                }
                executeTestCase(testCase);
            } catch (IOException e) {
                System.err.println("Error executing test: " + testCase.getTestCaseId());
                e.printStackTrace();
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        suite.setTotalExecutionTime(totalTime);
        suite.setExecutedDate(LocalDateTime.now());
        isRunning = false;
    }

    /**
     * Execute multiple test suites in parallel.
     */
    public void executeTestSuitesInParallel(List<String> suiteNames) {
        List<Future<?>> futures = new ArrayList<>();

        for (String suiteName : suiteNames) {
            Future<?> future = executorService.submit(() -> executeTestSuite(suiteName));
            futures.add(future);
        }

        // Wait for all suites to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Execute test cases matching a filter.
     */
    public void executeTestsByCategory(String category) {
        for (TestSuite suite : testSuites) {
            List<TestCase> cases = suite.getTestCasesByCategory(category);
            for (TestCase testCase : cases) {
                try {
                    executeTestCase(testCase);
                } catch (IOException e) {
                    System.err.println("Error executing test: " + testCase.getTestCaseId());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Execute tests by priority.
     */
    public void executeTestsByPriority(TestCase.TestPriority priority) {
        for (TestSuite suite : testSuites) {
            List<TestCase> cases = suite.getTestCasesByPriority(priority);
            for (TestCase testCase : cases) {
                try {
                    executeTestCase(testCase);
                } catch (IOException e) {
                    System.err.println("Error executing test: " + testCase.getTestCaseId());
                    e.printStackTrace();
                }
            }
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Execute API request based on test case configuration.
     */
    private ApiClient.ApiResponse executeApiRequest(TestCase testCase) throws IOException {
        String endpoint = testCase.getEndpoint();
        String method = testCase.getHttpMethod().toUpperCase();

        if ("GET".equals(method)) {
            return apiClient.get(endpoint);
        } else if ("POST".equals(method)) {
            return apiClient.post(endpoint, testCase.getTestData());
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    /**
     * Verify response against expected result.
     */
    private boolean verifyResponse(TestCase testCase, ApiClient.ApiResponse response) {
        Map<String, Object> expected = testCase.getExpectedResult();

        if (expected.isEmpty()) {
            // If no expected result defined, just check status code is not error
            return response.statusCode >= 200 && response.statusCode < 300;
        }

        // Check expected status code
        if (expected.containsKey("statusCode")) {
            int expectedCode = ((Number) expected.get("statusCode")).intValue();
            if (response.statusCode != expectedCode) {
                return false;
            }
        }

        // Check expected body content
        if (expected.containsKey("bodyContains")) {
            String expectedContent = (String) expected.get("bodyContains");
            return response.body.contains(expectedContent);
        }

        return true;
    }

    // ==================== Reporting & Analytics ====================

    /**
     * Get all test results.
     */
    public List<TestResult> getAllTestResults() {
        return new ArrayList<>(testResults);
    }

    /**
     * Get test results by status.
     */
    public List<TestResult> getTestResultsByStatus(TestCase.TestStatus status) {
        List<TestResult> filtered = new ArrayList<>();
        for (TestResult result : testResults) {
            if (result.getStatus() == status) {
                filtered.add(result);
            }
        }
        return filtered;
    }

    /**
     * Get total execution summary.
     */
    public ExecutionSummary getExecutionSummary() {
        ExecutionSummary summary = new ExecutionSummary();

        for (TestResult result : testResults) {
            summary.totalTests++;
            summary.totalExecutionTime += result.getExecutionTime();

            if (result.isPassed()) {
                summary.passedTests++;
            } else if (result.isFailed()) {
                summary.failedTests++;
            } else if (result.isSkipped()) {
                summary.skippedTests++;
            }
        }

        summary.calculatePercentages();
        return summary;
    }

    /**
     * Generate detailed test report.
     */
    public String generateDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n=== QA Agent Detailed Report ===\n");
        report.append("Agent: ").append(agentName).append("\n");
        report.append("Timestamp: ").append(LocalDateTime.now()).append("\n");
        report.append("Total Test Suites: ").append(testSuites.size()).append("\n\n");

        for (TestSuite suite : testSuites) {
            report.append("Suite: ").append(suite.getSuiteName()).append("\n");
            report.append("  Total: ").append(suite.getTotalTestCases()).append("\n");
            report.append("  Passed: ").append(suite.getPassedTestCount()).append("\n");
            report.append("  Failed: ").append(suite.getFailedTestCount()).append("\n");
            report.append("  Skipped: ").append(suite.getSkippedTestCount()).append("\n");
            report.append("  Pass Rate: ").append(String.format("%.2f%%", suite.getPassRate())).append("\n");
            report.append("  Avg Execution Time: ").append(suite.getAverageExecutionTime()).append("ms\n");
            report.append("  Total Execution Time: ").append(suite.getTotalExecutionTime()).append("ms\n\n");
        }

        ExecutionSummary summary = getExecutionSummary();
        report.append(summary.toString());

        return report.toString();
    }

    /**
     * Generate simple summary report.
     */
    public String generateSummaryReport() {
        ExecutionSummary summary = getExecutionSummary();
        return String.format(
            "\n=== Test Execution Summary ===\n" +
            "Total Tests: %d\n" +
            "Passed: %d (%.2f%%)\n" +
            "Failed: %d (%.2f%%)\n" +
            "Skipped: %d (%.2f%%)\n" +
            "Total Execution Time: %dms\n",
            summary.totalTests,
            summary.passedTests, summary.passedPercentage,
            summary.failedTests, summary.failedPercentage,
            summary.skippedTests, summary.skippedPercentage,
            summary.totalExecutionTime
        );
    }

    /**
     * Print test results for failed tests.
     */
    public void printFailedTests() {
        List<TestResult> failedTests = getTestResultsByStatus(TestCase.TestStatus.FAILED);

        System.out.println("\n=== Failed Tests ===");
        if (failedTests.isEmpty()) {
            System.out.println("No failed tests!");
            return;
        }

        for (TestResult result : failedTests) {
            System.out.println("\nTest: " + result.getTestCase().getTestName());
            System.out.println("Error: " + result.getErrorMessage());
            System.out.println("Response: " + result.getResponseBody());
        }
    }

    // ==================== Configuration ====================

    /**
     * Set maximum retry attempts.
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Set delay between retries in milliseconds.
     */
    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    /**
     * Check if agent is currently running tests.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Shutdown the agent and clean up resources.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get agent name.
     */
    public String getAgentName() {
        return agentName;
    }

    // ==================== Inner Class for Summary ====================

    /**
     * Execution summary statistics.
     */
    public static class ExecutionSummary {
        public int totalTests = 0;
        public int passedTests = 0;
        public int failedTests = 0;
        public int skippedTests = 0;
        public long totalExecutionTime = 0;
        public double passedPercentage = 0;
        public double failedPercentage = 0;
        public double skippedPercentage = 0;

        private void calculatePercentages() {
            if (totalTests > 0) {
                passedPercentage = (double) passedTests / totalTests * 100;
                failedPercentage = (double) failedTests / totalTests * 100;
                skippedPercentage = (double) skippedTests / totalTests * 100;
            }
        }

        @Override
        public String toString() {
            return String.format(
                "ExecutionSummary{total=%d, passed=%d, failed=%d, skipped=%d, time=%dms}",
                totalTests, passedTests, failedTests, skippedTests, totalExecutionTime
            );
        }
    }
}
