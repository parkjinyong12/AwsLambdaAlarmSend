package com.ruokit.service;

import com.ruokit.client.ExternalApiClient;
import com.ruokit.client.SlackClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DailyJobService {

    private static final Logger LOGGER = LogManager.getLogger(DailyJobService.class);

    private final ExternalApiClient externalApiClient = new ExternalApiClient();
    private final SlackClient slackClient = new SlackClient();

    public void runDailyJob() throws Exception {
        LOGGER.info("Calling external API for daily job");
        String apiResponse = externalApiClient.callExternalApi();
        LOGGER.info("External API call completed");

        if (needNotify(apiResponse)) {
            LOGGER.info("Notification required based on API response");
            slackClient.sendMessage("알림이 필요한 상황입니다.\n응답 값: " + apiResponse);
        } else {
            LOGGER.debug("No notification required for the current API response");
        }
    }

    private boolean needNotify(String apiResponse) {
        return apiResponse != null && apiResponse.contains("error");
    }
}
