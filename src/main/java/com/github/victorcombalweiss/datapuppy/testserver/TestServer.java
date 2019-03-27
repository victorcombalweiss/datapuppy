package com.github.victorcombalweiss.datapuppy.testserver;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TestServer extends Application<Configuration> {

    private static final Logger logger = LoggerFactory.getLogger(TestServer.class);

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            logger.error("Usage: <command> ACCESS_LOG_FILE_PATH");
            System.exit(1);
        }
        try {
            new TestServer(args[0]).run("server",
                    "com/github/victorcombalweiss/datapuppy/testserver/configuration.yml");
        } catch (Exception ex) {
            logger.error("An exception occurred", ex);
            System.exit(2);
        }
    }

    private final String accessLogFile;

    public TestServer(String accessLogFile) {
        this.accessLogFile = accessLogFile;
    }

    @Override
    public String getName() {
        return "Test service";
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        Map<String, String> stringsToSubstitute = new HashMap<>(1);
        stringsToSubstitute.put("ACCESS_LOG_FILE_PATH", accessLogFile);
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                new ResourceConfigurationSourceProvider(),
                new StrSubstitutor(stringsToSubstitute)));
        bootstrap.addBundle(new AssetsBundle("/testservice/", "/"));
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
        environment.jersey().disable();
    }
}
