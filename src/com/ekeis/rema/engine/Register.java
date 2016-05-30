package com.ekeis.rema.engine;

import com.ekeis.rema.engine.exceptions.InvalidRegisterValueException;
import com.ekeis.rema.prefs.Prefs;

/**
 * @author Elias Keis (30.05.2016)
 */
public class Register {
    private long value;

    public Register() {
        value = 0;
    }
    public Register(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        long min = Prefs.getInstance().getRegisterMin();
        long max = Prefs.getInstance().getRegisterMax();
        if (value < min || value > max) throw new InvalidRegisterValueException(value, min, max);
        this.value = value;
    }
}
