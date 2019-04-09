package com.github.victorcombalweiss.datapuppy.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.github.victorcombalweiss.datapuppy.agent.model.Alert;
import com.github.victorcombalweiss.datapuppy.agent.model.Alert.AlertType;

public class TestAgentHelper {

    @Test
    void testAgentHelper_normalCase_returnCorrectValue() throws IOException {
        Reader input = new StringReader("[{\"type\":\"PEAK_TRAFFIC_START\",\"time\":1554802619069}]");
        Alert alert = new Alert(AlertType.PEAK_TRAFFIC_STOP, Instant.ofEpochMilli(1554802920144L),
                Optional.empty());

        String result = new AgentHelper().prependToJsonArray(input, alert);

        assertEquals(
                "["
                + "{\"type\":\"PEAK_TRAFFIC_STOP\",\"time\":1554802920144},"
                + "{\"type\":\"PEAK_TRAFFIC_START\",\"time\":1554802619069}"
                + "]",
                result);
    }

    /*************************** Corner cases ********************************/

    @Test
    void testAgentHelper_emptyInput_returnSingletonArray() throws IOException {
        Reader input = new StringReader("");
        Alert alert = new Alert(AlertType.PEAK_TRAFFIC_STOP, Instant.ofEpochMilli(1554802920144L),
                Optional.empty());

        String result = new AgentHelper().prependToJsonArray(input, alert);

        assertEquals("[{\"type\":\"PEAK_TRAFFIC_STOP\",\"time\":1554802920144}]", result);
    }

    @Test
    void testAgentHelper_emptyArray_returnSingletonArray() throws Exception {
        Reader input = new StringReader("[]");
        Alert alert = new Alert(AlertType.PEAK_TRAFFIC_STOP, Instant.ofEpochMilli(1554802920144L),
                Optional.empty());

        String result = new AgentHelper().prependToJsonArray(input, alert);

        assertEquals("[{\"type\":\"PEAK_TRAFFIC_STOP\",\"time\":1554802920144}]", result);
    }

    @Test
    void testAgentHelper_nullAlert_returnUntouchedHistory() throws IOException {
        Reader input = new StringReader("[{\"type\":\"PEAK_TRAFFIC_START\",\"time\":1554802619069}]");
        Alert alert = null;

        String result = new AgentHelper().prependToJsonArray(input, alert);

        assertEquals("[{\"type\":\"PEAK_TRAFFIC_START\",\"time\":1554802619069}]", result);
    }

    @Test
    void testAgentHelper_invalidJsonElement_prependNewAlertAsIs() throws IOException {
        Reader input = new StringReader("[invalid]");
        Alert alert = new Alert(AlertType.PEAK_TRAFFIC_STOP, Instant.ofEpochMilli(1554802920144L),
                Optional.empty());

        String result = new AgentHelper().prependToJsonArray(input, alert);

        assertEquals(
                "["
                + "{\"type\":\"PEAK_TRAFFIC_STOP\",\"time\":1554802920144},"
                + "invalid"
                + "]",
                result);
    }

    @Test
    void testAgentHelper_inputIsJsonElementButNotArray_returnArray() throws IOException {
        Reader input = new StringReader("{\"key\":\"value\"}");
        Alert alert = new Alert(AlertType.PEAK_TRAFFIC_STOP, Instant.ofEpochMilli(1554802920144L),
                Optional.empty());

        String result = new AgentHelper().prependToJsonArray(input, alert);

        assertEquals(
                "["
                + "{\"type\":\"PEAK_TRAFFIC_STOP\",\"time\":1554802920144},"
                + "{\"key\":\"value\"}"
                + "]",
                result);
    }

    /***************************** Invalid input ********************************/

    @Test
    void testAgentHelper_nullInput_throwException() {
        Reader input = null;
        Alert alert = new Alert(AlertType.PEAK_TRAFFIC_STOP, Instant.ofEpochMilli(1554802920144L),
                Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> new AgentHelper().prependToJsonArray(input, alert));
    }

    @Test
    void testAgentHelper_invalidJsonInput_throwException() {
        Reader input = new StringReader("invalid");
        Alert alert = new Alert(AlertType.PEAK_TRAFFIC_STOP, Instant.ofEpochMilli(1554802920144L),
                Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> new AgentHelper().prependToJsonArray(input, alert));
    }
}
