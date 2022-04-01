package com.easypost.easyvcr.clients.apachehttpclient;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.clients.httpclient.RecordableHttpRequest;
import com.easypost.easyvcr.interactionconverters.ApacheInteractionConverter;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
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

public class RecordableCloseableHttpClient extends CloseableHttpClient {

    private final CloseableHttpClient client;

    private final Cassette cassette;

    private final Mode mode;

    private final AdvancedSettings advancedSettings;

    private final ApacheInteractionConverter converter;

    public RecordableCloseableHttpClient(Cassette cassette, Mode mode, AdvancedSettings advancedSettings) {
        this.client = HttpClients.createDefault();
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
        this.converter = new ApacheInteractionConverter();
    }

    public RecordableCloseableHttpClient(Cassette cassette, Mode mode) {
        this(cassette, mode, new AdvancedSettings());
    }

    private CloseableHttpResponse sendAndRecordResponse(HttpHost target, HttpRequest httpRequest, HttpContext context)
            throws IOException, VCRException {
        CloseableHttpResponse httpResponse = this.client.execute(target, httpRequest, context);

        HttpInteraction interaction = converter.createInteraction(httpResponse, httpRequest, advancedSettings.censors);

        this.cassette.updateInteraction(interaction, advancedSettings.matchRules, false);

        return httpResponse;
    }

    private CloseableHttpResponse populateWithCachedResponse(HttpHost target, HttpRequest httpRequest,
                                                             HttpContext context) throws VCRException {
        Request request = converter.createRequest(httpRequest, advancedSettings.censors);

        HttpInteraction matchingInteraction = converter.findMatchingInteraction(this.cassette, request, advancedSettings.matchRules);

        if (matchingInteraction == null) {
            return null;
        }

        return matchingInteraction.getResponse().toCloseableHttpResponse();
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext)
            throws IOException, ClientProtocolException {
        // TODO: Bypass and auto mode
        switch (mode) {
            case Record:
                try {
                    return sendAndRecordResponse(httpHost, httpRequest, httpContext);
                } catch (VCRException e) {
                    e.printStackTrace();
                }
            case Replay:
                try {
                    return populateWithCachedResponse(httpHost, httpRequest, httpContext);
                } catch (VCRException e) {
                    e.printStackTrace();
                }
            default:
                return null;
        }
    }

    @Override
    public HttpParams getParams() {
        return this.client.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return this.client.getConnectionManager();
    }

    @Override
    public void close() throws IOException {
        this.client.close();
    }
}
