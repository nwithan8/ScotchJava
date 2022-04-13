package com.easypost.easyvcr;

import com.easypost.easyvcr.internalutilities.Tools;
import com.easypost.easyvcr.requestelements.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class MatchRules {
    private final List<BiFunction<Request, Request, Boolean>> _rules;

    public MatchRules() {
        _rules = new ArrayList<>();
    }

    public static MatchRules Default() {
        return new MatchRules().byMethod().byFullUrl();
    }

    public static MatchRules DefaultStrict() {
        return new MatchRules().byMethod().byFullUrl().byBody();
    }

    private void by(BiFunction<Request, Request, Boolean> rule) {
        _rules.add(rule);
    }

    public MatchRules byBaseUrl() {
        by((received, recorded) -> {
            String receivedUri = received.getUri().getPath();
            String recordedUri = recorded.getUri().getPath();
            return receivedUri.equalsIgnoreCase(recordedUri);
        });
        return this;
    }

    public MatchRules byBody() {
        by((received, recorded) -> {
            if (received.getBody() == null && recorded.getBody() == null)
            // both have null bodies, so they match
            {
                return true;
            }

            if (received.getBody() == null || recorded.getBody() == null)
            // one has a null body, so they don't match
            {
                return false;
            }

            // convert body to base64string to assist comparison by removing special characters
            var receivedBody = Tools.toBase64String(received.getBody());
            var recordedBody = Tools.toBase64String(recorded.getBody());
            return receivedBody.equalsIgnoreCase(recordedBody);
        });
        return this;
    }

    public MatchRules byEverything() {
        by((received, recorded) -> {
            String receivedRequest = received.ToJson();
            String recordedRequest = recorded.ToJson();
            return receivedRequest.equalsIgnoreCase(recordedRequest);
        });
        return this;
    }

    public MatchRules byFullUrl() {
        return byFullUrl(false);
    }

    public MatchRules byFullUrl(boolean exact) {
        if (exact) {
            by((received, recorded) -> {
                var receivedUri = Tools.toBase64String(received.getUriString());
                var recordedUri = Tools.toBase64String(recorded.getUriString());
                return receivedUri.equalsIgnoreCase(recordedUri);
            });
        } else {
            byBaseUrl();
            by((received, recorded) -> {
                Map<String, String> receivedQuery = Tools.queryParametersToMap(received.getUri());
                Map<String, String> recordedQuery = Tools.queryParametersToMap(recorded.getUri());
                if (receivedQuery.size() != recordedQuery.size()) {
                    return false;
                }
                for (Map.Entry<String, String> entry : receivedQuery.entrySet()) {
                    if (!recordedQuery.containsKey(entry.getKey())) {
                        return false;
                    }
                }
                return true;
            });
        }

        return this;
    }

    public MatchRules byHeader(String name) {
        by((received, recorded) -> {
            Map<String, List<String>> receivedHeaders = received.getHeaders();
            Map<String, List<String>> recordedHeaders = recorded.getHeaders();
            if (!receivedHeaders.containsKey(name) || !recordedHeaders.containsKey(name)) {
                return false;
            }
            List<String> receivedHeader = receivedHeaders.get(name);
            List<String> recordedHeader = recordedHeaders.get(name);
            return receivedHeader.equals(recordedHeader);
        });
        return this;
    }

    public MatchRules byHeaders() {
        return byHeaders(false);
    }

    public MatchRules byHeaders(boolean exact) {
        if (exact) {
            // first, we'll check that there are the same number of headers in both requests. If they're are, then the second check is guaranteed to compare all headers.
            by((received, recorded) -> received.getHeaders().size() == recorded.getHeaders().size());
        }

        by((received, recorded) -> {
            Map<String, List<String>> receivedHeaders = received.getHeaders();
            Map<String, List<String>> recordedHeaders = recorded.getHeaders();
            for (String headerName : receivedHeaders.keySet()) {
                if (!recordedHeaders.containsKey(headerName)) {
                    return false;
                }
                if (!receivedHeaders.get(headerName).equals(recordedHeaders.get(headerName))) {
                    return false;
                }
            }
            return true;
        });
        return this;
    }

    public MatchRules byMethod() {
        by((received, recorded) -> received.getMethod().equalsIgnoreCase(recorded.getMethod()));
        return this;
    }

    public boolean requestsMatch(Request receivedRequest, Request recordedRequest) {
        if (_rules.size() == 0) {
            return true;
        }

        for (BiFunction<Request, Request, Boolean> rule : _rules) {
            if (!rule.apply(receivedRequest, recordedRequest)) {
                return false;
            }
        }
        return true;
    }
}
