package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;
import com.easypost.easyvcr.requestelements.Status;

import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class HttpClientInteractionConverter extends BaseInteractionConverter {
    public Map<String, List<String>> toHeaders(HttpHeaders headers) {
        return headers.map();
    }

    public Request createRequest(HttpRequest httpRequest, String body) {
        Request request = new Request();
        request.setMethod(httpRequest.method());
        request.setUri(httpRequest.uri());
        request.setHeaders(toHeaders(httpRequest.headers()));
        request.setBody(body);
        return request;
    }

    public Response createResponse(HttpResponse<String> httpResponse) {
        Response response = new Response();
        Status status = new Status(httpResponse.statusCode(), null);
        response.setStatus(status);
        response.setHeaders(toHeaders(httpResponse.headers()));
        response.setUri(httpResponse.request().uri());
        response.setBody(httpResponse.body());
        response.setHttpVersion(httpResponse.version());
        return response;
    }

    public HttpInteraction createInteraction(HttpResponse<String> httpResponse,
                                                                   String requestBody) {
        Request request = createRequest(httpResponse.request(), requestBody);
        Response response = createResponse(httpResponse);
        return _createInteraction(request, response);
    }
}
