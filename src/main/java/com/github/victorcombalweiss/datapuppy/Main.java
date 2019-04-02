package com.github.victorcombalweiss.datapuppy;

import java.nio.file.Paths;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.victorcombalweiss.datapuppy.agent.Agent;
import com.github.victorcombalweiss.datapuppy.testserver.TestServer;
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
        final String accessLogFilePath = "/tmp/access.log";
        final String alertFilePath = Paths
                .get(System.getProperty("user.home"))
                .resolve(".datapuppy/alerts")
                .toString();

        TestServer.main(new String[] { accessLogFilePath });
        Agent.main(new String[] { accessLogFilePath, "" + options.trafficThreshold, alertFilePath });
    }
}
