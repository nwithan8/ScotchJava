package com.easypost.easyvcr.clients.httpclient;

import com.easypost.easyvcr.internalutilities.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpRequest;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;

public class RecordableHttpRequestImpl extends RecordableHttpRequest implements RecordableWebSocketRequest {

    private final HttpHeaders userHeaders;
    private final RecordableHttpHeadersBuilder systemHeadersBuilder;
    private final URI uri;
    private volatile Proxy proxy; // ensure safe publishing
    private final InetSocketAddress authority; // only used when URI not specified
    private final String method;
    final HttpRequest.BodyPublisher requestPublisher;
    final boolean secure;
    final boolean expectContinue;
    private volatile boolean isWebSocket;
    @SuppressWarnings("removal")
    private volatile AccessControlContext acc;
    private final Duration timeout;  // may be null
    private final Optional<HttpClient.Version> version;

    private static String userAgent() {
        PrivilegedAction<String> pa = () -> System.getProperty("java.version");
        @SuppressWarnings("removal")
        String version = AccessController.doPrivileged(pa);
        return "Java-http-client/" + version;
    }

    /** The value of the User-Agent header for all requests sent by the client. */
    public static final String USER_AGENT = userAgent();

    /**
     * Creates an HttpRequestImpl from the given builder.
     */
    public RecordableHttpRequestImpl(RecordableHttpRequestBuilderImpl builder) {
        String method = builder.method();
        this.method = method == null ? "GET" : method;
        this.userHeaders = HttpHeaders.of(builder.headersBuilder().map(), Utils.ALLOWED_HEADERS);
        this.systemHeadersBuilder = new RecordableHttpHeadersBuilder();
        this.uri = builder.uri();
        assert uri != null;
        this.proxy = null;
        this.expectContinue = builder.expectContinue();
        this.secure = uri.getScheme().toLowerCase(Locale.US).equals("https");
        this.requestPublisher = builder.bodyPublisher();  // may be null
        this.timeout = builder.timeout();
        this.version = builder.version();
        this.authority = null;
    }

    /**
     * Creates a RecordableHttpRequestImpl from the given request.
     */
    public RecordableHttpRequestImpl(RecordableHttpRequest request, ProxySelector ps) {
        String method = request.method();
        if (method != null && !Utils.isValidName(method))
            throw new IllegalArgumentException("illegal method \""
                    + method.replace("\n","\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t")
                    + "\"");
        URI requestURI = Objects.requireNonNull(request.uri(),
                "uri must be non null");
        Duration timeout = request.timeout().orElse(null);
        this.method = method == null ? "GET" : method;
        this.userHeaders = HttpHeaders.of(request.headers().map(), Utils.VALIDATE_USER_HEADER);
        if (request instanceof RecordableHttpRequestImpl) {
            // all cases exception WebSocket should have a new system headers
            this.isWebSocket = ((RecordableHttpRequestImpl) request).isWebSocket;
            if (isWebSocket) {
                this.systemHeadersBuilder = ((RecordableHttpRequestImpl)request).systemHeadersBuilder;
            } else {
                this.systemHeadersBuilder = new RecordableHttpHeadersBuilder();
            }
        } else {
            RecordableHttpRequestBuilderImpl.checkURI(requestURI);
            checkTimeout(timeout);
            this.systemHeadersBuilder = new RecordableHttpHeadersBuilder();
        }
        if (!userHeaders.firstValue("User-Agent").isPresent()) {
            this.systemHeadersBuilder.setHeader("User-Agent", USER_AGENT);
        }
        this.uri = requestURI;
        if (isWebSocket) {
            // WebSocket determines and sets the proxy itself
            this.proxy = ((RecordableHttpRequestImpl) request).proxy;
        } else {
            if (ps != null)
                this.proxy = retrieveProxy(ps, uri);
            else
                this.proxy = null;
        }
        this.expectContinue = request.expectContinue();
        this.secure = uri.getScheme().toLowerCase(Locale.US).equals("https");
        this.requestPublisher = request.bodyPublisher().orElse(null);
        this.timeout = timeout;
        this.version = request.version();
        this.authority = null;
    }

