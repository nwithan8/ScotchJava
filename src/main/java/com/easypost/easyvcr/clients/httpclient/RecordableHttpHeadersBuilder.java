package com.easypost.easyvcr.clients.httpclient;

import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;

/** A mutable builder for collecting and building HTTP headers. */
public class RecordableHttpHeadersBuilder {

    private final TreeMap<String, List<String>> headersMap;

    public RecordableHttpHeadersBuilder() {
        headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    public RecordableHttpHeadersBuilder structuralCopy() {
        RecordableHttpHeadersBuilder builder = new RecordableHttpHeadersBuilder();
        for (Map.Entry<String, List<String>> entry : headersMap.entrySet()) {
            List<String> valuesCopy = new ArrayList<>(entry.getValue());
            builder.headersMap.put(entry.getKey(), valuesCopy);
        }
        return builder;
    }

    public void addHeader(String name, String value) {
        headersMap.computeIfAbsent(name, k -> new ArrayList<>(1))
                .add(value);
    }

    public void setHeader(String name, String value) {
        // headers typically have one value
        List<String> values = new ArrayList<>(1);
        values.add(value);
        headersMap.put(name, values);
    }

    public void clear() {
        headersMap.clear();
    }

    public Map<String, List<String>> map() {
        return headersMap;
    }

    public HttpHeaders build() {
        return HttpHeaders.of(headersMap, new BiPredicate<String, String>() {
            @Override
            public boolean test(String s, String s2) {
                return true;
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(" { ");
        sb.append(map());
        sb.append(" }");
        return sb.toString();
    }
}

