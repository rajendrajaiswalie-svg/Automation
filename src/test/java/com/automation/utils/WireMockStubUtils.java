package com.automation.utils;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.gson.Gson;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Utility class with common stub setup methods.
 * Reduces code duplication across test classes.
 */
public class WireMockStubUtils {

    private static final Gson gson = new Gson();

    /**
     * Setup a simple GET endpoint returning 200 with JSON response.
     */
    public static void setupGetStub(WireMockExtension wireMock, String endpoint, Object responseBody) {
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(gson.toJson(responseBody))
                )
        );
    }

    /**
     * Setup a GET endpoint returning 200 with plain text response.
     */
    public static void setupGetStubWithText(WireMockExtension wireMock, String endpoint, String response) {
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(response)
                )
        );
    }

    /**
     * Setup a GET endpoint returning 404 Not Found.
     */
    public static void setupNotFoundStub(WireMockExtension wireMock, String endpoint) {
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(404)
                    .withBody("{\"error\": \"Not found\"}")
                )
        );
    }

    /**
     * Setup a GET endpoint returning 500 Server Error.
     */
    public static void setupServerErrorStub(WireMockExtension wireMock, String endpoint) {
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(500)
                    .withBody("{\"error\": \"Internal server error\"}")
                )
        );
    }

    /**
     * Setup a GET endpoint with custom status code and response.
     */
    public static void setupGetStubWithStatus(WireMockExtension wireMock, 
                                              String endpoint, 
                                              int statusCode, 
                                              Object responseBody) {
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody(gson.toJson(responseBody))
                )
        );
    }

    /**
     * Setup a POST endpoint returning 201 Created.
     */
    public static void setupPostStub(WireMockExtension wireMock, String endpoint, Object responseBody) {
        wireMock.stubFor(
            post(urlEqualTo(endpoint))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(gson.toJson(responseBody))
                )
        );
    }

    /**
     * Setup a POST endpoint with custom status code.
     */
    public static void setupPostStubWithStatus(WireMockExtension wireMock, 
                                               String endpoint, 
                                               int statusCode, 
                                               Object responseBody) {
        wireMock.stubFor(
            post(urlEqualTo(endpoint))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody(gson.toJson(responseBody))
                )
        );
    }

    /**
     * Setup a GET endpoint with delayed response.
     */
    public static void setupGetStubWithDelay(WireMockExtension wireMock, 
                                             String endpoint, 
                                             Object responseBody, 
                                             int delayMs) {
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(gson.toJson(responseBody))
                    .withFixedDelay(delayMs)
                )
        );
    }

    /**
     * Setup a GET endpoint with random delay.
     */
    public static void setupGetStubWithRandomDelay(WireMockExtension wireMock, 
                                                   String endpoint, 
                                                   Object responseBody, 
                                                   int minDelayMs, 
                                                   int maxDelayMs) {
        wireMock.stubFor(
            get(urlEqualTo(endpoint))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(gson.toJson(responseBody))
                    .withRandomDelay(minDelayMs, maxDelayMs)
                )
        );
    }

    /**
     * Setup a GET endpoint with query parameter matching.
     */
    public static void setupGetStubWithQueryParam(WireMockExtension wireMock, 
                                                  String endpoint, 
                                                  String paramName, 
                                                  String paramValue, 
                                                  Object responseBody) {
        wireMock.stubFor(
            get(urlPathEqualTo(endpoint))
                .withQueryParam(paramName, equalTo(paramValue))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(gson.toJson(responseBody))
                )
        );
    }

    /**
     * Setup a DELETE endpoint returning 204 No Content.
     */
    public static void setupDeleteStub(WireMockExtension wireMock, String endpoint) {
        wireMock.stubFor(
            delete(urlEqualTo(endpoint))
                .willReturn(aResponse().withStatus(204))
        );
    }

    /**
     * Setup a PUT endpoint returning 200 OK.
     */
    public static void setupPutStub(WireMockExtension wireMock, String endpoint, Object responseBody) {
        wireMock.stubFor(
            put(urlEqualTo(endpoint))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(gson.toJson(responseBody))
                )
        );
    }

    /**
     * Verify GET request was made.
     */
    public static void verifyGetRequest(WireMockExtension wireMock, String endpoint) {
        wireMock.verify(getRequestedFor(urlEqualTo(endpoint)));
    }

    /**
     * Verify GET request was made N times.
     */
    public static void verifyGetRequestCount(WireMockExtension wireMock, String endpoint, int count) {
        wireMock.verify(count, getRequestedFor(urlEqualTo(endpoint)));
    }

    /**
     * Verify POST request was made.
     */
    public static void verifyPostRequest(WireMockExtension wireMock, String endpoint) {
        wireMock.verify(postRequestedFor(urlEqualTo(endpoint)));
    }

    /**
     * Verify POST request was made with body containing text.
     */
    public static void verifyPostRequestWithBody(WireMockExtension wireMock, String endpoint, String bodyText) {
        wireMock.verify(
            postRequestedFor(urlEqualTo(endpoint))
                .withRequestBody(containing(bodyText))
        );
    }
}
