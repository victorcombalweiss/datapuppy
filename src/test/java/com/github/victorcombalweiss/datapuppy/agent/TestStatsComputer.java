package com.github.victorcombalweiss.datapuppy.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.github.victorcombalweiss.datapuppy.agent.model.AccessStats;
import com.github.victorcombalweiss.datapuppy.agent.model.RequestWithWeight;
import com.github.victorcombalweiss.datapuppy.agent.model.StatsOfARequestCategory;
import com.google.common.collect.ImmutableMap;

public class TestStatsComputer {

    @Test
    void testStatsComputer_normalCase_returnCorrectStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 200 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 303 12");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(ImmutableMap.of(
                        "/api", 3,
                        "/report", 1),
                        Collections.emptySortedMap(),
                        Arrays.asList(
                                new RequestWithWeight(234, "127.0.0.1 - jill [09/May/2018:16:00:41 +0000] "
                                        + "\"GET /api/user HTTP/1.0\" 200 234"),
                                new RequestWithWeight(123, "127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                                        + "\"GET /report HTTP/1.0\" 200 123"),
                                new RequestWithWeight(34, "127.0.0.1 - frank [09/May/2018:16:00:42 +0000] "
                                        + "\"POST /api/user HTTP/1.0\" 200 34"),
                                new RequestWithWeight(12, "127.0.0.1 - mary [09/May/2018:16:00:42 +0000] "
                                        + "\"POST /api/user HTTP/1.0\" 303 12")
                                )),
                result);
    }

    @Test
    void testStatsComputer_normalCase_returnAlphabeticalOrderInSectionHitsWhenTie() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /pear HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /banana/pork HTTP/1.0\" 200 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /pear/lamb HTTP/1.0\" 200 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /banana/chicken HTTP/1.0\" 503 12");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                ImmutableMap.of(
                        "/banana", 2,
                        "/pear", 2),
                result.sectionHits);
    }

    @Test
    void testStatsComputer_normalCaseWithErrors_returnCorrectErrorStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report HTTP/1.0\" 404 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 404 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 500 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 503 12");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(ImmutableMap.of(
                503, new StatsOfARequestCategory(1, "POST /api/user", 1),
                500, new StatsOfARequestCategory(1, "POST /api/user", 1),
                404, new StatsOfARequestCategory(2, "GET /report", 1)), result.errors);
    }

    @Test
    void testStatsComputer_normalCaseMoreThanFiveRequests_trimHeaviestResponses() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report HTTP/1.0\" 404 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 404 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 500 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 503 12");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /me/again HTTP/1.0\" 503 11");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /more HTTP/1.0\" 503 11");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(5, result.heaviestResponses.size());
    }

    /**************************** Corner cases *******************************/

    @Test
    void testStatsComputer_extendedNcsaLogFormat_returnNonEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("192.168.99.1 - - [04/Apr/2019:15:06:02 +0000] "
                + "\"GET /pear/pork HTTP/1.1\" 404 243 \"-\" "
                + "\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/73.0.3683.86 Safari/537.36\" 81");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(1, result.sectionHits.size());
    }

    @Test
    void testStatsComputer_rootUrl_returnCorrectSectionHits() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET / HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(ImmutableMap.of("/", 1), result.sectionHits);
    }

    @Test
    void testStatsComputer_pathWithTrailingSlash_returnCorrectSectionHits() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/ HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(ImmutableMap.of("/banana", 1), result.sectionHits);
    }

    @Test
    void testStatsComputer_noLog_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_onlyOneSection_returnCorrectSectionHits() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/pork HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/ HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(ImmutableMap.of("/banana", 2), result.sectionHits);
    }

    @Test
    void testStatsComputer_zeroResponseWeight_returnCorrectHeaviestResponses() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/pork HTTP/1.0\" 200 0");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                Arrays.asList(new RequestWithWeight(0, "127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                        + "\"GET /banana/pork HTTP/1.0\" 200 0")),
                result.heaviestResponses);
    }

    /**************************** Invalid input *******************************/

    @Test
    void testStatsComputer_nullLog_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog(null);

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_emptyLogLine_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_wrongFormatLogLine_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 200 123 "
                + "something");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_wrongHttpMethod_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"HI /banana/pork HTTP/1.0\" 200 123 ");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_wrongHttpStatus_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 666 123 ");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_wrongResponseWeight_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 200 iAmAWeight ");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }

    @Test
    void testStatsComputer_negativeResponseWeight_returnEmptyStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 200 -1");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(
                        Collections.emptyMap(),
                        Collections.emptySortedMap(),
                        Collections.emptyList()),
                result);
    }
}
