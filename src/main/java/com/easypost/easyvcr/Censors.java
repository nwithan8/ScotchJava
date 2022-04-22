package com.easypost.easyvcr;

import com.easypost.easyvcr.internalutilities.Tools;
import com.easypost.easyvcr.internalutilities.json.Serialization;
import com.google.gson.JsonSyntaxException;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Censors {
    private final List<String> _bodyParamsToCensor;
    private final String _censorText;
    private final List<String> _headersToCensor;
    private final List<String> _queryParamsToCensor;

    public Censors() {
        this(Statics.DefaultCensorText);
    }

    public Censors(String censorString) {
        _queryParamsToCensor = new ArrayList<>();
        _bodyParamsToCensor = new ArrayList<>();
        _headersToCensor = new ArrayList<>();
        _censorText = censorString;
    }

    public static Censors Default() {
        return new Censors();
    }

    public static Censors DefaultStrict() {
        Censors censors = new Censors();
        for (String key : Statics.DefaultCredentialHeadersToHide) {
            censors.hideHeader(key);
        }
        for (String key : Statics.DefaultCredentialParametersToHide) {
            censors.hideQueryParameter(key);
            censors.hideBodyParameter(key);
        }
        return censors;
    }

    public Censors hideBodyParameter(String parameterKey) {
        _bodyParamsToCensor.add(parameterKey);
        return this;
    }

    public Censors hideHeader(String headerKey) {
        _headersToCensor.add(headerKey);
        return this;
    }

    public Censors hideQueryParameter(String parameterKey) {
        _queryParamsToCensor.add(parameterKey);
        return this;
    }

    public String applyBodyParametersCensors(String body) {
        if (body == null || body.length() == 0) {
            // short circuit if body is null or empty
            return body;
        }

        Map<String, Object> bodyParameters;
        try {
            bodyParameters = Serialization.convertJsonToObject(body, Map.class);
        } catch (JsonSyntaxException ignored) {
            // short circuit if body is not a JSON dictionary
            return body;
        }

        if (bodyParameters == null || bodyParameters.size() == 0) {
            // short circuit if there are no body parameters
            return body;
        }

        for (String parameterKey : _bodyParamsToCensor) {
            if (bodyParameters.containsKey(parameterKey)) {
                bodyParameters.put(parameterKey, _censorText);
            }
        }

        return Serialization.convertObjectToJson(bodyParameters);
    }

    public Map<String, List<String>> applyHeadersCensors(Map<String, List<String>> headers) {
        if (headers == null || headers.size() == 0) {
            // short circuit if there are no headers to censor
            return headers;
        }

        final Map<String, List<String>> headersCopy = new HashMap<>(headers);

        for (String headerKey : _headersToCensor) {
            if (headersCopy.containsKey(headerKey)) {
                headersCopy.put(headerKey, Collections.singletonList(_censorText));
            }
        }
        return headersCopy;
    }

    public String applyQueryParametersCensors(String url) {
        if (url == null) {
            // short circuit if url is null
            return url;
        }
        URI uri = URI.create(url);
        Map<String, String> queryParameters = Tools.queryParametersToMap(uri);
        if (queryParameters.size() == 0) {
            // short circuit if there are no query parameters to censor
            return url;
        }

        for (String parameterKey : _queryParamsToCensor) {
            if (queryParameters.containsKey(parameterKey)) {
                queryParameters.put(parameterKey, _censorText);
            }
        }

        List<NameValuePair> censoredQueryParametersList = Tools.mapToQueryParameters(queryParameters);
        String formattedQueryParameters = URLEncodedUtils.format(censoredQueryParametersList, StandardCharsets.UTF_8);
        if (formattedQueryParameters.length() == 0) {
            // short circuit if there are no query parameters to censor
            return url;
        }

        return uri.getScheme() + "://" + uri.getHost() + uri.getPath() + "?" + formattedQueryParameters;
    }
}
