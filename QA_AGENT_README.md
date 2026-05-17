# QA Agent - Test Automation Framework

## 🎯 Overview

The **QA Agent** is a powerful test automation framework built on JUnit5 and WireMock that provides:

- ✅ **Test Management** - Create, organize, and manage test cases
- ✅ **Test Execution** - Execute tests individually, in suites, or in parallel
- ✅ **Smart Retry** - Automatic retry mechanism for flaky tests
- ✅ **Reporting** - Comprehensive test reports and analytics
- ✅ **Filtering** - Filter tests by priority, category, or status
- ✅ **Metrics** - Detailed execution metrics and statistics

## 📁 Project Structure

```
src/test/java/com/automation/qa/
├── agent/
│   ├── QAAgent.java                      # Main QA Agent class (Core Framework)
│   └── QAAgentTest.java                  # Unit tests for QA Agent
├── model/
│   ├── TestCase.java                     # Test case data model
│   ├── TestSuite.java                    # Test suite container
│   └── TestResult.java                   # Test execution result
└── examples/
    ├── QAAgentPracticalExamplesTest.java # Real-world usage examples
    └── QAAgentCodeExamplesTest.java      # 14+ code examples
```

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| [QA_AGENT_GUIDE.md](QA_AGENT_GUIDE.md) | Comprehensive guide with detailed explanations |
| [QA_AGENT_QUICK_REFERENCE.md](QA_AGENT_QUICK_REFERENCE.md) | Quick lookup reference |
| This file | Overview and getting started |

## 🚀 Quick Start

### 1. Initialize QA Agent

```java
QAAgent qaAgent = new QAAgent("My-QA-Agent", apiClient);
```

### 2. Create Test Suite

```java
TestSuite suite = qaAgent.createTestSuite("My Test Suite");
```

### 3. Add Test Case

```java
TestCase tc = new TestCase("TC-001", "Test Name", "/api/endpoint", "GET");
tc.setPriority(TestCase.TestPriority.CRITICAL);
tc.setExpectedResult(Map.of("statusCode", 200));
suite.addTestCase(tc);
```

### 4. Execute Tests

```java
qaAgent.executeTestSuite("My Test Suite");
```

### 5. View Results

```java
System.out.println(qaAgent.generateSummaryReport());
```

## 📖 Documentation Guide

### For Beginners
1. Start here: [QA_AGENT_QUICK_REFERENCE.md](QA_AGENT_QUICK_REFERENCE.md)
2. Run: `QAAgentCodeExamplesTest.java` (14 examples)
3. Copy-paste patterns that match your needs

### For Intermediate Users
1. Read: [QA_AGENT_GUIDE.md](QA_AGENT_GUIDE.md)
2. Study: `QAAgentPracticalExamplesTest.java` (8 real-world scenarios)
3. Explore: `QAAgentTest.java` (unit tests)

### For Advanced Users
1. Review: `QAAgent.java` (1000+ lines, fully documented)
2. Extend: Create custom reporters or metrics
3. Integrate: Combine with CI/CD pipelines

## 🔍 Find Examples

### By Use Case

| Use Case | File | Method |
|----------|------|--------|
| Create test suite | `QAAgentCodeExamplesTest.java` | `example4TestSuiteManagement()` |
| Execute single test | `QAAgentCodeExamplesTest.java` | `example1BasicTest()` |
| POST with data | `QAAgentCodeExamplesTest.java` | `example2PostWithData()` |
| Set priority/category | `QAAgentCodeExamplesTest.java` | `example3PriorityAndCategory()` |
| Filter tests | `QAAgentCodeExamplesTest.java` | `example5FilterTests()` |
| Execute by category | `QAAgentCodeExamplesTest.java` | `example6ExecuteByCategory()` |
| Retry mechanism | `QAAgentCodeExamplesTest.java` | `example7RetryMechanism()` |
| Analyze results | `QAAgentCodeExamplesTest.java` | `example8ResultAnalysis()` |
| Generate reports | `QAAgentCodeExamplesTest.java` | `example9Reports()` |
| Multi-suite parallel | `QAAgentCodeExamplesTest.java` | `example10ComplexScenario()` |
| E-commerce flow | `QAAgentPracticalExamplesTest.java` | `example1UserManagementTests()` |
| Test by priority | `QAAgentPracticalExamplesTest.java` | `example5FilterAndExecuteByPriority()` |

