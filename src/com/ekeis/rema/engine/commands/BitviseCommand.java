/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.Register;

/**
 * @author Elias Keis (31.05.2016)
 */
public class BitviseCommand extends Command {

    public enum Type {NOT, ASL, ASR, LSR};

    private Type type;

    public BitviseCommand(Machine machine, int line, Type type) {
        super(machine, line);
        this.type = type;
    }

    @Override
    public void perform() {
        Register Ra = machine.getAkku();
        long a = Ra.getValue();
        String msg;
        switch (type) {
            case NOT:
                a = ~a;
                msg = "bitvise.not";
                break;
            case LSR:
                a >>>= 1;
                msg = "bitvise.lsr";
                break;
            case ASR:
                a >>= 1;
                msg = "bitvise.asr";
                break;
            case ASL:
                a <<= 1;
                msg = "bitvise.asl";
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        Ra.setValue(a);
        machine.increaseIP();
        logExecution(msg);
    }
}
