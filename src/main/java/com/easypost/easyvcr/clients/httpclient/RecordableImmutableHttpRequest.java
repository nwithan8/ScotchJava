package com.easypost.easyvcr.clients.httpclient;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.interactionconverters.HttpClientInteractionConverter;
import com.easypost.easyvcr.internalutilities.Utils;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

public final class RecordableImmutableHttpRequest extends RecordableHttpRequest {
    private final String method;
    private final URI uri;
    private final HttpHeaders headers;
    private final Optional<HttpRequest.BodyPublisher> requestPublisher;
    private final boolean expectContinue;
    private final Optional<Duration> timeout;
    private final Optional<Version> version;

    private final HttpClientInteractionConverter converter;

    private final Cassette cassette;
    private final Mode mode;
    private final AdvancedSettings advancedSettings;

    /**
     * Creates an ImmutableHttpRequest from the given builder.
     */
    RecordableImmutableHttpRequest(RecordableHttpRequestBuilderImpl builder, Cassette cassette, Mode mode,
                                   AdvancedSettings advancedSettings, HttpClientInteractionConverter converter) {
        this.method = Objects.requireNonNull(builder.method());
        this.uri = Objects.requireNonNull(builder.uri());
        this.headers = HttpHeaders.of(builder.headersBuilder().map(), Utils.ALLOWED_HEADERS);
        this.requestPublisher = Optional.ofNullable(builder.bodyPublisher());
        this.expectContinue = builder.expectContinue();
        this.timeout = Optional.ofNullable(builder.timeout());
        this.version = Objects.requireNonNull(builder.version());

        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
        this.converter = converter;
    }

    public RecordableBodyHandler getBodyHandler() {
        return new RecordableBodyHandler(cassette, mode, advancedSettings, converter);
    }

    @Override
    public String method() {return method;}

    @Override
    public URI uri() {return uri;}

    @Override
    public HttpHeaders headers() {return headers;}

    @Override
    public Optional<HttpRequest.BodyPublisher> bodyPublisher() {
        return requestPublisher;
    }

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

