package com.github.victorcombalweiss.datapuppy.agent.model;

import org.eclipse.jetty.http.HttpMethod;

import nl.basjes.parse.core.Field;

public class AccessLog {

    private String clientIp;
    private HttpMethod httpMethod;
    private String request;
    private int httpStatus;
    private int responseWeight;

    @Field("IP:connection.client.host")
    public void setClientIp(final String clientIp) {
        this.clientIp = clientIp;
    }

    public String getClientIp() {
        return clientIp;
    }

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

    @Field("BYTESCLF:response.body.bytes")
    public void setResponseWeight(final String bytes) {
        this.responseWeight = Integer.parseInt(bytes);
    }

    public int getResponseWeight() {
        return responseWeight;
    }
}
