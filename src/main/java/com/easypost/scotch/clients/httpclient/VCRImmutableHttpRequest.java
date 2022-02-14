package com.easypost.scotch.clients.httpclient;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

final class VCRImmutableHttpRequest extends VCRHttpRequest {

    private final VCR vcr;

    private final String method;
    private final URI uri;
    private final HttpHeaders headers;
    private final Optional<HttpRequest.BodyPublisher> requestPublisher;
    private final boolean expectContinue;
    private final Optional<Duration> timeout;
    private final Optional<Version> version;

    /**
     * Creates an ImmutableHttpRequest from the given builder.
     */
    VCRImmutableHttpRequest(VCRHttpRequestBuilderImpl builder, VCR vcr) {
        this.method = Objects.requireNonNull(builder.method());
        this.uri = Objects.requireNonNull(builder.uri());
        this.headers = HttpHeaders.of(builder.headersBuilder().map(), VCRUtils.ALLOWED_HEADERS);
        this.requestPublisher = Optional.ofNullable(builder.bodyPublisher());
        this.expectContinue = builder.expectContinue();
        this.timeout = Optional.ofNullable(builder.timeout());
        this.version = Objects.requireNonNull(builder.version());

        // TODO: record cassette here
        this.vcr = vcr;
        vcr.initTrackedRequest(this);
    }

    @Override
    public String method() {return method;}

    @Override
    public URI uri() {return uri;}

    @Override
    public HttpHeaders headers() {return headers;}

    @Override
    public Optional<BodyPublisher> bodyPublisher() {return requestPublisher;}

    @Override
    public boolean expectContinue() {return expectContinue;}

    @Override
    public Optional<Duration> timeout() {return timeout;}

    @Override
    public Optional<Version> version() {return version;}

    @Override
    public String toString() {
        return uri.toString() + " " + method;
    }
}

