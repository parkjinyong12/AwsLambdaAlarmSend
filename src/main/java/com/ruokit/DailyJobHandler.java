package com.ruokit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.ruokit.service.DailyJobService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DailyJobHandler implements RequestHandler<Object, String> {

    private static final Logger LOGGER = LogManager.getLogger(DailyJobHandler.class);

    private final DailyJobService dailyJobService = new DailyJobService();

    @Override
    public String handleRequest(Object input, Context context) {
        LOGGER.info("Daily job execution started");
        try {
            dailyJobService.runDailyJob();
            LOGGER.info("Daily job execution finished successfully");
            return "OK";
        } catch (Exception e) {
            LOGGER.error("Daily job execution failed", e);
            return "ERROR";
        }
    }
}
