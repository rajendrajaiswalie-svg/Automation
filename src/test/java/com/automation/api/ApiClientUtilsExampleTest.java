package com.automation.api;

import com.automation.config.WireMockTestConfig;
import com.automation.utils.WireMockStubUtils;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Example test class using WireMockTestConfig and WireMockStubUtils utilities.
 * This demonstrates how to simplify test code using shared utilities.
 */
@DisplayName("API Tests using Utilities")
class ApiClientUtilsExampleTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockTestConfig.createWireMockExtension();

    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        String baseUrl = WireMockTestConfig.getBaseUrl(wireMock);
        apiClient = new ApiClient(baseUrl);
    }

    @Test
    @DisplayName("Should get user successfully using utilities")
    void testGetUserWithUtilities() throws IOException {
        // Arrange - Create user response object
        Map<String, Object> userResponse = new LinkedHashMap<>();
        userResponse.put("id", 1);
        userResponse.put("name", "John Doe");
        userResponse.put("email", "john@example.com");

        // Setup stub using utility method
        WireMockStubUtils.setupGetStub(wireMock, "/users/1", userResponse);

        // Act
        ApiClient.ApiResponse response = apiClient.get("/users/1");

        // Assert
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.body).contains("John Doe");

        // Verify using utility method
        WireMockStubUtils.verifyGetRequest(wireMock, "/users/1");
    }

    @Test
    @DisplayName("Should create user successfully using utilities")
    void testCreateUserWithUtilities() throws IOException {
        // Arrange
        Map<String, Object> newUserResponse = new LinkedHashMap<>();
        newUserResponse.put("id", 2);
        newUserResponse.put("name", "Jane Doe");
        newUserResponse.put("email", "jane@example.com");
        newUserResponse.put("createdAt", "2024-01-15T10:30:00Z");

        WireMockStubUtils.setupPostStub(wireMock, "/users", newUserResponse);

        ApiClientTest.UserRequest newUser = new ApiClientTest.UserRequest("Jane Doe", "jane@example.com");

        // Act
        ApiClient.ApiResponse response = apiClient.post("/users", newUser);

        // Assert
        assertThat(response.statusCode).isEqualTo(201);
        assertThat(response.body).contains("Jane Doe");

        // Verify
        WireMockStubUtils.verifyPostRequest(wireMock, "/users");
    }

    @Test
    @DisplayName("Should handle not found error using utilities")
    void testNotFoundWithUtilities() throws IOException {
        // Arrange
        WireMockStubUtils.setupNotFoundStub(wireMock, "/users/999");

        // Act
        ApiClient.ApiResponse response = apiClient.get("/users/999");

        // Assert
        assertThat(response.statusCode).isEqualTo(404);
    }

    @Test
    @DisplayName("Should handle server error using utilities")
    void testServerErrorWithUtilities() throws IOException {
        // Arrange
        WireMockStubUtils.setupServerErrorStub(wireMock, "/error");

        // Act
        ApiClient.ApiResponse response = apiClient.get("/error");

        // Assert
        assertThat(response.statusCode).isEqualTo(500);
    }

    @Test
    @DisplayName("Should test with query parameter using utilities")
    void testQueryParameterWithUtilities() throws IOException {
        // Arrange
        Map<String, Object> usersResponse = new LinkedHashMap<>();
        usersResponse.put("total", 1);

        WireMockStubUtils.setupGetStubWithQueryParam(
            wireMock, 
            "/users", 
            "role", 
            "admin", 
            usersResponse
        );

        // Act
        ApiClient.ApiResponse response = apiClient.get("/users?role=admin");

        // Assert
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.body).contains("\"total\":1");
    }

    @Test
    @DisplayName("Should test with delayed response using utilities")
    void testDelayedResponseWithUtilities() throws IOException {
        // Arrange
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("status", "processing");

        WireMockStubUtils.setupGetStubWithDelay(
            wireMock, 
            "/long-operation", 
            responseBody, 
            200  // 200ms delay
        );

        // Act
        long startTime = System.currentTimeMillis();
        ApiClient.ApiResponse response = apiClient.get("/long-operation");
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(duration).isGreaterThanOrEqualTo(200);
    }

    @Test
    @DisplayName("Should verify multiple requests using utilities")
    void testMultipleRequestsWithUtilities() throws IOException {
        // Arrange
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");

        WireMockStubUtils.setupGetStub(wireMock, "/health", response);

        // Act
        apiClient.get("/health");
        apiClient.get("/health");
        apiClient.get("/health");

        // Assert - Verify using utility method
        WireMockStubUtils.verifyGetRequestCount(wireMock, "/health", 3);
    }

    @Test
    @DisplayName("Should verify POST request body using utilities")
    void testVerifyPostBodyWithUtilities() throws IOException {
        // Arrange
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", 1);

        WireMockStubUtils.setupPostStub(wireMock, "/users", response);

        ApiClientTest.UserRequest user = new ApiClientTest.UserRequest("Test User", "test@example.com");

        // Act
        apiClient.post("/users", user);

        // Assert - Verify body contains specific text
        WireMockStubUtils.verifyPostRequestWithBody(wireMock, "/users", "Test User");
    }

    @Test
    @DisplayName("Should delete resource using utilities")
    void testDeleteWithUtilities() throws IOException {
        // Arrange
        WireMockStubUtils.setupDeleteStub(wireMock, "/users/1");

        // Act - Note: ApiClient doesn't have delete method, this is just for stub setup demo
        // In real scenario, you would extend ApiClient with delete() method

        // Assert
        wireMock.verify(0, com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor(
            com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo("/users/1")
        ));
    }
}
