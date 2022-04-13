package com.easypost.easyvcr.interactionconverters;

import com.easypost.easyvcr.Cassette;
import com.easypost.easyvcr.MatchRules;
import com.easypost.easyvcr.VCRException;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import com.easypost.easyvcr.requestelements.Request;
import com.easypost.easyvcr.requestelements.Response;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BaseInteractionConverter {

    public InputStream copyInputStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        try {
            stream.reset();
        } catch (IOException ignored) {
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = stream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException ignored) {
            return new ByteArrayInputStream(new byte[] { });
        }
    }

    public InputStream createInputStream(String string) {
        if (string == null) {
            return new ByteArrayInputStream(new byte[] { });
        }
        return new ByteArrayInputStream(string.getBytes());
    }

    public String readBodyFromInputStream(InputStream stream) {
        if (stream == null) {
            return null;
        }
        InputStream copy = copyInputStream(stream);
        String body = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(copy));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            body = content.toString();
        } catch (IOException ignored) {
        }
        return body;
    }

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
