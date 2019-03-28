package com.github.victorcombalweiss.datapuppy.agent.model;

import java.util.Date;

public class Alert {

    enum AlertType {
        PEAK_TRAFFIC_START,
        PEAK_TRAFFIC_STOP
    }

    public final AlertType type;
    public final Date time;

    public Alert(AlertType type, Date time) {
        this.type = type;
        this.time = time;
    }
}
