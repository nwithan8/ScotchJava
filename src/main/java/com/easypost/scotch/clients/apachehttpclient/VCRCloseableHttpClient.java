package com.easypost.scotch.clients.apachehttpclient;

import com.easypost.scotch.ScotchMode;
import com.easypost.scotch.cassettes.Cassette;
import com.easypost.scotch.interaction.Helpers;
import com.easypost.scotch.interaction.HttpInteraction;
import com.easypost.scotch.interaction.Request;
import com.easypost.scotch.interaction.Response;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

import java.io.IOException;
import java.net.URI;

public class VCRCloseableHttpClient {

    private final CloseableHttpClient client;
    private final String cassettePath;
    private final ScotchMode mode;
    private HttpInteraction cachedInteraction;

    public VCRCloseableHttpClient(String cassettePath, ScotchMode mode) {
        this.client = HttpClients.createDefault();
        this.cassettePath = cassettePath;
        this.mode = mode;
        this.cachedInteraction = new HttpInteraction(new Request(), new Response());
    }

    private static HttpHost determineTarget(HttpUriRequest request) throws ClientProtocolException {
        HttpHost target = null;
        URI requestURI = request.getURI();
        if (requestURI.isAbsolute()) {
            target = URIUtils.extractHost(requestURI);
            if (target == null) {
                throw new ClientProtocolException("URI does not specify a valid host name: " + requestURI);
            }
        }

        return target;
    }

    private CloseableHttpResponse sendAndRecordResponse(HttpHost target, HttpRequest httpRequest, HttpContext context)
            throws IOException {
        CloseableHttpResponse httpResponse = this.client.execute(target, httpRequest, context);

        HttpInteraction interaction = Helpers.createInteractionFromApacheHttpRequestAndResponse(httpResponse, httpRequest);

        Cassette.updateInteraction(this.cassettePath, interaction);

        return httpResponse;
    }

    private CloseableHttpResponse populateWithCachedResponse(HttpHost target, HttpRequest httpRequest,
                                                             HttpContext context) throws IOException {
        Request request = Helpers.createRequestFromApacheHttpRequest(httpRequest);

        HttpInteraction matchingRecordedInteraction =
                Cassette.findInteractionMatchingRequest(this.cassettePath, request);

        if (matchingRecordedInteraction == null) {
            return null;
        }

        return matchingRecordedInteraction.getResponse().toCloseableHttpResponse();
    }

    public CloseableHttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        switch (this.mode) {
            case Recording:
                return sendAndRecordResponse(target, request, context);
            case Replaying:
                return populateWithCachedResponse(target, request, context);
            case None:
            default:
                return this.client.execute(target, request, context);
        }
    }

    public CloseableHttpResponse execute(HttpHost target, HttpRequest request)
            throws IOException, ClientProtocolException {
        return this.execute(target, request, (HttpContext) null);
    }

    public CloseableHttpResponse execute(HttpUriRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        Args.notNull(request, "HTTP request");
        return this.execute(determineTarget(request), request, context);
    }

    public CloseableHttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        return this.execute(request, (HttpContext) null);
    }

    public HttpParams getParams() {
        return this.client.getParams();
    }

    public ClientConnectionManager getConnectionManager() {
        return this.client.getConnectionManager();
    }

    public void close() throws IOException {
        this.client.close();
    }
}
