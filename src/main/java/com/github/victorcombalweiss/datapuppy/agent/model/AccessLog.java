package com.github.victorcombalweiss.datapuppy.agent.model;

import nl.basjes.parse.core.Field;

public class AccessLog {

    private String request;

    @Field("HTTP.PATH:request.firstline.uri.path")
    public void setRequest(final String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }
}
