package com.easypost.scotch.clients.httpclient;

import com.easypost.scotch.ScotchMode;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublisher;

import static java.util.Objects.requireNonNull;

public class VCRHttpRequestBuilderImpl implements VCRHttpRequest.Builder {

    private VCR vcr;

    private VCRHttpHeadersBuilder headersBuilder;
    private URI uri;
    private String method;
    private boolean expectContinue;
    private HttpRequest.BodyPublisher bodyPublisher;
    private volatile Optional<HttpClient.Version> version;
    private Duration duration;

    public VCRHttpRequestBuilderImpl(URI uri, VCR vcr) {
        requireNonNull(uri, "uri must be non-null");
        checkURI(uri);
        this.uri = uri;
        this.headersBuilder = new VCRHttpHeadersBuilder();
        this.method = "GET"; // default, as per spec
        this.version = Optional.empty();

        this.vcr = vcr;
        this.vcr.clear(); // reset any cached request and response
    }

    public VCRHttpRequestBuilderImpl(VCR vcr) {
        this.headersBuilder = new VCRHttpHeadersBuilder();
        this.method = "GET"; // default, as per spec
        this.version = Optional.empty();

        this.vcr = vcr;
    }

    @Override
    public VCRHttpRequestBuilderImpl uri(URI uri) {
        requireNonNull(uri, "uri must be non-null");
        checkURI(uri);
        this.uri = uri;
        return this;
    }

    static void checkURI(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null)
            throw VCRUtils.newIAE("URI with undefined scheme");
        scheme = scheme.toLowerCase(Locale.US);
        if (!(scheme.equals("https") || scheme.equals("http"))) {
            throw VCRUtils.newIAE("invalid URI scheme %s", scheme);
        }
        if (uri.getHost() == null) {
            throw VCRUtils.newIAE("unsupported URI %s", uri);
        }
    }

    @Override
    public VCRHttpRequestBuilderImpl copy() {
        VCRHttpRequestBuilderImpl b = new VCRHttpRequestBuilderImpl(this.vcr);
        b.uri = this.uri;
        b.headersBuilder = this.headersBuilder.structuralCopy();
        b.method = this.method;
        b.expectContinue = this.expectContinue;
        b.bodyPublisher = bodyPublisher;
        b.uri = uri;
        b.duration = duration;
        b.version = version;
        return b;
    }

    private void checkNameAndValue(String name, String value) {
        requireNonNull(name, "name");
        requireNonNull(value, "value");
        if (!VCRUtils.isValidName(name)) {
            throw VCRUtils.newIAE("invalid header name: \"%s\"", name);
        }
        /*if (!Utils.ALLOWED_HEADERS.test(name, null)) {
            throw newIAE("restricted header name: \"%s\"", name);
        }*/
        if (!VCRUtils.isValidValue(value)) {
            throw VCRUtils.newIAE("invalid header value: \"%s\"", value);
        }
    }

    @Override
    public VCRHttpRequestBuilderImpl setHeader(String name, String value) {
        checkNameAndValue(name, value);
        headersBuilder.setHeader(name, value);
        return this;
    }

    @Override
    public VCRHttpRequest.Builder method(String method, BodyPublisher bodyPublisher) {
        return null;
    }

    @Override
    public VCRHttpRequestBuilderImpl header(String name, String value) {
        checkNameAndValue(name, value);
        headersBuilder.addHeader(name, value);
        return this;
    }

    @Override
    public VCRHttpRequestBuilderImpl headers(String... params) {
        requireNonNull(params);
        if (params.length == 0 || params.length % 2 != 0) {
            throw VCRUtils.newIAE("wrong number, %d, of parameters", params.length);
        }
        for (int i = 0; i < params.length; i += 2) {
            String name  = params[i];
            String value = params[i + 1];
            header(name, value);
        }
        return this;
    }

    @Override
    public VCRHttpRequestBuilderImpl expectContinue(boolean enable) {
        expectContinue = enable;
        return this;
    }

    @Override
    public VCRHttpRequestBuilderImpl version(HttpClient.Version version) {
        requireNonNull(version);
        this.version = Optional.of(version);
        return this;
    }

    VCRHttpHeadersBuilder headersBuilder() {  return headersBuilder; }

    URI uri() { return uri; }

    String method() { return method; }

    boolean expectContinue() { return expectContinue; }

    HttpRequest.BodyPublisher bodyPublisher() { return bodyPublisher; }

    Optional<HttpClient.Version> version() { return version; }

    public VCRHttpRequest.Builder GET() {
        return method0("GET", null);
    }

    public VCRHttpRequest.Builder POST(VCRHttpRequest.BodyPublisher body) {
        this.vcr.noteRequestBody(body.contents);
        return method0("POST", body);
    }

    public VCRHttpRequest.Builder DELETE() {
        return method0("DELETE", null);
    }

    public VCRHttpRequest.Builder PUT(VCRHttpRequest.BodyPublisher body) {
        return method0("PUT", body);
    }

    private VCRHttpRequest.Builder method0(String method, VCRHttpRequest.BodyPublisher body) {
        assert method != null;
        assert !method.isEmpty();
        this.method = method;
        this.bodyPublisher = body.bodyPublisher;
        return this;
    }

    @Override
    public VCRHttpRequest build() {
        if (uri == null)
            throw new IllegalStateException("uri is null");
        assert method != null;
        return new VCRImmutableHttpRequest(this, this.vcr);
    }

    public Object buildForWebSocket() {
        // VCR does not support web socket requests
        return null;
    }

    @Override
    public VCRHttpRequest.Builder timeout(Duration duration) {
        requireNonNull(duration);
        if (duration.isNegative() || Duration.ZERO.equals(duration))
            throw new IllegalArgumentException("Invalid duration: " + duration);
        this.duration = duration;
        return this;
    }

    Duration timeout() { return duration; }

}
