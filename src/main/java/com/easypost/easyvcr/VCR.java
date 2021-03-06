package com.easypost.easyvcr;

import com.easypost.easyvcr.clients.apachehttpclient.RecordableCloseableHttpClient;
import com.easypost.easyvcr.clients.httpclient.RecordableHttpRequest;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class VCR {
    private Cassette _currentCassette;
    private Mode _mode;
    private AdvancedSettings _advancedSettings;

    public VCR(AdvancedSettings advancedSettings) {
        _advancedSettings = advancedSettings;
    }

    public VCR() {
        _advancedSettings = new AdvancedSettings();
    }

    public String getCassetteName() {
        if (_currentCassette == null) {
            return null;
        }
        return _currentCassette.name;
    }

    public RecordableCloseableHttpClient getApacheHttpClient() {
        throw new UnsupportedOperationException("ApacheHttpClient is not supported in this version of EasyVCR");
        // return new RecordableCloseableHttpClient(this._currentCassette, this._mode, this._advancedSettings);
    }

    public RecordableHttpRequest.Builder getHttpClientBuilder(URI uri) {
        throw new UnsupportedOperationException("HttpClient is not supported in this version of EasyVCR");
        // return RecordableHttpRequest.newBuilder(uri, this._currentCassette, this._mode, this._advancedSettings);
    }

    public RecordableURL getHttpUrlConnection(URL url) throws MalformedURLException {
        return new RecordableURL(url, this._currentCassette, this._mode, this._advancedSettings);
    }

    public RecordableURL getHttpUrlConnection(String url) throws MalformedURLException {
        return new RecordableURL(url, this._currentCassette, this._mode, this._advancedSettings);
    }

    public Mode getMode() {
        if (_mode == Mode.Bypass) {
            return Mode.Bypass;
        }
        Mode environmentMode = getModeFromEnvironment();
        return environmentMode != null ? environmentMode : _mode;
    }

    private void setMode(Mode mode) {
        _mode = mode;
    }

    public AdvancedSettings getAdvancedSettings() {
        return _advancedSettings;
    }

    public void setAdvancedSettings(AdvancedSettings advancedSettings) {
        _advancedSettings = advancedSettings;
    }

    public void eject() {
        _currentCassette = null;
    }

    public void erase() {
        if (_currentCassette != null) {
            _currentCassette.erase();
        }
    }

    public void insert(Cassette cassette) {
        _currentCassette = cassette;
    }

    public void pause() {
        setMode(Mode.Bypass);
    }

    public void record() {
        setMode(Mode.Record);
    }

    public void replay() {
        setMode(Mode.Replay);
    }

    public void recordIfNeeded() {
        setMode(Mode.Auto);
    }

    private Mode getModeFromEnvironment() {
        final String keyName = "EASYVCR_MODE";
        try {
            String keyValue = System.getenv(keyName);
            if (keyValue == null) {
                return null;
            }
            return Mode.valueOf(keyValue);
        } catch (Exception ignored) {
            return null;
        }
    }
}
