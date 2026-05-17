package com.automation.config;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Central configuration for WireMock test setup.
 * Can be reused across multiple test classes.
 */
public class WireMockTestConfig {

    /**
     * Creates a WireMock extension with standard configuration.
     */
    public static WireMockExtension createWireMockExtension() {
        return WireMockExtension.newInstance()
                .options(wireMockConfig()
                    .dynamicPort()
                    .disableRequestJournal()  // Improve performance
                )
                .configureStaticDsl(true)
                .build();
    }

    /**
     * Creates a WireMock extension with fixed port (for specific scenarios).
     */
    public static WireMockExtension createWireMockExtensionWithFixedPort(int port) {
        return WireMockExtension.newInstance()
                .options(wireMockConfig()
                    .port(port)
                )
                .configureStaticDsl(true)
                .build();
    }

    /**
     * Get base URL for the given WireMock extension.
     */
    public static String getBaseUrl(WireMockExtension wireMock) {
        return "http://localhost:" + wireMock.getPort();
    }

    /**
     * Get base URL with specific port.
     */
    public static String getBaseUrl(int port) {
        return "http://localhost:" + port;
    }

    /**
     * Get API endpoint URL.
     */
    public static String getEndpointUrl(WireMockExtension wireMock, String endpoint) {
        return getBaseUrl(wireMock) + endpoint;
    }

    /**
     * Get API endpoint URL with port.
     */
    public static String getEndpointUrl(int port, String endpoint) {
        return getBaseUrl(port) + endpoint;
    }
}
