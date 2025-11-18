package com.ruokit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruokit.model.AccessTokenResponse;
import com.ruokit.model.ExternalApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExternalApiClient {

    private static final Logger LOGGER = LogManager.getLogger(ExternalApiClient.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final String DEFAULT_TOKEN_URL = "https://api.kiwoom.com/oauth2/token";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ExternalApiClient() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build(), new ObjectMapper());
    }

    ExternalApiClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public String callExternalApi() throws IOException, InterruptedException {
        String url = System.getenv("TARGET_API_URL");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("TARGET_API_URL environment variable is not configured");
        }

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT);

        String token = fetchAccessToken();
        requestBuilder.header("Authorization", "Bearer " + token);

        HttpRequest request = requestBuilder.build();
        LOGGER.info("Sending external API request to {}", url);

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode / 100 != 2) {
            throw new IllegalStateException("External API call failed with status code " + statusCode);
        }

        String body = response.body();
        LOGGER.debug("External API response received: {} characters", body != null ? body.length() : 0);
        logParsedResponse(body);
        return body;
    }

    private String fetchAccessToken() throws IOException, InterruptedException {
        String appKey = System.getenv("TARGET_API_APP_KEY");
        if (appKey == null || appKey.isBlank()) {
            throw new IllegalStateException("TARGET_API_APP_KEY environment variable is not configured");
        }

        String secretKey = System.getenv("TARGET_API_SECRET");
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("TARGET_API_SECRET environment variable is not configured");
        }

        String tokenUrl = System.getenv("TARGET_API_TOKEN_URL");
        if (tokenUrl == null || tokenUrl.isBlank()) {
            tokenUrl = DEFAULT_TOKEN_URL;
        }

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("appkey", appKey.trim());
        body.put("secretkey", secretKey.trim());

        String payload = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        LOGGER.info("Requesting OAuth token from {}", tokenUrl);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException("Access token request failed with status code " + response.statusCode());
        }

        AccessTokenResponse tokenResponse = objectMapper.readValue(response.body(), AccessTokenResponse.class);
        if (tokenResponse == null || tokenResponse.getToken() == null || tokenResponse.getToken().isBlank()) {
            throw new IllegalStateException("Access token response did not include a token");
        }

        LOGGER.debug("Received OAuth token expiring at {}", tokenResponse.getExpiresDt());
        return tokenResponse.getToken().trim();
    }

    private void logParsedResponse(String body) {
        if (body == null || body.isBlank()) {
            return;
        }

        try {
            ExternalApiResponse apiResponse = objectMapper.readValue(body, ExternalApiResponse.class);
            LOGGER.debug("External API status: {} message: {}", apiResponse.getStatus(), apiResponse.getMessage());
        } catch (Exception ex) {
            LOGGER.debug("External API response is not JSON or could not be parsed: {}", ex.getMessage());
        }
    }
}
