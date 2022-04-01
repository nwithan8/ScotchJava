package com.easypost.easyvcr.requestelements;

import org.apache.http.ProtocolVersion;

import java.net.http.HttpClient;
import java.util.List;

public class HttpVersion {
    private String protocol;

    private int minor = 0;

    private int major = 0;

    public HttpVersion(String version) {
        this.protocol = version;
    }

    public HttpVersion(HttpClient.Version version) {
        List<String> elements = List.of(version.name().split("_"));
        try {
            this.protocol = elements.get(0);
            this.major = Integer.parseInt(elements.get(1));
            this.minor = Integer.parseInt(elements.get(2));
        } catch (Exception ignored) {
        }
    }

    public HttpVersion(ProtocolVersion version) {
        this.protocol = version.getProtocol();
        this.major = version.getMajor();
        this.minor = version.getMinor();
    }

    public HttpClient.Version asHttpClientVersion() {
        String combinedName = this.protocol + "_" + this.major;
        if (this.minor > 0) {
            combinedName += "_" + this.minor;
        }
        return HttpClient.Version.valueOf(combinedName);
    }

    public ProtocolVersion asProtocolVersion() {
        return new ProtocolVersion(this.protocol, this.major, this.minor);
    }

    public String toString() {
        String string = this.protocol;
        if (this.major > 0) {
            string += "/" + this.major + "." + this.minor;
        }
        return string;
    }
}
