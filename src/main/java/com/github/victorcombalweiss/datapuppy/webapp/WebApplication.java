package com.github.victorcombalweiss.datapuppy.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class WebApplication extends Application<Configuration> {

    private static final Logger logger = LoggerFactory.getLogger(WebApplication.class);

    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Usage: <command> ALERT_FILE_PATH STATS_FILE_PATH");
            System.exit(1);
        }
        String alertFilePath = args[0];
        String statsFilePath = args[1];
        try {
            new WebApplication(alertFilePath, statsFilePath).run("server",
                    WebApplication.class.getPackage().getName().replaceAll("\\.", "/") + "/configuration.yml");
        } catch (Exception ex) {
            logger.error("An error occurred when trying to run server", ex);
            System.exit(2);
        }
    }

    private final String alertFilePath;
    private final String statsFilePath;

    public WebApplication(String alertFilePath, String statsFilePath) {
        this.alertFilePath = alertFilePath;
        this.statsFilePath = statsFilePath;
    }

    @Override
    public String getName() {
        return "Datapuppy web application server";
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new ResourceConfigurationSourceProvider());
        bootstrap.addBundle(new AssetsBundle(
            "/" + WebApplication.class.getPackage().getName().replaceAll("\\.", "/") + "/public",
            "/",
            "index.html"));
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(new ApiResource(alertFilePath, statsFilePath));
    }
}
