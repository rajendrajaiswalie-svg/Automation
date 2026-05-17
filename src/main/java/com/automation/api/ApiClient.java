package com.automation.api;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple API Client for making HTTP requests.
 */
public class ApiClient {
    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    private final Gson gson;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
    }

    /**
     * Performs a GET request to the specified endpoint.
     */
    public ApiResponse get(String endpoint) throws IOException {
        String url = baseUrl + endpoint;
        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String body = EntityUtils.toString(response.getEntity());
            return new ApiResponse(response.getStatusLine().getStatusCode(), body);
        }
    }

    /**
     * Performs a POST request with JSON body.
     */
    public ApiResponse post(String endpoint, Object body) throws IOException {
        String url = baseUrl + endpoint;
        HttpPost request = new HttpPost(url);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(gson.toJson(body)));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return new ApiResponse(response.getStatusLine().getStatusCode(), responseBody);
        }
    }

    public void close() throws IOException {
        httpClient.close();
    }

    /**
     * Simple response object.
     */
    public static class ApiResponse {
        public final int statusCode;
        public final String body;

        public ApiResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public <T> T asJson(Class<T> clazz) {
            Gson gson = new Gson();
            return gson.fromJson(body, clazz);
        }
    }
}