## 📊 Core Classes

### QAAgent
Main orchestrator for test management and execution.
- **File**: `src/test/java/com/automation/qa/agent/QAAgent.java`
- **Key Methods**:
  - `executeTestCase()` - Execute single test
  - `executeTestSuite()` - Execute suite
  - `generateSummaryReport()` - Get summary
  - `getExecutionSummary()` - Get metrics

### TestCase
Represents a single test case.
- **File**: `src/test/java/com/automation/qa/model/TestCase.java`
- **Key Properties**:
  - `priority` - CRITICAL, HIGH, MEDIUM, LOW
  - `category` - Test category
  - `status` - Current status
  - `expectedResult` - Expected outcome

### TestSuite
Container for related test cases.
- **File**: `src/test/java/com/automation/qa/model/TestSuite.java`
- **Key Methods**:
  - `addTestCase()` - Add test
  - `getPassRate()` - Get pass percentage
  - `getTotalTestCases()` - Get count

### TestResult
Execution result of a test.
- **File**: `src/test/java/com/automation/qa/model/TestResult.java`
- **Key Properties**:
  - `status` - PASSED, FAILED, SKIPPED
  - `statusCode` - HTTP response code
  - `executionTime` - Time taken
  - `errorMessage` - Error details

## 🎓 Learning Path

```
Level 1: Basics (30 min)
├─ Read: QA_AGENT_QUICK_REFERENCE.md
├─ Run: example1BasicTest()
├─ Run: example2PostWithData()
└─ Copy code pattern to your tests

Level 2: Intermediate (2 hours)
├─ Read: QA_AGENT_GUIDE.md sections 1-3
├─ Run: All 14 examples in QAAgentCodeExamplesTest
├─ Run: QAAgentPracticalExamplesTest
└─ Create your first test suite

Level 3: Advanced (1 day)
├─ Read: Full QA_AGENT_GUIDE.md
├─ Study: QAAgent.java implementation
├─ Study: QAAgentTest.java unit tests
├─ Create custom reporter
└─ Integrate with CI/CD
```

## 🔑 Key Features

### 1. Test Organization
```java
TestSuite suite = qaAgent.createTestSuite("User Tests");
suite.addTestCase(testCase);
```

### 2. Test Execution
```java
// Single
qaAgent.executeTestCase(testCase);

// Suite
qaAgent.executeTestSuite("Suite Name");

// Parallel
qaAgent.executeTestSuitesInParallel(suiteNames);

// Filtered
qaAgent.executeTestsByPriority(TestCase.TestPriority.CRITICAL);
```

### 3. Test Filtering
```java
suite.getTestCasesByCategory("User");
suite.getTestCasesByPriority(TestCase.TestPriority.CRITICAL);
suite.getTestCasesByStatus(TestCase.TestStatus.FAILED);
```

### 4. Reporting
```java
qaAgent.generateSummaryReport();    // High-level summary
qaAgent.generateDetailedReport();   // Detailed breakdown
qaAgent.printFailedTests();         // Failed test analysis
```

### 5. Metrics
```java
QAAgent.ExecutionSummary summary = qaAgent.getExecutionSummary();
suite.getPassRate();
suite.getAverageExecutionTime();
```

## 🧪 Running Tests

### Run All QA Agent Tests
```bash
mvn test -Dtest=QAAgent*
```

### Run Practical Examples
```bash
mvn test -Dtest=QAAgentPracticalExamplesTest
```

