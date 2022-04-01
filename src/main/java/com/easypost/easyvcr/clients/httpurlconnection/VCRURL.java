package com.easypost.easyvcr.clients.httpurlconnection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLStreamHandler;

public class VCRURL {

    private final URL url;

    private final OldVCR oldVcr;

    public VCRURL(String protocol, String host, int port, String file, URLStreamHandler handler, OldVCR oldVcr)
            throws MalformedURLException {
        this.oldVcr = oldVcr;
        this.url = new URL(protocol, host, port, file, handler);
    }

    public VCRURL(String protocol, String host, int port, String file, OldVCR oldVcr) throws MalformedURLException {
        this(protocol, host, port, file, null, oldVcr);
    }

    public VCRURL(String protocol, String host, String file, OldVCR oldVcr) throws MalformedURLException {
        this(protocol, host, -1, file, oldVcr);
    }

    public VCRURL(URL context, String spec, URLStreamHandler handler, OldVCR oldVcr) throws MalformedURLException {
        this.oldVcr = oldVcr;
        this.url = new URL(context, spec, handler);
    }

    public VCRURL(String spec, OldVCR oldVcr) throws MalformedURLException {
        this(null, spec, oldVcr);
    }

    public VCRURL(URL context, String spec, OldVCR oldVcr) throws MalformedURLException {
        this(context, spec, null, oldVcr);
    }

    public VCRURL(URL context, OldVCR oldVcr) {
        this.oldVcr = oldVcr;
        this.url = context;
    }

    public VCRHttpURLConnection openConnection() throws java.io.IOException {
        return new VCRHttpURLConnection(this.url, this.oldVcr);
    }

    public VCRHttpURLConnection openConnection(Proxy proxy) throws java.io.IOException {
        return new VCRHttpURLConnection(this.url, this.oldVcr, proxy);
    }

    public VCRHttpsURLConnection openConnectionSecure() throws IOException {
        return new VCRHttpsURLConnection(this.url, this.oldVcr);
    }

    public VCRHttpsURLConnection openConnectionSecure(Proxy proxy) throws IOException {
        return new VCRHttpsURLConnection(this.url, this.oldVcr, proxy);
    }
}
