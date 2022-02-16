package com.easypost.scotch.clients.httpurlconnection;

import com.easypost.scotch.VCR;
import com.easypost.scotch.interaction.Helpers;
import com.easypost.scotch.interaction.HttpInteraction;
import com.easypost.scotch.interaction.Request;
import com.easypost.scotch.interaction.Response;
import sun.net.www.protocol.http.Handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownServiceException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class VCRHttpUrlConnection extends sun.net.www.protocol.http.HttpURLConnection {

    private final VCR vcr;
    private HttpInteraction cachedInteraction;
    private String body;
    private String queryString;

    public VCRHttpUrlConnection(URL url, VCR vcr) throws IOException {
        super(url, new Handler());
        this.vcr = vcr;
        this.cachedInteraction = new HttpInteraction(new Request(), new Response());
        this.body = null;
        this.queryString = null;
    }

    public VCRHttpUrlConnection(URL url, VCR vcr, Proxy proxy) throws IOException {
        super(url, proxy);
        this.vcr = vcr;
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
            String tempUrlWithParams = super.getURL().toURI().toString();
            if (queryString != null) {
                tempUrlWithParams += "?" + queryString;
            }
            request.setUriString(tempUrlWithParams);
            request.setBody(body);
            request.setMethod(super.getRequestMethod());
        } catch (URISyntaxException ignored) {
        }

        return request;
    }

    private Response createResponse() {
        Response response = new Response();
        try {
            // response.setHeaders(super.getHeaderFields());
            if (!connected) {
                super.connect();
            }
            response.setStatusCode(super.getResponseCode());
            response.setMessage(super.getResponseMessage());
            response.setUri(super.getURL().toURI());
            response.setHeaders(super.getHeaderFields());
            response.setBody(Helpers.readBodyFromInputStream(super.getInputStream()));
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
        this.vcr.tapeOverExistingInteraction(this.cachedInteraction);
    }

    private boolean loadMatchingInteraction() {
        Request request = createRequest();
        this.cachedInteraction = this.vcr.seekMatchingInteraction(request);
        return this.cachedInteraction != null;
    }

    public void addQueryParameters(Map<String, String> parameters) throws IOException {
        // please use this way to add parameters so we can capture it
        this.queryString = getParamsString(parameters);

        this.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(getOutputStream());
        out.writeBytes(this.queryString);

        if (this.vcr.inRecordMode()) {
            recordInteraction();
        }
    }

    public void addBody(String body) throws IOException {
        // please use this way to add a body so we can capture it
        this.body = body;

        this.setDoOutput(true);
        try (OutputStream os = getOutputStream()) {
            byte[] input = this.body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (this.vcr.inRecordMode()) {
            recordInteraction();
        }
    }

    private String readResponseBody() {
        String body = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getInputStream()));
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

    @Override
    public void connect() throws IOException {
        super.connect();
        // might as well load the cassette if we're replaying, or save if we're recording
        if (this.vcr.inRecordMode()) {
            recordInteraction();
        } else if (this.vcr.inPlaybackMode()) {
            loadMatchingInteraction();
        }
    }

    @Override
    public void disconnect() {
        // might as well record if we're connecting
        super.disconnect();
        recordInteraction();
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
    @Override
    public String getHeaderFieldKey(int n) {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getHeaders().keySet().toArray()[n].toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getHeaderFieldKey(n);
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
    @Override
    public String getHeaderField(int n) {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getHeaders().values().toArray()[n].toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getHeaderField(n);
    }

    /**
     * Get the request method.
     *
     * @return the HTTP request method
     * @see #setRequestMethod(java.lang.String)
     */
    @Override
    public String getRequestMethod() {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getRequest().getMethod();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getRequestMethod();
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
    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        super.setRequestMethod(method);
        if (this.vcr.inRecordMode()) {
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
    @Override
    public int getResponseCode() throws IOException {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getStatusCode();
                } catch (Exception ignored) {
                    return -1;
                }
            }
            return -1;
        }
        return super.getResponseCode();
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
    @Override
    public String getResponseMessage() throws IOException {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getMessage();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getResponseMessage();
    }

    /**
     * Returns the value of this {@code URLConnection}'s {@code URL}
     * field.
     *
     * @return the value of this {@code URLConnection}'s {@code URL}
     * field.
     */
    @Override
    public URL getURL() {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getUri().toURL();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getURL();
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
    @Override
    public String getHeaderField(String name) {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getHeaders().get(name).toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getHeaderField(name);
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
    @Override
    public Map<String, List<String>> getHeaderFields() {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getHeaders();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getHeaderFields();
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
    @Override
    public Object getContent() throws IOException {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getResponse().getBody();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getContent();
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
    @Override
    public void setRequestProperty(String key, String value) {
        super.setRequestProperty(key, value);
        if (this.vcr.inRecordMode()) {
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
    @Override
    public void addRequestProperty(String key, String value) {
        super.addRequestProperty(key, value);
        if (this.vcr.inRecordMode()) {
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
    @Override
    public String getRequestProperty(String key) {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getRequest().getHeaders().get(key).toString();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getRequestProperty(key);
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
    @Override
    public Map<String, List<String>> getRequestProperties() {
        if (this.vcr.inPlaybackMode()) {
            if (loadMatchingInteraction()) {
                try {
                    return this.cachedInteraction.getRequest().getHeaders();
                } catch (Exception ignored) {
                    return null;
                }
            }
            return null;
        }
        return super.getRequestProperties();
    }
}
