package com.github.victorcombalweiss.datapuppy.agent.model;

import java.time.Instant;

public class Alert {

    public enum AlertType {
        PEAK_TRAFFIC_START,
        PEAK_TRAFFIC_STOP
    }

    public final AlertType type;
    public final Instant time;

    public Alert(AlertType type, Instant time) {
        this.type = type;
        this.time = time;
    }
}
