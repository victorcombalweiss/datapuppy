package com.github.victorcombalweiss.datapuppy.agent;

import com.github.victorcombalweiss.datapuppy.agent.model.AccessStats;

class StatsComputer {

    void ingestLog(String requestLog) {
        // Not doing anything for the moment
    }

    AccessStats getStatsAndReset() {
        return new AccessStats();
    }
}
