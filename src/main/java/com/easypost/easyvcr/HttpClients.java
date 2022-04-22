package com.easypost.easyvcr;

import com.easypost.easyvcr.clients.apachehttpclient.RecordableCloseableHttpClient;
import com.easypost.easyvcr.clients.httpclient.RecordableHttpRequest;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpClients {

    public static Object newClient(HttpClientType type, String url, Cassette cassette,
                                   Mode mode, AdvancedSettings advancedSettings)
            throws URISyntaxException, IOException {
        switch (type) {
            case Apache:
                return newApacheClient(cassette, mode, advancedSettings);
            case Java11HttpClient:
                return newHttpClient(url, cassette, mode, advancedSettings);
            case HttpUrlConnection:
                return newHttpURLConnection(url, cassette, mode, advancedSettings);
            case HttpsUrlConnection:
                return newHttpsURLConnection(url, cassette, mode, advancedSettings);
            default:
                throw new IllegalArgumentException("Unsupported HttpClientType: " + type);
        }
    }

    public static Object newClient(HttpClientType type, String url, Cassette cassette,
                                   Mode mode) throws URISyntaxException, IOException {
        return newClient(type, url, cassette, mode, null);
    }

    public static RecordableHttpRequest newHttpClient(String url, Cassette cassette, Mode mode,
                                                      AdvancedSettings advancedSettings) throws URISyntaxException {
        throw new UnsupportedOperationException("Not implemented yet");
        // return RecordableHttpRequest.newBuilder(new URI(url), cassette, mode, advancedSettings).build();
    }

    public static RecordableHttpRequest newHttpClient(String url, Cassette cassette, Mode mode)
            throws URISyntaxException {
        return newHttpClient(url, cassette, mode, null);
    }

    public static RecordableCloseableHttpClient newApacheClient(Cassette cassette, Mode mode,
                                                                AdvancedSettings advancedSettings) {
        throw new UnsupportedOperationException("Not implemented yet");
        // return new RecordableCloseableHttpClient(cassette, mode, advancedSettings);
    }

    public static RecordableCloseableHttpClient newApacheClient(Cassette cassette, Mode mode) {
        return newApacheClient(cassette, mode, null);
    }

    private static RecordableURL newRecordableURL(String url, Cassette cassette, Mode mode,
                                                  AdvancedSettings advancedSettings) throws MalformedURLException {
        return new RecordableURL(url, cassette, mode, advancedSettings);
    }

    public static RecordableHttpURLConnection newHttpURLConnection(String url, Cassette cassette, Mode mode,
                                                                   AdvancedSettings advancedSettings)
            throws IOException {
        return newRecordableURL(url, cassette, mode, advancedSettings).openConnection();
    }

    public static RecordableHttpURLConnection newHttpURLConnection(String url, Cassette cassette, Mode mode) throws IOException {
        return newRecordableURL(url, cassette, mode, null).openConnection();
    }

    public static RecordableHttpsURLConnection newHttpsURLConnection(String url, Cassette cassette, Mode mode,
                                                                     AdvancedSettings advancedSettings)
            throws IOException {
        return newRecordableURL(url, cassette, mode, advancedSettings).openConnectionSecure();
    }

    public static RecordableHttpsURLConnection newHttpsURLConnection(String url, Cassette cassette, Mode mode)
            throws IOException {
        return newRecordableURL(url, cassette, mode, null).openConnectionSecure();
    }
}
