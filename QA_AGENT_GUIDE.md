# QA Agent Documentation

## Overview

The **QA Agent** is a comprehensive test automation framework built on top of JUnit5 and WireMock. It provides automated test case management, execution, reporting, and analytics capabilities.

## Architecture

### Components

1. **TestCase** - Represents individual test cases with metadata
2. **TestSuite** - Groups test cases with aggregated metrics
3. **TestResult** - Tracks execution results and performance
4. **QAAgent** - Main orchestrator for test management and execution

## Key Features

✅ **Test Case Management**
- Create and organize test cases
- Define test data and expected results
- Categorize and prioritize tests
- Track test metadata

✅ **Test Execution**
- Execute single test cases
- Execute entire test suites
- Execute tests by category or priority
- Parallel test execution
- Automatic retry on failure

✅ **Reporting & Analytics**
- Summary reports
- Detailed execution reports
- Pass/fail rates and statistics
- Performance metrics
- Failed test analysis

✅ **Advanced Features**
- Configurable retry mechanisms
- Delay configuration between retries
- Concurrent test execution
- Test filtering and grouping
- Mock API integration

## Quick Start

### 1. Create QA Agent

```java
QAAgent qaAgent = new QAAgent("My-QA-Agent", apiClient);
```

### 2. Create Test Suite

```java
TestSuite suite = qaAgent.createTestSuite("User API Tests");
```

### 3. Create Test Cases

```java
TestCase testCase = new TestCase("TC-001", "Get User", "/api/users/1", "GET");
testCase.setPriority(TestCase.TestPriority.HIGH);
testCase.setCategory("User");
testCase.setExpectedResult(Map.of(
    "statusCode", 200,
    "bodyContains", "user"
));

suite.addTestCase(testCase);
```

### 4. Execute Tests

```java
qaAgent.executeTestSuite("User API Tests");
```

### 5. View Results

```java
String report = qaAgent.generateSummaryReport();
System.out.println(report);
```

## Detailed Usage

### Creating Test Cases

```java
// Basic test case
TestCase testCase = new TestCase(
    "TC-001",          // Test Case ID
    "Test Name",       // Display Name
    "/api/endpoint",   // API Endpoint
    "GET"              // HTTP Method
);

// Configure test case
testCase.setDescription("Test description");
testCase.setCategory("Category Name");
testCase.setPriority(TestCase.TestPriority.CRITICAL); // CRITICAL, HIGH, MEDIUM, LOW

// Set test data (for POST/PUT requests)
testCase.setTestData(Map.of(
    "name", "John",
    "email", "john@example.com"
));

// Set expected result
testCase.setExpectedResult(Map.of(
    "statusCode", 200,
    "bodyContains", "expected text"
));

// Skip test if needed
testCase.setSkipped(false);
```

### Executing Tests

#### Single Test Case
```java
TestResult result = qaAgent.executeTestCase(testCase);

if (result.isPassed()) {
    System.out.println("Test passed!");
} else if (result.isFailed()) {
    System.out.println("Test failed: " + result.getErrorMessage());
}
```

#### With Automatic Retry
```java
qaAgent.setMaxRetries(3);
qaAgent.setRetryDelayMs(1000);

TestResult result = qaAgent.executeTestCaseWithRetry(testCase);
```

#### Entire Test Suite
```java
qaAgent.executeTestSuite("Suite Name");
```

#### Multiple Suites in Parallel
```java
qaAgent.executeTestSuitesInParallel(Arrays.asList(
    "Suite 1",
    "Suite 2",
    "Suite 3"
));
```

#### By Category
```java
qaAgent.executeTestsByCategory("User Management");
```

#### By Priority
```java
qaAgent.executeTestsByPriority(TestCase.TestPriority.CRITICAL);
```

### Filtering Tests

#### Get Tests by Category
```java
List<TestCase> userTests = suite.getTestCasesByCategory("User");
```

#### Get Tests by Priority
```java
List<TestCase> criticalTests = suite.getTestCasesByPriority(
    TestCase.TestPriority.CRITICAL
);
```

#### Get Tests by Status
```java
List<TestCase> failedTests = suite.getTestCasesByStatus(
    TestCase.TestStatus.FAILED
);
```

### Reporting

#### Summary Report
```java
String summary = qaAgent.generateSummaryReport();
System.out.println(summary);

// Output:
// === Test Execution Summary ===
// Total Tests: 10
// Passed: 8 (80.00%)
// Failed: 2 (20.00%)
// Skipped: 0 (0.00%)
// Total Execution Time: 5420ms
```

#### Detailed Report
```java
String detailed = qaAgent.generateDetailedReport();
System.out.println(detailed);

// Shows individual suite metrics and overall summary
```

#### Test Results
```java
List<TestResult> allResults = qaAgent.getAllTestResults();
List<TestResult> passedResults = qaAgent.getTestResultsByStatus(
    TestCase.TestStatus.PASSED
);
List<TestResult> failedResults = qaAgent.getTestResultsByStatus(
    TestCase.TestStatus.FAILED
);
```

#### Execution Summary
```java
QAAgent.ExecutionSummary summary = qaAgent.getExecutionSummary();

System.out.println("Total: " + summary.totalTests);
System.out.println("Passed: " + summary.passedTests);
System.out.println("Failed: " + summary.failedTests);
System.out.println("Pass Rate: " + summary.passedPercentage + "%");
System.out.println("Time: " + summary.totalExecutionTime + "ms");
```

#### Failed Tests
```java
qaAgent.printFailedTests();

// Prints all failed test details
```

### Suite Metrics

