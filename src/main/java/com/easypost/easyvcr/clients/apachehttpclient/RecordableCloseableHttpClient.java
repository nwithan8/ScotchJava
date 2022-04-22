package com.easypost.easyvcr.clients.apachehttpclient;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.interactionconverters.ApacheInteractionConverter;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

import static com.easypost.easyvcr.internalutilities.Tools.simulateDelay;

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
        this.advancedSettings = advancedSettings != null ? advancedSettings : new AdvancedSettings();
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

    private HttpInteraction loadExistingInteraction(HttpHost target, HttpRequest httpRequest, HttpContext context)
            throws VCRException {
        Request request = converter.createRequest(httpRequest, advancedSettings.censors);

        HttpInteraction matchingInteraction =
                converter.findMatchingInteraction(this.cassette, request, advancedSettings.matchRules);

        return matchingInteraction;
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext)
            throws IOException {
        switch (mode) {
            case Record:
                try {
                    return sendAndRecordResponse(httpHost, httpRequest, httpContext);
                } catch (VCRException e) {
                    throw new RuntimeException(e);
                }
            case Replay:
                try {
                    HttpInteraction recording = loadExistingInteraction(httpHost, httpRequest, httpContext);
                    simulateDelay(recording, advancedSettings);
                    assert recording != null;
                    return recording.getResponse().toCloseableHttpResponse();
                } catch (VCRException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            case Auto:
                try {
                    HttpInteraction recording = loadExistingInteraction(httpHost, httpRequest, httpContext);
                    simulateDelay(recording, advancedSettings);
                    assert recording != null;
                    return recording.getResponse().toCloseableHttpResponse();
                } catch (VCRException e) {
                    try {
                        return sendAndRecordResponse(httpHost, httpRequest, httpContext);
                    } catch (VCRException e2) {
                        throw new RuntimeException(e2);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            case Bypass:
                return this.client.execute(httpHost, httpRequest, httpContext);
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
