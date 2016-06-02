/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.Register;

/**
 * @author Elias Keis (31.05.2016)
 */
public class BitwiseCommand extends Command {

    public enum Type {NOT, ASL, ASR, LSR};

    private Type type;

    public BitwiseCommand(Machine machine, int line, Type type) {
        super(machine, line);
        this.type = type;
    }

    @Override
    public void perform() {
        Register Ra = machine.getAkku();
        long a = Ra.getValue();
        long aOld = a;
        String msg;
        switch (type) {
            case NOT:
                a = ~a;
                msg = "bitwise.not";
                break;
            case LSR:
                a >>>= 1;
                msg = "bitwise.lsr";
                break;
            case ASR:
                a >>= 1;
                msg = "bitwise.asr";
                break;
            case ASL:
                a <<= 1;
                msg = "bitwise.asl";
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        Ra.setValue(a);
        machine.increaseIP();
        logExecution(msg, Long.toBinaryString(aOld));
    }
}
