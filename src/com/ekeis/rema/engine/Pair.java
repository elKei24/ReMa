/*
 * Copyright (c) 2017 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine;

public class Pair<KeyType, ValueType> {
    private KeyType key;
    private ValueType value;

    public Pair(KeyType key, ValueType value) {
        this.key = key;
        this.value = value;
    }

    public KeyType getKey() {
        return key;
    }

    public void setKey(KeyType key) {
        this.key = key;
    }

    public ValueType getValue() {
        return value;
    }

    public void setValue(ValueType value) {
        this.value = value;
    }
}
