package com.veeva.vault.custom.app.model.http;


import com.veeva.vault.custom.app.model.files.File;

public class HttpResponse {
    private String responseBody;
    private Integer responseCode;
    private File responseFile;
    private String responseView;

    public HttpResponse() {
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public File getResponseFile() {
        return responseFile;
    }

    public void setResponseFile(File responseFile) {
        this.responseFile = responseFile;
    }

    public String getResponseView() {
        return responseView;
    }

    public void setResponseView(String responseView) {
        this.responseView = responseView;
    }
}
