package com.easypost.scotch.clients.scotchhttpclient;

import com.easypost.scotch.cassettes.Cassette;
import com.easypost.scotch.ScotchMode;
import com.easypost.scotch.interaction.HttpInteraction;
import com.easypost.scotch.interaction.Request;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static com.easypost.scotch.interaction.Helpers.createInteractionFromHttpResponse;

public class VCRScotchHttpClient {

    // custom HTTP client, utilizes java.net.http.HttpClient internally

    private final HttpClient client;

    private final String cassettePath;

    private final ScotchMode mode;

    public VCRScotchHttpClient(HttpClient client, String cassettePath, ScotchMode mode) {
        this.client = client;
        this.cassettePath = cassettePath;
        this.mode = mode;
    }

    public static Map.Entry<String, String> makeHeaderEntry(String key, String value) {
        return new AbstractMap.SimpleEntry<String, String>(key, value);
    }

    private HttpResponse<String> sendAndRecordResponse(HttpRequest request, String requestBody)
            throws IOException, InterruptedException {

        HttpResponse<String> httpResponse = this.client.send(request, HttpResponse.BodyHandlers.ofString());

        HttpInteraction interaction = createInteractionFromHttpResponse(httpResponse, requestBody);

        Cassette.updateInteraction(this.cassettePath, interaction);

        return httpResponse;
    }

    private HttpResponse<String> populateWithCachedResponse(HttpRequest httpRequest, String requestBody) {
        Request request = new Request();
        request.setMethod(httpRequest.method());
        request.setUri(httpRequest.uri());
        request.setBody(requestBody);

        HttpInteraction matchingRecordedInteraction = Cassette.findInteractionMatchingRequest(this.cassettePath, request);

        if (matchingRecordedInteraction == null) {
            return null;
        }

        return matchingRecordedInteraction.getResponse().toHttpResponse(httpRequest);
    }

