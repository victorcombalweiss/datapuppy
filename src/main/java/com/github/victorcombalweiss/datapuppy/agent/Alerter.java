package com.github.victorcombalweiss.datapuppy.agent;

import java.util.Date;
import java.util.Optional;

import com.github.victorcombalweiss.datapuppy.agent.model.Alert;

class Alerter {

    void ingestLog(String requestLog, Date forDate) {
        // Not doing anything at the moment
    }

    Optional<Alert> getNewAlert(Date forDate) {
        return Optional.empty();
    }
}