### Run Code Examples
```bash
mvn test -Dtest=QAAgentCodeExamplesTest
```

### Run Specific Example
```bash
mvn test -Dtest=QAAgentCodeExamplesTest#example1BasicTest
```

## 💡 Common Patterns

### Pattern 1: Simple GET Test
```java
TestCase tc = new TestCase("TC-001", "Get User", "/api/users", "GET");
tc.setExpectedResult(Map.of("statusCode", 200));
qaAgent.executeTestCase(tc);
```

### Pattern 2: POST with Data
```java
TestCase tc = new TestCase("TC-002", "Create User", "/api/users", "POST");
tc.setTestData(Map.of("name", "John"));
tc.setExpectedResult(Map.of("statusCode", 201));
qaAgent.executeTestCase(tc);
```

### Pattern 3: Priority Test
```java
TestCase tc = new TestCase("TC-003", "Critical Op", "/api/op", "POST");
tc.setPriority(TestCase.TestPriority.CRITICAL);
tc.setCategory("Operations");
```

### Pattern 4: Execute & Report
```java
qaAgent.executeTestSuite("My Suite");
System.out.println(qaAgent.generateSummaryReport());
qaAgent.printFailedTests();
```

## 🔗 Related Documentation

- [README.md](README.md) - Project overview
- [WIREMOCK_CHEATSHEET.md](WIREMOCK_CHEATSHEET.md) - WireMock reference
- [GETTING_STARTED.md](GETTING_STARTED.md) - JUnit5 guide

## 📋 Test Priorities

| Priority | Meaning |
|----------|---------|
| CRITICAL | Must pass, blocks release |
| HIGH | Important, should pass |
| MEDIUM | Nice to have |
| LOW | Can defer |

## 📊 Test Statuses

| Status | Meaning |
|--------|---------|
| PENDING | Waiting to execute |
| RUNNING | Currently executing |
| PASSED | Test passed ✓ |
| FAILED | Test failed ✗ |
| SKIPPED | Test skipped |
| BLOCKED | Cannot run |

## ⚙️ Configuration

```java
// Set retry attempts
qaAgent.setMaxRetries(5);           // Default: 3

// Set retry delay
qaAgent.setRetryDelayMs(2000);      // Default: 1000ms

// Check if running
boolean running = qaAgent.isRunning();

// Cleanup
qaAgent.shutdown();
```

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| Test not running | Check if suite is paused: `suite.setPaused(false)` |
| Wrong status code | Verify mock stub is returning correct code |
| Tests too slow | Check execution times, use parallel execution |
| Flaky tests | Increase retries: `setMaxRetries(5)` |
| No results | Ensure tests were executed before generating report |

## 🎯 Best Practices

1. **Organize Tests** - Use categories and priorities
2. **Set Clear Expectations** - Define what success looks like
3. **Use Descriptions** - Help others understand tests
4. **Monitor Metrics** - Track pass rates over time
5. **Parallel Execution** - Run independent suites together
6. **Regular Review** - Analyze failed tests
7. **Update Tests** - Keep tests synchronized with API changes

## 📞 Support Resources

- **Quick Lookup**: [QA_AGENT_QUICK_REFERENCE.md](QA_AGENT_QUICK_REFERENCE.md)
- **Detailed Guide**: [QA_AGENT_GUIDE.md](QA_AGENT_GUIDE.md)
- **Code Examples**: `QAAgentCodeExamplesTest.java` (14 examples)
- **Real-World Examples**: `QAAgentPracticalExamplesTest.java` (8 scenarios)
- **Source Code**: `QAAgent.java` (Fully documented)

## 🎉 Next Steps

1. **Read** the Quick Reference
2. **Run** one of the 14 code examples
3. **Create** your first test suite
4. **Execute** and view results
5. **Generate** reports
6. **Share** results with team

Happy testing! 🚀
