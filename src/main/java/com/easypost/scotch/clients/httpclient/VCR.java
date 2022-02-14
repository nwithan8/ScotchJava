package com.easypost.scotch.clients.httpclient;

import com.easypost.scotch.ScotchMode;
import com.easypost.scotch.cassettes.Cassette;
import com.easypost.scotch.interaction.HttpInteraction;
import com.easypost.scotch.interaction.Request;
import com.easypost.scotch.interaction.Response;

import java.net.http.HttpResponse;

public class VCR {
    private final String cassettePath;
    public final ScotchMode mode;
    private Request currentRequest;
    private Response currentResponse;

    public VCR(String cassettePath, ScotchMode mode) {
        this.cassettePath = cassettePath;
        this.mode = mode;
        this.currentRequest = new Request();
        this.currentResponse = new Response();
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
        HttpInteraction interaction = new HttpInteraction(this.currentRequest, this.currentResponse);
        Cassette.updateInteraction(this.cassettePath, interaction);
    }

    public void clear() {
        currentRequest = new Request();
        currentResponse = new Response();
    }

    public Response loadCachedResponse() {
        HttpInteraction matchingInteraction = Cassette.findInteractionMatchingRequest(this.cassettePath, this.currentRequest);
        if (matchingInteraction == null) {
            return new Response();
        }
        return matchingInteraction.getResponse();
    }

    public HttpResponse<String> parse(HttpResponse<String> httpResponse) {
        return null;
    }
}
