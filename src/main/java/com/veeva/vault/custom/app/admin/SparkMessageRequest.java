package com.veeva.vault.custom.app.admin;

import com.veeva.vault.custom.app.model.http.HttpRequest;
import com.veeva.vault.custom.app.model.http.HttpResponse;

public class SparkMessageRequest {
    private HttpRequest httpRequest;
    private Processor processor;

    public SparkMessageRequest(HttpRequest httpRequest, Processor processor) {
        this.httpRequest = httpRequest;
        this.processor = processor;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public Processor getProcessor() {
        return processor;
    }
}
