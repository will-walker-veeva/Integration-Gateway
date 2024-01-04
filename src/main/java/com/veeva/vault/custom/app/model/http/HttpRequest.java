package com.veeva.vault.custom.app.model.http;

import java.util.Map;

/**
 * Class representing an incoming HttpRequest to be handled by the script processor, available as 'request' variable
 */
public class HttpRequest {
    private String requestProcessorId;
    private String requestBody;
    private Map<String, String> parameters;
    private Map<String, String> headers;
    private String requesterHost;
    private String requesterServerName;

    /**
     * @hidden
     */
    public HttpRequest(){

    }

    /**
     * @hidden
     * @param requestProcessorId
     * @param headers
     * @param parameters
     * @param requestBody
     * @param requesterHost
     * @param requesterServerName
     */
    public HttpRequest(String requestProcessorId, Map<String, String> headers, Map<String, String> parameters, String requestBody, String requesterHost, String requesterServerName) {
        this.requestProcessorId = requestProcessorId;
        this.requestBody = requestBody;
        this.parameters = parameters;
        this.headers = headers;
        this.requesterHost = requesterHost;
        this.requesterServerName = requesterServerName;
    }

    /**
     * @hidden
     * @param requestBody
     */
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * @hidden
     * @param parameters
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * @hidden
     * @param headers
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * @hidden
     * @param requesterHost
     */
    public void setRequesterHost(String requesterHost) {
        this.requesterHost = requesterHost;
    }

    /**
     * @hidden
     * @param requesterServerName
     */
    public void setRequesterServerName(String requesterServerName) {
        this.requesterServerName = requesterServerName;
    }

    /**
     * Returns the request body in String format, which can then be deserialized if necessary
     * @return
     */
    public String getRequestBody() {
        return requestBody;
    }

    /**
     * Returns a java.util.Map of the parameters of this request.
     * @return
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Returns a java.util.Map of the headers of this request.
     * @return
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns the Internet Protocol (IP) address of the client or last proxy that sent the request.
     * @return
     */
    public String getRequesterHost() {
        return requesterHost;
    }

    /**
     * Returns the fully qualified name of the client or the last proxy that sent the request.
     * @return
     */
    public String getRequesterServerName() {
        return requesterServerName;
    }

    /**
     * @hidden
     */
    public String getRequestProcessorId() {
        return requestProcessorId;
    }
}
