package com.easypost.scotch.interaction;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Helpers {
    public static Map<String, List<String>> toHeaders(HttpHeaders headers) {
        return headers.map();
    }

    private static Request createRequestFromHttpRequest(HttpRequest httpRequest, String body) {
        Request request = new Request();
        request.setMethod(httpRequest.method());
        request.setUri(httpRequest.uri());
        request.setHeaders(toHeaders(httpRequest.headers()));
        request.setBody(body);
        return request;
    }

    private static Response createResponseFromHttpResponse(HttpResponse<String> httpResponse) {
        Response response = new Response();
        response.setStatusCode(httpResponse.statusCode());
        response.setHeaders(toHeaders(httpResponse.headers()));
        response.setUri(httpResponse.request().uri());
        response.setBody(httpResponse.body());
        response.setVersion(httpResponse.version());
        return response;
    }

    public static HttpInteraction createInteractionFromHttpResponse(HttpResponse<String> httpResponse,
                                                                    String requestBody) {
        HttpInteraction interaction = new HttpInteraction();

        Request request = createRequestFromHttpRequest(httpResponse.request(), requestBody);
        interaction.setRequest(request);

        Response response = createResponseFromHttpResponse(httpResponse);
        interaction.setResponse(response);

        interaction.setRecordedAt(Instant.now().getEpochSecond());

        return interaction;
    }

    public static boolean requestsMatch(Request receivedRequest, Request recordedRequest) {
        boolean methodAndURIMatch = (receivedRequest.getMethod().equalsIgnoreCase(recordedRequest.getMethod()) &&
                receivedRequest.getUriString().equalsIgnoreCase(recordedRequest.getUriString()));
        if (!methodAndURIMatch) {
            return false;
        }
        if (receivedRequest.getBody() != null && recordedRequest.getBody() != null) {
            // both have no body, so they match
            return true;
        } else if (receivedRequest.getBody() == null || receivedRequest.getBody() == null) {
            // one of them has no body, so they don't match
            return false;
        } else {
            // both have a body, check if they match
            return receivedRequest.getBody().equalsIgnoreCase(recordedRequest.getBody());
        }
    }

    public static boolean interactionRequestsMatch(HttpInteraction receivedInteraction,
                                                   HttpInteraction recordedInteraction) {
        return requestsMatch(receivedInteraction.getRequest(), recordedInteraction.getRequest());
    }
}
