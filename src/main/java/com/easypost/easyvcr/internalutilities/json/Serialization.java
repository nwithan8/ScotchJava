package com.easypost.easyvcr.internalutilities.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class Serialization {
    public static <T> T convertJsonToObject(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    public static <T> T convertJsonToObject(JsonElement json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    public static String convertObjectToJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }
}
