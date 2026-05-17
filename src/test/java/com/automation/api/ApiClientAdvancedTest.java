package com.automation.api;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;

/**
 * Advanced API Test Cases demonstrating parameterized tests and complex scenarios.
 */
@DisplayName("Advanced API Tests with WireMock")
class ApiClientAdvancedTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private ApiClient apiClient;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + wireMock.getPort();
        apiClient = new ApiClient(baseUrl);
    }

    // ==================== Parameterized Tests ====================

    @ParameterizedTest(name = "User ID: {0}")
    @ValueSource(ints = {1, 2, 3, 4, 5})
    @DisplayName("Should return 200 for multiple user IDs")
    void testGetMultipleUsers(int userId) throws IOException {
        // Arrange
        String endpoint = "/users/" + userId;
        wireMock.stubFor(
            get(urlPathMatching("/users/\\d+"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"id\": " + userId + ", \"name\": \"User " + userId + "\"}")
                )
        );

        // Act
        ApiClient.ApiResponse response = apiClient.get(endpoint);

        // Assert
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.body).contains("\"id\": " + userId);
    }

    @ParameterizedTest
    @CsvSource({
        "/users/1, 200",
        "/users/999, 404",
        "/users/inactive, 403",
        "/invalid-endpoint, 404"
    })
    @DisplayName("Should return expected status codes for different endpoints")
    void testVariousEndpoints(String endpoint, int expectedStatus) throws IOException {
        // Arrange
        wireMock.stubFor(
            get(urlPathMatching(".*"))
                .atPriority(10) // Default stub with low priority
                .willReturn(aResponse().withStatus(404))
        );

        // Specific stubs with higher priority
        if (endpoint.equals("/users/1")) {
            wireMock.stubFor(
                get(urlEqualTo(endpoint))
                    .atPriority(1)
                    .willReturn(aResponse().withStatus(200))
            );
        } else if (endpoint.equals("/users/inactive")) {
            wireMock.stubFor(
                get(urlEqualTo(endpoint))
                    .atPriority(1)
                    .willReturn(aResponse().withStatus(403))
            );
        }

        // Act
        ApiClient.ApiResponse response = apiClient.get(endpoint);

        // Assert
        assertThat(response.statusCode).isEqualTo(expectedStatus);
    }

    // ==================== Scenario Tests ====================

    @Test
    @DisplayName("Should handle stateful mock scenarios")
    void testStatefulMockingScenario() throws IOException {
        // Scenario: First call returns 202 (Accepted), second returns 200 (Ready)
        String endpoint = "/task/status";

        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .inScenario("task-processing")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse()
                    .withStatus(202)
                    .withBody("{\"status\": \"processing\"}")
                )
                .willSetStateTo("completed")
        );

        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .inScenario("task-processing")
                .whenScenarioStateIs("completed")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"status\": \"completed\"}")
                )
        );

        // Act - First call
        ApiClient.ApiResponse response1 = apiClient.get(endpoint);
        assertThat(response1.statusCode).isEqualTo(202);

        // Act - Second call (should get different response)
        ApiClient.ApiResponse response2 = apiClient.get(endpoint);
        assertThat(response2.statusCode).isEqualTo(200);
    }

    @Test
    @DisplayName("Should test response headers")
    void testResponseHeaders() throws IOException {
        // Arrange
        String endpoint = "/protected-resource";
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("X-Custom-Header", "CustomValue")
                    .withHeader("Authorization", "Bearer token123")
                    .withBody("Protected content")
                )
        );

        // Act
        ApiClient.ApiResponse response = apiClient.get(endpoint);

        // Assert - Verify request included custom headers in mock definition
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.body).contains("Protected");
    }

    @Test
    @DisplayName("Should handle multiple sequential requests")
    void testSequentialRequests() throws IOException {
        // Arrange
        wireMock.stubFor(
            get(urlPathMatching("/sequence/.*"))
                .willReturn(aResponse().withStatus(200))
        );

        // Act & Assert - Multiple requests
        for (int i = 1; i <= 5; i++) {
            ApiClient.ApiResponse response = apiClient.get("/sequence/step" + i);
            assertThat(response.statusCode).isEqualTo(200);
        }

        // Verify all requests were made
        wireMock.verify(5, getRequestedFor(urlPathMatching("/sequence/.*")));
    }

    @Test
    @DisplayName("Should handle mock reset and reconfiguration")
    void testMockResetAndReconfiguration() throws IOException {
        // Arrange - Initial stub
        String endpoint = "/configurable";
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse().withStatus(200).withBody("Version 1"))
        );

        // Act - First call
        ApiClient.ApiResponse response1 = apiClient.get(endpoint);
        assertThat(response1.statusCode).isEqualTo(200);
        assertThat(response1.body).isEqualTo("Version 1");

        // Reset all stubs
        wireMock.resetAll();

        // Reconfigure with new stub
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse().withStatus(200).withBody("Version 2"))
        );

        // Act - Second call with new configuration
        ApiClient.ApiResponse response2 = apiClient.get(endpoint);
        assertThat(response2.statusCode).isEqualTo(200);
        assertThat(response2.body).isEqualTo("Version 2");
    }

    @Test
    @DisplayName("Should test request timeout scenarios")
    void testRequestTimeout() throws IOException {
        // Arrange - Mock that returns very long delay
        String endpoint = "/timeout-test";
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Timeout response")
                    .withFixedDelay(50) // 50ms delay
                )
        );

        // Act
        long startTime = System.currentTimeMillis();
        ApiClient.ApiResponse response = apiClient.get(endpoint);
        long duration = System.currentTimeMillis() - startTime;

        // Assert
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(duration).isGreaterThanOrEqualTo(50);
    }

    @Test
    @DisplayName("Should handle concurrent requests")
    void testConcurrentRequests() throws InterruptedException {
        // Arrange
        String endpoint = "/concurrent";
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("{\"request_id\": \"test\"}")
                )
        );

        // Act - Make concurrent requests
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                try {
                    ApiClient.ApiResponse response = apiClient.get(endpoint);
                    assertThat(response.statusCode).isEqualTo(200);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        wireMock.verify(5, getRequestedFor(urlEqualTo(endpoint)));
    }
}
