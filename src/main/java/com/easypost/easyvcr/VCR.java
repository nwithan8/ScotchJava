package com.easypost.easyvcr;

import com.easypost.easyvcr.clients.apachehttpclient.RecordableCloseableHttpClient;

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
        return _currentCassette.Name;
    }

    // TODO: Add client
    public RecordableCloseableHttpClient getApacheHttpClient() {
        return new RecordableCloseableHttpClient(this._currentCassette, this._mode, this._advancedSettings);
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
