package com.easypost.easyvcr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Statics {
    static List<String> DefaultCredentialHeadersToHide = new ArrayList<>(
            Arrays.asList(
                    "Authorization"
            )
    );

    static List<String> DefaultCredentialParametersToHide = new ArrayList<>(
            Arrays.asList(
                    "api_key",
                    "apiKey",
                    "key",
                    "api_token",
                    "apiToken",
                    "token",
                    "access_token",
                    "client_id",
                    "client_secret",
                    "password",
                    "secret",
                    "username"
            )
    );

    static String DefaultCensorText = "*****";
}
