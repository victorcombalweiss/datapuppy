package com.github.victorcombalweiss.datapuppy.agent;

import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.victorcombalweiss.datapuppy.agent.model.Alert;

public class Agent {

    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    public static void main(String[] args) {

        if (args == null || args.length < 2) {
            logger.error("Usage: <command> ACCESS_LOG_FILE_PATH TRAFFIC_THRESHOLD");
            System.exit(1);
        }
        int trafficThreshold = 0;
        try {
            trafficThreshold = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
            logger.error("Please provide an integer for TRAFFIC_THRESHOLD");
            System.exit(1);
        }
        final String accessLogFilePath = args[0];
        final int secondsBetweenChecks = 10;

        StatsComputer statsComputer = new StatsComputer();
        Alerter alerter = new Alerter(trafficThreshold);

        Tailer.create(
                Paths.get(accessLogFilePath).toFile(),
                new TailerListenerAdapter() {

                    @Override
                    public void handle(String line) {
                        statsComputer.ingestLog(line);
                        alerter.ingestLog(line, new Date());
                    }
                },
                1000,
                true);

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                () -> logger.info("Outputting stats: " + statsComputer.getStatsAndReset()),
                0, secondsBetweenChecks, TimeUnit.SECONDS);

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                () -> {
                    Optional<Alert> alert = alerter.getNewAlert(new Date());
                    if (alert.isPresent()) {
                        logger.info("Triggering alert : " + alert.get());
                    }
                },
                0, 100, TimeUnit.MILLISECONDS);
    }
}
