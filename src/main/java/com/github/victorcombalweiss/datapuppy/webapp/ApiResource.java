package com.github.victorcombalweiss.datapuppy.webapp;

import java.io.File;
import java.nio.file.Paths;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ApiResource {

    private final String alertFilePath;
    private final String statsFilePath;

    ApiResource(String alertFilePath, String statsFilePath) {
        this.alertFilePath = alertFilePath;
        this.statsFilePath = statsFilePath;
    }

    @Path("/alerts")
    @GET
    @Timed
    public File getAlerts() {
        return getFileOrNull(alertFilePath);
    }

    @Path("/stats")
    @GET
    @Timed
    public File getStats() {
        return getFileOrNull(statsFilePath);
    }

    private File getFileOrNull(String filePath) {
        File result = Paths.get(filePath).toFile();
        if (!result.exists()) {
            return null;
        }
        return result;
    }
}
