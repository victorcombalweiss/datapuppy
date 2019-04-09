package com.github.victorcombalweiss.datapuppy.agent;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.victorcombalweiss.datapuppy.agent.model.Alert;

class AgentHelper {

    private static final Logger logger = LoggerFactory.getLogger(AgentHelper.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    AgentHelper() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    String prependToJsonArray(Reader input, Alert alert) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Null passed as JSON array input to " + AgentHelper.class);
        }
        String savedInput = IOUtils.toString(input);
        if (alert == null) {
            logger.error("Null alert passed to " + AgentHelper.class);
            return savedInput;
        }
        try {
            JsonNode history = objectMapper.readTree(new StringReader(savedInput));
            if (history == null) {
                history = objectMapper.createArrayNode();
            }
            if (!history.isArray()) {
                history = objectMapper.createArrayNode().add(history);
            }
            ArrayNode historyAsArray = (ArrayNode)history;
            historyAsArray.insert(0, objectMapper.valueToTree(alert));
            return objectMapper.writeValueAsString(historyAsArray);
        }
        catch (JsonParseException ex) {
            logger.error("Invalid JSON in alert history file: ", ex);
            if (savedInput.charAt(0) == '[') {
                return '[' + objectMapper.writeValueAsString(alert)
                    + (savedInput.length() > 1
                            ? ',' + savedInput.substring(1)
                            : ']');
            }
            throw new IllegalStateException("Unfixable JSON in alert history file: ", ex);
        }
    }
}
