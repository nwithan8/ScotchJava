package com.easypost.easyvcr.clients.httpclient;

import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

public class RecordableBodyPublisher implements HttpRequest.BodyPublisher {
    @Override
    public long contentLength() {
        return 0;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super ByteBuffer> subscriber) {

    }

    protected HttpRequest.BodyPublisher toHttpBodyPublisher() {
        return (HttpRequest.BodyPublisher) this;
    }
}
