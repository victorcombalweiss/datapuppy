package com.github.victorcombalweiss.datapuppy.agent.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AccessStats {

    public final Map<String, Integer> sectionHits;

    public AccessStats(Map<String, Integer> sectionHits) {
        this.sectionHits = sectionHits == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(sectionHits));
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
