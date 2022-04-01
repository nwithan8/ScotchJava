package com.easypost.easyvcr.requestelements;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

public class Request extends HttpElement {

    private String body;

    private String method;

    private Map<String, List<String>> headers;

    private URI uri;

    public String getBody() {
        return body != null ? body : "";
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers.map();
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
}
