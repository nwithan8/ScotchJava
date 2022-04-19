package com.easypost.easyvcr.internalutilities.json;

import com.easypost.easyvcr.internalutilities.json.orders.OrderSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Serialization {
    public static <T> T convertJsonToObject(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    public static <T> List<T> convertJsonToListOfObjects(String json, Class<T> clazz) {
        Type typeOfT = TypeToken.getParameterized(List.class, clazz).getType();
        return (new Gson()).fromJson(json, typeOfT);
    }

    public static String convertObjectToJson(Object object) {
        return convertObjectToJson(object, null);
    }

    public static String convertObjectToJson(Object obj, OrderSerializer orderSerializer) {
        Gson gson = new Gson();
        /*
        if (orderSerializer != null) {
            gson = new GsonBuilder().registerTypeAdapter(obj.getClass(), orderSerializer).create();
        }
         */
        return gson.toJson(obj);
    }
}
