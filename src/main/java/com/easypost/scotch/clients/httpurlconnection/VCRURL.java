package com.easypost.scotch.clients.httpurlconnection;

import com.easypost.scotch.ScotchMode;
import com.easypost.scotch.VCR;
import com.easypost.scotch.cassettes.Cassette;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;

public class VCRURL {

    private final URL url;

    private final VCR vcr;

    public VCRURL(String protocol, String host, int port, String file, URLStreamHandler handler, VCR vcr) throws MalformedURLException {
        this.vcr = vcr;
        this.url = new URL(protocol, host, port, file, handler);
    }

    public VCRURL(String protocol, String host, int port, String file, VCR vcr)
            throws MalformedURLException {
        this(protocol, host, port, file, null, vcr);
    }

    public VCRURL(String protocol, String host, String file, VCR vcr)
            throws MalformedURLException {
        this(protocol, host, -1, file, vcr);
    }

    public VCRURL(URL context, String spec, URLStreamHandler handler, VCR vcr)
            throws MalformedURLException {
        this.vcr = vcr;
        this.url = new URL(context, spec, handler);
    }

    public VCRURL(String spec, VCR vcr) throws MalformedURLException {
        this(null, spec, vcr);
    }

    public VCRURL(URL context, String spec, VCR vcr) throws MalformedURLException {
        this(context, spec, null, vcr);
    }

    public VCRURL(URL context, VCR vcr) {
        this.vcr = vcr;
        this.url = context;
    }

    public VCRHttpUrlConnection openConnection() throws java.io.IOException {
        return new VCRHttpUrlConnection(this.url, this.vcr);
    }

    public VCRHttpUrlConnection openConnection(Proxy proxy) throws java.io.IOException {
        return new VCRHttpUrlConnection(this.url, this.vcr, proxy);
    }
}
