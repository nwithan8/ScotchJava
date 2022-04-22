package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Censors;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;
import com.easypost.easyvcr.requestelements.Status;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.easypost.easyvcr.internalutilities.Tools.readFromInputStream;

public class HttpUrlConnectionInteractionConverter extends BaseInteractionConverter {
    public Request createRecordedRequest(HttpURLConnection connection, Censors censors) {
        try {
            // collect elements from the connection
            String uriString = connection.getURL().toString();
            Map<String, List<String>> headers = connection.getRequestProperties();
            String method = connection.getRequestMethod();

            // apply censors
            uriString = censors.applyQueryParametersCensors(uriString);
            headers = censors.applyHeadersCensors(headers);

            // create the request
            Request request = new Request();
            request.setMethod(method);
            request.setUri(new URI(uriString));
            request.setHeaders(headers);
            // TODO: Capture and censor body
            // request.setBody(body);

            return request;
        } catch (URISyntaxException ignored) {
        }
        // FIXME: make it so this won't blow up on error
        return null;
    }

    public ResponseAndTime createRecordedResponse(HttpURLConnection connection, Censors censors) {
        try {
            // quickly time how long it takes to get the initial response
            Instant start = Instant.now();
            int responseCode = connection.getResponseCode();
            Instant end = Instant.now();
            long milliseconds = Duration.between(start, end).toMillis();

            // collect elements from the connection
            String message = connection.getResponseMessage();
            String uriString = connection.getURL().toString();
            Map<String, List<String>> headers = connection.getHeaderFields();
            String body = null;
            String errors = null;
            try {
                body = readFromInputStream(connection.getInputStream());
                errors = readFromInputStream(connection.getErrorStream());
            } catch (NullPointerException ignored) {
            }

            // apply censors
            uriString = censors.applyQueryParametersCensors(uriString);
            headers = censors.applyHeadersCensors(headers);
            // we don't censor the response body, only the request body

            // create the response
            Response response = new Response();
            response.setStatus(new Status(responseCode, message));
            response.setUri(new URI(uriString));
            response.setHeaders(headers);
            if (body != null) {
                response.setBody(body);
            }
            if (errors != null) {
                response.setErrors(errors);
            }

            return new ResponseAndTime(response, milliseconds);
        } catch (URISyntaxException | IOException ignored) {
        }
        // FIXME: make it so this won't blow up on error
        return null;
    }

    public HttpInteraction createInteraction(HttpURLConnection connection, Censors censors) {
        Request request = createRecordedRequest(connection, censors);
        ResponseAndTime responseAndTime = createRecordedResponse(connection, censors);
        return _createInteraction(request, responseAndTime.response, responseAndTime.time);
    }
}
