package com.github.victorcombalweiss.datapuppy.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static java.util.AbstractMap.SimpleEntry;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.github.victorcombalweiss.datapuppy.agent.model.AccessStats;

public class TestStatsComputer {

    @Test
    void testStatsComputer_normalCase_returnCorrectStats() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /report HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.1 - jill [09/May/2018:16:00:41 +0000] \"GET /api/user HTTP/1.0\" 200 234");
        statsComputer.ingestLog("127.0.0.1 - frank [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 200 34");
        statsComputer.ingestLog("127.0.0.1 - mary [09/May/2018:16:00:42 +0000] \"POST /api/user HTTP/1.0\" 503 12");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(
                new AccessStats(Arrays.asList(
                        new SimpleEntry<>("/api", 3),
                        new SimpleEntry<>("/report", 1))),
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

        assertEquals(Arrays.asList(
                        new SimpleEntry<>("/banana", 2),
                        new SimpleEntry<>("/pear", 2)),
                result.sectionHits);
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

        assertEquals(Arrays.asList(new SimpleEntry<>("/", 1)), result.sectionHits);
    }

    @Test
    void testStatsComputer_pathWithTrailingSlash_returnCorrectSectionHits() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/ HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(Arrays.asList(new SimpleEntry<>("/banana", 1)), result.sectionHits);
    }

    @Test
    void testStatsComputer_noLog_returnEmptySectionHits() {
        StatsComputer statsComputer = new StatsComputer();

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(Collections.emptyList(), result.sectionHits);
    }

    @Test
    void testStatsComputer_onlyOneSection_returnCorrectSectionHits() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/pork HTTP/1.0\" 200 123");
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] \"GET /banana/ HTTP/1.0\" 200 123");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(Arrays.asList(new SimpleEntry<>("/banana", 2)), result.sectionHits);
    }

    /**************************** Invalid input *******************************/

    @Test
    void testStatsComputer_nullLog_returnEmptySectionHits() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog(null);

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(Collections.emptyList(), result.sectionHits);
    }

    @Test
    void testStatsComputer_emptyLogLine_returnEmptySectionHits() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(Collections.emptyList(), result.sectionHits);
    }


    @Test
    void testStatsComputer_wrongFormatLogLine_returnEmptySectionHits() {
        StatsComputer statsComputer = new StatsComputer();
        statsComputer.ingestLog("127.0.0.1 - james [09/May/2018:16:00:39 +0000] "
                + "\"GET /banana/pork HTTP/1.0\" 200 123 "
                + "something");

        AccessStats result = statsComputer.getStatsAndReset();

        assertEquals(Collections.emptyList(), result.sectionHits);
    }
}
