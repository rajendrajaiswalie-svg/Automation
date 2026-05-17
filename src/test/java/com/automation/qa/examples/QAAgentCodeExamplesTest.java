package com.automation.qa.examples;

import com.automation.api.ApiClient;
import com.automation.config.WireMockTestConfig;
import com.automation.qa.agent.QAAgent;
import com.automation.qa.model.TestCase;
import com.automation.qa.model.TestResult;
import com.automation.qa.model.TestSuite;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;

/**
 * Code examples and snippets for QA Agent usage patterns.
 */
@DisplayName("QA Agent Code Examples")
class QAAgentCodeExamplesTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private QAAgent qaAgent;
    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        String baseUrl = WireMockTestConfig.getBaseUrl(wireMock);
        apiClient = new ApiClient(baseUrl);
        qaAgent = new QAAgent("CodeExamples-Agent", apiClient);
        setupMocks();
    }

    @AfterEach
    void tearDown() {
        qaAgent.shutdown();
    }

    private void setupMocks() {
        wireMock.stubFor(get(urlPathMatching(".*"))
            .willReturn(aResponse().withStatus(200).withBody("{\"success\": true}")));
        wireMock.stubFor(post(urlPathMatching(".*"))
            .willReturn(aResponse().withStatus(201).withBody("{\"id\": 1}")));
    }

    // ==================== Example 1: Basic Test Creation ====================

    @Test
    @DisplayName("Example 1: Create and Execute Basic Test")
    void example1BasicTest() throws Exception {
        // Create test case
        TestCase testCase = new TestCase(
            "TC-001",           // Test ID
            "Get Users",        // Test Name
            "/api/users",       // Endpoint
            "GET"               // HTTP Method
        );

        // Set expected result
        testCase.setExpectedResult(Map.of("statusCode", 200));

        // Execute
        TestResult result = qaAgent.executeTestCase(testCase);

        // Verify
        assertThat(result.isPassed()).isTrue();
        System.out.println("✓ " + result.getSummary());
    }

    // ==================== Example 2: Test with Data ====================

    @Test
    @DisplayName("Example 2: POST Test with Test Data")
    void example2PostWithData() throws Exception {
        TestCase testCase = new TestCase(
            "TC-002",
            "Create User",
            "/api/users",
            "POST"
        );

        // Add test data
        testCase.setTestData(Map.of(
            "name", "John Doe",
            "email", "john@example.com",
            "role", "admin"
        ));

        // Set expected result
        testCase.setExpectedResult(Map.of("statusCode", 201));

        // Execute
        TestResult result = qaAgent.executeTestCase(testCase);

        assertThat(result.isPassed()).isTrue();
        System.out.println("✓ Created: " + result.getResponseBody());
    }

    // ==================== Example 3: Priority & Category ====================

    @Test
    @DisplayName("Example 3: Test Case with Priority and Category")
    void example3PriorityAndCategory() throws Exception {
        TestCase testCase = new TestCase(
            "TC-003",
            "Critical User Flow",
            "/api/users",
            "GET"
        );

        // Set priority and category
        testCase.setPriority(TestCase.TestPriority.CRITICAL);
        testCase.setCategory("User Management");
        testCase.setDescription("Critical path test for user operations");

        testCase.setExpectedResult(Map.of("statusCode", 200));

        TestResult result = qaAgent.executeTestCase(testCase);

        assertThat(result.isPassed()).isTrue();
        System.out.println("✓ Priority: " + testCase.getPriority());
        System.out.println("✓ Category: " + testCase.getCategory());
    }

    // ==================== Example 4: Test Suite Management ====================

    @Test
    @DisplayName("Example 4: Create and Manage Test Suite")
    void example4TestSuiteManagement() throws Exception {
        // Create suite
        TestSuite suite = qaAgent.createTestSuite("E-Commerce Tests");
        suite.setDescription("Complete e-commerce flow tests");

        // Add multiple tests
        for (int i = 1; i <= 3; i++) {
            TestCase tc = new TestCase(
                "TC-" + String.format("%03d", i),
                "Test " + i,
                "/api/endpoint" + i,
                "GET"
            );
            tc.setExpectedResult(Map.of("statusCode", 200));
            suite.addTestCase(tc);
        }

        // Execute all
        qaAgent.executeTestSuite("E-Commerce Tests");

        // Check results
        System.out.println("Total: " + suite.getTotalTestCases());
        System.out.println("Passed: " + suite.getPassedTestCount());
        System.out.println("Pass Rate: " + String.format("%.2f%%", suite.getPassRate()));

        assertThat(suite.isAllTestsPassed()).isTrue();
    }

    // ==================== Example 5: Test Filtering ====================

    @Test
    @DisplayName("Example 5: Filter and Execute Tests")
    void example5FilterTests() throws Exception {
        // Create suite with different priorities
        TestSuite suite = qaAgent.createTestSuite("Filtered Tests");

        String[] priorities = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};
        for (String priority : priorities) {
            TestCase tc = new TestCase(
                "TC-" + priority,
                priority + " Priority Test",
                "/api/test",
                "GET"
            );
            tc.setPriority(TestCase.TestPriority.valueOf(priority));
            tc.setExpectedResult(Map.of("statusCode", 200));
            suite.addTestCase(tc);
        }

        // Get critical tests
        List<TestCase> criticalTests = suite.getTestCasesByPriority(
            TestCase.TestPriority.CRITICAL
        );

        System.out.println("Critical tests: " + criticalTests.size());
        assertThat(criticalTests).hasSize(1);
    }

    // ==================== Example 6: Execute by Category ====================

    @Test
    @DisplayName("Example 6: Execute Tests by Category")
    void example6ExecuteByCategory() throws Exception {
        // Create suite
        TestSuite suite = qaAgent.createTestSuite("Categorized Tests");

        // Add tests with categories
        String[] categories = {"User", "Product", "Order"};
        for (String category : categories) {
            TestCase tc = new TestCase(
                "TC-" + category,
                category + " Test",
                "/api/" + category.toLowerCase(),
                "GET"
            );
            tc.setCategory(category);
            tc.setExpectedResult(Map.of("statusCode", 200));
            suite.addTestCase(tc);
        }

        // Get "User" category tests
        List<TestCase> userTests = suite.getTestCasesByCategory("User");

        System.out.println("User tests: " + userTests.size());
        assertThat(userTests).hasSize(1);
        assertThat(userTests.get(0).getCategory()).isEqualTo("User");
    }

    // ==================== Example 7: Retry Mechanism ====================

    @Test
    @DisplayName("Example 7: Test with Automatic Retry")
    void example7RetryMechanism() throws Exception {
        // Configure retry
        qaAgent.setMaxRetries(3);
        qaAgent.setRetryDelayMs(500);

        TestCase testCase = new TestCase(
            "TC-007",
            "Retry Test",
            "/api/endpoint",
            "GET"
        );
        testCase.setExpectedResult(Map.of("statusCode", 200));

        // Execute with retry
        TestResult result = qaAgent.executeTestCaseWithRetry(testCase);

        System.out.println("Retries: " + testCase.getRetryCount());
        System.out.println("Status: " + result.getStatus());

        assertThat(result.isPassed()).isTrue();
    }

    // ==================== Example 8: Detailed Result Analysis ====================

    @Test
    @DisplayName("Example 8: Analyze Test Results")
    void example8ResultAnalysis() throws Exception {
        TestCase testCase = new TestCase(
            "TC-008",
            "Analysis Test",
            "/api/endpoint",
            "GET"
        );
        testCase.setExpectedResult(Map.of("statusCode", 200));

        TestResult result = qaAgent.executeTestCase(testCase);

        // Analyze result
        System.out.println("=== Test Result Analysis ===");
        System.out.println("Test Name: " + result.getTestCase().getTestName());
        System.out.println("Status: " + result.getStatus());
        System.out.println("Status Code: " + result.getStatusCode());
        System.out.println("Execution Time: " + result.getExecutionTime() + "ms");
        System.out.println("Response: " + result.getResponseBody());
        System.out.println("Passed: " + result.isPassed());

        if (result.isFailed()) {
            System.out.println("Error: " + result.getErrorMessage());
        }

        assertThat(result.getExecutionTime()).isGreaterThan(0);
    }

    // ==================== Example 9: Generate Reports ====================

    @Test
    @DisplayName("Example 9: Generate Execution Reports")
    void example9Reports() throws Exception {
        // Create and execute tests
        TestSuite suite = qaAgent.createTestSuite("Report Suite");

        for (int i = 1; i <= 5; i++) {
            TestCase tc = new TestCase(
                "TC-" + String.format("%03d", i),
                "Test " + i,
                "/api/endpoint",
                "GET"
            );
            tc.setExpectedResult(Map.of("statusCode", 200));
            suite.addTestCase(tc);
        }

        qaAgent.executeTestSuite("Report Suite");

        // Generate summary
        String summary = qaAgent.generateSummaryReport();
        System.out.println(summary);

        // Get execution summary
        QAAgent.ExecutionSummary execSummary = qaAgent.getExecutionSummary();
        System.out.println("\n=== Quick Stats ===");
        System.out.println("Total: " + execSummary.totalTests);
        System.out.println("Passed: " + execSummary.passedTests);
        System.out.println("Time: " + execSummary.totalExecutionTime + "ms");

        assertThat(execSummary.totalTests).isEqualTo(5);
    }

    // ==================== Example 10: Complex Scenario ====================

    @Test
    @DisplayName("Example 10: Complex Multi-Suite Scenario")
    void example10ComplexScenario() throws Exception {
        // Setup 3 different test suites
        String[] suiteNames = {"User Tests", "Product Tests", "Order Tests"};

        for (String suiteName : suiteNames) {
            TestSuite suite = qaAgent.createTestSuite(suiteName);

            // Add 3 tests per suite
            for (int i = 1; i <= 3; i++) {
                TestCase tc = new TestCase(
                    "TC-" + suiteName.replace(" ", "") + i,
                    suiteName + " - Test " + i,
                    "/api/" + suiteName.toLowerCase().replace(" ", ""),
                    "GET"
                );

                // Set priority based on test number
                if (i == 1) {
                    tc.setPriority(TestCase.TestPriority.CRITICAL);
                } else if (i == 2) {
                    tc.setPriority(TestCase.TestPriority.HIGH);
                } else {
                    tc.setPriority(TestCase.TestPriority.MEDIUM);
                }

                tc.setExpectedResult(Map.of("statusCode", 200));
                suite.addTestCase(tc);
            }
        }

        // Execute suites in parallel
        qaAgent.executeTestSuitesInParallel(Arrays.asList(suiteNames));

        // Generate detailed report
        String report = qaAgent.generateDetailedReport();
        System.out.println(report);

        // Check overall status
        QAAgent.ExecutionSummary summary = qaAgent.getExecutionSummary();
        assertThat(summary.totalTests).isEqualTo(9);
        assertThat(summary.passedPercentage).isEqualTo(100.0);
    }

    // ==================== Example 11: Skip Tests ====================

    @Test
    @DisplayName("Example 11: Skip Test Cases")
    void example11SkipTests() throws Exception {
        TestSuite suite = qaAgent.createTestSuite("Skip Example");

        TestCase tc1 = new TestCase("TC-001", "Normal Test", "/api/1", "GET");
        tc1.setExpectedResult(Map.of("statusCode", 200));

        TestCase tc2 = new TestCase("TC-002", "Skipped Test", "/api/2", "GET");
        tc2.setSkipped(true);  // Mark as skipped
        tc2.setExpectedResult(Map.of("statusCode", 200));

        suite.addTestCases(Arrays.asList(tc1, tc2));

        qaAgent.executeTestSuite("Skip Example");

        System.out.println("Total: " + suite.getTotalTestCases());
        System.out.println("Passed: " + suite.getPassedTestCount());
        System.out.println("Skipped: " + suite.getSkippedTestCount());

        assertThat(suite.getSkippedTestCount()).isGreaterThanOrEqualTo(1);
    }

    // ==================== Example 12: Body Content Matching ====================

    @Test
    @DisplayName("Example 12: Match Response Body Content")
    void example12BodyContentMatching() throws Exception {
        TestCase testCase = new TestCase(
            "TC-012",
            "Body Content Match",
            "/api/endpoint",
            "GET"
        );

        // Set expected result with body content check
        testCase.setExpectedResult(Map.of(
            "statusCode", 200,
            "bodyContains", "success"
        ));

        TestResult result = qaAgent.executeTestCase(testCase);

        System.out.println("Response: " + result.getResponseBody());
        System.out.println("Contains 'success': " + 
            result.getResponseBody().contains("success"));

        assertThat(result.isPassed()).isTrue();
    }

    // ==================== Example 13: Metrics & Statistics ====================

    @Test
    @DisplayName("Example 13: Test Metrics and Statistics")
    void example13Metrics() throws Exception {
        TestSuite suite = qaAgent.createTestSuite("Metrics Suite");

        // Create tests with delays
        for (int i = 1; i <= 5; i++) {
            TestCase tc = new TestCase(
                "TC-" + String.format("%03d", i),
                "Test " + i,
                "/api/endpoint",
                "GET"
            );
            tc.setExpectedResult(Map.of("statusCode", 200));
            suite.addTestCase(tc);
        }

        qaAgent.executeTestSuite("Metrics Suite");

        // Print metrics
        System.out.println("=== Test Metrics ===");
        System.out.println("Total Tests: " + suite.getTotalTestCases());
        System.out.println("Passed: " + suite.getPassedTestCount());
        System.out.println("Failed: " + suite.getFailedTestCount());
        System.out.println("Pass Rate: " + String.format("%.2f%%", suite.getPassRate()));
        System.out.println("Avg Execution Time: " + suite.getAverageExecutionTime() + "ms");
        System.out.println("Total Execution Time: " + suite.getTotalExecutionTime() + "ms");

        assertThat(suite.getPassRate()).isEqualTo(100.0);
    }

    // ==================== Example 14: Get All Results ====================

    @Test
    @DisplayName("Example 14: Retrieve and Process All Results")
    void example14AllResults() throws Exception {
        // Execute some tests
        TestCase tc1 = new TestCase("TC-001", "Test 1", "/api/1", "GET");
        tc1.setExpectedResult(Map.of("statusCode", 200));

        TestCase tc2 = new TestCase("TC-002", "Test 2", "/api/2", "GET");
        tc2.setExpectedResult(Map.of("statusCode", 200));

        qaAgent.executeTestCase(tc1);
        qaAgent.executeTestCase(tc2);

        // Get all results
        List<TestResult> allResults = qaAgent.getAllTestResults();

        System.out.println("=== All Test Results ===");
        for (TestResult result : allResults) {
            System.out.println(result.getSummary());
        }

        // Get by status
        List<TestResult> passed = qaAgent.getTestResultsByStatus(
            TestCase.TestStatus.PASSED
        );

        System.out.println("\nPassed Tests: " + passed.size());
        assertThat(allResults.size()).isGreaterThanOrEqualTo(2);
    }
}
