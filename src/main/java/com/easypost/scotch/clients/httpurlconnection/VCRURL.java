package com.easypost.scotch.clients.httpurlconnection;

import com.easypost.scotch.ScotchMode;
import com.easypost.scotch.interaction.HttpInteraction;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;

public class VCRURL {

    private final URL url;

    private final String cassettePath;

    private final ScotchMode mode;

    public VCRURL(String protocol, String host, int port, String file, URLStreamHandler handler, String cassettePath,
                  ScotchMode mode) throws MalformedURLException {
        this.cassettePath = cassettePath;
        this.mode = mode;
        this.url = new URL(protocol, host, port, file, handler);
    }

    public VCRURL(String protocol, String host, int port, String file, String cassettePath, ScotchMode mode)
            throws MalformedURLException {
        this(protocol, host, port, file, null, cassettePath, mode);
    }

    public VCRURL(String protocol, String host, String file, String cassettePath, ScotchMode mode)
            throws MalformedURLException {
        this(protocol, host, -1, file, cassettePath, mode);
    }

    public VCRURL(URL context, String spec, URLStreamHandler handler, String cassettePath, ScotchMode mode)
            throws MalformedURLException {
        this.cassettePath = cassettePath;
        this.mode = mode;
        this.url = new URL(context, spec, handler);
    }

    public VCRURL(String spec, String cassettePath, ScotchMode mode) throws MalformedURLException {
        this(null, spec, cassettePath, mode);
    }

    public VCRURL(URL context, String spec, String cassettePath, ScotchMode mode) throws MalformedURLException {
        this(context, spec, null, cassettePath, mode);
    }

    public VCRHttpUrlConnection openConnection() throws java.io.IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) this.url.openConnection();
        return new VCRHttpUrlConnection(httpURLConnection, this.cassettePath, this.mode);
    }

    /*
    public VCRHttpUrlConnection openConnection(Proxy proxy) throws java.io.IOException {
        VCRHttpUrlConnection connection = (VCRHttpUrlConnection) this.url.openConnection(proxy);
        connection.setCassettePath(this.cassettePath);
        connection.setMode(this.mode);
        return connection;
    }
     */
}
