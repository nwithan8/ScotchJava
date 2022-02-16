package com.easypost.scotch.interaction;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helpers {
    public static Map<String, List<String>> toHeaders(HttpHeaders headers) {
        return headers.map();
    }

    public static Map<String, List<String>> toHeaders(List<Header> headers) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (Header header : headers) {
            List<String> value = new ArrayList<>();
            value.add(header.getValue());
            map.put(header.getName(), value);
        }
        return map;
    }

    public static InputStream copyInputStream(InputStream stream) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            stream.transferTo(baos);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ignored) {
            return new ByteArrayInputStream(new byte[] {});
        }
    }

    public static String readBodyFromInputStream(InputStream stream) {
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

    public static Request createRequestFromCustomHttpRequest(HttpRequest httpRequest, String body) {
        Request request = new Request();
        request.setMethod(httpRequest.method());
        request.setUri(httpRequest.uri());
        request.setHeaders(toHeaders(httpRequest.headers()));
        request.setBody(body);
        return request;
    }

    private static Response createResponseFromCustomHttpResponse(HttpResponse<String> httpResponse) {
        Response response = new Response();
        response.setStatusCode(httpResponse.statusCode());
        response.setHeaders(toHeaders(httpResponse.headers()));
        response.setUri(httpResponse.request().uri());
        response.setBody(httpResponse.body());
        response.setVersion(httpResponse.version());
        return response;
    }

    public static HttpInteraction createInteractionFromCustomHttpResponse(HttpResponse<String> httpResponse,
                                                                          String requestBody) {
        HttpInteraction interaction = new HttpInteraction();

        Request request = createRequestFromCustomHttpRequest(httpResponse.request(), requestBody);
        interaction.setRequest(request);

        Response response = createResponseFromCustomHttpResponse(httpResponse);
        interaction.setResponse(response);

        interaction.setRecordedAt(Instant.now().getEpochSecond());

        return interaction;
    }

    public static Request createRequestFromApacheHttpRequest(org.apache.http.HttpRequest httpRequest) {
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

    private static Response createResponseFromApacheHttpResponse(CloseableHttpResponse httpResponse,
                                                                 org.apache.http.HttpRequest httpRequest) {
        Response response = new Response();
        try {
            response.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            response.setMessage(httpResponse.getStatusLine().getReasonPhrase());
            response.setHeaders(toHeaders(List.of(httpResponse.getAllHeaders())));
            response.setBody(readBodyFromInputStream(httpResponse.getEntity().getContent()));
            response.setUri(URI.create(httpRequest.getRequestLine().getUri()));
            response.setVersion(httpResponse.getProtocolVersion());
        } catch (IOException ignored) {
        }

        return response;
    }

    public static HttpInteraction createInteractionFromApacheHttpRequestAndResponse(CloseableHttpResponse httpResponse,
                                                                                    org.apache.http.HttpRequest httpRequest) {
        HttpInteraction interaction = new HttpInteraction();

        Request request = createRequestFromApacheHttpRequest(httpRequest);
        interaction.setRequest(request);

        Response response = createResponseFromApacheHttpResponse(httpResponse, httpRequest);
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
