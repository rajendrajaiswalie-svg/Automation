# Getting Started with JUnit5 & WireMock API Testing

## Quick Start (5 minutes)

### 1. Build the Project
```bash
cd Automation
mvn clean install
```

### 2. Run All Tests
```bash
mvn test
```

### 3. Run Specific Test
```bash
mvn test -Dtest=ApiClientTest
```

## Project Structure

```
Automation/
├── pom.xml                          # Maven dependencies
├── README.md                        # Full documentation
├── WIREMOCK_CHEATSHEET.md          # Quick reference guide
├── GETTING_STARTED.md              # This file
├── src/
│   ├── main/java/
│   │   └── com/automation/
│   │       └── api/
│   │           └── ApiClient.java  # HTTP client implementation
│   └── test/java/
│       └── com/automation/
│           ├── api/
│           │   ├── ApiClientTest.java              # Basic tests
│           │   ├── ApiClientAdvancedTest.java      # Advanced tests
│           │   └── ApiClientUtilsExampleTest.java  # Using utilities
│           ├── config/
│           │   └── WireMockTestConfig.java         # Shared configuration
│           └── utils/
│               └── WireMockStubUtils.java          # Stub utilities
└── src/test/resources/
    ├── application-test.properties  # Test configuration
    └── __files/
        └── user_response.json       # Mock response data
```

## Understanding WireMock

WireMock is a library to mock HTTP endpoints for testing. Instead of calling real APIs:

```java
// ❌ Bad: Depends on real API
apiClient.get("https://api.real-server.com/users");

// ✅ Good: Use WireMock to mock
wireMock.stubFor(
    get(urlEqualTo("/users"))
        .willReturn(aResponse().withStatus(200))
);
```

## Basic Test Structure

Every test follows this pattern:

```java
@Test
void testSomething() {
    // 1. ARRANGE - Setup the mock
    wireMock.stubFor(
        get(urlEqualTo("/endpoint"))
            .willReturn(aResponse().withStatus(200))
    );

    // 2. ACT - Perform the action
    ApiClient.ApiResponse response = apiClient.get("/endpoint");

    // 3. ASSERT - Verify the result
    assertThat(response.statusCode).isEqualTo(200);
}
```

## Common Test Scenarios

### Test Successful Response (200)
```java
@Test
void testSuccess() throws IOException {
    WireMockStubUtils.setupGetStub(wireMock, "/users", 
        Map.of("id", 1, "name", "John"));
    
    ApiClient.ApiResponse response = apiClient.get("/users");
    
    assertThat(response.statusCode).isEqualTo(200);
}
```

### Test Not Found Error (404)
```java
@Test
void testNotFound() throws IOException {
    WireMockStubUtils.setupNotFoundStub(wireMock, "/users/999");
    
    ApiClient.ApiResponse response = apiClient.get("/users/999");
    
    assertThat(response.statusCode).isEqualTo(404);
}
```

### Test Server Error (500)
```java
@Test
void testServerError() throws IOException {
    WireMockStubUtils.setupServerErrorStub(wireMock, "/error");
    
    ApiClient.ApiResponse response = apiClient.get("/error");
    
    assertThat(response.statusCode).isEqualTo(500);
}
```

### Test POST with Data
```java
@Test
void testCreateUser() throws IOException {
    Map<String, Object> newUser = Map.of(
        "id", 2,
        "name", "Jane"
    );
    
    WireMockStubUtils.setupPostStub(wireMock, "/users", newUser);
    
    ApiClient.ApiResponse response = apiClient.post("/users", 
        new UserRequest("Jane", "jane@example.com"));
    
    assertThat(response.statusCode).isEqualTo(201);
}
```

### Test Query Parameters
```java
@Test
void testQueryParams() throws IOException {
    WireMockStubUtils.setupGetStubWithQueryParam(
        wireMock, "/users", "role", "admin", 
        List.of(Map.of("id", 1))
    );
    
    ApiClient.ApiResponse response = apiClient.get("/users?role=admin");
    
    assertThat(response.statusCode).isEqualTo(200);
}
```

### Test Delayed Responses
```java
@Test
void testDelayedResponse() throws IOException {
    WireMockStubUtils.setupGetStubWithDelay(
        wireMock, "/slow", 
        Map.of("status", "ok"), 
        500  // 500ms delay
    );
    
    long start = System.currentTimeMillis();
    ApiClient.ApiResponse response = apiClient.get("/slow");
    long duration = System.currentTimeMillis() - start;
    
    assertThat(duration).isGreaterThanOrEqualTo(500);
}
```

### Verify Requests
```java
@Test
void testVerifyRequest() throws IOException {
    WireMockStubUtils.setupGetStub(wireMock, "/ping", 
        Map.of("status", "ok"));
    
    apiClient.get("/ping");
    apiClient.get("/ping");
    
    // Verify endpoint was called twice
    WireMockStubUtils.verifyGetRequestCount(wireMock, "/ping", 2);
}
```

## Creating Your Own Tests

### Step 1: Add Test Method
```java
@Test
@DisplayName("Should do something")
void testMyFeature() throws IOException {
    // Your test code
}
```

