package com.veeva.vault.custom.app.model.http;

import java.util.Map;

public class HttpRequest {
    private String requestProcessorId;
    private String requestBody;
    private Map<String, String> parameters;
    private Map<String, String> headers;
    private String requesterHost;
    private String requesterServerName;

    public HttpRequest(){

    }

    public HttpRequest(String requestProcessorId, Map<String, String> headers, Map<String, String> parameters, String requestBody, String requesterHost, String requesterServerName) {
        this.requestProcessorId = requestProcessorId;
        this.requestBody = requestBody;
        this.parameters = parameters;
        this.headers = headers;
        this.requesterHost = requesterHost;
        this.requesterServerName = requesterServerName;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setRequesterHost(String requesterHost) {
        this.requesterHost = requesterHost;
    }

    public void setRequesterServerName(String requesterServerName) {
        this.requesterServerName = requesterServerName;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getRequesterHost() {
        return requesterHost;
    }

    public String getRequesterServerName() {
        return requesterServerName;
    }

    public String getRequestProcessorId() {
        return requestProcessorId;
    }
}
