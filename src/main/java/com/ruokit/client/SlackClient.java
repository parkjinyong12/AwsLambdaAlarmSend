package com.ruokit.client;

import com.ruokit.model.SlackPayload;
import com.ruokit.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class SlackClient {

    private static final Logger LOGGER = LogManager.getLogger(SlackClient.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public void sendMessage(String message) {
        String webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        if (webhookUrl == null || webhookUrl.isBlank()) {
            LOGGER.warn("SLACK_WEBHOOK_URL is not configured. Skipping Slack notification.");
            return;
        }

        try {
            SlackPayload payload = new SlackPayload(JsonUtils.escapeJson(message));
            String body = buildPayload(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                LOGGER.error("Slack webhook returned status {} with body {}", response.statusCode(), response.body());
            } else {
                LOGGER.info("Slack notification sent successfully");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to send Slack notification", e);
        }
    }

    private String buildPayload(SlackPayload payload) {
        return "{\"text\":\"" + payload.getText() + "\"}";
    }
}
