package com.github.victorcombalweiss.datapuppy.agent.model;

import java.util.Objects;

public class SummaryStats {

    public static class ErrorPercentages {
        public final double clientErrors;
        public final double serverErrors;

        public ErrorPercentages(double clientErrors, double serverErrors) {
            this.clientErrors = clientErrors;
            this.serverErrors = serverErrors;
        }

        @Override
        public boolean equals(Object otherObject) {
            if (otherObject == null || !(otherObject instanceof ErrorPercentages)) {
                return false;
            }
            ErrorPercentages other = (ErrorPercentages)otherObject;
            return this.clientErrors == other.clientErrors
                    && this.serverErrors == other.serverErrors;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clientErrors) * 32 + Objects.hash(serverErrors);
        }

        @Override
        public String toString() {
            return "{client errors: " + clientErrors + ", server errors: " + serverErrors + "}";
        }
    }

    public final double requestsPerSecond;
    public final double requestsPerIp;
    public final int medianResponseSize;
    public final ErrorPercentages errorPercentages;

    public SummaryStats(double requestsPerSecond, double requestsPerIp, int medianResponseSize,
            ErrorPercentages errorPercentages) {
        this.requestsPerSecond = requestsPerSecond;
        this.requestsPerIp = requestsPerIp;
        this.medianResponseSize = medianResponseSize;
        this.errorPercentages = errorPercentages;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || !(otherObject instanceof SummaryStats)) {
            return false;
        }
        SummaryStats other = (SummaryStats)otherObject;
        return this.requestsPerSecond == other.requestsPerSecond
                && this.requestsPerIp == other.requestsPerIp
                && this.medianResponseSize == other.medianResponseSize
                && this.errorPercentages.equals(other.errorPercentages);
    }

    @Override
    public int hashCode() {
        int hashCode = Objects.hash(requestsPerSecond);
        hashCode += 31 * hashCode + Objects.hash(requestsPerIp);
        hashCode += 31 * hashCode + medianResponseSize;
        hashCode += 31 * hashCode + errorPercentages.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return "{requests per second: " + requestsPerSecond
                + ", requests per ip: " + requestsPerIp
                + ", median response size: " + medianResponseSize
                + ", error percentages: " + errorPercentages
                + "}";
    }
}
