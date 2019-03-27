package com.github.victorcombalweiss.datapuppy;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.victorcombalweiss.datapuppy.testserver.TestServer;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        final String accessLogFilePath = "/tmp/access.log";
        final int secondsBetweenChecks = 10;

        TestServer.main(new String[] { accessLogFilePath });

        Queue<String> logLines = new LinkedList<>();
        Tailer.create(
                Paths.get(accessLogFilePath).toFile(),
                new TailerListenerAdapter() {

                    @Override
                    public void handle(String line) {
                        synchronized (logLines) {
                            logLines.add(line);
                        }
                    }
                },
                1000,
                true);

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                () -> {
                    logger.info("Processing new log lines");
                    synchronized (logLines) {
                        while (!logLines.isEmpty()) {
                            logger.info("Processing log line: " + logLines.remove());
                        }
                    }
                },
                0, secondsBetweenChecks, TimeUnit.SECONDS);
    }
}
