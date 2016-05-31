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
        String msg;
        switch (type) {
            case ADD:
                a+=b;
                msg = "arithmetic.add";
                break;
            case SUB:
                a-=b;
                msg = "arithmetic.sub";
                break;
            case MULT:
                a*=b;
                msg = "arithmetic.mult";
                break;
            case DIV:
                a/=b;
                msg = "arithmetic.div";
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        Ra.setValue(a);
        machine.increaseIP();
        logExecution(msg, b, register);
    }
}
