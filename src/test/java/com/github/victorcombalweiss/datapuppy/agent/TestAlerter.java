package com.github.victorcombalweiss.datapuppy.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.github.victorcombalweiss.datapuppy.agent.model.Alert;
import com.github.victorcombalweiss.datapuppy.agent.model.Alert.AlertType;

public class TestAlerter {

    @Test
    void testAlerter_lowTrafficNeverHighBefore_returnEmptyOptional() {
        Alerter alerter = new Alerter(10, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 30).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 1, 0).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testAlerter_highTrafficForTheFirstTime_returnCorrespondingAlert() {
        Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 300_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));

        assertEquals(AlertType.PEAK_TRAFFIC_START, result.get().type);
    }

    @Test
    void testAlerter_continuedHighTraffic_returnEmptyOptional() {
        Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 300_000_000).toInstant(ZoneOffset.UTC));
        alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testAlerter_lowTrafficAfterPeak_returnCorrespondingAlert() {
        Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 300_000_000).toInstant(ZoneOffset.UTC));
        alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 1, 0).toInstant(ZoneOffset.UTC));

        assertEquals(AlertType.PEAK_TRAFFIC_STOP, result.get().type);
    }


    @Test
    void testAlerter_continuedLowTrafficAfterPeak_returnEmptyOptional() {
        Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 300_000_000).toInstant(ZoneOffset.UTC));
        alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));
        alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 1, 0).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 1, 1).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }


    @Test
    void testAlerter_highTrafficAsSecondPeak_returnCorrespondingAlert() {
        Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 300_000_000).toInstant(ZoneOffset.UTC));
        alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));

        alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 3).toInstant(ZoneOffset.UTC));

        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 3, 100_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 3, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 3, 300_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 4).toInstant(ZoneOffset.UTC));

        assertEquals(AlertType.PEAK_TRAFFIC_START, result.get().type);
    }

    @Test
    void testAlerter_highTrafficForTheFirstTime_returnCorrectTime() {
        Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 300_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));

        assertEquals(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC), result.get().time);
    }

    @Test
    void testAlerter_oldLogsPresent_ignoreOldLogs() {
        Alerter alerter = new Alerter(0.01, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 100_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 300_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 3, 0).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }

    /************************************* Corner cases ****************************************/

    @Test
    void testAlerter_trafficEqualToThreshold_returnHighTrafficAlert() {
        Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 1).toInstant(ZoneOffset.UTC));

        assertEquals(AlertType.PEAK_TRAFFIC_START, result.get().type);
    }

    @Test
    void testAlerter_logsOutOfOrderAndLastAddedOneTooOld_considerLowTraffic() {
        Alerter alerter = new Alerter(0.01, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 2, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 3, 0).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testAlerter_logBeforeStartTime_returnEmptyOptional() {
        Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 12, 0, 0).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 500_000_000).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testAlerter_logAfterPollingTime_returnEmptyOptionalByIgnoringThem() {
        Alerter alerter = new Alerter(0.01, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 200_000_000).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0, 300_000_000).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0, 100_000_000).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }

    @Test
    void testAlerter_noLog_returnEmptyOptional() {
        Alerter alerter = new Alerter(0.01, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 1).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }


    @Test
    void testAlerter_pollTimeEqualToStartTime_returnEmptyOptional() {
        Alerter alerter = new Alerter(0.01, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        alerter.ingestLog("", LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));

        Optional<Alert> result = alerter.getNewAlert(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));

        assertEquals(Optional.empty(), result);
    }

    /************************************* Invalid input ****************************************/

    @Test()
    void testAlerter_negativeTrafficThreshold_throwException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Alerter(-1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC)));
    }

    @Test()
    void testAlerter_zeroTrafficThreshold_throwException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Alerter(0, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC)));
    }

    @Test()
    void testAlerter_nullStartTime_throwException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Alerter(1, null));
    }

    @Test()
    void testAlerter_nullLogTime_throwException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Alerter alerter = new Alerter(0, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
                    alerter.ingestLog("", null);
                });
    }

    @Test()
    void testAlerter_nullPollTime_throwException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Alerter alerter = new Alerter(1, LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
                    alerter.getNewAlert(null);
                });
    }
}
