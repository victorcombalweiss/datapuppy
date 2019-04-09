package com.github.victorcombalweiss.datapuppy.agent;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.github.victorcombalweiss.datapuppy.agent.model.Alert;
import com.github.victorcombalweiss.datapuppy.agent.model.Alert.AlertType;
import com.google.common.collect.BoundType;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

class Alerter {

    private static final int AVERAGING_PERIOD_IN_SECONDS = 120;

    private final double trafficThreshold;
    private final Instant startTime;
    private final SortedMultiset<Instant> logDates = TreeMultiset.create();

    private AlertType currentStatus = null;

    Alerter(double trafficThreshold, Instant startTime) {
        if (trafficThreshold <= 0) {
            throw new IllegalArgumentException("Traffic threshold must be strictly positive");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Null passed as start time to " + Alerter.class.getSimpleName());
        }
        this.trafficThreshold = trafficThreshold;
        this.startTime = startTime;
    }

    void ingestLog(String requestLog, Instant logTime) {
        logDates.add(logTime);
    }

    Optional<Alert> getNewAlert(Instant forTime) {
        if (forTime == null) {
            throw new IllegalArgumentException("Null passed as time for which to check for alerts");
        }
        Instant startOfAveragingPeriod = forTime.minusSeconds(AVERAGING_PERIOD_IN_SECONDS);
        if (startOfAveragingPeriod.isBefore(startTime)) {
            startOfAveragingPeriod = startTime;
        }

        logDates.headMultiset(startOfAveragingPeriod, BoundType.OPEN).clear();

        double averagingPeriodDurationInSeconds = Duration.between(startOfAveragingPeriod, forTime)
                .toNanos() / 1_000_000_000.0;
        int requestCount = logDates.headMultiset(forTime, BoundType.CLOSED).size();
        double requestsPerSecond = requestCount / averagingPeriodDurationInSeconds;

        if (Double.isInfinite(requestsPerSecond)) {
            return Optional.empty();
        }
        Alert result = null;
        if (requestsPerSecond >= trafficThreshold && currentStatus != AlertType.PEAK_TRAFFIC_START) {
            result = new Alert(AlertType.PEAK_TRAFFIC_START, forTime, Optional.of(requestCount));
        }
        else if (requestsPerSecond < trafficThreshold && currentStatus == AlertType.PEAK_TRAFFIC_START) {
            result = new Alert(AlertType.PEAK_TRAFFIC_STOP, forTime, Optional.empty());
        }
        if (result != null) {
            currentStatus = result.type;
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
