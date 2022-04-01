package com.easypost.easyvcr.requestelements;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class Response extends HttpElement {
    private String body;

    private HttpVersion httpVersion;

    private Map<String, List<String>> headers;

    private Status status;

    private String errors;
    
    private URI uri;

    public HttpResponse<String> toHttpResponse(HttpRequest associatedRequest) {

        return new HttpResponse<String>() {
            @Override
            public int statusCode() {
                return status.getCode();
            }

            @Override
            public HttpRequest request() {
                return associatedRequest;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(headers, (key, value) -> true);
            }

            @Override
            public String body() {
                return body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return uri;
            }

            @Override
            public HttpClient.Version version() {
                return httpVersion.asHttpClientVersion();
            }
        };
    }

    public CloseableHttpResponse toCloseableHttpResponse() {

        return new CloseableHttpResponse() {
            @Override
            public void close() throws IOException {

            }

            @Override
            public StatusLine getStatusLine() {
                return null;
            }

            @Override
            public void setStatusLine(StatusLine statusLine) {

            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i) {

            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {

            }

            @Override
            public void setStatusCode(int i) throws IllegalStateException {

            }

            @Override
            public void setReasonPhrase(String s) throws IllegalStateException {

            }

            @Override
            public HttpEntity getEntity() {
                return null;
            }

            @Override
            public void setEntity(HttpEntity httpEntity) {

            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public void setLocale(Locale locale) {

            }

            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public boolean containsHeader(String s) {
                return false;
            }

            @Override
            public Header[] getHeaders(String s) {
                return new Header[0];
            }

            @Override
            public Header getFirstHeader(String s) {
                return null;
            }

            @Override
            public Header getLastHeader(String s) {
                return null;
            }

            @Override
            public Header[] getAllHeaders() {
                return new Header[0];
            }

            @Override
            public void addHeader(Header header) {

            }

            @Override
            public void addHeader(String s, String s1) {

            }

            @Override
            public void setHeader(Header header) {

            }

            @Override
            public void setHeader(String s, String s1) {

            }

            @Override
            public void setHeaders(Header[] headers) {

            }

            @Override
            public void removeHeader(Header header) {

            }

            @Override
            public void removeHeaders(String s) {

            }

            @Override
            public HeaderIterator headerIterator() {
                return null;
            }

            @Override
            public HeaderIterator headerIterator(String s) {
                return null;
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public void setParams(HttpParams httpParams) {

            }
        };
    }

    public String getBody() {
        return this.body;
    }

    public  void setBody(String body) {
        this.body = body;
    }

    public HttpVersion getHttpVersion() {
        return this.httpVersion;
    }

    public  void setHttpVersion(HttpClient.Version version) {
        this.httpVersion = new HttpVersion(version);
    }

    public  void setHttpVersion(ProtocolVersion version) {
        this.httpVersion = new HttpVersion(version);
    }

    public  void setHttpVersion(String version) {
        this.httpVersion = new HttpVersion(version);
    }

    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    public  void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Status getStatus() {
        return this.status;
    }

    public  void setStatus(Status status) {
        this.status = status;
    }

    public String getErrors() {
        return this.errors;
    }

    public  void setErrors(String errors) {
        this.errors = errors;
    }

    public URI getUri() {
        return this.uri;
    }

    public  void setUri(URI uri) {
        this.uri = uri;
    }

    public String getUriString() {
        return this.uri.toString();
    }

    public  void setUriString(String uriString) {
        this.uri = URI.create(uriString);
    }
}
