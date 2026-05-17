# WireMock JUnit5 API Test Automation

A comprehensive example project demonstrating REST API test automation using **JUnit5** and **WireMock**.

## Overview

This project provides:
- ✅ Complete JUnit5 test suite with WireMock integration
- ✅ Example API client for HTTP communication
- ✅ Basic and advanced testing scenarios
- ✅ Parameterized tests with JUnit5
- ✅ Best practices for API mocking and testing

## Project Structure

```
wiremock-junit5-tests/
├── pom.xml                                    # Maven configuration with dependencies
├── src/
│   ├── main/java/com/automation/api/
│   │   └── ApiClient.java                    # Simple HTTP API client
│   └── test/java/com/automation/api/
│       ├── ApiClientTest.java                # Basic API test cases
│       └── ApiClientAdvancedTest.java        # Advanced test scenarios
└── README.md                                  # This file
```

## Dependencies

- **JUnit 5.9.2** - Testing framework
- **WireMock 3.0.1** - API mocking library
- **Apache HttpComponents 4.5.13** - HTTP client
- **Gson 2.10.1** - JSON serialization
- **AssertJ 3.24.1** - Fluent assertions

## Installation & Setup

### Prerequisites
- Java 11 or higher
- Maven 3.6+

### Clone and Build

```bash
cd Automation
mvn clean install
```

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=ApiClientTest
mvn test -Dtest=ApiClientAdvancedTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=ApiClientTest#testGetRequestSuccess
```

## Test Examples

### 1. Basic GET Request Test

```java
@Test
@DisplayName("Should return 200 status for successful GET request")
void testGetRequestSuccess() throws IOException {
    // Arrange - Setup mock response
    String endpoint = "/users/1";
    wireMock.stubFor(
        get(urlEqualTo(endpoint))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(mockResponse)
            )
    );

    // Act - Make the request
    ApiClient.ApiResponse response = apiClient.get(endpoint);

    // Assert - Verify the response
    assertThat(response.statusCode).isEqualTo(200);
}
```

### 2. POST Request Test

```java
@Test
@DisplayName("Should successfully create a resource with POST")
void testPostRequestSuccess() throws IOException {
    String endpoint = "/users";
    UserRequest newUser = new UserRequest("Jane Doe", "jane@example.com");
    
    wireMock.stubFor(
        post(urlEqualTo(endpoint))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(aResponse()
                .withStatus(201)
                .withBody(mockResponse)
            )
    );

    ApiClient.ApiResponse response = apiClient.post(endpoint, newUser);
    assertThat(response.statusCode).isEqualTo(201);
}
```

### 3. Parameterized Tests

```java
@ParameterizedTest
@CsvSource({
    "/users/1, 200",
    "/users/999, 404",
    "/users/inactive, 403"
})
@DisplayName("Should return expected status codes")
void testVariousEndpoints(String endpoint, int expectedStatus) throws IOException {
    // Test logic here
}
```

### 4. Stateful Mocking (Scenarios)

```java
@Test
@DisplayName("Should handle stateful mock scenarios")
void testStatefulMockingScenario() throws IOException {
    // First call returns 202
    wireMock.stubFor(
        get(urlEqualTo(endpoint))
            .inScenario("task-processing")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse().withStatus(202))
            .willSetStateTo("completed")
    );

    // Second call returns 200
    wireMock.stubFor(
        get(urlEqualTo(endpoint))
            .inScenario("task-processing")
            .whenScenarioStateIs("completed")
            .willReturn(aResponse().withStatus(200))
    );
}
```

### 5. Request Verification

```java
@Test
void testVerifyRequestCount() throws IOException {
    wireMock.stubFor(get(urlEqualTo("/ping"))
        .willReturn(aResponse().withStatus(200)));

    apiClient.get("/ping");
    apiClient.get("/ping");
    apiClient.get("/ping");

    // Verify the endpoint was called 3 times
    wireMock.verify(3, getRequestedFor(urlEqualTo("/ping")));
}
```

## Key Features of the Test Suite

### ApiClientTest.java (Basic Tests)
- ✅ Successful GET/POST requests
- ✅ Error handling (404, 500)
- ✅ Query parameter matching
- ✅ Request verification
- ✅ Response delays
- ✅ JSON body pattern matching

### ApiClientAdvancedTest.java (Advanced Tests)
- ✅ Parameterized tests with multiple values
- ✅ CSV-based parameterized tests
- ✅ Stateful scenario testing
- ✅ Response header verification
- ✅ Sequential request handling
- ✅ Mock reset and reconfiguration
- ✅ Timeout scenarios
- ✅ Concurrent request testing

## WireMock Common Methods

| Method | Purpose |
|--------|---------|
| `get(urlEqualTo(...))` | Match exact URL for GET requests |
| `post(urlEqualTo(...))` | Match exact URL for POST requests |
| `urlPathMatching(...)` | Match URL with regex pattern |
| `withQueryParam(name, value)` | Match query parameters |
| `withRequestBody(...)` | Match request body content |
| `willReturn(aResponse()...)` | Define mock response |
| `withStatus(code)` | Set HTTP status code |
| `withBody(content)` | Set response body |
| `withHeader(name, value)` | Add response header |
| `withFixedDelay(ms)` | Add delay to response |
| `verify(count, ...)` | Verify request was made N times |
| `stubFor(...)` | Register a mock stub |
| `resetAll()` | Clear all stubs |

## Best Practices

1. **Use DisplayName** for clear test descriptions
2. **Arrange-Act-Assert** pattern for test structure
3. **Test both success and failure scenarios**
4. **Verify request details** (headers, body, parameters)
5. **Use AssertJ** for fluent and readable assertions
6. **Keep mocks focused** on one endpoint per test
7. **Use parameterized tests** to reduce code duplication
8. **Reset mocks** between tests if needed

## Extending the Tests

### Adding Custom Assertions
```java
assertThat(response.statusCode)
    .as("API response status")
    .isEqualTo(200);
```

### Testing with Complex JSON
```java
UserResponse user = response.asJson(UserResponse.class);
assertThat(user.name).isEqualTo("John Doe");
assertThat(user.email).isEqualTo("john@example.com");
```

### Reusing Mock Definitions
```java
private void setupUserMock(int userId, int status) {
    wireMock.stubFor(
        get(urlEqualTo("/users/" + userId))
            .willReturn(aResponse().withStatus(status))
    );
}
```

## Running Tests in CI/CD

```bash
# Run with coverage
mvn test jacoco:report

# Run with specific profile
mvn test -P integration-tests

# Fail build if tests fail
mvn clean test --fail-at-end
```

## Troubleshooting

### Port Already in Use
WireMock uses dynamic ports, but if conflicts occur:
```java
.options(wireMockConfig()
    .port(8080)  // Explicit port instead of dynamic
)
```

### Stub Not Matching
- Check URL exactly matches (trailing slashes, query params)
- Verify request headers if `withHeader()` is used
- Use `wireMock.findStubMapping()` to debug

### Timeout Issues
- Increase test timeout if adding delays:
```java
@Test(timeout = 5000)  // 5 second timeout
```

## Additional Resources

- [WireMock Official Documentation](http://wiremock.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/assertj-core-features-highlight.html)
- [RESTful API Testing Best Practices](https://restfulapi.net/)

## License

This example project is free to use and modify for educational and commercial purposes.
