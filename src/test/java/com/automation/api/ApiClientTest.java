package com.automation.api;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;

/**
 * JUnit5 Test Suite for API testing using WireMock.
 * Demonstrates best practices for testing REST APIs with mocked endpoints.
 */
@DisplayName("API Tests with WireMock")
class ApiClientTest {

    // Create WireMock extension with dynamic port
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        // Initialize API client with WireMock's base URL
        String baseUrl = "http://localhost:" + wireMock.getPort();
        apiClient = new ApiClient(baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (apiClient != null) {
            apiClient.close();
        }
    }

    // ==================== GET Request Tests ====================

    @Test
    @DisplayName("Should return 200 status for successful GET request")
    void testGetRequestSuccess() throws IOException {
        // Arrange
        String endpoint = "/users/1";
        String mockResponse = """
                {
                    "id": 1,
                    "name": "John Doe",
                    "email": "john@example.com"
                }""";

        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)
                )
        );

        // Act
        ApiClient.ApiResponse response = apiClient.get(endpoint);

        // Assert
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.body).contains("John Doe", "john@example.com");
        
        // Verify the call was made
        wireMock.verify(getRequestedFor(urlEqualTo(endpoint)));
    }

    @Test
    @DisplayName("Should handle 404 Not Found")
    void testGetRequestNotFound() throws IOException {
        // Arrange
        String endpoint = "/users/999";
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(404)
                    .withBody("{\"error\": \"User not found\"}")
                )
        );

        // Act
        ApiClient.ApiResponse response = apiClient.get(endpoint);

        // Assert
        assertThat(response.statusCode).isEqualTo(404);
        assertThat(response.body).contains("error");
    }

    @Test
    @DisplayName("Should return 500 server error")
    void testGetRequestServerError() throws IOException {
        // Arrange
        String endpoint = "/users/error";
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(500)
                    .withBody("Internal Server Error")
                )
        );

        // Act
        ApiClient.ApiResponse response = apiClient.get(endpoint);

        // Assert
        assertThat(response.statusCode).isEqualTo(500);
    }

    // ==================== POST Request Tests ====================

    @Test
    @DisplayName("Should successfully create a resource with POST")
    void testPostRequestSuccess() throws IOException {
        // Arrange
        String endpoint = "/users";
        UserRequest newUser = new UserRequest("Jane Doe", "jane@example.com");
        
        String mockResponse = """
                {
                    "id": 2,
                    "name": "Jane Doe",
                    "email": "jane@example.com",
                    "createdAt": "2024-01-15T10:30:00Z"
                }""";

        wireMock.stubFor(
            post(urlEqualTo(endpoint))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mockResponse)
                )
        );

        // Act
        ApiClient.ApiResponse response = apiClient.post(endpoint, newUser);

        // Assert
        assertThat(response.statusCode).isEqualTo(201);
        assertThat(response.body).contains("Jane Doe", "jane@example.com");
        
        // Verify the request was made with correct content type
        wireMock.verify(
            postRequestedFor(urlEqualTo(endpoint))
                .withHeader("Content-Type", equalTo("application/json"))
        );
    }

    @Test
    @DisplayName("Should handle POST validation error")
    void testPostRequestValidationError() throws IOException {
        // Arrange
        String endpoint = "/users";
        UserRequest invalidUser = new UserRequest("", ""); // Invalid data

        wireMock.stubFor(
            post(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(400)
                    .withBody("{\"error\": \"Validation failed\"}")
                )
        );

        // Act
        ApiClient.ApiResponse response = apiClient.post(endpoint, invalidUser);

        // Assert
        assertThat(response.statusCode).isEqualTo(400);
    }

    // ==================== Advanced Tests ====================

    @Test
    @DisplayName("Should handle request with query parameters")
    void testGetRequestWithQueryParams() throws IOException {
        // Arrange
        String endpoint = "/users";
        wireMock.stubFor(
            get(urlPathEqualTo("/users"))
                .withQueryParam("role", equalTo("admin"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("[{\"id\": 1, \"name\": \"Admin User\"}]")
                )
        );

        // Act
        ApiClient.ApiResponse response = apiClient.get(endpoint + "?role=admin");

        // Assert
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.body).contains("Admin User");
    }

    @Test
    @DisplayName("Should verify request count")
    void testVerifyRequestCount() throws IOException {
        // Arrange
        String endpoint = "/ping";
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse().withStatus(200).withBody("pong"))
        );

        // Act
        apiClient.get(endpoint);
        apiClient.get(endpoint);
        apiClient.get(endpoint);

        // Assert
        wireMock.verify(3, getRequestedFor(urlEqualTo(endpoint)));
    }

    @Test
    @DisplayName("Should handle response with delay")
    void testResponseWithDelay() throws IOException {
        // Arrange
        String endpoint = "/slow-endpoint";
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Delayed response")
                    .withFixedDelay(100) // 100ms delay
                )
        );

        // Act
        long startTime = System.currentTimeMillis();
        ApiClient.ApiResponse response = apiClient.get(endpoint);
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(duration).isGreaterThanOrEqualTo(100);
    }

    @Test
    @DisplayName("Should match request body pattern")
    void testMatchRequestBodyPattern() throws IOException {
        // Arrange
        String endpoint = "/users";
        
        wireMock.stubFor(
            post(urlEqualTo(endpoint))
                .withRequestBody(matchingJsonPath("$.name", containing("Doe")))
                .willReturn(aResponse()
                    .withStatus(201)
                    .withBody("{\"id\": 1}")
                )
        );

        // Act
        UserRequest user = new UserRequest("John Doe", "john@example.com");
        ApiClient.ApiResponse response = apiClient.post(endpoint, user);

        // Assert
        assertThat(response.statusCode).isEqualTo(201);
    }

    // ==================== Helper Classes ====================

    public static class UserRequest {
        public String name;
        public String email;

        public UserRequest(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}
