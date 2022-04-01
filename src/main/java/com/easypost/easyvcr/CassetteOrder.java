package com.easypost.easyvcr;

import com.easypost.easyvcr.internalutilities.json.orders.AlphabeticalSerializer;
import com.easypost.easyvcr.internalutilities.json.orders.OrderSerializer;

public class CassetteOrder {
    public enum Direction {
        Ascending, Descending
    }

    public static class OrderOption {
        public OrderSerializer serializer;

        protected OrderOption(OrderSerializer orderSerializer) {
            this.serializer = orderSerializer;
        }
    }

    public static class None extends OrderOption {
        public None() {
            super(null);
        }
    }


    public static class Alphabetical extends OrderOption {
        public Alphabetical() {
            this(Direction.Ascending);
        }

        public Alphabetical(Direction direction) {
            super(new AlphabeticalSerializer(direction));
        }
    }
}
