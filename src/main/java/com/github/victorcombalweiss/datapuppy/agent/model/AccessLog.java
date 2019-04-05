package com.github.victorcombalweiss.datapuppy.agent.model;

import org.eclipse.jetty.http.HttpMethod;

import nl.basjes.parse.core.Field;

public class AccessLog {

    private HttpMethod httpMethod;
    private String request;
    private int httpStatus;

    @Field("HTTP.PATH:request.firstline.uri.path")
    public void setRequest(final String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    @Field("STRING:request.status.last")
    public void setHttpStatus(final String code) {
        this.httpStatus = Integer.parseInt(code);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    @Field("HTTP.METHOD:request.firstline.method")
    public void setHttpMethod(final String method) {
        this.httpMethod = HttpMethod.valueOf(method.toUpperCase());
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
}
