package com.github.victorcombalweiss.datapuppy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.victorcombalweiss.datapuppy.agent.Agent;
import com.github.victorcombalweiss.datapuppy.testserver.TestServer;
import com.github.victorcombalweiss.datapuppy.webapp.WebApplication;
import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;
import com.google.devtools.common.options.OptionsParser;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static class CommandLineOptions extends OptionsBase {

        @Option(
            name = "trafficThreshold",
            abbrev = 't',
            help = "Traffic threshold over which to trigger alerts, in requests per second",
            defaultValue = "10"
        )
        public int trafficThreshold;

        @Option(
            name = "logFile",
            abbrev = 'f',
            help = "Path of log file to read from",
            defaultValue = "/tmp/access.log"
        )
        public String logFilePath;
    }

    public static void main(String[] args) throws Exception {
        OptionsParser parser = OptionsParser.newOptionsParser(CommandLineOptions.class);
        parser.parseAndExitUponError(args);
        CommandLineOptions options = parser.getOptions(CommandLineOptions.class);

        if (options.trafficThreshold <= 0) {
          logger.error("Usage:");
          logger.error(parser.describeOptions(Collections.<String, String>emptyMap(),
                  OptionsParser.HelpVerbosity.LONG));
          return;
        }

        printResource("ascii-logo.txt", System.out);
        final Path agentOutputDirectory = Paths
                .get(System.getProperty("user.home"))
                .resolve(".datapuppy");
        final String alertFilePath = agentOutputDirectory.resolve("alerts.json")
                .toString();
        final String statsFilePath = agentOutputDirectory.resolve("stats.json")
                .toString();

        System.out.println("Loading...");
        TestServer.main(new String[] { options.logFilePath });
        Agent.main(new String[] { options.logFilePath, "" + options.trafficThreshold, alertFilePath,
                statsFilePath });
        WebApplication.main(new String[] { alertFilePath, statsFilePath });

        System.out.println("Ready!\n\n"
                + "Watch stats and alerts on port 9000 of this machine (for example http://localhost:9000)\n\n"
                + "Generate traffic by sending requests to port 8080 of this machine\n\n");
    }

    private static void printResource(String resourcePath, PrintStream outputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        Main.class.getPackage().getName().replaceAll("\\.", "/") + "/" + resourcePath)))) {
            while (reader.ready()) {
                outputStream.println(reader.readLine());
            }
        }
    }
}
