package com.easypost.easyvcr.requestelements;

import com.easypost.easyvcr.internalutilities.json.Serialization;

public class HttpElement {
    public String ToJson() {
        return Serialization.convertObjectToJson(this);
    }
}