### Step 2: Setup Mock Stub
```java
// Option A: Use utility method
WireMockStubUtils.setupGetStub(wireMock, "/my-endpoint", responseData);

// Option B: Setup directly
wireMock.stubFor(
    get(urlEqualTo("/my-endpoint"))
        .willReturn(aResponse().withStatus(200).withBody("..."))
);
```

### Step 3: Perform Action
```java
ApiClient.ApiResponse response = apiClient.get("/my-endpoint");
```

### Step 4: Verify Result
```java
assertThat(response.statusCode).isEqualTo(200);
assertThat(response.body).contains("expected-text");
```

## Test Utilities Reference

### Setup Methods
```java
// GET requests
WireMockStubUtils.setupGetStub(wireMock, "/endpoint", responseBody);
WireMockStubUtils.setupGetStubWithStatus(wireMock, "/endpoint", 201, body);
WireMockStubUtils.setupGetStubWithDelay(wireMock, "/endpoint", body, 500);
WireMockStubUtils.setupGetStubWithRandomDelay(wireMock, "/endpoint", body, 100, 500);
WireMockStubUtils.setupGetStubWithQueryParam(wireMock, "/endpoint", "key", "value", body);

// POST requests
WireMockStubUtils.setupPostStub(wireMock, "/endpoint", responseBody);
WireMockStubUtils.setupPostStubWithStatus(wireMock, "/endpoint", 201, body);

// Error responses
WireMockStubUtils.setupNotFoundStub(wireMock, "/endpoint");
WireMockStubUtils.setupServerErrorStub(wireMock, "/endpoint");

// Other methods
WireMockStubUtils.setupDeleteStub(wireMock, "/endpoint");
WireMockStubUtils.setupPutStub(wireMock, "/endpoint", responseBody);
```

### Verify Methods
```java
WireMockStubUtils.verifyGetRequest(wireMock, "/endpoint");
WireMockStubUtils.verifyGetRequestCount(wireMock, "/endpoint", 3);
WireMockStubUtils.verifyPostRequest(wireMock, "/endpoint");
WireMockStubUtils.verifyPostRequestWithBody(wireMock, "/endpoint", "text");
```

## Assertions with AssertJ

```java
// Status code assertions
assertThat(response.statusCode).isEqualTo(200);
assertThat(response.statusCode).isBetween(200, 299);

// Body assertions
assertThat(response.body).contains("text");
assertThat(response.body).doesNotContain("text");
assertThat(response.body).startsWith("{");
assertThat(response.body).endsWith("}");

// JSON assertions
assertThat(response.body)
    .isNotEmpty()
    .isNotBlank();
```

## Debugging Tests

### See All Registered Stubs
```java
wireMock.getAllStubMappings().getMappings().forEach(stub -> 
    System.out.println(stub.getRequest().getUrl())
);
```

### Check Unmatched Requests
```java
List<LoggedRequest> unmatched = wireMock.findUnmatchedRequests().getRequests();
System.out.println("Unmatched: " + unmatched.size());
```

### Reset All Stubs
```java
wireMock.resetAll();
```

## Running Tests

### All tests
```bash
mvn test
```

### Specific class
```bash
mvn test -Dtest=ApiClientTest
```

### Specific method
```bash
mvn test -Dtest=ApiClientTest#testGetRequestSuccess
```

### With output
```bash
mvn test -X  # Debug output
mvn test -e  # Show errors
```

## Common Issues

### Problem: Stub not matching
**Solution:** 
- Check URL exactly matches (watch trailing slashes)
- Verify HTTP method (GET vs POST)
- Check header matchers if used

```java
// Wrong - won't match
get(urlEqualTo("/users/"))  // URL has trailing slash

// Right
get(urlEqualTo("/users"))   // No trailing slash
```

### Problem: Test times out
**Solution:** Check if mock has infinite delay
```java
// Add timeout to test
@Test(timeout = 5000)
void testWithTimeout() { ... }
```

### Problem: Multiple stubs for same URL
**Solution:** Use priority (higher = more important)
```java
wireMock.stubFor(get(urlPathMatching(".*"))
    .atPriority(10)  // Low priority
    .willReturn(...));

wireMock.stubFor(get(urlEqualTo("/specific"))
    .atPriority(1)   // High priority
    .willReturn(...));
```

## Next Steps

1. **Basic Tests:** Study `ApiClientTest.java`
2. **Advanced Tests:** Review `ApiClientAdvancedTest.java`
3. **Use Utilities:** See `ApiClientUtilsExampleTest.java`
4. **Read References:** Check `WIREMOCK_CHEATSHEET.md` and `README.md`
5. **Write Your Tests:** Create your own test classes

## Useful Links

- [WireMock Documentation](http://wiremock.org/docs/)
- [JUnit 5 Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Guide](https://assertj.github.io/)

## Key Takeaways

✅ WireMock mocks HTTP endpoints for isolated testing  
✅ Use `@RegisterExtension` to inject WireMock into tests  
✅ Three steps: Arrange → Act → Assert  
✅ Use utilities to reduce code duplication  
✅ Verify requests were made correctly  
✅ Test success, error, and edge cases  

Happy testing! 🎉
