package com.github.victorcombalweiss.datapuppy;

import com.github.victorcombalweiss.datapuppy.model.AccessStats;

class StatsComputer {

    void ingestLog(String requestLog) {
        // Not doing anything for the moment
    }

    AccessStats getStatsAndReset() {
        return new AccessStats();
    }
}
