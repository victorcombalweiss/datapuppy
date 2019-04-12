package com.github.victorcombalweiss.datapuppy.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.victorcombalweiss.datapuppy.agent.model.AccessLog;
import com.github.victorcombalweiss.datapuppy.agent.model.AccessStats;
import com.github.victorcombalweiss.datapuppy.agent.model.RequestWithWeight;
import com.github.victorcombalweiss.datapuppy.agent.model.StatsOfARequestCategory;
import com.github.victorcombalweiss.datapuppy.agent.model.SummaryStats;
import com.github.victorcombalweiss.datapuppy.agent.model.SummaryStats.ErrorPercentages;
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
    private static final int MAX_ELEMENT_COUNT_IN_LISTS = 5;

    private Instant timeIntervalStart;
    private int requestCount = 0;
    private int clientErrorCount = 0;
    private int serverErrorCount = 0;

    private final Set<String> uniqueIps = new HashSet<>();
    private final List<Integer> responseWeights = new ArrayList<>();
    private final Map<String, Integer> sectionHits = new HashMap<>();
    private final NavigableMap<Integer, StatsOfARequestCategory> errorStats =
            new TreeMap<>(Collections.reverseOrder());
    private final Map<String, Integer> singleRequestOccurrences = new HashMap<>();
    private final NavigableSet<RequestWithWeight> requestsOrderedByWeight = new TreeSet<>();

    private final Parser<AccessLog> accessLogParser = new HttpdLoglineParser<>(AccessLog.class, LOG_FORMATS);

    StatsComputer(Instant startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Null passed as start time to " + StatsComputer.class.getName());
        }
        timeIntervalStart = startTime;
    }

    synchronized void ingestLog(String rawAccessLog) {
        if (rawAccessLog == null) {
            logger.error("Null passed as access log to " + StatsComputer.class.getName());
            return;
        }
        try {
            AccessLog accessLog = accessLogParser.parse(rawAccessLog);
            updateSummaryData(accessLog);
            updateSectionHits(accessLog);
            updateErrorStats(accessLog);
            updateRequestsOrderedByWeight(accessLog, rawAccessLog);
        } catch (DissectionFailure | InvalidDissectorException | MissingDissectorsException ex) {
            logger.error("Failed parsing access log line. Skipping it: '" + rawAccessLog + "'",
                    ex);
        }
    }

    private void updateSummaryData(AccessLog accessLog) {
        requestCount++;
        uniqueIps.add(accessLog.getClientIp());
        responseWeights.add(accessLog.getResponseWeight());
        if (HttpStatus.isClientError(accessLog.getHttpStatus())) {
            clientErrorCount++;
        }
        else if (HttpStatus.isServerError(accessLog.getHttpStatus())) {
            serverErrorCount++;
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
            return "/";
        }
        return request.substring(0, indexOfSlash);
    }

    private void updateErrorStats(AccessLog accessLog) {
        int httpStatus = accessLog.getHttpStatus();
        if (!HttpStatus.isClientError(httpStatus) && !HttpStatus.isServerError(httpStatus)) {
            return;
        }
        String fullRequest = httpStatus + " " + accessLog.getHttpMethod() + " " + accessLog.getRequest();

        Integer currentSingleRequestOccurrences = singleRequestOccurrences.get(fullRequest);
        int newSingleRequestOccurrences = currentSingleRequestOccurrences == null
                ? 1 : currentSingleRequestOccurrences + 1;
        singleRequestOccurrences.put(fullRequest, newSingleRequestOccurrences);

        StatsOfARequestCategory currentStats = errorStats.get(httpStatus);
        int errorCodeOccurrences = currentStats == null ? 1 : currentStats.occurrences + 1;
        Integer previousTopRequestOccurrences = currentStats == null
                ? 0 : singleRequestOccurrences.get(httpStatus + " " + currentStats.topRequest);
        boolean newRequestIsTopRequestForItsErrorCode = previousTopRequestOccurrences == null
                || newSingleRequestOccurrences > previousTopRequestOccurrences;
        String topRequest = newRequestIsTopRequestForItsErrorCode
                ? accessLog.getHttpMethod() + " " + accessLog.getRequest() : currentStats.topRequest;
        int topRequestOccurrences = newRequestIsTopRequestForItsErrorCode
                ? newSingleRequestOccurrences : previousTopRequestOccurrences;
        errorStats.put(httpStatus, new StatsOfARequestCategory(
                errorCodeOccurrences,
                topRequest,
                topRequestOccurrences));
        while (errorStats.size() > MAX_ELEMENT_COUNT_IN_LISTS) {
            errorStats.pollLastEntry();
        }
    }

    private void updateRequestsOrderedByWeight(AccessLog accessLog, String rawAccessLog) {
        int responseWeight = accessLog.getResponseWeight();
        requestsOrderedByWeight.add(new RequestWithWeight(responseWeight, rawAccessLog));
        while (requestsOrderedByWeight.size() > MAX_ELEMENT_COUNT_IN_LISTS) {
            requestsOrderedByWeight.pollLast();
        }
    }

    synchronized AccessStats getStatsAndReset(Instant forTime) {
        AccessStats stats = getStats(forTime);
        reset(forTime);
        return stats;
    }

    private AccessStats getStats(Instant forTime) {
        if (forTime == null) {
            throw new IllegalArgumentException("Null passed as poll time to get stats");
        }
        long timeInterval = forTime.toEpochMilli() - timeIntervalStart.toEpochMilli();
        if (timeInterval < 0) {
            throw new IllegalArgumentException("Polling stats at time prior to last poll");
        }
        if (timeInterval == 0) {
            timeInterval = 1;
        }
        double requestsPerSecond = 1000.0 * requestCount / timeInterval;
        double requestsPerIp = requestCount == 0 ? 0 : requestCount / uniqueIps.size();
        Collections.sort(responseWeights);
        int medianWeight = responseWeights.isEmpty() ? 0 : responseWeights.get((responseWeights.size() -1) / 2);
        ErrorPercentages errorPercentages = requestCount == 0
                ? new ErrorPercentages(0, 0)
                : new ErrorPercentages(
                        (double)clientErrorCount / requestCount,
                        (double)serverErrorCount / requestCount);

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
                .limit(MAX_ELEMENT_COUNT_IN_LISTS)
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        Map::putAll);

        return new AccessStats(
                new SummaryStats(requestsPerSecond, requestsPerIp, medianWeight, errorPercentages),
                orderedSectionHits, errorStats, new ArrayList<>(requestsOrderedByWeight));
    }

    private void reset(Instant timeIntervalStart) {
        this.timeIntervalStart = timeIntervalStart;
        requestCount = 0;
        clientErrorCount = 0;
        serverErrorCount = 0;
        uniqueIps.clear();
        responseWeights.clear();
        sectionHits.clear();
        errorStats.clear();
        singleRequestOccurrences.clear();
        requestsOrderedByWeight.clear();
    }
}
