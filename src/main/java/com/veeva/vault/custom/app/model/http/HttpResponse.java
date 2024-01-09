package com.veeva.vault.custom.app.model.http;


import com.veeva.vault.custom.app.model.files.File;

/**
 * Class representing an outgoing HttpResponse to be handled by the script processor, available as 'response' variable
 */
public class HttpResponse {
    private String responseBody;
    private Integer responseCode;
    private File responseFile;
    private String responseView;

    /**
     * @hidden
     */
    public HttpResponse() {
        this.responseCode = 200;
    }

    /**
     * @hidden
     * @return
     */
    public String getResponseBody() {
        return responseBody;
    }

    /**
     * Sets the body for this response, which should be in serialized report
     * @param responseBody
     */
    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    /**
     * @hidden
     * @return
     */
    public Integer getResponseCode() {
        return responseCode;
    }

    /**
     * Sets the status code for this response, e.g 200 for OK. Valid status codes are those in the 2XX, 3XX, 4XX, and 5XX ranges. Other status codes are treated as container specific.
     * @param responseCode
     */
    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * @hidden
     * @return
     */
    public File getResponseFile() {
        return responseFile;
    }

    /**
     * Sets the file for this response if using a file response
     * @param responseFile
     */
    public void setResponseFile(File responseFile) {
        this.responseFile = responseFile;
    }

    /**
     * @hidden
     * @return
     */
    public String getResponseView() {
        return responseView;
    }

    /**
     * Sets the HTML string for this response if using a webpage response
     * @param responseView
     */
    public void setResponseView(String responseView) {
        this.responseView = responseView;
    }
}
