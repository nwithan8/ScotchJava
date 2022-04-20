package com.easypost.easyvcr.requestelements;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.easypost.easyvcr.internalutilities.Tools.createInputStream;

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
                // not implemented
            }

            @Override
            public StatusLine getStatusLine() {
                // not implemented
                return new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        return Response.this.httpVersion.asProtocolVersion();
                    }

                    @Override
                    public int getStatusCode() {
                        return Response.this.status.getCode();
                    }

                    @Override
                    public String getReasonPhrase() {
                        return Response.this.status.getMessage();
                    }
                };
            }

            @Override
            public void setStatusLine(StatusLine statusLine) {
                // not implemented
            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i) {
                // not implemented
            }

            @Override
            public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {
                // not implemented
            }

            @Override
            public HttpEntity getEntity() {
                return new HttpEntity() {
                    @Override
                    public boolean isRepeatable() {
                        return false;
                    }

                    @Override
                    public boolean isChunked() {
                        return false;
                    }

                    @Override
                    public long getContentLength() {
                        return Response.this.body.length();
                    }

                    @Override
                    public Header getContentType() {
                        // TODO: May be accidentally recursive
                        return Response.this.toCloseableHttpResponse().getFirstHeader("Content-Type");
                    }

                    @Override
                    public Header getContentEncoding() {
                        // TODO: May be accidentally recursive
                        return Response.this.toCloseableHttpResponse().getFirstHeader("Content-Encoding");
                    }

                    @Override
                    public InputStream getContent() throws IOException, UnsupportedOperationException {
                        return createInputStream(Response.this.body);
                    }

                    @Override
                    public void writeTo(OutputStream outputStream) throws IOException {
                        // not implemented
                    }

                    @Override
                    public boolean isStreaming() {
                        return false;
                    }

                    @Override
                    public void consumeContent() throws IOException {
                        // not implemented
                    }
                };
            }            @Override
            public void setStatusCode(int i) throws IllegalStateException {
                // not implemented
            }

            @Override
            public void setEntity(HttpEntity httpEntity) {
                // not implemented
            }            @Override
            public void setReasonPhrase(String s) throws IllegalStateException {
                // not implemented
            }

            @Override
            public Locale getLocale() {
                // not implemented
                return null;
            }

            @Override
            public void setLocale(Locale locale) {
                // not implemented
            }

            @Override
            public ProtocolVersion getProtocolVersion() {
                return Response.this.getHttpVersion().asProtocolVersion();
            }

            @Override
            public boolean containsHeader(String s) {
                return Response.this.getHeaders().containsKey(s);
            }

            @Override
            public Header[] getHeaders(String s) {
                List<String> matchingHeaderValues = Response.this.getHeaders().get(s);
                if (matchingHeaderValues == null) {
                    return null;
                }
                Header[] headers = new Header[matchingHeaderValues.size()];
                for (int i = 0; i < matchingHeaderValues.size(); i++) {
                    headers[i] = new BasicHeader(s, matchingHeaderValues.get(i));
                }
                return headers;
            }

            @Override
            public Header getFirstHeader(String s) {
                Header[] headers = getHeaders(s);
                if (headers == null || headers.length == 0) {
                    return null;
                }
                return headers[0];
            }

            @Override
            public Header getLastHeader(String s) {
                Header[] headers = getHeaders(s);
                if (headers == null || headers.length == 0) {
                    return null;
                }
                return headers[headers.length - 1];
            }

            @Override
            public Header[] getAllHeaders() {
                Map<String, List<String>> headerMap = Response.this.getHeaders();
                if (headerMap == null) {
                    return null;
                }
                List<Header> headers = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
                    for (String value : entry.getValue()) {
                        headers.add(new BasicHeader(entry.getKey(), value));
                    }
                }
                return headers.toArray(new Header[headers.size()]);
            }

            @Override
            public void addHeader(Header header) {
                // not implemented
            }

            @Override
            public void addHeader(String s, String s1) {
                // not implemented
            }

            @Override
            public void setHeader(Header header) {
                // not implemented
            }

            @Override
            public void setHeader(String s, String s1) {
                // not implemented
            }

            @Override
            public void setHeaders(Header[] headers) {
                // not implemented
            }

            @Override
            public void removeHeader(Header header) {
                // not implemented
            }

            @Override
            public void removeHeaders(String s) {
                // not implemented
            }

            @Override
            public HeaderIterator headerIterator() {
                // not implemented
                return null;
            }

            @Override
            public HeaderIterator headerIterator(String s) {
                // not implemented
                return null;
            }

            @Override
            public HttpParams getParams() {
                // not implemented
                return null;
            }

            @Override
            public void setParams(HttpParams httpParams) {
                // not implemented
            }




        };
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public HttpVersion getHttpVersion() {
        return this.httpVersion;
    }

    public void setHttpVersion(HttpClient.Version version) {
        this.httpVersion = new HttpVersion(version);
    }

    public void setHttpVersion(ProtocolVersion version) {
        this.httpVersion = new HttpVersion(version);
    }

    public void setHttpVersion(String version) {
        this.httpVersion = new HttpVersion(version);
    }

    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrors() {
        return this.errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
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
