package com.ruokit.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruokit.model.ExternalApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class ExternalApiClient {

    private static final Logger LOGGER = LogManager.getLogger(ExternalApiClient.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

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

        String token = System.getenv("TARGET_API_TOKEN");
        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + token.trim());
        }

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
