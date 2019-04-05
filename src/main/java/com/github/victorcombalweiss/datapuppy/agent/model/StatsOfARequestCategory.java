package com.github.victorcombalweiss.datapuppy.agent.model;

public class StatsOfARequestCategory {

    public final int occurrences;
    public final String topRequest;
    public final int topRequestOccurrences;

    public StatsOfARequestCategory(int occurrences, String topRequest, int topRequestOccurrences) {
        this.occurrences = occurrences;
        this.topRequest = topRequest;
        this.topRequestOccurrences = topRequestOccurrences;
    }

    @Override
    public String toString() {
        return "{occurrences: " + occurrences + ", top request: '" + topRequest
                + "', top request occurrences: " + topRequestOccurrences + "}";
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || !(otherObject instanceof StatsOfARequestCategory)) {
            return false;
        }
        StatsOfARequestCategory other = (StatsOfARequestCategory)otherObject;
        return this.occurrences == other.occurrences
                && this.topRequest.equals(other.topRequest)
                && this.topRequestOccurrences == other.topRequestOccurrences;
    }

    @Override
    public int hashCode() {
        int hashCode = occurrences;
        hashCode += 31 * hashCode + topRequest.hashCode();
        hashCode += 31 * hashCode + topRequestOccurrences;
        return hashCode;
    }
}
