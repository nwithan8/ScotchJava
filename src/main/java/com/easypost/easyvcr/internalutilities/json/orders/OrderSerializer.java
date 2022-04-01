package com.easypost.easyvcr.internalutilities.json.orders;

import com.easypost.easyvcr.CassetteOrder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OrderSerializer implements JsonSerializer<Object> {

    @Override
    public JsonElement serialize(Object obj, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();
        List<String> fieldNames = getOrderedFieldNames(obj);
        for (String fieldName : fieldNames) {
            try {
                var property = BeanUtils.getProperty(obj, fieldName);
                object.add(fieldName, jsonSerializationContext.serialize(property));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    protected List<String> getOrderedFieldNames(Object obj) {
        return getFieldNames(obj);
    }

    protected List<String> getFieldNames(Object obj) {
        Class<?> componentClass = obj.getClass();
        List<String> fieldNames = new ArrayList<>();

        for (Field field : componentClass.getFields()) {
            fieldNames.add(field.getName());
        }

        return fieldNames;
    }
}
