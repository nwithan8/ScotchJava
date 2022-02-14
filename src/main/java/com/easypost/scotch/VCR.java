package com.easypost.scotch;

import com.easypost.scotch.cassettes.Cassette;
import com.easypost.scotch.clients.httpclient.VCRImmutableHttpRequest;
import com.easypost.scotch.interaction.HttpInteraction;
import com.easypost.scotch.interaction.Request;
import com.easypost.scotch.interaction.Response;

import java.net.http.HttpResponse;

public class VCR {
    private Cassette cassette;
    private ScotchMode mode;

    private Request currentRequest;
    private Response currentResponse;

    public VCR(Cassette cassette) {
        this.cassette = cassette;
        this.mode = ScotchMode.None;
        this.currentRequest = new Request();
        this.currentResponse = new Response();
    }

    public VCR() {
        this.cassette = null;
        this.mode = ScotchMode.None;
        this.currentRequest = new Request();
        this.currentResponse = new Response();
    }

    public void insert(Cassette cassette) {
        this.cassette = cassette;
    }

    public void eject() {
        this.cassette = null;
    }

    public void record() {
        this.mode = ScotchMode.Recording;
    }

    public void replay() {
        this.mode = ScotchMode.Replaying;
    }

    public void pause() {
        this.mode = ScotchMode.None;
    }

    public boolean inRecordMode() {
        return this.mode == ScotchMode.Recording;
    }

    public boolean inPlaybackMode() {
        return this.mode == ScotchMode.Replaying;
    }

    // Happens first when making a request
    public void noteRequestBody(String body) {
        currentRequest.setBody(body);
    }

    // Happens second when making a request
    public void noteRequestDetails(VCRImmutableHttpRequest httpRequest) {
        currentRequest.setMethod(httpRequest.method());
        currentRequest.setUri(httpRequest.uri());
        currentRequest.setHeaders(httpRequest.headers());
    }

    // Happens first when getting a response
    public void noteResponseDetails(HttpResponse.ResponseInfo responseInfo) {
        currentResponse.setStatusCode(responseInfo.statusCode());
        currentResponse.setHeaders(responseInfo.headers().map());
        currentResponse.setVersion(responseInfo.version());
    }

    // Happens second when getting a response
    public void noteResponseBody(String body) {
        currentResponse.setBody(body);
    }

    public void saveRecording() {
        assert this.cassette != null;
        HttpInteraction interaction = new HttpInteraction(this.currentRequest, this.currentResponse);
        Cassette.updateInteractionOnCassette(this.cassette, interaction);
    }

    public void clear() {
        currentRequest = new Request();
        currentResponse = new Response();
    }

    public Response loadCachedResponse() {
        HttpInteraction matchingInteraction =
                Cassette.findInteractionMatchingRequestOnCassette(this.cassette, this.currentRequest);
        if (matchingInteraction == null) {
            return new Response();
        }
        return matchingInteraction.getResponse();
    }

    public void tapeOverExistingInteraction(HttpInteraction interaction) {
        assert this.cassette != null;
        this.cassette.updateInteraction(interaction);
    }

    public HttpInteraction seekMatchingInteraction(Request request) {
        assert this.cassette != null;
        return this.cassette.findInteractionMatchingRequest(request);
    }
}
