# QA Agent Quick Reference

## Setup

```java
// Initialize
QAAgent qaAgent = new QAAgent("Agent-Name", apiClient);

// Create test suite
TestSuite suite = qaAgent.createTestSuite("Suite Name");

// Cleanup
qaAgent.shutdown();
```

## Create Test Case

```java
TestCase tc = new TestCase("TC-001", "Test Name", "/endpoint", "GET");

// Configure
tc.setDescription("Detailed description");
tc.setCategory("Category");
tc.setPriority(TestCase.TestPriority.CRITICAL);  // CRITICAL, HIGH, MEDIUM, LOW
tc.setSkipped(false);

// Input data
tc.setTestData(Map.of("key", "value"));

// Expected output
tc.setExpectedResult(Map.of(
    "statusCode", 200,
    "bodyContains", "expected text"
));

// Add to suite
suite.addTestCase(tc);
```

## Execute Tests

| Method | Purpose |
|--------|---------|
| `executeTestCase(tc)` | Execute single test |
| `executeTestCaseWithRetry(tc)` | Execute with automatic retry |
| `executeTestSuite(suiteName)` | Execute all tests in suite |
| `executeTestSuitesInParallel(names)` | Run multiple suites in parallel |
| `executeTestsByCategory(category)` | Run tests by category |
| `executeTestsByPriority(priority)` | Run tests by priority |

### Examples

```java
// Single test
TestResult result = qaAgent.executeTestCase(testCase);

// With retry (max 3 attempts)
qaAgent.setMaxRetries(3);
qaAgent.setRetryDelayMs(1000);
TestResult result = qaAgent.executeTestCaseWithRetry(testCase);

// Entire suite
qaAgent.executeTestSuite("Suite Name");

// Multiple suites in parallel
qaAgent.executeTestSuitesInParallel(Arrays.asList("Suite1", "Suite2"));

// Filter by category
qaAgent.executeTestsByCategory("User Management");

// Filter by priority
qaAgent.executeTestsByPriority(TestCase.TestPriority.CRITICAL);
```

## Get Test Results

```java
// All results
List<TestResult> all = qaAgent.getAllTestResults();

// By status
List<TestResult> passed = qaAgent.getTestResultsByStatus(
    TestCase.TestStatus.PASSED
);
List<TestResult> failed = qaAgent.getTestResultsByStatus(
    TestCase.TestStatus.FAILED
);

// Summary
QAAgent.ExecutionSummary summary = qaAgent.getExecutionSummary();
System.out.println("Total: " + summary.totalTests);
System.out.println("Passed: " + summary.passedTests);
System.out.println("Failed: " + summary.failedTests);
```

## Filter Tests in Suite

```java
TestSuite suite = qaAgent.getTestSuite("Suite Name");

// By category
List<TestCase> userTests = suite.getTestCasesByCategory("User");

// By priority
List<TestCase> critical = suite.getTestCasesByPriority(
    TestCase.TestPriority.CRITICAL
);

// By status
List<TestCase> failed = suite.getTestCasesByStatus(
    TestCase.TestStatus.FAILED
);
```

## Suite Metrics

```java
TestSuite suite = qaAgent.getTestSuite("Suite Name");

// Counts
int total = suite.getTotalTestCases();
int passed = suite.getPassedTestCount();
int failed = suite.getFailedTestCount();
int skipped = suite.getSkippedTestCount();

// Percentages
double passRate = suite.getPassRate();        // 0-100%
double failRate = suite.getFailureRate();     // 0-100%

// Time
long avgTime = suite.getAverageExecutionTime();   // ms
long totalTime = suite.getTotalExecutionTime();   // ms

// Status
boolean success = suite.isAllTestsPassed();
```

## Reporting

```java
// Summary report
String summary = qaAgent.generateSummaryReport();
System.out.println(summary);

// Detailed report
String detailed = qaAgent.generateDetailedReport();
System.out.println(detailed);

// Print failed tests
qaAgent.printFailedTests();
```

## Test Result Details

```java
TestResult result = qaAgent.executeTestCase(testCase);

// Status
if (result.isPassed()) { ... }
if (result.isFailed()) { ... }
if (result.isSkipped()) { ... }

// Details
int statusCode = result.getStatusCode();
String response = result.getResponseBody();
String error = result.getErrorMessage();
long time = result.getExecutionTime();

// Summary
String summary = result.getSummary();  // One-line summary
```

## Test Case Status

```
PENDING  → Not yet executed
RUNNING  → Currently executing
PASSED   → Test passed
FAILED   → Test failed
SKIPPED  → Test skipped
BLOCKED  → Cannot run
```

