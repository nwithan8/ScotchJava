package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;
import com.easypost.easyvcr.requestelements.Status;
import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

public class ApacheInteractionConverter extends BaseInteractionConverter {
    public Request createRequest(org.apache.http.HttpRequest httpRequest, Censors censors) {
        try {
            // collect elements from the connection
            String method = httpRequest.getRequestLine().getMethod();
            String uriString = httpRequest.getRequestLine().getUri();
            Map<String, List<String>> headers = toHeaders(List.of(httpRequest.getAllHeaders()));
            String body = null;
            if (httpRequest instanceof HttpPost) {
                body = readFromInputStream(((HttpPost) httpRequest).getEntity().getContent());
            } else if (httpRequest instanceof HttpPut) {
                body = readFromInputStream(((HttpPut) httpRequest).getEntity().getContent());
            } else if (httpRequest instanceof HttpPatch) {
                body = readFromInputStream(((HttpPatch) httpRequest).getEntity().getContent());
            }

            // apply censors
            uriString = censors.censorQueryParameters(uriString);
            headers = censors.censorHeaders(headers);
            body = censors.censorBodyParameters(body);

            // create the request
            Request request = new Request();
            request.setUri(URI.create(uriString));
            request.setMethod(method);
            request.setHeaders(headers);
            request.setBody(body);
            return request;
        } catch (IOException ignored) {
        }
        // FIXME: make it so this won't blow up on error
        return null;
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

    public ResponseAndTime createResponse(CloseableHttpResponse httpResponse, org.apache.http.HttpRequest httpRequest,
                                          Censors censors) {
        try {
            // quickly time how long it takes to get the initial response
            Instant start = Instant.now();
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            Instant end = Instant.now();
            long milliseconds = Duration.between(start, end).toMillis();

            // collect elements from the connection
            ProtocolVersion httpVersion = httpResponse.getProtocolVersion();
            String message = httpResponse.getStatusLine().getReasonPhrase();
            String uriString = httpRequest.getRequestLine().getUri();
            Map<String, List<String>> headers = toHeaders(List.of(httpResponse.getAllHeaders()));
            String body = readFromInputStream(httpResponse.getEntity().getContent());

            // apply censors
            uriString = censors.censorQueryParameters(uriString);
            headers = censors.censorHeaders(headers);
            body = censors.censorBodyParameters(body);

            // create the response
            Response response = new Response();
            response.setHttpVersion(httpVersion);
            response.setStatus(new Status(responseCode, message));
            response.setUri(URI.create(uriString));
            response.setHeaders(headers);
            response.setBody(body);

            return new ResponseAndTime(response, milliseconds);
        } catch (IOException ignored) {
        }
        // FIXME: make it so this won't blow up on error
        return null;
    }

    public HttpInteraction createInteraction(CloseableHttpResponse httpResponse,
                                             org.apache.http.HttpRequest httpRequest, Censors censors) {
        Request request = createRequest(httpRequest, censors);
        ResponseAndTime responseAndTime = createResponse(httpResponse, httpRequest, censors);
        return _createInteraction(request, responseAndTime.response, responseAndTime.time);
    }
}