    private static void checkTimeout(Duration duration) {
        if (duration != null) {
            if (duration.isNegative() || Duration.ZERO.equals(duration))
                throw new IllegalArgumentException("Invalid duration: " + duration);
        }
    }

    /** Returns a new instance suitable for redirection. */
    public static RecordableHttpRequestImpl newInstanceForRedirection(URI uri,
                                                                      String method,
                                                                      RecordableHttpRequestImpl other,
                                                                      boolean mayHaveBody) {
        return new RecordableHttpRequestImpl(uri, method, other, mayHaveBody);
    }

    /** Returns a new instance suitable for authentication. */
    public static RecordableHttpRequestImpl newInstanceForAuthentication(
            RecordableHttpRequestImpl other) {
        RecordableHttpRequestImpl
                request = new RecordableHttpRequestImpl(other.uri(), other.method(), other, true);
        if (request.isWebSocket()) {
            Utils.setWebSocketUpgradeHeaders(request);
        }
        return request;
    }

    /**
     * Creates a RecordableHttpRequestImpl using fields of an existing request impl.
     * The newly created RecordableHttpRequestImpl does not copy the system headers.
     */
    private RecordableHttpRequestImpl(URI uri,
                                      String method,
                                      RecordableHttpRequestImpl other,
                                      boolean mayHaveBody) {
        assert method == null || Utils.isValidName(method);
        this.method = method == null? "GET" : method;
        this.userHeaders = other.userHeaders;
        this.isWebSocket = other.isWebSocket;
        this.systemHeadersBuilder = new RecordableHttpHeadersBuilder();
        if (!userHeaders.firstValue("User-Agent").isPresent()) {
            this.systemHeadersBuilder.setHeader("User-Agent", USER_AGENT);
        }
        this.uri = uri;
        this.proxy = other.proxy;
        this.expectContinue = other.expectContinue;
        this.secure = uri.getScheme().toLowerCase(Locale.US).equals("https");
        this.requestPublisher = mayHaveBody ? publisher(other) : null; // may be null
        this.acc = other.acc;
        this.timeout = other.timeout;
        this.version = other.version();
        this.authority = null;
    }

    private HttpRequest.BodyPublisher publisher(RecordableHttpRequestImpl other) {
        HttpRequest.BodyPublisher res = other.requestPublisher;
        if (!Objects.equals(method, other.method)) {
            res = null;
        }
        return res;
    }

    /* used for creating CONNECT requests  */
    RecordableHttpRequestImpl(String method, InetSocketAddress authority, Utils.ProxyHeaders headers) {
        // TODO: isWebSocket flag is not specified, but the assumption is that
        // such a request will never be made on a connection that will be returned
        // to the connection pool (we might need to revisit this constructor later)
        assert "CONNECT".equalsIgnoreCase(method);
        this.method = method;
        this.systemHeadersBuilder = new RecordableHttpHeadersBuilder();
        this.systemHeadersBuilder.map().putAll(headers.systemHeaders().map());
        this.userHeaders = headers.userHeaders();
        this.uri = URI.create("socket://" + authority.getHostString() + ":"
                + Integer.toString(authority.getPort()) + "/");
        this.proxy = null;
        this.requestPublisher = null;
        this.authority = authority;
        this.secure = false;
        this.expectContinue = false;
        this.timeout = null;
        // The CONNECT request sent for tunneling is only used in two cases:
        //   1. websocket, which only supports HTTP/1.1
        //   2. SSL tunneling through a HTTP/1.1 proxy
        // In either case we do not want to upgrade the connection to the proxy.
        // What we want to possibly upgrade is the tunneled connection to the
        // target server (so not the CONNECT request itself)
        this.version = Optional.of(HttpClient.Version.HTTP_1_1);
    }

    final boolean isConnect() {
        return "CONNECT".equalsIgnoreCase(method);
    }

    /**
     * Creates a RecordableHttpRequestImpl from the given set of Headers and the associated
     * "parent" request. Fields not taken from the headers are taken from the
     * parent.
     */
    static RecordableHttpRequestImpl createPushRequest(RecordableHttpRequestImpl parent,
                                                       HttpHeaders headers)
            throws IOException
    {
        return new RecordableHttpRequestImpl(parent, headers);
    }

