package com.automation.qa.model;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a suite/group of test cases with aggregated metrics.
 */
public class TestSuite {
    private String suiteName;
    private String description;
    private List<TestCase> testCases;
    private LocalDateTime createdDate;
    private LocalDateTime executedDate;
    private long totalExecutionTime;
    private boolean paused;

    public TestSuite(String suiteName) {
        this.suiteName = suiteName;
        this.testCases = new ArrayList<>();
        this.createdDate = LocalDateTime.now();
        this.totalExecutionTime = 0;
        this.paused = false;
    }

    /**
     * Add a test case to the suite.
     */
    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
    }

    /**
     * Add multiple test cases.
     */
    public void addTestCases(List<TestCase> cases) {
        testCases.addAll(cases);
    }

    /**
     * Remove a test case from the suite.
     */
    public void removeTestCase(TestCase testCase) {
        testCases.remove(testCase);
    }

    /**
     * Get test cases by category.
     */
    public List<TestCase> getTestCasesByCategory(String category) {
        List<TestCase> filtered = new ArrayList<>();
        for (TestCase testCase : testCases) {
            if (category.equals(testCase.getCategory())) {
                filtered.add(testCase);
            }
        }
        return filtered;
    }

    /**
     * Get test cases by priority.
     */
    public List<TestCase> getTestCasesByPriority(TestCase.TestPriority priority) {
        List<TestCase> filtered = new ArrayList<>();
        for (TestCase testCase : testCases) {
            if (priority == testCase.getPriority()) {
                filtered.add(testCase);
            }
        }
        return filtered;
    }

    /**
     * Get test cases by status.
     */
    public List<TestCase> getTestCasesByStatus(TestCase.TestStatus status) {
        List<TestCase> filtered = new ArrayList<>();
        for (TestCase testCase : testCases) {
            if (status == testCase.getStatus()) {
                filtered.add(testCase);
            }
        }
        return filtered;
    }

    /**
     * Get total number of test cases.
     */
    public int getTotalTestCases() {
        return testCases.size();
    }

    /**
     * Get number of passed tests.
     */
    public int getPassedTestCount() {
        return (int) testCases.stream()
                .filter(tc -> tc.getStatus() == TestCase.TestStatus.PASSED)
                .count();
    }

    /**
     * Get number of failed tests.
     */
    public int getFailedTestCount() {
        return (int) testCases.stream()
                .filter(tc -> tc.getStatus() == TestCase.TestStatus.FAILED)
                .count();
    }

    /**
     * Get number of skipped tests.
     */
    public int getSkippedTestCount() {
        return (int) testCases.stream()
                .filter(tc -> tc.getStatus() == TestCase.TestStatus.SKIPPED || tc.isSkipped())
                .count();
    }

    /**
     * Get number of pending tests.
     */
    public int getPendingTestCount() {
        return (int) testCases.stream()
                .filter(tc -> tc.getStatus() == TestCase.TestStatus.PENDING)
                .count();
    }

    /**
     * Get pass rate percentage.
     */
    public double getPassRate() {
        int total = getTotalTestCases();
        int passed = getPassedTestCount();
        return total > 0 ? (double) passed / total * 100 : 0;
    }

    /**
     * Get failure rate percentage.
     */
    public double getFailureRate() {
        int total = getTotalTestCases();
        int failed = getFailedTestCount();
        return total > 0 ? (double) failed / total * 100 : 0;
    }

    /**
     * Check if all tests passed.
     */
    public boolean isAllTestsPassed() {
        return getFailedTestCount() == 0 && getPassedTestCount() == getTotalTestCases();
    }

    /**
     * Get average execution time per test.
     */
    public long getAverageExecutionTime() {
        long total = testCases.stream()
                .mapToLong(TestCase::getExecutionTime)
                .sum();
        int count = testCases.size();
        return count > 0 ? total / count : 0;
    }

    // Getters and Setters
    public String getSuiteName() {
        return suiteName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getExecutedDate() {
        return executedDate;
    }

    public void setExecutedDate(LocalDateTime executedDate) {
        this.executedDate = executedDate;
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void setTotalExecutionTime(long totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public String toString() {
        return "TestSuite{" +
                "suiteName='" + suiteName + '\'' +
                ", total=" + getTotalTestCases() +
                ", passed=" + getPassedTestCount() +
                ", failed=" + getFailedTestCount() +
                ", passRate=" + String.format("%.2f%%", getPassRate()) +
                '}';
    }
}
