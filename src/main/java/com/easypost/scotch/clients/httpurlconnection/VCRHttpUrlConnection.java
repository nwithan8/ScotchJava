package com.easypost.scotch.clients.httpurlconnection;

import com.easypost.scotch.ScotchMode;
import com.easypost.scotch.cassettes.Cassette;
import com.easypost.scotch.interaction.HttpInteraction;
import com.easypost.scotch.interaction.Request;
import com.easypost.scotch.interaction.Response;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketPermission;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownServiceException;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.List;
import java.util.Map;

public class VCRHttpUrlConnection {

    private final HttpURLConnection connection;
    private final String cassettePath;
    private final ScotchMode mode;
    private HttpInteraction cachedInteraction;

    private String body;
    private String queryString;

    public VCRHttpUrlConnection(HttpURLConnection connection, String cassettePath, ScotchMode mode) {
        this.connection = connection;
        this.cassettePath = cassettePath;
        this.mode = mode;
        this.cachedInteraction = new HttpInteraction(new Request(), new Response());
        this.body = null;
        this.queryString = null;
    }

    private static String getParamsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }

    private Request createRequest() {
        Request request = new Request();
        try {
            String tempUrlWithParams = this.connection.getURL().toURI().toString();
            if (queryString != null) {
                tempUrlWithParams += "?" + queryString;
            }
            request.setUriString(tempUrlWithParams);
            request.setBody(body);
            request.setMethod(this.connection.getRequestMethod());
        } catch (URISyntaxException ignored) {
        }

        return request;
    }

    private Response createResponse() {
        Response response = new Response();
        try {
            response.setStatusCode(this.connection.getResponseCode());
            response.setMessage(this.connection.getResponseMessage());
            response.setUri(this.connection.getURL().toURI());
            response.setBody(readResponseBody());
            response.setHeaders(this.connection.getHeaderFields());
        } catch (URISyntaxException | IOException ignored) {
        }

        return response;
    }

    private void recordInteraction() {
        // record or re-record the interaction
        // important to call directly on connection, rather than this.function() to avoid potential recursion
        Request request = createRequest();
        Response response = createResponse();

        this.cachedInteraction = new HttpInteraction(request, response);
        Cassette.updateInteraction(this.cassettePath, this.cachedInteraction);
    }

    private boolean loadMatchingInteraction() {
        Request request = createRequest();
        this.cachedInteraction = Cassette.findInteractionMatchingRequest(this.cassettePath, request);
        return this.cachedInteraction != null;
    }

    public void addQueryParameters(Map<String, String> parameters) throws IOException {
        // please use this way to add parameters so we can capture it
        this.queryString = getParamsString(parameters);

        this.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(this.connection.getOutputStream());
        out.writeBytes(this.queryString);

        if (this.mode == ScotchMode.Recording) {
            recordInteraction();
        }
    }

    public void addBody(String body) throws IOException {
        // please use this way to add a body so we can capture it
        this.body = body;

        this.setDoOutput(true);
        try (OutputStream os = this.connection.getOutputStream()) {
            byte[] input = this.body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (this.mode == ScotchMode.Recording) {
            recordInteraction();
        }
    }

    private String readResponseBody() {
        String body = null;
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(this.connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            body = content.toString();
        } catch (IOException ignored) {
        }
        return body;
    }

    public void connect() throws IOException {
        // might as well load the cassette if we're replaying, or save if we're recording
        this.connection.connect();
        if (this.mode == ScotchMode.Recording) {
            recordInteraction();
        } else if (this.mode == ScotchMode.Replaying) {
            loadMatchingInteraction();
        }
    }

    public void disconnect() {
        // might as well record if we're connecting
        this.connection.disconnect();
        recordInteraction();
    }

    public boolean usingProxy() {
        return this.connection.usingProxy();
    }

    /**
     * Supplies an {@link java.net.Authenticator Authenticator} to be used
     * when authentication is requested through the HTTP protocol for
     * this {@code VCRHttpUrlConnection}.
     * If no authenticator is supplied, the
     * {@linkplain Authenticator#setDefault(java.net.Authenticator) default
     * authenticator} will be used.
     *
     * @param auth The {@code Authenticator} that should be used by this
     *             {@code VCRHttpUrlConnection}.
     * @throws UnsupportedOperationException if setting an Authenticator is
     *                                       not supported by the underlying implementation.
     * @throws IllegalStateException         if URLConnection is already connected.
     * @throws NullPointerException          if the supplied {@code auth} is {@code null}.
     * @implSpec The default behavior of this method is to unconditionally
     * throw {@link UnsupportedOperationException}. Concrete
     * implementations of {@code VCRHttpUrlConnection}
     * which support supplying an {@code Authenticator} for a
     * specific {@code VCRHttpUrlConnection} instance should
     * override this method to implement a different behavior.
     * @implNote Depending on authentication schemes, an implementation
     * may or may not need to use the provided authenticator
     * to obtain a password. For instance, an implementation that
     * relies on third-party security libraries may still invoke the
     * default authenticator if these libraries are configured
     * to do so.
     * Likewise, an implementation that supports transparent
     * NTLM authentication may let the system attempt
     * to connect using the system user credentials first,
     * before invoking the provided authenticator.
     * <br>
     * However, if an authenticator is specifically provided,
     * then the underlying connection may only be reused for
     * {@code VCRHttpUrlConnection} instances which share the same
     * {@code Authenticator} instance, and authentication information,
     * if cached, may only be reused for an {@code VCRHttpUrlConnection}
     * sharing that same {@code Authenticator}.
     * @since 9
     */
    public void setAuthenticator(Authenticator auth) {
        // ignore for cassette
        this.connection.setAuthenticator(auth);
    }

    /**
     * Returns the key for the {@code n}<sup>th</sup> header field.
     * Some implementations may treat the {@code 0}<sup>th</sup>
     * header field as special, i.e. as the status line returned by the HTTP
     * server. In this case, {@link #getHeaderField(int) getHeaderField(0)} returns the status
     * line, but {@code getHeaderFieldKey(0)} returns null.
     *
     * @param n an index, where {@code n >=0}.
     * @return the key for the {@code n}<sup>th</sup> header field,
     * or {@code null} if the key does not exist.
     */
    public String getHeaderFieldKey(int n) {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getHeaders().keySet().toArray()[n].toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getHeaderFieldKey(n);
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is known in
     * advance.
     * <p>
     * An exception will be thrown if the application
     * attempts to write more data than the indicated
     * content-length, or if the application closes the OutputStream
     * before writing the indicated amount.
     * <p>
     * When output streaming is enabled, authentication
     * and redirection cannot be handled automatically.
     * A HttpRetryException will be thrown when reading
     * the response if authentication or redirection are required.
     * This exception can be queried for the details of the error.
     * <p>
     * This method must be called before the URLConnection is connected.
     * <p>
     * <B>NOTE:</B> {@link #setFixedLengthStreamingMode(long)} is recommended
     * instead of this method as it allows larger content lengths to be set.
     *
     * @param contentLength The number of bytes which will be written
     *                      to the OutputStream.
     * @throws IllegalStateException    if URLConnection is already connected
     *                                  or if a different streaming mode is already enabled.
     * @throws IllegalArgumentException if a content length less than
     *                                  zero is specified.
     * @see #setChunkedStreamingMode(int)
     * @since 1.5
     */
    public void setFixedLengthStreamingMode(int contentLength) {
        // ignore for cassette
        this.connection.setFixedLengthStreamingMode(contentLength);
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is known in
     * advance.
     *
     * <P> An exception will be thrown if the application attempts to write
     * more data than the indicated content-length, or if the application
     * closes the OutputStream before writing the indicated amount.
     *
     * <P> When output streaming is enabled, authentication and redirection
     * cannot be handled automatically. A {@linkplain HttpRetryException} will
     * be thrown when reading the response if authentication or redirection
     * are required. This exception can be queried for the details of the
     * error.
     *
     * <P> This method must be called before the URLConnection is connected.
     *
     * <P> The content length set by invoking this method takes precedence
     * over any value set by {@link #setFixedLengthStreamingMode(int)}.
     *
     * @param contentLength The number of bytes which will be written to the OutputStream.
     * @throws IllegalStateException    if URLConnection is already connected or if a different
     *                                  streaming mode is already enabled.
     * @throws IllegalArgumentException if a content length less than zero is specified.
     * @since 1.7
     */
    public void setFixedLengthStreamingMode(long contentLength) {
        // ignore for cassette
        this.connection.setFixedLengthStreamingMode(contentLength);
    }

    /**
     * This method is used to enable streaming of a HTTP request body
     * without internal buffering, when the content length is <b>not</b>
     * known in advance. In this mode, chunked transfer encoding
     * is used to send the request body. Note, not all HTTP servers
     * support this mode.
     * <p>
     * When output streaming is enabled, authentication
     * and redirection cannot be handled automatically.
     * A HttpRetryException will be thrown when reading
     * the response if authentication or redirection are required.
     * This exception can be queried for the details of the error.
     * <p>
     * This method must be called before the URLConnection is connected.
     *
     * @param chunklen The number of bytes to write in each chunk.
     *                 If chunklen is less than or equal to zero, a default
     *                 value will be used.
     * @throws IllegalStateException if URLConnection is already connected
     *                               or if a different streaming mode is already enabled.
     * @see #setFixedLengthStreamingMode(int)
     * @since 1.5
     */
    public void setChunkedStreamingMode(int chunklen) {
        // ignore for cassette
        this.connection.setChunkedStreamingMode(chunklen);
    }

    /**
     * Returns the value for the {@code n}<sup>th</sup> header field.
     * Some implementations may treat the {@code 0}<sup>th</sup>
     * header field as special, i.e. as the status line returned by the HTTP
     * server.
     * <p>
     * This method can be used in conjunction with the
     * {@link #getHeaderFieldKey getHeaderFieldKey} method to iterate through all
     * the headers in the message.
     *
     * @param n an index, where {@code n>=0}.
     * @return the value of the {@code n}<sup>th</sup> header field,
     * or {@code null} if the value does not exist.
     * @see java.net.HttpURLConnection#getHeaderFieldKey(int)
     */
    public String getHeaderField(int n) {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getHeaders().values().toArray()[n].toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getHeaderField(n);
    }

    /**
     * Returns the value of this {@code VCRHttpUrlConnection}'s
     * {@code instanceFollowRedirects} field.
     *
     * @return the value of this {@code VCRHttpUrlConnection}'s
     * {@code instanceFollowRedirects} field.
     * @see #setInstanceFollowRedirects(boolean)
     * @since 1.3
     */
    public boolean getInstanceFollowRedirects() {
        // ignore for cassette
        return this.connection.getInstanceFollowRedirects();
    }

    /**
     * Sets whether HTTP redirects (requests with response code 3xx) should
     * be automatically followed by this {@code VCRHttpUrlConnection}
     * instance.
     * <p>
     * The default value comes from followRedirects, which defaults to
     * true.
     *
     * @param followRedirects a {@code boolean} indicating
     *                        whether or not to follow HTTP redirects.
     * @see #getInstanceFollowRedirects
     * @since 1.3
     */
    public void setInstanceFollowRedirects(boolean followRedirects) {
        // ignore for cassette
        this.connection.setInstanceFollowRedirects(followRedirects);
    }

    /**
     * Get the request method.
     *
     * @return the HTTP request method
     * @see #setRequestMethod(java.lang.String)
     */
    public String getRequestMethod() {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getRequest().getMethod();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getRequestMethod();
    }

    /**
     * Set the method for the URL request, one of:
     * <UL>
     * <LI>GET
     * <LI>POST
     * <LI>HEAD
     * <LI>OPTIONS
     * <LI>PUT
     * <LI>DELETE
     * <LI>TRACE
     * </UL> are legal, subject to protocol restrictions.  The default
     * method is GET.
     *
     * @param method the HTTP method
     * @throws ProtocolException if the method cannot be reset or if
     *                           the requested method isn't valid for HTTP.
     * @throws SecurityException if a security manager is set and the
     *                           method is "TRACE", but the "allowHttpTrace"
     *                           NetPermission is not granted.
     * @see #getRequestMethod()
     */
    public void setRequestMethod(String method) throws ProtocolException {
        this.connection.setRequestMethod(method);
        if (this.mode == ScotchMode.Recording) {
            recordInteraction();
        }
    }

    /**
     * Gets the status code from an HTTP response message.
     * For example, in the case of the following status lines:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 401 Unauthorized
     * </PRE>
     * It will return 200 and 401 respectively.
     * Returns -1 if no code can be discerned
     * from the response (i.e., the response is not valid HTTP).
     *
     * @return the HTTP Status-Code, or -1
     * @throws IOException if an error occurred connecting to the server.
     */
    public int getResponseCode() throws IOException {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getStatusCode();
                } catch (Exception ignored) {
                    return -1;
                }
            }
            return -1;
        }
        return this.connection.getResponseCode();
    }

    /**
     * Gets the HTTP response message, if any, returned along with the
     * response code from a server.  From responses like:
     * <PRE>
     * HTTP/1.0 200 OK
     * HTTP/1.0 404 Not Found
     * </PRE>
     * Extracts the Strings "OK" and "Not Found" respectively.
     * Returns null if none could be discerned from the responses
     * (the result was not valid HTTP).
     *
     * @return the HTTP response message, or {@code null}
     * @throws IOException if an error occurred connecting to the server.
     */
    public String getResponseMessage() throws IOException {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getMessage();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getResponseMessage();
    }

    /**
     * Returns a {@link SocketPermission} object representing the
     * permission necessary to connect to the destination host and port.
     *
     * @return a {@code SocketPermission} object representing the
     * permission necessary to connect to the destination
     * host and port.
     * @throws IOException if an error occurs while computing
     *                     the permission.
     */
    public Permission getPermission() throws IOException {
        // ignore for cassette
        return this.connection.getPermission();
    }

    /**
     * Returns the error stream if the connection failed
     * but the server sent useful data nonetheless. The
     * typical example is when an HTTP server responds
     * with a 404, which will cause a FileNotFoundException
     * to be thrown in connect, but the server sent an HTML
     * help page with suggestions as to what to do.
     *
     * <p>This method will not cause a connection to be initiated.  If
     * the connection was not connected, or if the server did not have
     * an error while connecting or if the server had an error but
     * no error data was sent, this method will return null. This is
     * the default.
     *
     * @return an error stream if any, null if there have been no
     * errors, the connection is not connected or the server sent no
     * useful data.
     */
    public InputStream getErrorStream() {
        // ignore for cassette
        return this.connection.getErrorStream();
    }

    /**
     * Returns setting for connect timeout.
     * <p>
     * 0 return implies that the option is disabled
     * (i.e., timeout of infinity).
     *
     * @return an {@code int} that indicates the connect timeout
     * value in milliseconds
     * @see #setConnectTimeout(int)
     * @see #connect()
     * @since 1.5
     */
    public int getConnectTimeout() {
        // ignore for cassette
        return this.connection.getConnectTimeout();
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used
     * when opening a communications link to the resource referenced
     * by this URLConnection.  If the timeout expires before the
     * connection can be established, a
     * java.net.SocketTimeoutException is raised. A timeout of zero is
     * interpreted as an infinite timeout.
     *
     * <p> Some non-standard implementation of this method may ignore
     * the specified timeout. To see the connect timeout set, please
     * call getConnectTimeout().
     *
     * @param timeout an {@code int} that specifies the connect
     *                timeout value in milliseconds
     * @throws IllegalArgumentException if the timeout parameter is negative
     * @see #getConnectTimeout()
     * @see #connect()
     * @since 1.5
     */
    public void setConnectTimeout(int timeout) {
        // ignore for cassette
        this.connection.setConnectTimeout(timeout);
    }

    /**
     * Returns setting for read timeout. 0 return implies that the
     * option is disabled (i.e., timeout of infinity).
     *
     * @return an {@code int} that indicates the read timeout
     * value in milliseconds
     * @see #setReadTimeout(int)
     * @see InputStream#read()
     * @since 1.5
     */
    public int getReadTimeout() {
        // ignore for cassette
        return this.connection.getReadTimeout();
    }

    /**
     * Sets the read timeout to a specified timeout, in
     * milliseconds. A non-zero value specifies the timeout when
     * reading from Input stream when a connection is established to a
     * resource. If the timeout expires before there is data available
     * for read, a java.net.SocketTimeoutException is raised. A
     * timeout of zero is interpreted as an infinite timeout.
     *
     * <p> Some non-standard implementation of this method ignores the
     * specified timeout. To see the read timeout set, please call
     * getReadTimeout().
     *
     * @param timeout an {@code int} that specifies the timeout
     *                value to be used in milliseconds
     * @throws IllegalArgumentException if the timeout parameter is negative
     * @see #getReadTimeout()
     * @see InputStream#read()
     * @since 1.5
     */
    public void setReadTimeout(int timeout) {
        // ignore for cassette
        this.connection.setReadTimeout(timeout);
    }

    /**
     * Returns the value of this {@code URLConnection}'s {@code URL}
     * field.
     *
     * @return the value of this {@code URLConnection}'s {@code URL}
     * field.
     */
    public URL getURL() {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getUri().toURL();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getURL();
    }

    /**
     * Returns the value of the {@code content-type} header field.
     *
     * @return the content type of the resource that the URL references,
     * or {@code null} if not known.
     * @see java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public String getContentType() {
        return getHeaderField("content-type");
    }

    /**
     * Returns the value of the {@code content-encoding} header field.
     *
     * @return the content encoding of the resource that the URL references,
     * or {@code null} if not known.
     * @see java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public String getContentEncoding() {
        return getHeaderField("content-encoding");
    }

    /**
     * Returns the value of the {@code expires} header field.
     *
     * @return the expiration date of the resource that this URL references,
     * or 0 if not known. The value is the number of milliseconds since
     * January 1, 1970 GMT.
     * @see java.net.URLConnection#getHeaderField(java.lang.String)
     */
    public long getExpiration() {
        return this.connection.getExpiration();
    }

    /**
     * Returns the value of the named header field.
     * <p>
     * If called on a connection that sets the same header multiple times
     * with possibly different values, only the last value is returned.
     *
     * @param name the name of a header field.
     * @return the value of the named header field, or {@code null}
     * if there is no such field in the header.
     */
    public String getHeaderField(String name) {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getHeaders().get(name).toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getHeaderField(name);
    }

    /**
     * Returns an unmodifiable Map of the header fields.
     * The Map keys are Strings that represent the
     * response-header field names. Each Map value is an
     * unmodifiable List of Strings that represents
     * the corresponding field values.
     *
     * @return a Map of header fields
     * @since 1.4
     */
    public Map<String, List<String>> getHeaderFields() {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getHeaders();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getHeaderFields();
    }

    /**
     * Retrieves the contents of this URL connection.
     * <p>
     * This method first determines the content type of the object by
     * calling the {@code getContentType} method. If this is
     * the first time that the application has seen that specific content
     * type, a content handler for that content type is created.
     * <p> This is done as follows:
     * <ol>
     * <li>If the application has set up a content handler factory instance
     *     using the {@code setContentHandlerFactory} method, the
     *     {@code createContentHandler} method of that instance is called
     *     with the content type as an argument; the result is a content
     *     handler for that content type.
     * <li>If no {@code ContentHandlerFactory} has yet been set up,
     *     or if the factory's {@code createContentHandler} method
     *     returns {@code null}, then the {@linkplain java.util.ServiceLoader
     *     ServiceLoader} mechanism is used to locate {@linkplain
     *     java.net.ContentHandlerFactory ContentHandlerFactory}
     *     implementations using the system class
     *     loader. The order that factories are located is implementation
     *     specific, and an implementation is free to cache the located
     *     factories. A {@linkplain java.util.ServiceConfigurationError
     *     ServiceConfigurationError}, {@code Error} or {@code RuntimeException}
     *     thrown from the {@code createContentHandler}, if encountered, will
     *     be propagated to the calling thread. The {@code
     *     createContentHandler} method of each factory, if instantiated, is
     *     invoked, with the content type, until a factory returns non-null,
     *     or all factories have been exhausted.
     * <li>Failing that, this method tries to load a content handler
     *     class as defined by {@link java.net.ContentHandler ContentHandler}.
     *     If the class does not exist, or is not a subclass of {@code
     *     ContentHandler}, then an {@code UnknownServiceException} is thrown.
     * </ol>
     *
     * @return the object fetched. The {@code instanceof} operator
     * should be used to determine the specific kind of object
     * returned.
     * @throws IOException             if an I/O error occurs while
     *                                 getting the content.
     * @throws UnknownServiceException if the protocol does not support
     *                                 the content type.
     * @see java.net.ContentHandlerFactory#createContentHandler(java.lang.String)
     * @see java.net.URLConnection#getContentType()
     * @see java.net.URLConnection#setContentHandlerFactory(java.net.ContentHandlerFactory)
     */
    public Object getContent() throws IOException {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getBody();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getContent();
    }

    /**
     * Returns a {@code String} representation of this URL connection.
     *
     * @return a string representation of this {@code URLConnection}.
     */
    public String toString() {
        // ignore for cassette
        return this.connection.toString();
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code doInput} flag.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code doInput} flag.
     * @see #setDoInput(boolean)
     */
    public boolean getDoInput() {
        // ignore for cassette
        return this.connection.getDoInput();
    }

    /**
     * Sets the value of the {@code doInput} field for this
     * {@code URLConnection} to the specified value.
     * <p>
     * A URL connection can be used for input and/or output.  Set the doInput
     * flag to true if you intend to use the URL connection for input,
     * false if not.  The default is true.
     *
     * @param doinput the new value.
     * @throws IllegalStateException if already connected
     * @see #getDoInput()
     */
    public void setDoInput(boolean doinput) {
        // ignore for cassette
        this.connection.setDoInput(doinput);
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code doOutput} flag.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code doOutput} flag.
     * @see #setDoOutput(boolean)
     */
    public boolean getDoOutput() {
        // ignore for cassette
        return this.connection.getDoOutput();
    }

    /**
     * Sets the value of the {@code doOutput} field for this
     * {@code URLConnection} to the specified value.
     * <p>
     * A URL connection can be used for input and/or output.  Set the doOutput
     * flag to true if you intend to use the URL connection for output,
     * false if not.  The default is false.
     *
     * @param dooutput the new value.
     * @throws IllegalStateException if already connected
     * @see #getDoOutput()
     */
    public void setDoOutput(boolean dooutput) {
        // ignore for cassette
        if (this.connection.getDoOutput() != dooutput) {
            this.connection.setDoOutput(dooutput);
        }
    }

    /**
     * Returns the value of the {@code allowUserInteraction} field for
     * this object.
     *
     * @return the value of the {@code allowUserInteraction} field for
     * this object.
     * @see #setAllowUserInteraction(boolean)
     */
    public boolean getAllowUserInteraction() {
        // ignore for cassette
        return this.connection.getAllowUserInteraction();
    }

    /**
     * Set the value of the {@code allowUserInteraction} field of
     * this {@code URLConnection}.
     *
     * @param allowuserinteraction the new value.
     * @throws IllegalStateException if already connected
     * @see #getAllowUserInteraction()
     */
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        // ignore for cassette
        this.connection.setAllowUserInteraction(allowuserinteraction);
    }

    /**
     * Returns the value of this {@code URLConnection}'s
     * {@code useCaches} field.
     *
     * @return the value of this {@code URLConnection}'s
     * {@code useCaches} field.
     * @see #setUseCaches(boolean)
     */
    public boolean getUseCaches() {
        // ignore for cassette
        return this.connection.getUseCaches();
    }

    /**
     * Sets the value of the {@code useCaches} field of this
     * {@code URLConnection} to the specified value.
     * <p>
     * Some protocols do caching of documents.  Occasionally, it is important
     * to be able to "tunnel through" and ignore the caches (e.g., the
     * "reload" button in a browser).  If the UseCaches flag on a connection
     * is true, the connection is allowed to use whatever caches it can.
     * If false, caches are to be ignored.
     * The default value comes from defaultUseCaches, which defaults to
     * true.
     *
     * @param usecaches a {@code boolean} indicating whether
     *                  or not to allow caching
     * @throws IllegalStateException if already connected
     * @see #getUseCaches()
     */
    public void setUseCaches(boolean usecaches) {
        // ignore for cassette
        this.connection.setUseCaches(usecaches);
    }

    /**
     * Returns the value of this object's {@code ifModifiedSince} field.
     *
     * @return the value of this object's {@code ifModifiedSince} field.
     * @see #setIfModifiedSince(long)
     */
    public long getIfModifiedSince() {
        // ignore for cassette
        return this.connection.getIfModifiedSince();
    }

    /**
     * Sets the value of the {@code ifModifiedSince} field of
     * this {@code URLConnection} to the specified value.
     *
     * @param ifmodifiedsince the new value.
     * @throws IllegalStateException if already connected
     * @see #getIfModifiedSince()
     */
    public void setIfModifiedSince(long ifmodifiedsince) {
        // ignore for cassette
        this.connection.setIfModifiedSince(ifmodifiedsince);
    }

    /**
     * Returns the default value of a {@code URLConnection}'s
     * {@code useCaches} flag.
     * <p>
     * This default is "sticky", being a part of the static state of all
     * URLConnections.  This flag applies to the next, and all following
     * URLConnections that are created.
     *
     * @return the default value of a {@code URLConnection}'s
     * {@code useCaches} flag.
     * @see #setDefaultUseCaches(boolean)
     */
    public boolean getDefaultUseCaches() {
        // ignore for cassette
        return this.connection.getDefaultUseCaches();
    }

    /**
     * Sets the default value of the {@code useCaches} field to the
     * specified value.
     *
     * @param defaultusecaches the new value.
     * @see #getDefaultUseCaches()
     */
    public void setDefaultUseCaches(boolean defaultusecaches) {
        // ignore for cassette
        this.connection.setDefaultUseCaches(defaultusecaches);
    }

    /**
     * Sets the general request property. If a property with the key already
     * exists, overwrite its value with the new value.
     *
     * <p> NOTE: HTTP requires all request properties which can
     * legally have multiple instances with the same key
     * to use a comma-separated list syntax which enables multiple
     * properties to be appended into a single property.
     *
     * @param key   the keyword by which the request is known
     *              (e.g., "{@code Accept}").
     * @param value the value associated with it.
     * @throws IllegalStateException if already connected
     * @throws NullPointerException  if key is {@code null}
     * @see #getRequestProperty(java.lang.String)
     */
    public void setRequestProperty(String key, String value) {
        this.connection.setRequestProperty(key, value);
        if (this.mode == ScotchMode.Recording) {
            recordInteraction();
        }
    }

    /**
     * Adds a general request property specified by a
     * key-value pair.  This method will not overwrite
     * existing values associated with the same key.
     *
     * @param key   the keyword by which the request is known
     *              (e.g., "{@code Accept}").
     * @param value the value associated with it.
     * @throws IllegalStateException if already connected
     * @throws NullPointerException  if key is null
     * @see #getRequestProperties()
     * @since 1.4
     */
    public void addRequestProperty(String key, String value) {
        this.connection.addRequestProperty(key, value);
        if (this.mode == ScotchMode.Recording) {
            recordInteraction();
        }
    }

    /**
     * Returns the value of the named general request property for this
     * connection.
     *
     * @param key the keyword by which the request is known (e.g., "Accept").
     * @return the value of the named general request property for this
     * connection. If key is null, then null is returned.
     * @throws IllegalStateException if already connected
     * @see #setRequestProperty(java.lang.String, java.lang.String)
     */
    public String getRequestProperty(String key) {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getRequest().getHeaders().get(key).toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getRequestProperty(key);
    }

    /**
     * Returns an unmodifiable Map of general request
     * properties for this connection. The Map keys
     * are Strings that represent the request-header
     * field names. Each Map value is a unmodifiable List
     * of Strings that represents the corresponding
     * field values.
     *
     * @return a Map of the general request properties for this connection.
     * @throws IllegalStateException if already connected
     * @since 1.4
     */
    public Map<String, List<String>> getRequestProperties() {
        if (this.mode == ScotchMode.Replaying) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getRequest().getHeaders();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return this.connection.getRequestProperties();
    }

    public InputStream getInputStream() throws IOException {
        return this.connection.getInputStream();
    }
}
