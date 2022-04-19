package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.MatchRules;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;

public class BaseInteractionConverter {

    public HttpInteraction findMatchingInteraction(Cassette cassette, Request request, MatchRules matchRules)
            throws VCRException {
        for (HttpInteraction recordedInteraction : cassette.read()) {
            if (matchRules.requestsMatch(request, recordedInteraction.getRequest())) {
                return recordedInteraction;
            }
        }
        return null;
    }

    protected HttpInteraction _createInteraction(Request request, Response response, long duration) {
        return new HttpInteraction(request, response, duration);
    }

    public static class ResponseAndTime {
        public Response response;
        public long time;

        public ResponseAndTime(Response response, long time) {
            this.response = response;
            this.time = time;
        }
    }
}
