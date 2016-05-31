/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine;

import com.ekeis.rema.prefs.Prefs;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Elias Keis (30.05.2016)
 */
public class Machine {
    private static final Logger log = Logger.getLogger(Machine.class.getName());
    private List<Register> registers;

    private Set<MachineListener> listeners = new LinkedHashSet<>();
    public enum Flag {ZERO, NEGATIVE, OVERFLOW};

    private Register counter;
    private boolean flagZero, flagNegative, flagOverflow;
    private Program program = new Program();

    public Machine() {
        reset();
    }

    public synchronized void reset(Program program) {
        reset();
        this.program = program;
    }
    public synchronized void reset(String code) {
        reset(new Program(code));
    }

    public synchronized void reset() {
        log.info("Resetting machine ...");
        counter = new Register(1);

        int numRegisters = Prefs.getInstance().getNumberRegisters();
        registers = new ArrayList<>(numRegisters);
        for (int i = 0; i < numRegisters; i++) {
            Register r = new Register();
            registers.add(r);
            r.addListener(new Register.RegisterListener() {
                @Override
                public void onValueSet(RegisterValueSetEvent e) {
                    if (e.wasOverflow()) setFlagOverflow(true);
                }
            });
        }
    }

    public synchronized void step() {
        log.info("Machine performing step ...");
    }
    public synchronized void run() {
        log.info("Machine performing run ...");
    }

    //getters/setters
    public List<Register> getRegisters() {
        return registers;
    }
    public Register getCounter() {
        return counter;
    }

    public boolean isFlagZero() {
        return flagZero;
    }

    public void setFlagZero(boolean flagZero) {
        if (flagZero != this.flagZero) {
            this.flagZero = flagZero;
            onFlagChange(Flag.ZERO, flagZero);
        }
    }

    public boolean isFlagNegative() {
        return flagNegative;
    }

    public void setFlagNegative(boolean flagNegative) {
        if (flagNegative != this.flagNegative) {
            this.flagNegative = flagNegative;
            onFlagChange(Flag.NEGATIVE, flagNegative);
        }
    }

    public boolean isFlagOverflow() {
        return flagOverflow;
    }

    public void setFlagOverflow(boolean flagOverflow) {
        if (flagOverflow != this.flagOverflow) {
            this.flagOverflow = flagOverflow;
            onFlagChange(Flag.OVERFLOW, flagOverflow);
        }
    }

    //listener
    public interface MachineListener {
        void onFlagChange(Machine machine, Flag flag, boolean value);
    }
    protected void onFlagChange(Flag flag, boolean value) {
        for (MachineListener l : new ArrayList<>(listeners)) {
            l.onFlagChange(this, flag, value);
        }
    }
}
