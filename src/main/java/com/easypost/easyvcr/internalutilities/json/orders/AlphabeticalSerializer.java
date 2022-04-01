package com.easypost.easyvcr.internalutilities.json.orders;

import com.easypost.easyvcr.CassetteOrder;

import java.util.Collections;
import java.util.List;

public class AlphabeticalSerializer extends OrderSerializer {
    private final CassetteOrder.Direction _direction;

    public AlphabeticalSerializer(CassetteOrder.Direction direction) {
        _direction = direction;
    }

    public AlphabeticalSerializer() {
        _direction = CassetteOrder.Direction.Ascending;
    }

    @Override
    protected List<String> getOrderedFieldNames(Object obj) {
        List<String> fieldNames = getFieldNames(obj);
        fieldNames.sort(Collections.reverseOrder().reversed());
        if (_direction == CassetteOrder.Direction.Descending) {
            Collections.reverse(fieldNames);
        }
        return fieldNames;
    }
}
