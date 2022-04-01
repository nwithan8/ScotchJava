package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.MatchRules;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.clients.httpclient.RecordableImmutableHttpRequest;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;
import com.easypost.easyvcr.requestelements.Status;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;

public class BaseInteractionConverter {

    protected Request currentRequest;
    protected Response currentResponse;

    public InputStream copyInputStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        try {
            stream.reset();
        } catch (IOException ignored) {
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = stream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ignored) {
            return new ByteArrayInputStream(new byte[] { });
        }
    }

    public InputStream createInputStream(String string) {
        if (string == null) {
            return new ByteArrayInputStream(new byte[] { });
        }
        return new ByteArrayInputStream(string.getBytes());
    }

    public String readBodyFromInputStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        InputStream copy = copyInputStream(stream);
        String body = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(copy));
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

    public HttpInteraction findMatchingInteraction(Cassette cassette, Request request, MatchRules matchRules) throws VCRException {
        for (HttpInteraction recordedInteraction : cassette.read()) {
            if (matchRules.requestsMatch(request, recordedInteraction.getRequest())) {
                return recordedInteraction;
            }
        }
        return null;
    }

    protected HttpInteraction _createInteraction(Request request, Response response) {
        return new HttpInteraction(request, response);
    }

    // Happens first when making a request
    public void noteRequestBody(String body) {
        currentRequest.setBody(body);
    }

    // Happens second when making a request
    public void noteRequestDetails(RecordableImmutableHttpRequest httpRequest) {
        currentRequest.setMethod(httpRequest.method());
        currentRequest.setUri(httpRequest.uri());
        currentRequest.setHeaders(httpRequest.headers());
    }

    // Happens first when getting a response
    public void noteResponseDetails(HttpResponse.ResponseInfo responseInfo) {
        Status status = new Status(responseInfo.statusCode(), null);
        currentResponse.setStatus(status);
        currentResponse.setHeaders(responseInfo.headers().map());
        currentResponse.setHttpVersion(responseInfo.version());
    }

    public Response loadCachedResponse(Cassette cassette, MatchRules matchRules) throws VCRException {
        HttpInteraction matchingInteraction = findMatchingInteraction(cassette, currentRequest, matchRules);
        if (matchingInteraction == null) {
            return new Response();
        }
        return matchingInteraction.getResponse();
    }

    // Happens second when getting a response
    public void noteResponseBody(String body) {
        currentResponse.setBody(body);
    }

    public void saveRecording(Cassette cassette, MatchRules matchRules, boolean bypassSearch) throws VCRException {
        assert cassette != null;
        HttpInteraction interaction = new HttpInteraction(this.currentRequest, this.currentResponse);
        cassette.updateInteraction(interaction, matchRules, bypassSearch);
    }

}
