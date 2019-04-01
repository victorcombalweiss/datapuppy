package com.github.victorcombalweiss.datapuppy.agent.model;

import java.time.LocalDateTime;

public class Alert {

    public enum AlertType {
        PEAK_TRAFFIC_START,
        PEAK_TRAFFIC_STOP
    }

    public final AlertType type;
    public final LocalDateTime time;

    public Alert(AlertType type, LocalDateTime time) {
        this.type = type;
        this.time = time;
    }
}
