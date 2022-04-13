package com.easypost.easyvcr.internalutilities;

import com.easypost.easyvcr.AdvancedSettings;
import com.easypost.easyvcr.requestelements.HttpInteraction;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Tools {
    public static File getFile(String filePath) {
        if (filePath == null) {
            return null;
        }
        return Paths.get(filePath).toFile();
    }

    public static String getFilePath(String folderPath, String fileName) {
        return Paths.get(folderPath, fileName).toString();
    }

    public static String toBase64String(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static Map<String, String> queryParametersToMap(URI uri) {
        List<NameValuePair> receivedQueryDict =
                URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        if (receivedQueryDict == null || receivedQueryDict.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> queryDict = new java.util.Hashtable<>();
        for (NameValuePair pair : receivedQueryDict) {
            queryDict.put(pair.getName(), pair.getValue());
        }
        return queryDict;
    }

    public static List<NameValuePair> mapToQueryParameters(Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return Collections.emptyList();
        }
        List<NameValuePair> nvpList = new ArrayList<>(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            nvpList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return nvpList;
    }

    public static void simulateDelay(HttpInteraction interaction, AdvancedSettings advancedSettings) throws InterruptedException {
        if (advancedSettings.simulateDelay) {
            Thread.sleep(interaction.getDuration());
        } else {
            Thread.sleep(advancedSettings.manualDelay);
        }
    }
}
