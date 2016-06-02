/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.Register;

/**
 * @author Elias Keis (31.05.2016)
 */
public class Bitwise2Command extends Command {

    public enum Type {OR, AND, XOR};

    private Type type;
    private int register;

    public Bitwise2Command(Machine machine, int line, int register, Type type) {
        super(machine, line);
        this.type = type;
        this.register = register;
    }

    @Override
    public void perform() {
        Register Ra = machine.getAkku();
        Register Rx = machine.getRegister(index, register);
        long a = Ra.getValue();
        long aOld = a;
        long b = Rx.getValue();
        String msg;
        switch (type) {
            case OR:
                a |= b;
                msg = "bitwise.or";
                break;
            case XOR:
                a ^= b;
                msg = "bitwise.xor";
                break;
            case AND:
                a &= b;
                msg = "bitwise.and";
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        Ra.setValue(a);
        machine.increaseIP();
        logExecution(msg, Long.toBinaryString(aOld), Long.toBinaryString(b), register);
    }
}
