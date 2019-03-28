package com.github.victorcombalweiss.datapuppy;

import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.victorcombalweiss.datapuppy.model.Alert;
import com.github.victorcombalweiss.datapuppy.testserver.TestServer;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        final String accessLogFilePath = "/tmp/access.log";
        final int secondsBetweenChecks = 10;

        TestServer.main(new String[] { accessLogFilePath });

        StatsComputer statsComputer = new StatsComputer();
        Alerter alerter = new Alerter();

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
