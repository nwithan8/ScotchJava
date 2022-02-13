package com.easypost.scotch.interaction;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class Request {

    private String method;

    private URI uri;

    private Map<String, List<String>> headers;

    private String body;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public URI getUri() {
        return this.uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getUriString() {
        return this.uri.toString();
    }

    public void setUriString(String uriString) {
        this.uri = URI.create(uriString);
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body != null ? body : "";
    }

    public void setBody(String body) {
        this.body = body;
    }
}
