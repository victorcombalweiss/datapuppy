package com.github.victorcombalweiss.datapuppy.agent.model;

public class RequestWithWeight implements Comparable<RequestWithWeight> {

    public final int bytes;
    public final String request;

    public RequestWithWeight(int bytes, String request) {
        this.bytes = bytes;
        this.request = request;
    }

    @Override
    public String toString() {
        return "{bytes: " + bytes + ", request: '" + request + "'}";
    }

    @Override
    public boolean equals(Object otherObject) {
        if (otherObject == null || !(otherObject instanceof RequestWithWeight)) {
            return false;
        }
        RequestWithWeight other = (RequestWithWeight)otherObject;
        return this.bytes == other.bytes
                && this.request.equals(other.request);
    }

    @Override
    public int hashCode() {
        return bytes * 31 + request.hashCode();
    }

    @Override
    public int compareTo(RequestWithWeight other) {
        if (other == null) {
            return 1;
        }
        if (this.bytes > other.bytes) {
            return -1;
        }
        if (this.bytes == other.bytes) {
            if (this.request == null) {
                if (other.request == null) {
                    return 0;
                }
                return -1;
            }
            return this.request.compareTo(other.request);
        }
        return 1;
    }
}
