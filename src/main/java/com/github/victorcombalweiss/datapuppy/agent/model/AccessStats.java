package com.github.victorcombalweiss.datapuppy.agent.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccessStats {

    public final List<Map.Entry<String, Integer>> sectionHits;

    public AccessStats(List<Map.Entry<String, Integer>> sectionHits) {
        this.sectionHits = sectionHits == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(
                        sectionHits.stream()
                            .map(entry -> new SimpleEntry<>(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return sectionHits.toString();
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || !(otherObject instanceof AccessStats)) {
            return false;
        }
        AccessStats other = (AccessStats)otherObject;
        return this.sectionHits.equals(other.sectionHits);
    }

    @Override
    public int hashCode() {
        return this.sectionHits.hashCode();
    }
}
