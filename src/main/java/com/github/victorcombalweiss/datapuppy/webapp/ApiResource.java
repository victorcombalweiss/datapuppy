package com.github.victorcombalweiss.datapuppy.webapp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringJoiner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class ApiResource {

    private static final Logger logger = LoggerFactory.getLogger(ApiResource.class);

    private final String alertFilePath;

    ApiResource(String alertFilePath) {
        this.alertFilePath = alertFilePath;
    }

    @Path("/alerts")
    @GET
    @Timed
    public String getAlerts() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(alertFilePath))) {
            StringJoiner stringJoiner = new StringJoiner(",", "[", "]");
            while (reader.ready()) {
                stringJoiner.add(reader.readLine());
            }
            return stringJoiner.toString();
        } catch (FileNotFoundException ex) {
            logger.info("Alert file path does not exist");
            return "[]";
        } catch (IOException ex) {
            logger.error("Error occurred while trying to read from alert file", ex);
            throw ex;
        }
    }

}
