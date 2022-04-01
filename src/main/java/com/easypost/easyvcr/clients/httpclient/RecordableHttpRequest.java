package com.easypost.easyvcr.clients.httpclient;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.Charset;
import java.time.Duration;

public abstract class RecordableHttpRequest extends HttpRequest {

    protected RecordableHttpRequest() {
    }

    public static RecordableHttpRequest.Builder newBuilder(URI uri, Cassette cassette, Mode mode,
                                                           AdvancedSettings advancedSettings) {
        return new RecordableHttpRequestBuilderImpl(uri, cassette, mode, advancedSettings);
    }

    public RecordableBodyHandler getBodyHandler() {
        return null;
    }

    public interface Builder {

        /**
         * Sets this {@code HttpRequest}'s request {@code URI}.
         *
         * @param uri the request URI
         * @return this builder
         * @throws IllegalArgumentException if the {@code URI} scheme is not
         *                                  supported
         */
        public RecordableHttpRequest.Builder uri(URI uri);

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
        public RecordableHttpRequest.Builder expectContinue(boolean enable);

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
        public RecordableHttpRequest.Builder version(HttpClient.Version version);

        /**
         * Adds the given name value pair to the set of headers for this request.
         * The given value is added to the list of values for that name.
         *
         * @param name  the header name
         * @param value the header value
         * @return this builder
         * @throws IllegalArgumentException if the header name or value is not
         *                                  valid, see <a href="https://tools.ietf.org/html/rfc7230#section-3.2">
         *                                  RFC 7230 section-3.2</a>, or the header name or value is restricted
         *                                  by the implementation.
         * @implNote An implementation may choose to restrict some header names
         * or values, as the HTTP Client may determine their value itself.
         * For example, "Content-Length", which will be determined by
         * the request Publisher. In such a case, an implementation of
         * {@code HttpRequest.Builder} may choose to throw an
         * {@code IllegalArgumentException} if such a header is passed
         * to the builder.
         */
        public RecordableHttpRequest.Builder header(String name, String value);

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
         *                                  parameters, or if a header name or value is not valid, see
         *                                  <a href="https://tools.ietf.org/html/rfc7230#section-3.2">
         *                                  RFC 7230 section-3.2</a>, or a header name or value is
         *                                  {@linkplain #header(String, String) restricted} by the
         *                                  implementation.
         */
        public RecordableHttpRequest.Builder headers(String... headers);

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
        public abstract RecordableHttpRequest.Builder timeout(Duration duration);

        /**
         * Sets the given name value pair to the set of headers for this
         * request. This overwrites any previously set values for name.
         *
         * @param name  the header name
         * @param value the header value
         * @return this builder
         * @throws IllegalArgumentException if the header name or value is not valid,
         *                                  see <a href="https://tools.ietf.org/html/rfc7230#section-3.2">
         *                                  RFC 7230 section-3.2</a>, or the header name or value is
         *                                  {@linkplain #header(String, String) restricted} by the
         *                                  implementation.
         */
        public RecordableHttpRequest.Builder setHeader(String name, String value);

        /**
         * Sets the request method of this builder to GET.
         * This is the default.
         *
         * @return this builder
         */
        public RecordableHttpRequest.Builder GET();

        /**
         * Sets the request method of this builder to POST and sets its
         * request body publisher to the given value.
         *
         * @param bodyPublisher the body publisher
         * @return this builder
         */
        public RecordableHttpRequest.Builder POST(RecordableHttpRequest.BodyPublisher bodyPublisher);

        /**
         * Sets the request method of this builder to PUT and sets its
         * request body publisher to the given value.
         *
         * @param bodyPublisher the body publisher
         * @return this builder
         */
        public RecordableHttpRequest.Builder PUT(RecordableHttpRequest.BodyPublisher bodyPublisher);

        /**
         * Sets the request method of this builder to DELETE.
         *
         * @return this builder
         */
        public RecordableHttpRequest.Builder DELETE();

        /**
         * Sets the request method and request body of this builder to the
         * given values.
         *
         * @param method        the method to use
         * @param bodyPublisher the body publisher
         * @return this builder
         * @throws IllegalArgumentException if the method name is not
         *                                  valid, see <a href="https://tools.ietf.org/html/rfc7230#section-3.1.1">
         *                                  RFC 7230 section-3.1.1</a>, or the method is restricted by the
         *                                  implementation.
         * @apiNote The {@link HttpRequest.BodyPublishers#noBody() noBody} request
         * body publisher can be used where no request body is required or
         * appropriate. Whether a method is restricted, or not, is
         * implementation specific. For example, some implementations may choose
         * to restrict the {@code CONNECT} method.
         */
        public RecordableHttpRequest.Builder method(String method, HttpRequest.BodyPublisher bodyPublisher);

        /**
         * Builds and returns an {@link HttpRequest}.
         *
         * @return a new {@code HttpRequest}
         * @throws IllegalStateException if a URI has not been set
         */
        public RecordableHttpRequest build();

        /**
         * Returns an exact duplicate copy of this {@code Builder} based on
         * current state. The new builder can then be modified independently of
         * this builder.
         *
         * @return an exact copy of this builder
         */
        public RecordableHttpRequest.Builder copy();
    }

    public static class BodyPublisher {

        public String contents;
        public HttpRequest.BodyPublisher bodyPublisher;

        public BodyPublisher(HttpRequest.BodyPublisher bodyPublisher, String contents) {
            this.bodyPublisher = bodyPublisher;
            this.contents = contents;
        }
    }

    public static class BodyPublishers {

        public static BodyPublisher ofString(String body) {
            HttpRequest.BodyPublisher pub = HttpRequest.BodyPublishers.ofString(body);
            return new BodyPublisher(pub, body);
        }

        public static BodyPublisher ofString(String s, Charset charset) {
            HttpRequest.BodyPublisher pub = HttpRequest.BodyPublishers.ofString(s, charset);
            return new BodyPublisher(pub, s);
        }

        public static BodyPublisher noBody() {
            HttpRequest.BodyPublisher pub = HttpRequest.BodyPublishers.noBody();
            return new BodyPublisher(pub, null);
        }
    }
}
