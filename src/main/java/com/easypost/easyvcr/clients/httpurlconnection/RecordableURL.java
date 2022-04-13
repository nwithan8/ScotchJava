package com.easypost.easyvcr.clients.httpurlconnection;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.Mode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;

public class RecordableURL {

    private final URL url;
    private final Cassette cassette;
    private final Mode mode;
    private final AdvancedSettings advancedSettings;

    public RecordableURL(String protocol, String host, int port, String file, URLStreamHandler handler,
                         Cassette cassette, Mode mode, AdvancedSettings advancedSettings) throws MalformedURLException {
        this.url = new URL(protocol, host, port, file, handler);
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
    }

    public RecordableURL(String protocol, String host, int port, String file, URLStreamHandler handler,
                         Cassette cassette, Mode mode) throws MalformedURLException {
        this(protocol, host, port, file, handler, cassette, mode, new AdvancedSettings());
    }

    public RecordableURL(String protocol, String host, int port, String file, Cassette cassette, Mode mode,
                         AdvancedSettings advancedSettings) throws MalformedURLException {
        this(protocol, host, port, file, null, cassette, mode, advancedSettings);
    }

    public RecordableURL(String protocol, String host, int port, String file, Cassette cassette, Mode mode)
            throws MalformedURLException {
        this(protocol, host, port, file, cassette, mode, new AdvancedSettings());
    }

    public RecordableURL(String protocol, String host, String file, Cassette cassette, Mode mode,
                         AdvancedSettings advancedSettings) throws MalformedURLException {
        this(protocol, host, -1, file, cassette, mode, advancedSettings);
    }

    public RecordableURL(String protocol, String host, String file, Cassette cassette, Mode mode)
            throws MalformedURLException {
        this(protocol, host, file, cassette, mode, new AdvancedSettings());
    }

    public RecordableURL(URL context, String spec, URLStreamHandler handler, Cassette cassette, Mode mode,
                         AdvancedSettings advancedSettings) throws MalformedURLException {
        this.url = new URL(context, spec, handler);
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
    }

    public RecordableURL(URL context, String spec, URLStreamHandler handler, Cassette cassette, Mode mode)
            throws MalformedURLException {
        this(context, spec, handler, cassette, mode, new AdvancedSettings());
    }

    public RecordableURL(String spec, Cassette cassette, Mode mode, AdvancedSettings advancedSettings)
            throws MalformedURLException {
        this(null, spec, cassette, mode, advancedSettings);
    }

    public RecordableURL(String spec, Cassette cassette, Mode mode) throws MalformedURLException {
        this(spec, cassette, mode, new AdvancedSettings());
    }

    public RecordableURL(URL context, String spec, Cassette cassette, Mode mode, AdvancedSettings advancedSettings)
            throws MalformedURLException {
        this(context, spec, null, cassette, mode, advancedSettings);
    }

    public RecordableURL(URL context, String spec, Cassette cassette, Mode mode) throws MalformedURLException {
        this(context, spec, cassette, mode, new AdvancedSettings());
    }

    public RecordableURL(URL context, Cassette cassette, Mode mode, AdvancedSettings advancedSettings)
            throws MalformedURLException {
        this.url = context;
        this.cassette = cassette;
        this.mode = mode;
        this.advancedSettings = advancedSettings;
    }

    public RecordableURL(URL context, Cassette cassette, Mode mode) throws MalformedURLException {
        this(context, cassette, mode, new AdvancedSettings());
    }

    public RecordableHttpURLConnection openConnection() throws java.io.IOException {
        return new RecordableHttpURLConnection(this.url, this.cassette, this.mode, this.advancedSettings);
    }

    public RecordableHttpURLConnection openConnection(Proxy proxy) throws java.io.IOException {
        return new RecordableHttpURLConnection(this.url, proxy, this.cassette, this.mode, this.advancedSettings);
    }

    public RecordableHttpsURLConnection openConnectionSecure() throws IOException {
        return new RecordableHttpsURLConnection(this.url, this.cassette, this.mode, this.advancedSettings);
    }

    public RecordableHttpsURLConnection openConnectionSecure(Proxy proxy) throws IOException {
        return new RecordableHttpsURLConnection(this.url, proxy, this.cassette, this.mode, this.advancedSettings);
    }
}
