package com.github.victorcombalweiss.datapuppy.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.github.victorcombalweiss.datapuppy.agent.model.AccessStats;
import com.github.victorcombalweiss.datapuppy.agent.model.RequestWithWeight;
import com.github.victorcombalweiss.datapuppy.agent.model.StatsOfARequestCategory;
import com.github.victorcombalweiss.datapuppy.agent.model.SummaryStats;
import com.github.victorcombalweiss.datapuppy.agent.model.SummaryStats.ErrorPercentages;
import com.google.common.collect.ImmutableMap;

public class TestStatsComputer {

    @Test
    void testStatsComputer_normalCase_returnCorrectStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report/ HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.2 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 200 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 34");
        statsComputer.ingestLog("127.0.0.2 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 303 12");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.4, 2.0, 34, new ErrorPercentages(0, 0)),
                        ImmutableMap.of(
                            "/api", 3,
                            "/report", 1),
                        Collections.emptySortedMap(),
                        Arrays.asList(
                                new RequestWithWeight(234, "127.0.0.2 - jill [09/May/2018:16:00:41 +0000] "
                                        + "\"GET /api/user HTTP/1.0\" 200 234"),
                                new RequestWithWeight(123, "127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                                        + "\"GET /report/ HTTP/1.0\" 200 123"),
                                new RequestWithWeight(34, "127.0.0.1 - frank [09/May/2018:16:00:42 +0000] "
                                        + "\"POST /api/user HTTP/1.0\" 200 34"),
                                new RequestWithWeight(12, "127.0.0.2 - mary [09/May/2018:16:00:42 +0000] "
                                        + "\"POST /api/user HTTP/1.0\" 303 12")
                                )),
                result);
    }

    @Test
    void testStatsComputer_normalCase_returnAlphabeticalOrderInSectionHitsWhenTie() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /pear/ HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /banana/pork HTTP/1.0\" 200 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /pear/lamb HTTP/1.0\" 200 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /banana/chicken HTTP/1.0\" 503 12");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                ImmutableMap.of(
                        "/banana", 2,
                        "/pear", 2),
                result.sectionHits);
    }

    @Test
    void testStatsComputer_normalCaseWithErrors_returnCorrectErrorStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report HTTP/1.0\" 404 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 404 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 500 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 503 12");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(ImmutableMap.of(
                503, new StatsOfARequestCategory(1, "POST /api/user", 1),
                500, new StatsOfARequestCategory(1, "POST /api/user", 1),
                404, new StatsOfARequestCategory(2, "GET /report", 1)), result.errors);
    }

    @Test
    void testStatsComputer_normalCaseMoreThanFiveSections_trimSectionHits() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /1/report HTTP/1.0\" 404 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /2/user HTTP/1.0\" 404 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /3/user HTTP/1.0\" 500 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /4/user HTTP/1.0\" 503 12");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /5/again HTTP/1.0\" 503 11");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /6/once HTTP/1.0\" 503 11");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(5, result.sectionHits.size());
    }

    @Test
    void testStatsComputer_normalCaseMoreThanFiveErrorStatuses_trimErrors() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report HTTP/1.0\" 400 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 401 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 500 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 501 12");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /me/again HTTP/1.0\" 502 11");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /more HTTP/1.0\" 503 11");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(5, result.errors.size());
    }

    @Test
    void testStatsComputer_normalCaseMoreThanFiveRequests_trimHeaviestResponses() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report HTTP/1.0\" 404 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 404 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 500 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 503 12");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /me/again HTTP/1.0\" 503 11");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /more HTTP/1.0\" 503 11");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(5, result.heaviestResponses.size());
    }

    @Test
    void testStatsComputer_normalCaseWithErrors_returnCorrectSummaryStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report/ HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.2 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 404 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 500 34");
        statsComputer.ingestLog("127.0.0.2 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 501 12");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new ErrorPercentages(0.25, 0.50),
                result.summary.errorPercentages);
    }

    /**************************** Corner cases *******************************/

    @Test
    void testStatsComputer_extendedNcsaLogFormat_returnNonEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("192.168.99.1 - - [04/Apr/2019:15:06:02 +0000] "
                + "\"GET /pear/pork HTTP/1.1\" 404 243 \"-\" "
                + "\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/73.0.3683.86 Safari/537.36\" 81");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(1, result.sectionHits.size());
    }

    @Test
    void testStatsComputer_rootUrl_returnCorrectSectionHits() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET / HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(ImmutableMap.of("/", 1), result.sectionHits);
    }

    @Test
    void testStatsComputer_pathWithTrailingSlash_returnCorrectSectionHits() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/ HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(ImmutableMap.of("/banana", 1), result.sectionHits);
    }

    @Test
    void testStatsComputer_rootSectionWithFile_considerAsRootSection() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                ImmutableMap.of("/", 1),
                result.sectionHits);
    }

    @Test
    void testStatsComputer_rootSectionWithFileWithoutLeadingSlash_considerAsRootSection() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET banana HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                ImmutableMap.of("/", 1),
                result.sectionHits);
    }

    @Test
    void testStatsComputer_noLog_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.0, 0.0, 0, new ErrorPercentages(0, 0)),
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_onlyOneSection_returnCorrectSectionHits() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/pork HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/ HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(ImmutableMap.of("/banana", 2), result.sectionHits);
    }

    @Test
    void testStatsComputer_zeroResponseWeight_returnCorrectHeaviestResponses() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/pork HTTP/1.0\" 200 0");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                Arrays.asList(new RequestWithWeight(0, "127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                        + "\"GET /banana/pork HTTP/1.0\" 200 0")),
                result.heaviestResponses);
    }

    @Test
    void testStatsComputer_singleLog_returnCorrectSummaryStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/pork HTTP/1.0\" 200 123456");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new SummaryStats(0.1, 1.0, 123456, new ErrorPercentages(0, 0)),
                result.summary);
    }

    @Test
    void testStatsComputer_singleIp_returnCorrectRequestsPerIp() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/pork HTTP/1.0\" 200 123456");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 200 234");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(2.0, result.summary.requestsPerIp);
    }

    @Test
    void testStatsComputer_pollAtSameTimestampAsStartTime_assumeOneMillisecondDifference() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/pork HTTP/1.0\" 200 123456");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));

        assertEquals(1000, result.summary.requestsPerSecond);
    }

    /**************************** Invalid input *******************************/

    @Test
    void testStatsComputer_nullStartTime_throwException() {
        assertThrows(IllegalArgumentException.class, () -> new StatsComputer(null));
    }

    @Test
    void testStatsComputer_nullLog_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog(null);

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.0, 0.0, 0, new ErrorPercentages(0, 0)),
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_emptyLogLine_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.0, 0.0, 0, new ErrorPercentages(0, 0)),
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_wrongFormatLogLine_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 200 123 "
                + "something");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.0, 0.0, 0, new ErrorPercentages(0, 0)),
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_wrongHttpMethod_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"HI /banana/pork HTTP/1.0\" 200 123 ");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.0, 0.0, 0, new ErrorPercentages(0, 0)),
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_wrongHttpStatus_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 666 123 ");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.0, 0.0, 0, new ErrorPercentages(0, 0)),
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_wrongResponseWeight_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 200 iAmAWeight ");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.0, 0.0, 0, new ErrorPercentages(0, 0)),
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_negativeResponseWeight_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 200 -1");

        AccessStats result = statsComputer.getStatsAndReset(LocalDateTime.of(2000, 1, 1, 13, 0, 10).toInstant(ZoneOffset.UTC));

        assertEquals(
                new AccessStats(
                        new SummaryStats(0.0, 0.0, 0, new ErrorPercentages(0, 0)),
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_nullPollTime_throwException() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        Instant forTime = null;

        assertThrows(
                IllegalArgumentException.class,
                () -> statsComputer.getStatsAndReset(forTime));
    }

    @Test
    void testStatsComputer_pollTimeBeforeStartTime_throwException() {
        StatsComputer statsComputer = new StatsComputer(LocalDateTime.of(2000, 1, 1, 13, 0, 0).toInstant(ZoneOffset.UTC));
        Instant forTime = LocalDateTime.of(2000, 1, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);

        assertThrows(
                IllegalArgumentException.class,
                () -> statsComputer.getStatsAndReset(forTime));
    }
}
