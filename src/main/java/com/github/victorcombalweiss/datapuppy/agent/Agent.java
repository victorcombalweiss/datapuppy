package com.github.victorcombalweiss.datapuppy.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victorcombalweiss.datapuppy.agent.model.AccessStats;
import com.github.victorcombalweiss.datapuppy.agent.model.Alert;

public class Agent {

    private static final Logger logger = LoggerFactory.getLogger(Agent.class);

    public static void main(String[] args) {

        if (args == null || args.length < 4) {
            logger.error("Usage: <command> ACCESS_LOG_FILE_PATH TRAFFIC_THRESHOLD "
                    + "ALERT_FILE_PATH STATS_FILE_PATH");
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
        final String alertFilePath = args[2];
        final String statsFilePath = args[3];

        StatsComputer statsComputer = new StatsComputer();
        Alerter alerter = new Alerter(trafficThreshold, Instant.now());

        Tailer.create(
                Paths.get(accessLogFilePath).toFile(),
                new TailerListenerAdapter() {

                    @Override
                    public void handle(String line) {
                        statsComputer.ingestLog(line);
                        alerter.ingestLog(line, Instant.now());
                    }
                },
                1000,
                true);

        ObjectMapper objectMapper = new ObjectMapper();
        Paths.get(statsFilePath).toFile().getParentFile().mkdirs();

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                () -> {
                    AccessStats stats = statsComputer.getStatsAndReset();
                    logger.info("Stats: " + stats);
                    try (Writer writer = new FileWriter(statsFilePath)) {
                        objectMapper.writeValue(writer, stats);
                    } catch (IOException ex) {
                        logger.error("Could not write to stats file", ex);
                    }
                },
                0, secondsBetweenChecks, TimeUnit.SECONDS);

        AgentHelper helper = new AgentHelper();
        Paths.get(alertFilePath).toFile().getParentFile().mkdirs();

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                () -> {
                    Optional<Alert> alert = alerter.getNewAlert(Instant.now());
                    if (alert.isPresent()) {
                        logger.info("Alert triggered");
                        String alertHistory;
                        try {
                            try (Reader reader = new BufferedReader(new FileReader(alertFilePath))) {
                                alertHistory = helper.prependToJsonArray(reader, alert.get());
                            }
                            catch (FileNotFoundException ex) {
                                alertHistory = helper.prependToJsonArray(new StringReader("[]"), alert.get());
                            }
                        }
                        catch (IOException ex) {
                            logger.error("Could not read from alert file", ex);
                            return;
                        }
                        try (PrintWriter writer = new PrintWriter(new BufferedWriter(
                                new FileWriter(alertFilePath)))) {
                            writer.write(alertHistory);
                        }
                        catch (IOException ex) {
                            logger.error("Could not write to alert file", ex);
                        }
                    }
                },
                0, 100, TimeUnit.MILLISECONDS);
    }
}
