package com.github.victorcombalweiss.datapuppy.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class WebApplication extends Application<Configuration> {

    private static final Logger logger = LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.err.println("Usage: <command> ALERT_FILE_PATH");
            System.exit(1);
        }
        String alertFilePath = args[0];
        try {
            new WebApplication(alertFilePath).run("server",
                    "com/github/victorcombalweiss/datapuppy/webapp/configuration.yml");
        } catch (Exception ex) {
            logger.error("An error occurred when trying to run server", ex);
            System.exit(2);
        }
    }

    private final String alertFilePath;

    public WebApplication(String alertFilePath) {
        this.alertFilePath = alertFilePath;
    }

    @Override
    public String getName() {
        return "Datapuppy web application server";
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(new ApiResource(alertFilePath));
    }
}
