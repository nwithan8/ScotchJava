package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;
import com.easypost.easyvcr.requestelements.Status;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApacheInteractionConverter extends BaseInteractionConverter {
    public Request createRequest(org.apache.http.HttpRequest httpRequest, Censors censors) {
        // TODO: Use censors here
        Request request = new Request();
        try {
            request.setUri(URI.create(httpRequest.getRequestLine().getUri()));
            request.setMethod(httpRequest.getRequestLine().getMethod());
            request.setHeaders(toHeaders(List.of(httpRequest.getAllHeaders())));
            if (httpRequest instanceof HttpPost) {
                request.setBody(readBodyFromInputStream(((HttpPost) httpRequest).getEntity().getContent()));
            } else if (httpRequest instanceof HttpPut) {
                request.setBody(readBodyFromInputStream(((HttpPut) httpRequest).getEntity().getContent()));
            } else if (httpRequest instanceof HttpPatch) {
                request.setBody(readBodyFromInputStream(((HttpPatch) httpRequest).getEntity().getContent()));
            }
        } catch (IOException ignored) {
        }
        return request;
    }

    public Map<String, List<String>> toHeaders(List<Header> headers) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (Header header : headers) {
            List<String> value = new ArrayList<>();
            value.add(header.getValue());
            map.put(header.getName(), value);
        }
        return map;
    }

    public Response createResponse(CloseableHttpResponse httpResponse, org.apache.http.HttpRequest httpRequest, Censors censors) {
        // TODO: Use censors here
        Response response = new Response();
        try {
            Status status = new Status(httpResponse.getStatusLine().getStatusCode(),
                    httpResponse.getStatusLine().getReasonPhrase());
            response.setStatus(status);
            response.setHeaders(toHeaders(List.of(httpResponse.getAllHeaders())));
            response.setBody(readBodyFromInputStream(httpResponse.getEntity().getContent()));
            response.setUri(URI.create(httpRequest.getRequestLine().getUri()));
            response.setHttpVersion(httpResponse.getProtocolVersion());
        } catch (IOException ignored) {
        }

        return response;
    }

    public HttpInteraction createInteraction(CloseableHttpResponse httpResponse,
                                             org.apache.http.HttpRequest httpRequest, Censors censors) {
        Request request = createRequest(httpRequest, censors);
        Response response = createResponse(httpResponse, httpRequest, censors);
        return _createInteraction(request, response);
    }


}