    // only used for push requests
    private RecordableHttpRequestImpl(RecordableHttpRequestImpl parent, HttpHeaders headers)
            throws IOException
    {
        this.method = headers.firstValue(":method")
                .orElseThrow(() -> new IOException("No method in Push Promise"));
        String path = headers.firstValue(":path")
                .orElseThrow(() -> new IOException("No path in Push Promise"));
        String scheme = headers.firstValue(":scheme")
                .orElseThrow(() -> new IOException("No scheme in Push Promise"));
        String authority = headers.firstValue(":authority")
                .orElseThrow(() -> new IOException("No authority in Push Promise"));
        StringBuilder sb = new StringBuilder();
        sb.append(scheme).append("://").append(authority).append(path);
        this.uri = URI.create(sb.toString());
        this.proxy = null;
        this.userHeaders = HttpHeaders.of(headers.map(), Utils.ALLOWED_HEADERS);
        this.systemHeadersBuilder = parent.systemHeadersBuilder;
        this.expectContinue = parent.expectContinue;
        this.secure = parent.secure;
        this.requestPublisher = parent.requestPublisher;
        this.acc = parent.acc;
        this.timeout = parent.timeout;
        this.version = parent.version;
        this.authority = null;
    }

    @Override
    public String toString() {
        return (uri == null ? "" : uri.toString()) + " " + method;
    }

    @Override
    public HttpHeaders headers() {
        return userHeaders;
    }

    InetSocketAddress authority() { return authority; }

    @Override
    public boolean expectContinue() { return expectContinue; }

    /** Retrieves the proxy, from the given ProxySelector, if there is one. */
    private static Proxy retrieveProxy(ProxySelector ps, URI uri) {
        Proxy proxy = null;
        List<Proxy> pl = ps.select(uri);
        if (!pl.isEmpty()) {
            Proxy p = pl.get(0);
            if (p.type() == Proxy.Type.HTTP)
                proxy = p;
        }
        return proxy;
    }

    InetSocketAddress proxy() {
        if (proxy == null || proxy.type() != Proxy.Type.HTTP
                || method.equalsIgnoreCase("CONNECT")) {
            return null;
        }
        return (InetSocketAddress)proxy.address();
    }

    boolean secure() { return secure; }

    @Override
    public void setProxy(Proxy proxy) {
        assert isWebSocket;
        this.proxy = proxy;
    }

    @Override
    public void isWebSocket(boolean is) {
        isWebSocket = is;
    }

    boolean isWebSocket() {
        return isWebSocket;
    }

    @Override
    public Optional<HttpRequest.BodyPublisher> bodyPublisher() {
        return requestPublisher == null ? Optional.empty()
                : Optional.of(requestPublisher);
    }

    /**
     * Returns the request method for this request. If not set explicitly,
     * the default method for any request is "GET".
     */
    @Override
    public String method() { return method; }

    @Override
    public URI uri() { return uri; }

    @Override
    public Optional<Duration> timeout() {
        return timeout == null ? Optional.empty() : Optional.of(timeout);
    }

    HttpHeaders getUserHeaders() { return userHeaders; }

    RecordableHttpHeadersBuilder getSystemHeadersBuilder() { return systemHeadersBuilder; }

    @Override
    public Optional<HttpClient.Version> version() { return version; }

    void addSystemHeader(String name, String value) {
        systemHeadersBuilder.addHeader(name, value);
    }

    @Override
    public void setSystemHeader(String name, String value) {
        systemHeadersBuilder.setHeader(name, value);
    }

    @SuppressWarnings("removal")
    InetSocketAddress getAddress() {
        URI uri = uri();
        if (uri == null) {
            return authority();
        }
        int p = uri.getPort();
        if (p == -1) {
            if (uri.getScheme().equalsIgnoreCase("https")) {
                p = 443;
            } else {
                p = 80;
            }
        }
        final String host = uri.getHost();
        final int port = p;
        if (proxy() == null) {
            PrivilegedAction<InetSocketAddress> pa = () -> new InetSocketAddress(host, port);
            return AccessController.doPrivileged(pa);
        } else {
            return InetSocketAddress.createUnresolved(host, port);
        }
    }
}

