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
 * Practical examples demonstrating QA Agent in real-world scenarios.
 */
@DisplayName("QA Agent Practical Examples")
class QAAgentPracticalExamplesTest {

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
        qaAgent = new QAAgent("E-Commerce-QA-Agent", apiClient);
        setupMocks();
    }

    @AfterEach
    void tearDown() {
        qaAgent.shutdown();
    }

    // ==================== Setup Mocks ====================

    private void setupMocks() {
        // User endpoints
        wireMock.stubFor(
            get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"id\": 1, \"name\": \"John Doe\", \"email\": \"john@example.com\"}"))
        );

        wireMock.stubFor(
            get(urlEqualTo("/api/users/999"))
                .willReturn(aResponse().withStatus(404).withBody("{\"error\": \"User not found\"}"))
        );

        wireMock.stubFor(
            post(urlEqualTo("/api/users"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(201)
                    .withBody("{\"id\": 2, \"name\": \"Jane Doe\"}"))
        );

        // Product endpoints
        wireMock.stubFor(
            get(urlEqualTo("/api/products"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("[{\"id\": 1, \"name\": \"Product 1\", \"price\": 99.99}]"))
        );

        wireMock.stubFor(
            get(urlPathMatching("/api/products/[0-9]+"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"id\": 1, \"name\": \"Product 1\"}"))
        );

        // Order endpoints
        wireMock.stubFor(
            post(urlEqualTo("/api/orders"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(201)
                    .withBody("{\"orderId\": 100, \"status\": \"confirmed\"}"))
        );

        wireMock.stubFor(
            get(urlPathMatching("/api/orders/[0-9]+"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"orderId\": 100, \"status\": \"delivered\"}"))
        );
    }

    // ==================== Example 1: User Management Test Suite ====================

    @Test
    @DisplayName("Example 1: User Management API Tests")
    void example1UserManagementTests() throws Exception {
        // Create test suite
        TestSuite userSuite = qaAgent.createTestSuite("User Management API");
        userSuite.setDescription("Test user CRUD operations");

        // Test Case 1: Get existing user
        TestCase tc1 = new TestCase("TC-U001", "Get User by ID", "/api/users/1", "GET");
        tc1.setCategory("User");
        tc1.setPriority(TestCase.TestPriority.CRITICAL);
        tc1.setDescription("Should return user details for valid user ID");
        tc1.setExpectedResult(Map.of(
            "statusCode", 200,
            "bodyContains", "John Doe"
        ));

        // Test Case 2: Get non-existing user
        TestCase tc2 = new TestCase("TC-U002", "Get Non-Existing User", "/api/users/999", "GET");
        tc2.setCategory("User");
        tc2.setPriority(TestCase.TestPriority.HIGH);
        tc2.setExpectedResult(Map.of("statusCode", 404));

        // Test Case 3: Create new user
        TestCase tc3 = new TestCase("TC-U003", "Create New User", "/api/users", "POST");
        tc3.setCategory("User");
        tc3.setPriority(TestCase.TestPriority.CRITICAL);
        tc3.setTestData(Map.of("name", "Jane Doe", "email", "jane@example.com"));
        tc3.setExpectedResult(Map.of("statusCode", 201));

        userSuite.addTestCases(Arrays.asList(tc1, tc2, tc3));

        // Execute suite
        qaAgent.executeTestSuite("User Management API");

        // Print results
        System.out.println(userSuite);
        assertThat(userSuite.getPassedTestCount()).isEqualTo(3);
    }

    // ==================== Example 2: Product Catalog Test Suite ====================

    @Test
    @DisplayName("Example 2: Product Catalog API Tests")
    void example2ProductCatalogTests() throws Exception {
        // Create test suite
        TestSuite productSuite = qaAgent.createTestSuite("Product Catalog API");

        // Test Case 1: Get all products
        TestCase tc1 = new TestCase("TC-P001", "Get All Products", "/api/products", "GET");
        tc1.setCategory("Product");
        tc1.setPriority(TestCase.TestPriority.HIGH);
        tc1.setExpectedResult(Map.of("statusCode", 200));

        // Test Case 2: Get product by ID
        TestCase tc2 = new TestCase("TC-P002", "Get Product by ID", "/api/products/1", "GET");
        tc2.setCategory("Product");
        tc2.setPriority(TestCase.TestPriority.HIGH);
        tc2.setExpectedResult(Map.of("statusCode", 200, "bodyContains", "Product 1"));

        productSuite.addTestCases(Arrays.asList(tc1, tc2));

        // Execute suite
        qaAgent.executeTestSuite("Product Catalog API");

        // Verify results
        System.out.println(productSuite);
        assertThat(productSuite.isAllTestsPassed()).isTrue();
        assertThat(productSuite.getPassRate()).isEqualTo(100.0);
    }

    // ==================== Example 3: Order Management Test Suite ====================

    @Test
    @DisplayName("Example 3: Order Management API Tests")
    void example3OrderManagementTests() throws Exception {
        // Create test suite
        TestSuite orderSuite = qaAgent.createTestSuite("Order Management API");

        // Test Case 1: Create order
        TestCase tc1 = new TestCase("TC-O001", "Create Order", "/api/orders", "POST");
        tc1.setCategory("Order");
        tc1.setPriority(TestCase.TestPriority.CRITICAL);
        tc1.setTestData(Map.of(
            "userId", 1,
            "productIds", List.of(1, 2, 3),
            "quantity", 5
        ));
        tc1.setExpectedResult(Map.of("statusCode", 201, "bodyContains", "confirmed"));

        // Test Case 2: Get order status
        TestCase tc2 = new TestCase("TC-O002", "Get Order Status", "/api/orders/100", "GET");
        tc2.setCategory("Order");
        tc2.setPriority(TestCase.TestPriority.HIGH);
        tc2.setExpectedResult(Map.of("statusCode", 200, "bodyContains", "delivered"));

        orderSuite.addTestCases(Arrays.asList(tc1, tc2));

        // Execute suite
        qaAgent.executeTestSuite("Order Management API");

        // Print results
        System.out.println(orderSuite);
        assertThat(orderSuite.getPassedTestCount()).isEqualTo(2);
    }

    // ==================== Example 4: Run Multiple Suites and Report ====================

    @Test
    @DisplayName("Example 4: Execute Multiple Test Suites with Report")
    void example4MultiSuiteExecution() throws Exception {
        // Create and populate multiple suites
        TestSuite suite1 = qaAgent.createTestSuite("User Tests");
        TestCase tc1 = new TestCase("TC-001", "Get User", "/api/users/1", "GET");
        tc1.setExpectedResult(Map.of("statusCode", 200));
        suite1.addTestCase(tc1);

        TestSuite suite2 = qaAgent.createTestSuite("Product Tests");
        TestCase tc2 = new TestCase("TC-002", "Get Products", "/api/products", "GET");
        tc2.setExpectedResult(Map.of("statusCode", 200));
        suite2.addTestCase(tc2);

        TestSuite suite3 = qaAgent.createTestSuite("Order Tests");
        TestCase tc3 = new TestCase("TC-003", "Create Order", "/api/orders", "POST");
        tc3.setExpectedResult(Map.of("statusCode", 201));
        suite3.addTestCase(tc3);

        // Execute all suites
        qaAgent.executeTestSuitesInParallel(Arrays.asList(
            "User Tests",
            "Product Tests",
            "Order Tests"
        ));

        // Generate and print reports
        System.out.println(qaAgent.generateSummaryReport());
        System.out.println(qaAgent.generateDetailedReport());

        // Verify
        QAAgent.ExecutionSummary summary = qaAgent.getExecutionSummary();
        assertThat(summary.totalTests).isEqualTo(3);
        assertThat(summary.passedTests).isEqualTo(3);
    }

    // ==================== Example 5: Test Filtering and Execution ====================

    @Test
    @DisplayName("Example 5: Execute Tests by Priority")
    void example5FilterAndExecuteByPriority() throws Exception {
        // Create test suite with different priorities
        TestSuite suite = qaAgent.createTestSuite("Priority Tests");

        TestCase critical = new TestCase("TC-001", "Critical Test", "/api/users/1", "GET");
        critical.setPriority(TestCase.TestPriority.CRITICAL);
        critical.setExpectedResult(Map.of("statusCode", 200));

        TestCase high = new TestCase("TC-002", "High Priority", "/api/products", "GET");
        high.setPriority(TestCase.TestPriority.HIGH);
        high.setExpectedResult(Map.of("statusCode", 200));

        TestCase low = new TestCase("TC-003", "Low Priority", "/api/orders/100", "GET");
        low.setPriority(TestCase.TestPriority.LOW);
        low.setExpectedResult(Map.of("statusCode", 200));

        suite.addTestCases(Arrays.asList(critical, high, low));

        // Execute only CRITICAL tests
        qaAgent.executeTestsByPriority(TestCase.TestPriority.CRITICAL);

        // Verify
        List<TestResult> results = qaAgent.getTestResultsByStatus(TestCase.TestStatus.PASSED);
        assertThat(results).isNotEmpty();
    }

    // ==================== Example 6: Test Category Grouping ====================

    @Test
    @DisplayName("Example 6: Test by Category")
    void example6ExecuteByCategory() throws Exception {
        // Create test suite with different categories
        TestSuite suite = qaAgent.createTestSuite("Categorized Tests");

        TestCase userTest = new TestCase("TC-001", "User Test", "/api/users/1", "GET");
        userTest.setCategory("User");
        userTest.setExpectedResult(Map.of("statusCode", 200));

        TestCase productTest = new TestCase("TC-002", "Product Test", "/api/products", "GET");
        productTest.setCategory("Product");
        productTest.setExpectedResult(Map.of("statusCode", 200));

        TestCase userTest2 = new TestCase("TC-003", "Create User", "/api/users", "POST");
        userTest2.setCategory("User");
        userTest2.setExpectedResult(Map.of("statusCode", 201));

        suite.addTestCases(Arrays.asList(userTest, productTest, userTest2));

        // Execute only User category tests
        qaAgent.executeTestsByCategory("User");

        // Verify
        List<TestResult> results = qaAgent.getTestResultsByStatus(TestCase.TestStatus.PASSED);
        assertThat(results.size()).isGreaterThanOrEqualTo(2);
    }

    // ==================== Example 7: Failed Test Analysis ====================

    @Test
    @DisplayName("Example 7: Analyze Failed Tests")
    void example7FailedTestAnalysis() throws Exception {
        // Create test with wrong expectation (will fail)
        TestSuite suite = qaAgent.createTestSuite("Failure Analysis");

        TestCase failingTest = new TestCase("TC-001", "Will Fail", "/api/users/999", "GET");
        failingTest.setExpectedResult(Map.of("statusCode", 200)); // Expected 200 but will get 404
        suite.addTestCase(failingTest);

        // Execute
        qaAgent.executeTestSuite("Failure Analysis");

        // Check results
        List<TestResult> failedTests = qaAgent.getTestResultsByStatus(TestCase.TestStatus.FAILED);
        assertThat(failedTests).hasSize(1);

        // Print failed test details
        qaAgent.printFailedTests();
    }

    // ==================== Example 8: Detailed Metrics ====================

    @Test
    @DisplayName("Example 8: Test Metrics and Statistics")
    void example8MetricsAndStatistics() throws Exception {
        // Create and execute tests
        TestSuite suite = qaAgent.createTestSuite("Metrics Suite");

        TestCase tc1 = new TestCase("TC-001", "Test 1", "/api/users/1", "GET");
        tc1.setExpectedResult(Map.of("statusCode", 200));

        TestCase tc2 = new TestCase("TC-002", "Test 2", "/api/products", "GET");
        tc2.setExpectedResult(Map.of("statusCode", 200));

        TestCase tc3 = new TestCase("TC-003", "Test 3", "/api/orders", "POST");
        tc3.setExpectedResult(Map.of("statusCode", 201));

        suite.addTestCases(Arrays.asList(tc1, tc2, tc3));

        // Execute
        qaAgent.executeTestSuite("Metrics Suite");

        // Print metrics
        System.out.println("\n=== Test Metrics ===");
        System.out.println("Total: " + suite.getTotalTestCases());
        System.out.println("Passed: " + suite.getPassedTestCount());
        System.out.println("Failed: " + suite.getFailedTestCount());
        System.out.println("Pass Rate: " + String.format("%.2f%%", suite.getPassRate()));
        System.out.println("Avg Time: " + suite.getAverageExecutionTime() + "ms");

        // Verify metrics
        assertThat(suite.getPassRate()).isEqualTo(100.0);
        assertThat(suite.getAverageExecutionTime()).isGreaterThan(0);
    }
}
