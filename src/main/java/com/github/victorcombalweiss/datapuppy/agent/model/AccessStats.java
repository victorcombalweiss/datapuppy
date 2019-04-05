package com.github.victorcombalweiss.datapuppy.agent.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class AccessStats {

    public final Map<String, Integer> sectionHits;
    public final SortedMap<Integer, StatsOfARequestCategory> errors;

    public AccessStats(Map<String, Integer> sectionHits, SortedMap<Integer, StatsOfARequestCategory> errors) {
        this.sectionHits = sectionHits == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(sectionHits));
        this.errors = errors == null
                ? Collections.emptySortedMap()
                : Collections.unmodifiableSortedMap(new TreeMap<>(errors));
    }

    @Override
    public String toString() {
        return "{sectionHits: " + sectionHits + ", errors: " + errors + "}";
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || !(otherObject instanceof AccessStats)) {
            return false;
        }
        AccessStats other = (AccessStats)otherObject;
        return this.sectionHits.equals(other.sectionHits)
                && this.errors.equals(other.errors);
    }

    @Override
    public int hashCode() {
        return this.sectionHits.hashCode() * 31 + this.errors.hashCode();
    }
}
