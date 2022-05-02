package com.easypost.easyvcr;

import com.easypost.easyvcr.internalutilities.Files;
import com.easypost.easyvcr.internalutilities.Tools;
import com.easypost.easyvcr.internalutilities.json.Serialization;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Cassette {

    public final String name;

    private final String _filePath;

    private boolean _locked;

    public Cassette(String folderPath, String cassetteName) {
        name = cassetteName;
        _filePath = Tools.getFilePath(folderPath, cassetteName + ".json");
    }

    public int numInteractions() {
        try {
            return read().size();
        } catch (VCRException ex) {
            return 0;
        }
    }

    private File getFile() {
        return Tools.getFile(_filePath);
    }

    public void erase() {
        getFile().delete();
    }

    public void lock() {
        _locked = true;
    }

    public void unlock() {
        _locked = false;
    }

    public List<HttpInteraction> read() throws VCRException {
        checkIfLocked();

        if (!fileExists()) {
            return new ArrayList<>();
        }

        ArrayList<HttpInteraction> interactions = new ArrayList<>();

        String jsonString = Files.readFile(_filePath);
        if (jsonString == null) {
            return interactions; // empty list because file doesn't exist or is empty
        }
        JsonElement cassetteParseResult = JsonParser.parseString(jsonString);

        Gson gson = new Gson();
        for (JsonElement interaction : cassetteParseResult.getAsJsonArray()) {
            interactions.add(Serialization.convertJsonToObject(interaction, HttpInteraction.class));
        }
        return interactions;
    }

    public void updateInteraction(HttpInteraction httpInteraction, MatchRules matchRules, boolean bypassSearch)
            throws VCRException {
        List<HttpInteraction> existingInteractions = read();
        int matchingIndex = -1;
        if (!bypassSearch) {
            for (int i = 0; i < existingInteractions.size(); i++) {
                if (matchRules.requestsMatch(existingInteractions.get(i).getRequest(), httpInteraction.getRequest())) {
                    matchingIndex = i;
                    break;
                }
            }
        }
        if (matchingIndex < 0) {
            existingInteractions.add(httpInteraction);
        } else {
            existingInteractions.set(matchingIndex, httpInteraction);
        }

        write(existingInteractions);
    }

    private void checkIfLocked() throws VCRException {
        if (_locked) {
            throw new VCRException("Cassette is locked.");
        }
    }

    private boolean fileExists() {
        return getFile().exists();
    }

    private void write(List<HttpInteraction> httpInteractions) throws VCRException {
        checkIfLocked();


        String cassetteString = Serialization.convertObjectToJson(httpInteractions);

        Files.writeFile(_filePath, cassetteString);
    }
}