```java
TestSuite suite = qaAgent.getTestSuite("Suite Name");

// Get counts
int total = suite.getTotalTestCases();
int passed = suite.getPassedTestCount();
int failed = suite.getFailedTestCount();
int skipped = suite.getSkippedTestCount();

// Get percentages
double passRate = suite.getPassRate();      // 0-100%
double failRate = suite.getFailureRate();   // 0-100%

// Get time metrics
long avgTime = suite.getAverageExecutionTime();
long totalTime = suite.getTotalExecutionTime();

// Check status
boolean allPassed = suite.isAllTestsPassed();
```

## Test Case Priority

```java
enum TestPriority {
    CRITICAL,  // Must pass, blocks release
    HIGH,      // Important, should pass
    MEDIUM,    // Nice to have
    LOW        // Can defer to next version
}
```

## Test Case Status

```java
enum TestStatus {
    PENDING,   // Not yet executed
    RUNNING,   // Currently executing
    PASSED,    // Passed successfully
    FAILED,    // Failed
    SKIPPED,   // Skipped
    BLOCKED    // Cannot run due to dependency
}
```

## Real-World Example

```java
@Test
void testECommerceFlow() throws Exception {
    // Setup
    QAAgent qaAgent = new QAAgent("E-Commerce-QA", apiClient);
    TestSuite checkoutSuite = qaAgent.createTestSuite("Checkout Flow");

    // Create test cases
    TestCase getCart = new TestCase("TC-001", "Get Shopping Cart", 
        "/api/cart", "GET");
    getCart.setCategory("Shopping");
    getCart.setPriority(TestCase.TestPriority.CRITICAL);
    getCart.setExpectedResult(Map.of("statusCode", 200));

    TestCase updateCart = new TestCase("TC-002", "Update Cart", 
        "/api/cart", "POST");
    updateCart.setCategory("Shopping");
    updateCart.setPriority(TestCase.TestPriority.CRITICAL);
    updateCart.setTestData(Map.of("itemId", 123, "quantity", 2));
    updateCart.setExpectedResult(Map.of("statusCode", 200));

    TestCase checkout = new TestCase("TC-003", "Checkout", 
        "/api/orders", "POST");
    checkout.setCategory("Payment");
    checkout.setPriority(TestCase.TestPriority.CRITICAL);
    checkout.setTestData(Map.of("cartId", 1, "paymentMethod", "credit_card"));
    checkout.setExpectedResult(Map.of("statusCode", 201));

    checkoutSuite.addTestCases(Arrays.asList(getCart, updateCart, checkout));

    // Execute
    qaAgent.executeTestSuite("Checkout Flow");

    // Report
    System.out.println(qaAgent.generateSummaryReport());

    // Verify
    assertThat(checkoutSuite.isAllTestsPassed()).isTrue();
    assertThat(checkoutSuite.getPassRate()).isEqualTo(100.0);
}
```

## Configuration

### Set Max Retries
```java
qaAgent.setMaxRetries(5);  // Default: 3
```

### Set Retry Delay
```java
qaAgent.setRetryDelayMs(2000);  // 2 seconds between retries
```

### Check Running Status
```java
if (qaAgent.isRunning()) {
    System.out.println("Tests are running...");
}
```

### Shutdown Agent
```java
qaAgent.shutdown();  // Clean up resources
```

## Expected Result Matchers

The following matchers are supported in `setExpectedResult()`:

```java
// Status code match
Map.of("statusCode", 200)

// Body content match
Map.of("bodyContains", "expected text")

// Combined
Map.of(
    "statusCode", 200,
    "bodyContains", "success"
)
```

## File Structure

```
src/test/java/com/automation/qa/
├── agent/
│   ├── QAAgent.java              # Main QA Agent
│   └── QAAgentTest.java          # QA Agent tests
├── model/
│   ├── TestCase.java             # Test case model
│   ├── TestSuite.java            # Test suite model
│   └── TestResult.java           # Test result model
└── examples/
    └── QAAgentPracticalExamplesTest.java  # Real-world examples
```

## Best Practices

1. **Organize Tests** - Use categories and priorities effectively
2. **Set Expectations** - Always define expected results
3. **Use Descriptions** - Add meaningful descriptions to test cases
4. **Handle Failures** - Review failed tests and fix issues
5. **Monitor Metrics** - Track pass rates and execution times
6. **Parallel Execution** - Use parallel execution for faster feedback
7. **Retry Strategy** - Configure retries for flaky tests
8. **Report Generation** - Generate reports for stakeholder communication

## Troubleshooting

### Test Not Executing
- Check if test is marked as skipped
- Verify mock endpoints are properly configured
- Ensure test data is valid

### Failed Tests
- Use `qaAgent.printFailedTests()` to see error details
- Check expected result configuration
- Verify API response matches expectations

### Performance Issues
- Check test execution times
- Consider parallel execution
- Review retry configuration

## Advanced Examples

See [QAAgentPracticalExamplesTest.java](src/test/java/com/automation/qa/examples/QAAgentPracticalExamplesTest.java) for:
- User Management API Tests
- Product Catalog Tests
- Order Management Tests
- Multi-suite Execution
- Category-based Filtering
- Priority-based Filtering
- Failed Test Analysis
- Metrics and Statistics

## Integration with CI/CD

```bash
# Run all QA Agent tests
mvn test -Dtest=QAAgentTest

# Run practical examples
mvn test -Dtest=QAAgentPracticalExamplesTest

# Generate coverage report
mvn test jacoco:report
```

## Support

For issues or questions:
1. Check the examples in `QAAgentPracticalExamplesTest.java`
2. Review this documentation
3. Check test implementation in `QAAgentTest.java`