## Test Priority Levels

```
CRITICAL → Must pass, blocks release
HIGH     → Important, should pass
MEDIUM   → Nice to have
LOW      → Can defer
```

## Configuration

```java
// Retries
qaAgent.setMaxRetries(5);           // Default: 3
qaAgent.setRetryDelayMs(2000);      // Default: 1000ms

// Status
boolean isRunning = qaAgent.isRunning();

// Cleanup
qaAgent.shutdown();
```

## Suite Management

```java
// Create
TestSuite suite = qaAgent.createTestSuite("Name");

// Get
TestSuite suite = qaAgent.getTestSuite("Name");

// Add tests
suite.addTestCase(testCase);
suite.addTestCases(List.of(tc1, tc2, tc3));

// Remove
qaAgent.removeTestSuite("Name");

// List all
List<TestSuite> all = qaAgent.getAllTestSuites();

// Configuration
suite.setDescription("Suite description");
suite.setPaused(true);  // Pause suite execution
```

## Common Patterns

### Pattern 1: Simple Test
```java
TestCase tc = new TestCase("TC-001", "Get Data", "/api/data", "GET");
tc.setExpectedResult(Map.of("statusCode", 200));
suite.addTestCase(tc);
qaAgent.executeTestCase(tc);
```

### Pattern 2: POST with Validation
```java
TestCase tc = new TestCase("TC-002", "Create User", "/api/users", "POST");
tc.setTestData(Map.of("name", "John", "email", "john@example.com"));
tc.setExpectedResult(Map.of(
    "statusCode", 201,
    "bodyContains", "John"
));
suite.addTestCase(tc);
```

### Pattern 3: Categorized & Prioritized
```java
TestCase tc = new TestCase("TC-003", "Critical Operation", "/api/op", "POST");
tc.setCategory("Operations");
tc.setPriority(TestCase.TestPriority.CRITICAL);
tc.setExpectedResult(Map.of("statusCode", 200));
suite.addTestCase(tc);
```

### Pattern 4: Execute & Report
```java
qaAgent.executeTestSuite("My Suite");
System.out.println(qaAgent.generateSummaryReport());
qaAgent.printFailedTests();
```

### Pattern 5: Filter & Execute
```java
// Only CRITICAL tests
qaAgent.executeTestsByPriority(TestCase.TestPriority.CRITICAL);

// Only "User" category
qaAgent.executeTestsByCategory("User");

// Get results
List<TestResult> results = qaAgent.getTestResultsByStatus(
    TestCase.TestStatus.FAILED
);
```

## HTTP Methods

```java
TestCase tc = new TestCase("TC-001", "Name", "/endpoint", "GET");   // GET request
TestCase tc = new TestCase("TC-002", "Name", "/endpoint", "POST");  // POST request
```

## Expected Result Options

```java
// Status code check
tc.setExpectedResult(Map.of("statusCode", 200));

// Body content check
tc.setExpectedResult(Map.of("bodyContains", "success"));

// Combined check
tc.setExpectedResult(Map.of(
    "statusCode", 200,
    "bodyContains", "success"
));
```

## Test Execution Flow

```
1. Create TestCase with endpoint & method
2. Configure test (priority, category, expected result)
3. Add test data if needed
4. Add to TestSuite
5. Execute: qaAgent.executeTestSuite("Suite")
6. Check results: suite.getPassedTestCount()
7. Generate report: qaAgent.generateSummaryReport()
8. Print failures: qaAgent.printFailedTests()
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Tests not running | Check if suite is paused: `suite.isPaused()` |
| Wrong result | Verify expected result configuration |
| Slow tests | Check execution time: `result.getExecutionTime()` |
| Flaky tests | Increase retries: `setMaxRetries(5)` |
| Port issues | Use dynamic port with WireMock |

## Integration with JUnit5

```java
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig().dynamicPort())
    .build();

@BeforeEach
void setUp() {
    String baseUrl = "http://localhost:" + wireMock.getPort();
    apiClient = new ApiClient(baseUrl);
    qaAgent = new QAAgent("Test-Agent", apiClient);
}

@AfterEach
void tearDown() {
    qaAgent.shutdown();
}

@Test
void testFlow() {
    // Your test using QAAgent
}
```

## Performance Tips

1. Use parallel execution for multiple suites
2. Set reasonable timeouts for slow operations
3. Group related tests in same suite
4. Use priorities to run critical tests first
5. Enable retry only for flaky tests
6. Monitor average execution time
