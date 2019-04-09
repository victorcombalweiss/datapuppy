package com.github.victorcombalweiss.datapuppy.agent.model;

import java.time.Instant;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Alert {

    public enum AlertType {
        PEAK_TRAFFIC_START,
        PEAK_TRAFFIC_STOP
    }

    public final AlertType type;
    public final Instant time;

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Integer> requestCount;

    public Alert(AlertType type, Instant time, Optional<Integer> requestCount) {
        if (type == AlertType.PEAK_TRAFFIC_START && !requestCount.isPresent()) {
            throw new IllegalArgumentException("Request count is compulsory for "
                    + AlertType.PEAK_TRAFFIC_START + " alerts");
        }
        this.type = type;
        this.time = time;
        this.requestCount = requestCount;
    }
}
