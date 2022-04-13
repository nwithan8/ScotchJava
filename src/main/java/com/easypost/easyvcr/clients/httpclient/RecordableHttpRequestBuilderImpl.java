package com.easypost.easyvcr.clients.httpclient;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.interactionconverters.HttpClientInteractionConverter;
import com.easypost.easyvcr.internalutilities.Utils;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;
import com.easypost.easyvcr.requestelements.Status;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublisher;

import static java.util.Objects.requireNonNull;

public class RecordableHttpRequestBuilderImpl implements RecordableHttpRequest.Builder {

    private final Cassette cassette;
    private final Mode mode;
    private final AdvancedSettings advancedSettings;
    private final HttpClientInteractionConverter converter;

    private RecordableHttpHeadersBuilder headersBuilder;
    private URI uri;
    private String method;
    private boolean expectContinue;
    private HttpRequest.BodyPublisher bodyPublisher;
    private volatile Optional<HttpClient.Version> version;
    private Duration duration;

    public RecordableHttpRequestBuilderImpl(URI uri, Cassette cassette, Mode mode, AdvancedSettings advancedSettings, HttpClientInteractionConverter converter) {
        requireNonNull(uri, "uri must be non-null");
        checkURI(uri);
        this.uri = uri;
        this.headersBuilder = new RecordableHttpHeadersBuilder();
        this.method = "GET"; // default, as per spec
        this.version = Optional.empty();

        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
        this.converter = converter;
    }

    @Override
    public RecordableHttpRequestBuilderImpl uri(URI uri) {
        requireNonNull(uri, "uri must be non-null");
        checkURI(uri);
        this.uri = uri;
        return this;
    }

    static void checkURI(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw Utils.newIAE("URI with undefined scheme");
        }
        scheme = scheme.toLowerCase(Locale.US);
        if (!(scheme.equals("https") || scheme.equals("http"))) {
            throw Utils.newIAE("invalid URI scheme %s", scheme);
        }
        if (uri.getHost() == null) {
            throw Utils.newIAE("unsupported URI %s", uri);
        }
    }

    @Override
    public RecordableHttpRequestBuilderImpl copy() {
        RecordableHttpRequestBuilderImpl b =
                new RecordableHttpRequestBuilderImpl(this.uri, this.cassette, this.mode, this.advancedSettings, this.converter);
        b.headersBuilder = this.headersBuilder.structuralCopy();
        b.method = this.method;
        b.expectContinue = this.expectContinue;
        b.bodyPublisher = bodyPublisher;
        b.duration = duration;
        b.version = version;
        return b;
    }

    private void checkNameAndValue(String name, String value) {
        requireNonNull(name, "name");
        requireNonNull(value, "value");
        if (!Utils.isValidName(name)) {
            throw Utils.newIAE("invalid header name: \"%s\"", name);
        }
        /*if (!Utils.ALLOWED_HEADERS.test(name, null)) {
            throw newIAE("restricted header name: \"%s\"", name);
        }*/
        if (!Utils.isValidValue(value)) {
            throw Utils.newIAE("invalid header value: \"%s\"", value);
        }
    }

    @Override
    public RecordableHttpRequestBuilderImpl setHeader(String name, String value) {
        checkNameAndValue(name, value);
        headersBuilder.setHeader(name, value);
        return this;
    }

    @Override
    public RecordableHttpRequest.Builder method(String method, BodyPublisher bodyPublisher) {
        return null;
    }

    @Override
    public RecordableHttpRequestBuilderImpl header(String name, String value) {
        checkNameAndValue(name, value);
        headersBuilder.addHeader(name, value);
        return this;
    }

    @Override
    public RecordableHttpRequestBuilderImpl headers(String... params) {
        requireNonNull(params);
        if (params.length == 0 || params.length % 2 != 0) {
            throw Utils.newIAE("wrong number, %d, of parameters", params.length);
        }
        for (int i = 0; i < params.length; i += 2) {
            String name = params[i];
            String value = params[i + 1];
            header(name, value);
        }
        return this;
    }

    @Override
    public RecordableHttpRequestBuilderImpl expectContinue(boolean enable) {
        expectContinue = enable;
        return this;
    }

    @Override
    public RecordableHttpRequestBuilderImpl version(HttpClient.Version version) {
        requireNonNull(version);
        this.version = Optional.of(version);
        return this;
    }

    RecordableHttpHeadersBuilder headersBuilder() {return headersBuilder;}

    URI uri() {return uri;}

    String method() {return method;}

    boolean expectContinue() {return expectContinue;}

    HttpRequest.BodyPublisher bodyPublisher() {return bodyPublisher;}

    Optional<HttpClient.Version> version() {return version;}

    public RecordableHttpRequest.Builder GET() {
        return method0("GET", null);
    }

    public RecordableHttpRequest.Builder POST(RecordableHttpRequest.BodyPublisher body) {
        converter.noteRequestBody(body, advancedSettings.censors);
        return method0("POST", body);
    }

    public RecordableHttpRequest.Builder DELETE() {
        return method0("DELETE", null);
    }

    public RecordableHttpRequest.Builder PUT(RecordableHttpRequest.BodyPublisher body) {
        return method0("PUT", body);
    }

    private RecordableHttpRequest.Builder method0(String method, RecordableHttpRequest.BodyPublisher body) {
        assert method != null;
        assert !method.isEmpty();
        this.method = method;
        this.bodyPublisher = body.bodyPublisher;
        return this;
    }

    @Override
    public RecordableHttpRequest build() {
        if (uri == null) {
            throw new IllegalStateException("uri is null");
        }
        assert method != null;
        RecordableImmutableHttpRequest request =
                new RecordableImmutableHttpRequest(this, cassette, mode, advancedSettings, converter);
        try {
            converter.noteRequestDetails(request, advancedSettings.censors);
        } catch (Exception e) {
            throw new RuntimeException("Failed to cache request", e);
        }
        return request;
    }

    public Object buildForWebSocket() {
        // VCR does not support web socket requests
        return null;
    }

    @Override
    public RecordableHttpRequest.Builder timeout(Duration duration) {
        requireNonNull(duration);
        if (duration.isNegative() || Duration.ZERO.equals(duration)) {
            throw new IllegalArgumentException("Invalid duration: " + duration);
        }
        this.duration = duration;
        return this;
    }

    Duration timeout() {return duration;}

}
