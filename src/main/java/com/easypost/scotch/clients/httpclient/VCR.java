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

    public void initTrackedRequest(VCRImmutableHttpRequest httpRequest) {
        currentRequest = new Request();
        currentRequest.setMethod(httpRequest.method());
        currentRequest.setUri(httpRequest.uri());
        currentRequest.setHeaders(httpRequest.headers());
    }

    public Response loadCachedResponse() {
        // TODO: Find matching response for request
        HttpInteraction matchingInteraction = Cassette.findInteractionMatchingRequest(this.cassettePath, this.currentRequest);
        if (matchingInteraction == null) {
            return new Response();
        }
        return matchingInteraction.getResponse();
    }

    public void initTrackedResponse(HttpResponse.ResponseInfo responseInfo) {
        currentResponse = new Response();
        currentResponse.setStatusCode(responseInfo.statusCode());
        currentResponse.setHeaders(responseInfo.headers().map()); // same as above?
        currentResponse.setVersion(responseInfo.version());
    }

    public void addBodyToTrackedResponse(String body) {
        currentResponse.setBody(body);
    }

    public void saveRecording() {
        HttpInteraction interaction = new HttpInteraction(this.currentRequest, this.currentResponse);
        Cassette.updateInteraction(this.cassettePath, interaction);
    }

    public HttpResponse<String> parse(HttpResponse<String> httpResponse) {
        return null;
    }
}
