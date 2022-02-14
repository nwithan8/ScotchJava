package com.easypost.scotch.clients.httpclient;

import com.easypost.scotch.ScotchMode;
import jdk.internal.net.http.HttpRequestBuilderImpl;
import jdk.internal.net.http.RequestPublishers;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public abstract class VCRHttpRequest extends HttpRequest {

    protected VCRHttpRequest() {
    }

    public interface Builder {

        /**
         * Sets this {@code HttpRequest}'s request {@code URI}.
         *
         * @param uri the request URI
         * @return this builder
         * @throws IllegalArgumentException if the {@code URI} scheme is not
         *         supported
         */
        public VCRHttpRequest.Builder uri(URI uri);

        /**
         * Requests the server to acknowledge the request before sending the
         * body. This is disabled by default. If enabled, the server is
         * requested to send an error response or a {@code 100 Continue}
         * response before the client sends the request body. This means the
         * request publisher for the request will not be invoked until this
         * interim response is received.
         *
         * @param enable {@code true} if Expect continue to be sent
         * @return this builder
         */
        public VCRHttpRequest.Builder expectContinue(boolean enable);

        /**
         * Sets the preferred {@link HttpClient.Version} for this request.
         *
         * <p> The corresponding {@link HttpResponse} should be checked for the
         * version that was actually used. If the version is not set in a
         * request, then the version requested will be that of the sending
         * {@link HttpClient}.
         *
         * @param version the HTTP protocol version requested
         * @return this builder
         */
        public VCRHttpRequest.Builder version(HttpClient.Version version);

        /**
         * Adds the given name value pair to the set of headers for this request.
         * The given value is added to the list of values for that name.
         *
         * @implNote An implementation may choose to restrict some header names
         *           or values, as the HTTP Client may determine their value itself.
         *           For example, "Content-Length", which will be determined by
         *           the request Publisher. In such a case, an implementation of
         *           {@code HttpRequest.Builder} may choose to throw an
         *           {@code IllegalArgumentException} if such a header is passed
         *           to the builder.
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         * @throws IllegalArgumentException if the header name or value is not
         *         valid, see <a href="https://tools.ietf.org/html/rfc7230#section-3.2">
         *         RFC 7230 section-3.2</a>, or the header name or value is restricted
         *         by the implementation.
         */
        public VCRHttpRequest.Builder header(String name, String value);

        /**
         * Adds the given name value pairs to the set of headers for this
         * request. The supplied {@code String} instances must alternate as
         * header names and header values.
         * To add several values to the same name then the same name must
         * be supplied with each new value.
         *
         * @param headers the list of name value pairs
         * @return this builder
         * @throws IllegalArgumentException if there are an odd number of
         *         parameters, or if a header name or value is not valid, see
         *         <a href="https://tools.ietf.org/html/rfc7230#section-3.2">
         *         RFC 7230 section-3.2</a>, or a header name or value is
         *         {@linkplain #header(String, String) restricted} by the
         *         implementation.
         */
        public VCRHttpRequest.Builder headers(String... headers);

        /**
         * Sets a timeout for this request. If the response is not received
         * within the specified timeout then an {@link HttpTimeoutException} is
         * thrown from {@link HttpClient#send(java.net.http.HttpRequest,
         * java.net.http.HttpResponse.BodyHandler) HttpClient::send} or
         * {@link HttpClient#sendAsync(java.net.http.HttpRequest,
         * java.net.http.HttpResponse.BodyHandler) HttpClient::sendAsync}
         * completes exceptionally with an {@code HttpTimeoutException}. The effect
         * of not setting a timeout is the same as setting an infinite Duration,
         * i.e. block forever.
         *
         * @param duration the timeout duration
         * @return this builder
         * @throws IllegalArgumentException if the duration is non-positive
         */
        public abstract VCRHttpRequest.Builder timeout(Duration duration);

        /**
         * Sets the given name value pair to the set of headers for this
         * request. This overwrites any previously set values for name.
         *
         * @param name the header name
         * @param value the header value
         * @return this builder
         * @throws IllegalArgumentException if the header name or value is not valid,
         *         see <a href="https://tools.ietf.org/html/rfc7230#section-3.2">
         *         RFC 7230 section-3.2</a>, or the header name or value is
         *         {@linkplain #header(String, String) restricted} by the
         *         implementation.
         */
        public VCRHttpRequest.Builder setHeader(String name, String value);

        /**
         * Sets the request method of this builder to GET.
         * This is the default.
         *
         * @return this builder
         */
        public VCRHttpRequest.Builder GET();

        /**
         * Sets the request method of this builder to POST and sets its
         * request body publisher to the given value.
         *
         * @param bodyPublisher the body publisher
         *
         * @return this builder
         */
        public VCRHttpRequest.Builder POST(HttpRequest.BodyPublisher bodyPublisher);

        /**
         * Sets the request method of this builder to PUT and sets its
         * request body publisher to the given value.
         *
         * @param bodyPublisher the body publisher
         *
         * @return this builder
         */
        public VCRHttpRequest.Builder PUT(HttpRequest.BodyPublisher bodyPublisher);

        /**
         * Sets the request method of this builder to DELETE.
         *
         * @return this builder
         */
        public VCRHttpRequest.Builder DELETE();

        /**
         * Sets the request method and request body of this builder to the
         * given values.
         *
         * @apiNote The {@link HttpRequest.BodyPublishers#noBody() noBody} request
         * body publisher can be used where no request body is required or
         * appropriate. Whether a method is restricted, or not, is
         * implementation specific. For example, some implementations may choose
         * to restrict the {@code CONNECT} method.
         *
         * @param method the method to use
         * @param bodyPublisher the body publisher
         * @return this builder
         * @throws IllegalArgumentException if the method name is not
         *         valid, see <a href="https://tools.ietf.org/html/rfc7230#section-3.1.1">
         *         RFC 7230 section-3.1.1</a>, or the method is restricted by the
         *         implementation.
         */
        public VCRHttpRequest.Builder method(String method, HttpRequest.BodyPublisher bodyPublisher);

        /**
         * Builds and returns an {@link HttpRequest}.
         *
         * @return a new {@code HttpRequest}
         * @throws IllegalStateException if a URI has not been set
         */
        public VCRHttpRequest build();

        /**
         * Returns an exact duplicate copy of this {@code Builder} based on
         * current state. The new builder can then be modified independently of
         * this builder.
         *
         * @return an exact copy of this builder
         */
        public VCRHttpRequest.Builder copy();
    }

    public static VCRHttpRequest.Builder newVCRBuilder(URI uri, VCR vcr) {
        return new VCRHttpRequestBuilderImpl(uri, vcr);
    }

    /**
     * Creates a {@code Builder} whose initial state is copied from an existing
     * {@code HttpRequest}.
     *
     * <p> This builder can be used to build an {@code HttpRequest}, equivalent
     * to the original, while allowing amendment of the request state prior to
     * construction - for example, adding additional headers.
     *
     * <p> The {@code filter} is applied to each header name value pair as they
     * are copied from the given request. When completed, only headers that
     * satisfy the condition as laid out by the {@code filter} will be present
     * in the {@code Builder} returned from this method.
     *
     * @apiNote
     * The following scenarios demonstrate typical use-cases of the filter.
     * Given an {@code HttpRequest} <em>request</em>:
     * <br><br>
     * <ul>
     *  <li> Retain all headers:
     *  <pre>{@code HttpRequest.newBuilder(request, (n, v) -> true)}</pre>
     *
     *  <li> Remove all headers:
     *  <pre>{@code HttpRequest.newBuilder(request, (n, v) -> false)}</pre>
     *
     *  <li> Remove a particular header (e.g. Foo-Bar):
     *  <pre>{@code HttpRequest.newBuilder(request, (name, value) -> !name.equalsIgnoreCase("Foo-Bar"))}</pre>
     * </ul>
     *
     * @param request the original request
     * @param filter a header filter
     * @return a new request builder
     * @throws IllegalArgumentException if a new builder cannot be seeded from
     *         the given request (for instance, if the request contains illegal
     *         parameters)
     * @since 16
     */
    public static VCRHttpRequest.Builder newVCRBuilder(VCRHttpRequest request, BiPredicate<String, String> filter, VCR vcr) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(filter);

        final VCRHttpRequest.Builder builder = VCRHttpRequest.newVCRBuilder(vcr);
        builder.uri(request.uri());
        builder.expectContinue(request.expectContinue());

        // Filter unwanted headers
        HttpHeaders headers = HttpHeaders.of(request.headers().map(), filter);
        headers.map().forEach((name, values) ->
                values.forEach(value -> builder.header(name, value)));

        request.version().ifPresent(builder::version);
        request.timeout().ifPresent(builder::timeout);
        var method = request.method();
        request.bodyPublisher().ifPresentOrElse(
                // if body is present, set it
                bodyPublisher -> builder.method(method, bodyPublisher),
                // otherwise, the body is absent, special case for GET/DELETE,
                // or else use empty body
                () -> {
                    switch (method) {
                        case "GET" -> builder.GET();
                        case "DELETE" -> builder.DELETE();
                        default -> builder.method(method, HttpRequest.BodyPublishers.noBody());
                    }
                }
        );
        return builder;
    }

    /**
     * Creates an {@code HttpRequest} builder.
     *
     * @return a new request builder
     */
    public static VCRHttpRequest.Builder newVCRBuilder(VCR vcr) {
        return new VCRHttpRequestBuilderImpl(vcr);
    }
}