    private HttpRequest.Builder buildBaseRequest(URI uri, List<Map.Entry<String, String>> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers) {
                requestBuilder = requestBuilder.header(header.getKey(), header.getValue());
            }
        }
        return requestBuilder;
    }

    private HttpResponse<String> send(HttpRequest request, String body) throws IOException, InterruptedException {

        switch (this.mode) {
            case Recording:
                return sendAndRecordResponse(request, body);
            case Replaying:
                return populateWithCachedResponse(request, body);
            case None:
            default:
                return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    public HttpResponse<String> get(URI uri, List<Map.Entry<String, String>> headers)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = buildBaseRequest(uri, headers);

        HttpRequest request = requestBuilder.GET().build();

        return send(request, null);
    }

    public HttpResponse<String> get(URI uri) throws IOException, InterruptedException {
        return get(uri, null);
    }

    private HttpResponse<String> post(URI uri, List<Map.Entry<String, String>> headers,
                                      HttpRequest.BodyPublisher bodyPublisher, String body) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = buildBaseRequest(uri, headers);

        HttpRequest request = requestBuilder.POST(bodyPublisher).build();

        return send(request, body);
    }

    public HttpResponse<String> post(URI uri, List<Map.Entry<String, String>> headers, JsonObject json)
            throws IOException, InterruptedException {
        String jsonString = json.getAsString();
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonString);

        return post(uri, headers, bodyPublisher, jsonString);
    }

    public HttpResponse<String> post(URI uri, JsonObject json) throws IOException, InterruptedException {
        String jsonString = json.getAsString();
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonString);

        return post(uri, null, bodyPublisher, jsonString);
    }

    public HttpResponse<String> post(URI uri, List<Map.Entry<String, String>> headers, String body)
            throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);

        return post(uri, headers, bodyPublisher, body);
    }

    public HttpResponse<String> post(URI uri, String body) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);

        return post(uri, null, bodyPublisher, body);
    }

    public HttpResponse<String> post(URI uri, List<Map.Entry<String, String>> headers, byte[] bytes)
            throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(bytes);

        return post(uri, headers, bodyPublisher, new String(bytes, StandardCharsets.UTF_8));
    }

    public HttpResponse<String> post(URI uri, byte[] bytes) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(bytes);

        return post(uri, null, bodyPublisher, new String(bytes, StandardCharsets.UTF_8));
    }

    public HttpResponse<String> post(URI uri, List<Map.Entry<String, String>> headers, Path filePath)
            throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofFile(filePath);

        return post(uri, headers, bodyPublisher, null);
    }

    public HttpResponse<String> post(URI uri, Path filePath) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofFile(filePath);

        return post(uri, null, bodyPublisher, null);
    }

    public HttpResponse<String> post(URI uri, List<Map.Entry<String, String>> headers, File file)
            throws IOException, InterruptedException {
        return post(uri, headers, file.toPath());
    }

    public HttpResponse<String> post(URI uri, File file) throws IOException, InterruptedException {
        return post(uri, null, file.toPath());
    }

    public HttpResponse<String> post(URI uri, List<Map.Entry<String, String>> headers)
            throws IOException, InterruptedException {
        return post(uri, headers, HttpRequest.BodyPublishers.noBody(), null);
    }

    public HttpResponse<String> post(URI uri) throws IOException, InterruptedException {
        return post(uri, null, HttpRequest.BodyPublishers.noBody(), null);
    }

    private HttpResponse<String> put(URI uri, List<Map.Entry<String, String>> headers,
                                    HttpRequest.BodyPublisher bodyPublisher, String body) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = buildBaseRequest(uri, headers);

        HttpRequest request = requestBuilder.PUT(bodyPublisher).build();

        return send(request, body);
    }

    public HttpResponse<String> put(URI uri, List<Map.Entry<String, String>> headers, JsonObject json)
            throws IOException, InterruptedException {
        String jsonString = json.getAsString();
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonString);

        return put(uri, headers, bodyPublisher, jsonString);
    }

    public HttpResponse<String> put(URI uri, JsonObject json) throws IOException, InterruptedException {
        String jsonString = json.getAsString();
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonString);

        return put(uri, null, bodyPublisher, jsonString);
    }

    public HttpResponse<String> put(URI uri, List<Map.Entry<String, String>> headers, String body)
            throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);

        return put(uri, headers, bodyPublisher, body);
    }

    public HttpResponse<String> put(URI uri, String body) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);

        return put(uri, null, bodyPublisher, body);
    }

    public HttpResponse<String> put(URI uri, List<Map.Entry<String, String>> headers, byte[] bytes)
            throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(bytes);

        return put(uri, headers, bodyPublisher, new String(bytes, StandardCharsets.UTF_8));
    }

    public HttpResponse<String> put(URI uri, byte[] bytes) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(bytes);

        return put(uri, null, bodyPublisher, new String(bytes, StandardCharsets.UTF_8));
    }

    public HttpResponse<String> put(URI uri, List<Map.Entry<String, String>> headers, Path filePath)
            throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofFile(filePath);

        return put(uri, headers, bodyPublisher, null);
    }

    public HttpResponse<String> put(URI uri, Path filePath) throws IOException, InterruptedException {
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofFile(filePath);

        return put(uri, null, bodyPublisher, null);
    }

    public HttpResponse<String> put(URI uri, List<Map.Entry<String, String>> headers, File file)
            throws IOException, InterruptedException {
        return put(uri, headers, file.toPath());
    }

    public HttpResponse<String> put(URI uri, File file) throws IOException, InterruptedException {
        return put(uri, null, file.toPath());
    }

    public HttpResponse<String> put(URI uri, List<Map.Entry<String, String>> headers)
            throws IOException, InterruptedException {
        return put(uri, headers, HttpRequest.BodyPublishers.noBody(), null);
    }

    public HttpResponse<String> put(URI uri) throws IOException, InterruptedException {
        return put(uri, null, HttpRequest.BodyPublishers.noBody(), null);
    }

    private HttpResponse<String> delete(URI uri, List<Map.Entry<String, String>> headers)
            throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = buildBaseRequest(uri, headers);

        HttpRequest request = requestBuilder.DELETE().build();

        return send(request, null);
    }

    public HttpResponse<String> delete(URI uri) throws IOException, InterruptedException {
        return delete(uri, null);
    }
}
