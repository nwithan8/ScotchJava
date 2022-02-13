package com.easypost.scotch;

import com.easypost.scotch.clients.scotchhttpclient.VCRScotchHttpClient;

import java.net.http.HttpClient;

public class HttpClients {
    public static VCRScotchHttpClient NewVCRScotchHttpClient(HttpClient httpClient, String cassettePath, ScotchMode mode)
    {
        return new VCRScotchHttpClient(httpClient, cassettePath, mode);
    }

    public static VCRScotchHttpClient NewVCRScotchHttpClient(String cassettePath, ScotchMode mode)
    {
        return NewVCRScotchHttpClient(HttpClient.newHttpClient(), cassettePath, mode);
    }
}
