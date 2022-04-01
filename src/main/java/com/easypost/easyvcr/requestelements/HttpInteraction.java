package com.easypost.easyvcr.requestelements;

import java.time.Instant;

public class HttpInteraction extends HttpElement {
    private long recordedAt;

    private Request request;

    private Response response;

    public long getRecordedAt() {
        return this.recordedAt;
    }

    public void setRecordedAt(final long recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Request getRequest() {
        return this.request;
    }

    public void setRequest(final Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return this.response;
    }

    public void setResponse(final Response response) {
        this.response = response;
    }

    public HttpInteraction(Request request, Response response) {
        this.request = request;
        this.response = response;
        this.recordedAt = Instant.now().getEpochSecond();
    }
}
