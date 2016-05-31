/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.Register;

/**
 * @author Elias Keis (31.05.2016)
 */
public class ArithmeticCommand extends Command {

    public enum Type {ADD, SUB, MULT, DIV};

    private Type type;
    private int register;

    public ArithmeticCommand(Machine machine, int line, int register, Type type) {
        super(machine, line);
        this.type = type;
        this.register = register;
    }

    @Override
    public void perform() {
        Register Ra = machine.getAkku();
        Register Rx = machine.getRegister(index, register);
        long a = Ra.getValue();
        long b = Rx.getValue();
        switch (type) {
            case ADD:
                a+=b;
                break;
            case SUB:
                a-=b;
                break;
            case MULT:
                a*=b;
                break;
            case DIV:
                a/=b;
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        Ra.setValue(a);
        machine.increaseIP();
    }
}
