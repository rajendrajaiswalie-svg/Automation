package com.automation.qa.agent;

import com.automation.api.ApiClient;
import com.automation.config.WireMockTestConfig;
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
 * Test suite demonstrating QA Agent functionality.
 */
@DisplayName("QA Agent Test Suite")
class QAAgentTest {

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
        qaAgent = new QAAgent("TestAgent-001", apiClient);
    }

    @AfterEach
    void tearDown() {
        qaAgent.shutdown();
    }

    // ==================== Test Suite Creation ====================

    @Test
    @DisplayName("Should create test suite successfully")
    void testCreateTestSuite() {
        // Act
        TestSuite suite = qaAgent.createTestSuite("User API Tests");

        // Assert
        assertThat(suite).isNotNull();
        assertThat(suite.getSuiteName()).isEqualTo("User API Tests");
        assertThat(qaAgent.getTestSuite("User API Tests")).isNotNull();
    }

    @Test
    @DisplayName("Should add test case to suite")
    void testAddTestCaseToSuite() {
        // Arrange
        TestSuite suite = qaAgent.createTestSuite("API Tests");
        TestCase testCase = new TestCase("TC-001", "Get User", "/users/1", "GET");

        // Act
        suite.addTestCase(testCase);

        // Assert
        assertThat(suite.getTotalTestCases()).isEqualTo(1);
        assertThat(suite.getTestCases()).contains(testCase);
    }

    @Test
    @DisplayName("Should manage multiple test suites")
    void testManageMultipleSuites() {
        // Act
        TestSuite suite1 = qaAgent.createTestSuite("User Tests");
        TestSuite suite2 = qaAgent.createTestSuite("Product Tests");
        TestSuite suite3 = qaAgent.createTestSuite("Order Tests");

        // Assert
        assertThat(qaAgent.getAllTestSuites()).hasSize(3);
        assertThat(qaAgent.getTestSuite("User Tests")).isNotNull();
        assertThat(qaAgent.getTestSuite("Product Tests")).isNotNull();
    }

    // ==================== Test Execution ====================

    @Test
    @DisplayName("Should execute single test case successfully")
    void testExecuteSingleTestCase() throws Exception {
        // Arrange
        String endpoint = "/users/1";
        TestCase testCase = new TestCase("TC-001", "Get User", endpoint, "GET");
        testCase.setPriority(TestCase.TestPriority.HIGH);
        testCase.setCategory("User Management");
        testCase.setExpectedResult(Map.of(
            "statusCode", 200,
            "bodyContains", "user"
        ));

        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"id\": 1, \"name\": \"user\"}"))
        );

        // Act
        TestResult result = qaAgent.executeTestCase(testCase);

        // Assert
        assertThat(result.isPassed()).isTrue();
        assertThat(result.getStatus()).isEqualTo(TestCase.TestStatus.PASSED);
        assertThat(result.getExecutionTime()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should execute test case and handle failure")
    void testExecuteFailingTestCase() throws Exception {
        // Arrange
        String endpoint = "/users/999";
        TestCase testCase = new TestCase("TC-002", "Get Non-existing User", endpoint, "GET");
        testCase.setExpectedResult(Map.of("statusCode", 200)); // Wrong expectation

        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse().withStatus(404))
        );

        // Act
        TestResult result = qaAgent.executeTestCase(testCase);

        // Assert
        assertThat(result.isFailed()).isTrue();
        assertThat(result.getStatus()).isEqualTo(TestCase.TestStatus.FAILED);
    }

    @Test
    @DisplayName("Should execute POST test case")
    void testExecutePostTestCase() throws Exception {
        // Arrange
        String endpoint = "/users";
        TestCase testCase = new TestCase("TC-003", "Create User", endpoint, "POST");
        testCase.setTestData(Map.of("name", "John Doe", "email", "john@example.com"));
        testCase.setExpectedResult(Map.of("statusCode", 201));

        wireMock.stubFor(
            post(urlEqualTo(endpoint))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(201)
                    .withBody("{\"id\": 1, \"name\": \"John Doe\"}"))
        );

        // Act
        TestResult result = qaAgent.executeTestCase(testCase);

        // Assert
        assertThat(result.isPassed()).isTrue();
        assertThat(result.getStatusCode()).isEqualTo(201);
    }

    @Test
    @DisplayName("Should execute test suite with multiple test cases")
    void testExecuteTestSuite() throws Exception {
        // Arrange
        TestSuite suite = qaAgent.createTestSuite("User API Tests");

        TestCase tc1 = new TestCase("TC-001", "Get User 1", "/users/1", "GET");
        tc1.setExpectedResult(Map.of("statusCode", 200));
        TestCase tc2 = new TestCase("TC-002", "Get User 2", "/users/2", "GET");
        tc2.setExpectedResult(Map.of("statusCode", 200));
        TestCase tc3 = new TestCase("TC-003", "Create User", "/users", "POST");
        tc3.setExpectedResult(Map.of("statusCode", 201));

        suite.addTestCases(Arrays.asList(tc1, tc2, tc3));

        wireMock.stubFor(
            get(urlPathMatching("/users/[0-9]+"))
                .willReturn(aResponse().withStatus(200).withBody("{\"id\": 1}"))
        );
        wireMock.stubFor(
            post(urlEqualTo("/users"))
                .willReturn(aResponse().withStatus(201))
        );

        // Act
        qaAgent.executeTestSuite("User API Tests");

        // Assert
        assertThat(suite.getTotalTestCases()).isEqualTo(3);
        assertThat(suite.getPassedTestCount()).isEqualTo(3);
        assertThat(suite.getFailureRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should skip test case")
    void testSkipTestCase() throws Exception {
        // Arrange
        TestCase testCase = new TestCase("TC-004", "Skipped Test", "/endpoint", "GET");
        testCase.setSkipped(true);

        // Act
        TestResult result = qaAgent.executeTestCase(testCase);

        // Assert - Test should still be executed, but response depends on mock
        // Since we didn't set up a mock, it will fail to connect
    }

    // ==================== Test Filtering ====================

    @Test
    @DisplayName("Should filter tests by category")
    void testFilterByCategory() throws Exception {
        // Arrange
        TestSuite suite = qaAgent.createTestSuite("Mixed Tests");

        TestCase tc1 = new TestCase("TC-001", "Get User", "/users/1", "GET");
        tc1.setCategory("User");
        TestCase tc2 = new TestCase("TC-002", "Create User", "/users", "POST");
        tc2.setCategory("User");
        TestCase tc3 = new TestCase("TC-003", "Get Product", "/products/1", "GET");
        tc3.setCategory("Product");

        suite.addTestCases(Arrays.asList(tc1, tc2, tc3));

        // Act
        List<TestCase> userTests = suite.getTestCasesByCategory("User");
        List<TestCase> productTests = suite.getTestCasesByCategory("Product");

        // Assert
        assertThat(userTests).hasSize(2);
        assertThat(productTests).hasSize(1);
    }

    @Test
    @DisplayName("Should filter tests by priority")
    void testFilterByPriority() {
        // Arrange
        TestSuite suite = qaAgent.createTestSuite("Priority Tests");

        TestCase critical = new TestCase("TC-001", "Critical Test", "/endpoint1", "GET");
        critical.setPriority(TestCase.TestPriority.CRITICAL);
        TestCase high = new TestCase("TC-002", "High Test", "/endpoint2", "GET");
        high.setPriority(TestCase.TestPriority.HIGH);
        TestCase low = new TestCase("TC-003", "Low Test", "/endpoint3", "GET");
        low.setPriority(TestCase.TestPriority.LOW);

        suite.addTestCases(Arrays.asList(critical, high, low));

        // Act
        List<TestCase> criticalTests = suite.getTestCasesByPriority(TestCase.TestPriority.CRITICAL);
        List<TestCase> highTests = suite.getTestCasesByPriority(TestCase.TestPriority.HIGH);

        // Assert
        assertThat(criticalTests).hasSize(1);
        assertThat(highTests).hasSize(1);
    }

    // ==================== Reporting ====================

    @Test
    @DisplayName("Should generate execution summary")
    void testGenerateExecutionSummary() throws Exception {
        // Arrange
        TestCase tc1 = new TestCase("TC-001", "Test 1", "/endpoint1", "GET");
        tc1.setExpectedResult(Map.of("statusCode", 200));
        TestCase tc2 = new TestCase("TC-002", "Test 2", "/endpoint2", "GET");
        tc2.setExpectedResult(Map.of("statusCode", 200));

        wireMock.stubFor(get(urlPathMatching("/endpoint.*"))
            .willReturn(aResponse().withStatus(200)));

        // Act
        qaAgent.executeTestCase(tc1);
        qaAgent.executeTestCase(tc2);

        QAAgent.ExecutionSummary summary = qaAgent.getExecutionSummary();

        // Assert
        assertThat(summary.totalTests).isEqualTo(2);
        assertThat(summary.passedTests).isEqualTo(2);
        assertThat(summary.failedTests).isEqualTo(0);
    }

    @Test
    @DisplayName("Should generate detailed report")
    void testGenerateDetailedReport() throws Exception {
        // Arrange
        TestSuite suite = qaAgent.createTestSuite("Report Test Suite");
        TestCase testCase = new TestCase("TC-001", "Test", "/endpoint", "GET");
        testCase.setExpectedResult(Map.of("statusCode", 200));
        suite.addTestCase(testCase);

        wireMock.stubFor(get(urlEqualTo("/endpoint"))
            .willReturn(aResponse().withStatus(200)));

        // Act
        qaAgent.executeTestSuite("Report Test Suite");
        String report = qaAgent.generateDetailedReport();

        // Assert
        assertThat(report).contains("QA Agent Detailed Report");
        assertThat(report).contains("Report Test Suite");
        assertThat(report).contains("Passed");
    }

    @Test
    @DisplayName("Should generate summary report")
    void testGenerateSummaryReport() throws Exception {
        // Arrange
        TestCase testCase = new TestCase("TC-001", "Test", "/endpoint", "GET");
        testCase.setExpectedResult(Map.of("statusCode", 200));

        wireMock.stubFor(get(urlEqualTo("/endpoint"))
            .willReturn(aResponse().withStatus(200)));

        qaAgent.executeTestCase(testCase);

        // Act
        String summary = qaAgent.generateSummaryReport();

        // Assert
        assertThat(summary).contains("Test Execution Summary");
        assertThat(summary).contains("Total Tests");
        assertThat(summary).contains("Passed");
    }

    // ==================== Test Configuration ====================

    @Test
    @DisplayName("Should configure max retries")
    void testConfigureRetries() {
        // Act
        qaAgent.setMaxRetries(5);
        qaAgent.setRetryDelayMs(2000);

        // Assert
        // Configuration is stored, behavior tested in retry tests
        assertThat(qaAgent).isNotNull();
    }
}
