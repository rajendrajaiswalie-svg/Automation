# WireMock + JUnit5 Quick Reference

## Setup Test Class

```java
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@DisplayName("API Tests")
class MyApiTest {
    
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + wireMock.getPort();
    }
}
```

## Stubbing Requests

### Simple GET Stub
```java
wireMock.stubFor(
    get(urlEqualTo("/api/users"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{}"))
);
```

### GET with Query Parameters
```java
wireMock.stubFor(
    get(urlPathEqualTo("/api/users"))
        .withQueryParam("id", equalTo("123"))
        .willReturn(aResponse().withStatus(200))
);
```

### POST with Request Body
```java
wireMock.stubFor(
    post(urlEqualTo("/api/users"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalToJson("{\"name\":\"John\"}"))
        .willReturn(aResponse()
            .withStatus(201)
            .withBody("{\"id\":1}"))
);
```

### Match Request Body with JSON Path
```java
wireMock.stubFor(
    post(urlEqualTo("/api/users"))
        .withRequestBody(matchingJsonPath("$.name", containing("John")))
        .willReturn(aResponse().withStatus(201))
);
```

### URL Pattern Matching
```java
// Exact match
get(urlEqualTo("/exact/path"))

// Path match (ignore query params)
get(urlPathEqualTo("/api/users"))

// Regex pattern
get(urlPathMatching("/api/users/[0-9]+"))
```

## Response Configuration

```java
aResponse()
    .withStatus(200)                          // HTTP status
    .withHeader("X-Custom", "value")          // Custom header
    .withBody("response body")                // Response body
    .withBodyFile("response.json")            // Load from file
    .withFixedDelay(1000)                     // 1 second delay
    .withRandomDelay(100, 500)                // Random delay
    .withFault(Fault.CONNECTION_RESET_BY_PEER) // Simulate failure
```

## Verifying Requests

```java
// Verify request was made
wireMock.verify(getRequestedFor(urlEqualTo("/api/users")));

// Verify request count
wireMock.verify(3, getRequestedFor(urlEqualTo("/api/users")));

// Verify request with headers
wireMock.verify(postRequestedFor(urlEqualTo("/api/users"))
    .withHeader("Content-Type", equalTo("application/json"))
);

// Verify request with body
wireMock.verify(postRequestedFor(urlEqualTo("/api/users"))
    .withRequestBody(containing("John"))
);

// Verify request NOT made
wireMock.verify(0, getRequestedFor(urlEqualTo("/api/users")));
```

## Stateful Mocking (Scenarios)

```java
// First request returns 202
wireMock.stubFor(
    get(urlEqualTo("/task"))
        .inScenario("task-status")
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(aResponse().withStatus(202))
        .willSetStateTo("ready")
);

// Second request returns 200
wireMock.stubFor(
    get(urlEqualTo("/task"))
        .inScenario("task-status")
        .whenScenarioStateIs("ready")
        .willReturn(aResponse().withStatus(200))
);
```

## Advanced Features

### Priority (Higher number = higher priority)
```java
wireMock.stubFor(
    get(urlPathMatching(".*"))
        .atPriority(10)  // Low priority - default fallback
        .willReturn(aResponse().withStatus(404))
);

wireMock.stubFor(
    get(urlEqualTo("/specific"))
        .atPriority(1)   // High priority - matches first
        .willReturn(aResponse().withStatus(200))
);
```

### Conditional Stubs
```java
wireMock.stubFor(
    post(urlEqualTo("/api/users"))
        .withRequestBody(matchingJsonPath("$.role", equalTo("admin")))
        .willReturn(aResponse().withStatus(201))
);

wireMock.stubFor(
    post(urlEqualTo("/api/users"))
        .withRequestBody(matchingJsonPath("$.role", equalTo("user")))
        .willReturn(aResponse().withStatus(400))
);
```

## Matchers

| Matcher | Usage |
|---------|-------|
| `equalTo(value)` | Exact match |
| `containing(substring)` | Contains substring |
| `matching(regex)` | Regex pattern |
| `notContaining(substring)` | Doesn't contain |
| `matchingJsonPath(path, matcher)` | JSON path expression |
| `equalToJson(json)` | JSON content match |
| `equalToXml(xml)` | XML content match |

## Cleanup & Reset

```java
// Reset all stubs
wireMock.resetAll();

// Reset single mapping
wireMock.removeStub(stubMapping);

// Get all stubs
List<StubMapping> stubs = wireMock.getAllStubMappings().getMappings();
```

## Parameterized Tests

```java
@ParameterizedTest
@ValueSource(ints = {1, 2, 3})
void testMultipleIds(int id) {
    // Test with each value
}

@ParameterizedTest
@CsvSource({
    "/api/users, 200",
    "/api/invalid, 404"
})
void testEndpoints(String endpoint, int status) {
    // Test each row
}
```

## Common Patterns

### Test Setup & Teardown
```java
@BeforeEach
void setUp() {
    // Initialize before each test
    wireMock.resetAll();
}

@AfterEach
void tearDown() {
    // Cleanup after each test
}
```

### Response as JSON Object
```java
String responseJson = """
    {
        "id": 1,
        "name": "John",
        "email": "john@example.com"
    }""";

wireMock.stubFor(
    get(urlEqualTo("/api/user"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody(responseJson))
);
```

### File-based Responses
```java
// Create __files/response.json in src/test/resources/
wireMock.stubFor(
    get(urlEqualTo("/api/users"))
        .willReturn(aResponse()
            .withBodyFile("response.json"))
);
```

### Mock Server Lifecycle
```java
@RegisterExtension
static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(wireMockConfig()
        .dynamicPort()
        .notRequiringClientCertificate()
    )
    .configureStaticDsl(true)
    .build();

String port = String.valueOf(wireMock.getPort());
```

## Assertions with AssertJ

```java
ApiResponse response = apiClient.get("/endpoint");

assertThat(response.statusCode).isEqualTo(200);
assertThat(response.body).contains("expected");
assertThat(response.body).doesNotContain("unexpected");
assertThat(response.body).startsWith("{");
```

## Debugging Tips

```java
// Check if stub was registered
List<StubMapping> stubs = wireMock.getAllStubMappings().getMappings();
System.out.println("Total stubs: " + stubs.size());

// Find unmatched requests
List<LoggedRequest> requests = wireMock.findUnmatchedRequests().getRequests();

// Print all stubs (useful for debugging)
wireMock.getAllStubMappings().getMappings()
    .forEach(stub -> System.out.println(stub.getRequest().getUrl()));
```

## Common Errors & Solutions

| Error | Solution |
|-------|----------|
| Port not available | Use `dynamicPort()` instead of fixed port |
| Stub not matching | Check URL exactly (trailing slash, query params) |
| Wrong response | Check stub priority (higher number = higher priority) |
| Request not verified | Verify exact URL and method match |

## Maven Configuration

```xml
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock-jre8</artifactId>
    <version>3.0.1</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.9.2</version>
    <scope>test</scope>
</dependency>
```

## Run Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=MyApiTest

# Specific test method
mvn test -Dtest=MyApiTest#testMethod

# With Maven Surefire Plugin
mvn test -Dsurefire.suiteXmlFiles=testng.xml
```
