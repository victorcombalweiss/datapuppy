package com.github.victorcombalweiss.datapuppy.agent.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class AccessStats {

    public final Map<String, Integer> sectionHits;
    public final SortedMap<Integer, StatsOfARequestCategory> errors;
    public final List<RequestWithWeight> heaviestResponses;

    public AccessStats(Map<String, Integer> sectionHits, SortedMap<Integer, StatsOfARequestCategory> errors,
            List<RequestWithWeight> heaviestResponses) {
        this.sectionHits = sectionHits == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(sectionHits));
        this.errors = errors == null
                ? Collections.emptySortedMap()
                : Collections.unmodifiableSortedMap(new TreeMap<>(errors));
        this.heaviestResponses = heaviestResponses == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(heaviestResponses));
    }

    @Override
    public String toString() {
        return "{sectionHits: " + sectionHits + ", errors: " + errors
                + ", heaviest responses: " + heaviestResponses + "}";
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || !(otherObject instanceof AccessStats)) {
            return false;
        }
        AccessStats other = (AccessStats)otherObject;
        return this.sectionHits.equals(other.sectionHits)
                && this.errors.equals(other.errors)
                && this.heaviestResponses.equals(other.heaviestResponses);
    }

    @Override
    public int hashCode() {
        int hashCode = sectionHits.hashCode();
        hashCode += 31 * hashCode + errors.hashCode();
        hashCode += 31 * hashCode + heaviestResponses.hashCode();
        return hashCode;
    }
}
