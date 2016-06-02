/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Elias Keis (30.05.2016)
 */
public class Register {
    private Set<RegisterListener> listeners = new LinkedHashSet<>();
    private long value = 0;
    private long max, min;

    public Register(long min, long max) {
        this.min = min;
        this.max = max;
    }
    public Register(long min, long max, long value) {
        this(min, max);
        this.value = value;
    }

    public long getValue() {
        return value;
    }
    public void setValue(long value) {
        long oldVal = value;
        boolean overflow = false;
        //OVERFLOWCHECK DISABLED!!!, if (value < min || value > max) overflow = true;
        if (overflow) {
            this.value = (value - min) % (max - min) + min;
        } else {
            this.value = value;
        }
        onValueSet(oldVal, this.value, overflow);
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    //listeners stuff
    public synchronized void addListener(RegisterListener l) {
        listeners.add(l);
    }
    public synchronized void removeListener(RegisterListener l) {
        listeners.remove(l);
    }
    public interface RegisterListener {
        final class RegisterValueSetEvent {
            private Register register;
            private long oldVal, newVal;
            boolean wasOverflow;

            public RegisterValueSetEvent(Register register, long oldVal, long newVal, boolean wasOverflow) {
                this.register = register;
                this.oldVal = oldVal;
                this.newVal = newVal;
                this.wasOverflow = wasOverflow;
            }

            public Register getRegister() {
                return register;
            }

            public long getOldVal() {
                return oldVal;
            }

            public long getNewVal() {
                return newVal;
            }

            public boolean wasOverflow() {
                return wasOverflow;
            }
        }
        void onValueSet(RegisterValueSetEvent e);
    }
    protected void onValueSet(long oldVal, long newVal, boolean overflow) {
        RegisterListener.RegisterValueSetEvent e = new RegisterListener.RegisterValueSetEvent(this, oldVal, newVal, overflow);
        for (RegisterListener l : new ArrayList<>(listeners)) {
            l.onValueSet(e);
        }
    }
}
