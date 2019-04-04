package com.github.victorcombalweiss.datapuppy.agent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.victorcombalweiss.datapuppy.agent.model.AccessLog;
import com.github.victorcombalweiss.datapuppy.agent.model.AccessStats;
import com.google.common.base.Strings;

import nl.basjes.parse.core.Parser;
import nl.basjes.parse.core.exceptions.DissectionFailure;
import nl.basjes.parse.core.exceptions.InvalidDissectorException;
import nl.basjes.parse.core.exceptions.MissingDissectorsException;
import nl.basjes.parse.httpdlog.HttpdLoglineParser;

class StatsComputer {

    private static final Logger logger = LoggerFactory.getLogger(StatsComputer.class);
    private static final String LOG_FORMATS = "%h %l %u %t \"%r\" %>s %b"
            + "\n"
            + "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-agent}i\" %T";

    private final Map<String, Integer> sectionHits = new HashMap<>();
    private final Parser<AccessLog> accessLogParser = new HttpdLoglineParser<>(AccessLog.class, LOG_FORMATS);

    void ingestLog(String rawAccessLog) {
        if (rawAccessLog == null) {
            logger.error("Null passed as access log to " + StatsComputer.class.getName());
            return;
        }
        try {
            AccessLog accessLog = accessLogParser.parse(rawAccessLog);
            updateSectionHits(accessLog);
        } catch (DissectionFailure | InvalidDissectorException | MissingDissectorsException ex) {
            logger.error("Failed parsing access log line. Skipping it: '" + rawAccessLog + "'",
                    ex);
        }
    }

    private void updateSectionHits(AccessLog accessLog) {
        String section = getSection(accessLog.getRequest());
        Integer currentCount = sectionHits.get(section);
        if (currentCount == null) {
            currentCount = 0;
        }
        sectionHits.put(section, currentCount + 1);
    }

    private String getSection(String request) {
        if (Strings.isNullOrEmpty(request)) {
            return "/";
        }
        int indexOfStartOfSection = request.charAt(0) == '/' ? 1 : 0;
        int indexOfSlash = request.indexOf('/', indexOfStartOfSection);
        if (indexOfSlash < 0) {
            return request;
        }
        return request.substring(0, indexOfSlash);
    }

    AccessStats getStatsAndReset() {
        AccessStats stats = getStats();
        reset();
        return stats;
    }

    private AccessStats getStats() {
        Map<String, Integer> orderedSectionHits = sectionHits.entrySet()
                .stream()
                .sorted((entry1, entry2) -> {
                    if (entry1.getValue() > entry2.getValue()) {
                        return -1;
                    }
                    if (entry1.getValue() == entry2.getValue()) {
                        return entry1.getKey().compareTo(entry2.getKey());
                    }
                    return 1;
                })
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        Map::putAll);
        return new AccessStats(orderedSectionHits);
    }

    private void reset() {
        sectionHits.clear();
    }
}
