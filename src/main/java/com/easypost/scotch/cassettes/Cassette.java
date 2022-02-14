package com.easypost.scotch.cassettes;

import com.easypost.scotch.interaction.Helpers;
import com.easypost.scotch.interaction.HttpInteraction;
import com.easypost.scotch.interaction.Request;
import com.easypost.scotch.tools.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class Cassette {
    protected final String cassettePath;

    public Cassette(String cassettePath) {
        this.cassettePath = cassettePath;
    }

    private static List<HttpInteraction> readCassette(Cassette cassette) {
        ArrayList<HttpInteraction> interactions = new ArrayList<HttpInteraction>();

        String jsonString = Files.readFile(cassette.cassettePath);
        if (jsonString == null) {
            return interactions; // empty list because file doesn't exist or is empty
        }
        JsonElement cassetteParseResult = JsonParser.parseString(jsonString);

        Gson gson = new Gson();
        for (JsonElement interaction : cassetteParseResult.getAsJsonArray()) {
            interactions.add(gson.fromJson(interaction, HttpInteraction.class));
        }
        return interactions;
    }

    private static void writeCassette(Cassette cassette, List<HttpInteraction> interactions) {
        Gson gson = new Gson();
        String cassetteString = gson.toJson(interactions);
        Files.writeFile(cassette.cassettePath, cassetteString);
    }

    private static int findMatchingInteraction(List<HttpInteraction> existingInteractions,
                                               HttpInteraction interactionToMatch) {
        int matchingIndex = -1;
        for (int i = 0; i < existingInteractions.size(); i++) {
            if (Helpers.interactionRequestsMatch(existingInteractions.get(i), interactionToMatch)) {
                matchingIndex = i;
                break;
            }
        }
        return matchingIndex;
    }

    public static void updateInteractionOnCassette(Cassette cassette, HttpInteraction interaction) {
        List<HttpInteraction> existingInteractions = readCassette(cassette);
        int matchingIndex = findMatchingInteraction(existingInteractions, interaction);

        if (matchingIndex < 0) {
            existingInteractions.add(interaction);
        } else {
            existingInteractions.set(matchingIndex, interaction);
        }

        writeCassette(cassette, existingInteractions);
    }

    public static HttpInteraction findInteractionMatchingRequestOnCassette(Cassette cassette, Request request) {
        List<HttpInteraction> existingInteractions = readCassette(cassette);
        HttpInteraction interactionToMatch = new HttpInteraction();
        interactionToMatch.setRequest(request);
        int matchingIndex = findMatchingInteraction(existingInteractions, interactionToMatch);

        if (matchingIndex < 0) {
            return null;
        } else {
            return existingInteractions.get(matchingIndex);
        }
    }

    public void updateInteraction(HttpInteraction interaction) {
        updateInteractionOnCassette(this, interaction);
    }

    public HttpInteraction findInteractionMatchingRequest(Request request) {
        return findInteractionMatchingRequestOnCassette(this, request);
    }
}
