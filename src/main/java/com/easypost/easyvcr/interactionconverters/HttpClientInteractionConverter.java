package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.MatchRules;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.clients.httpclient.RecordableHttpRequest;
import com.easypost.easyvcr.clients.httpclient.RecordableImmutableHttpRequest;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;
import com.easypost.easyvcr.requestelements.Status;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class HttpClientInteractionConverter extends BaseInteractionConverter {

    private final Request currentRequest;
    private final Response currentResponse;

    private long duration;

    public HttpClientInteractionConverter() {
        super();
        currentRequest = new Request();
        currentResponse = new Response();
        duration = 0;
    }

    private Map<String, List<String>> toHeaders(HttpHeaders headers) {
        return headers.map();
    }

    // Happens first when making a request
    public void noteRequestBody(RecordableHttpRequest.BodyPublisher body, Censors censors) {
        String contents = censors.applyBodyParametersCensors(body.contents);
        currentRequest.setBody(contents);
    }

    // Happens second when making a request
    public void noteRequestDetails(RecordableImmutableHttpRequest httpRequest, Censors censors)
            throws URISyntaxException {
        // collect elements from the connection
        String uriString = httpRequest.uri().toString();
        HttpHeaders headers = httpRequest.headers();
        String method = httpRequest.method();

        // apply censors
        uriString = censors.applyQueryParametersCensors(uriString);
        Map<String, List<String>> headersMap = toHeaders(headers);
        headersMap = censors.applyHeadersCensors(headersMap);

        // create the request
        currentRequest.setMethod(method);
        currentRequest.setUri(new URI(uriString));
        currentRequest.setHeaders(headersMap);
    }

    // Happens first when getting a response
    public void noteResponseDetails(HttpResponse.ResponseInfo responseInfo, Censors censors) {
        // quickly time how long it takes to get the initial response
        // NOTE: This may not work as expected because we might already have all the response info by this point (i.e. we're not timing the actual HTTP request)
        Instant start = Instant.now();
        int responseCode = responseInfo.statusCode();
        Instant end = Instant.now();
        duration = Duration.between(start, end).toMillis();

        // collect elements from the connection
        HttpClient.Version version = responseInfo.version();
        HttpHeaders headers = responseInfo.headers();

        // apply censors
        Map<String, List<String>> headersMap = toHeaders(headers);
        headersMap = censors.applyHeadersCensors(headersMap);

        // create the response
        currentResponse.setStatus(new Status(responseCode, null));
        currentResponse.setHttpVersion(version);
        currentResponse.setHeaders(headersMap);
    }

    public HttpInteraction loadExistingInteraction(Cassette cassette, MatchRules matchRules) throws VCRException {
        HttpInteraction matchingInteraction = findMatchingInteraction(cassette, currentRequest, matchRules);
        return matchingInteraction;
    }

    // Happens second when getting a response
    public void noteResponseBody(String body) {
        currentResponse.setBody(body);
    }

    public void saveRecording(Cassette cassette, MatchRules matchRules, boolean bypassSearch) throws VCRException {
        assert cassette != null;
        HttpInteraction interaction = new HttpInteraction(this.currentRequest, this.currentResponse, this.duration);
        cassette.updateInteraction(interaction, matchRules, bypassSearch);
    }
}
