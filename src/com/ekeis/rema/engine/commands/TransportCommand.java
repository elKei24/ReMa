/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.Register;

/**
 * @author Elias Keis (31.05.2016)
 */
public class TransportCommand extends Command {

    public enum Type {STORE, LOAD};

    private Type type;
    private int register;

    public TransportCommand(Machine machine, int lineNr, int register, Type type) {
        super(machine, lineNr);
        this.type = type;
        this.register = register;
    }

    @Override
    public void perform() {
        Register Ra = machine.getAkku();
        Register Rx = machine.getRegister(index, register);
        String msg;
        long transported;
        switch (type) {
            case STORE:
                transported = Ra.getValue();
                Rx.setValue(transported);
                msg = "transport.store";
                break;
            case LOAD:
                transported = Rx.getValue();
                Ra.setValue(transported);
                msg = "transport.load";
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        machine.increaseIP();
        logExecution(msg, transported, register);
    }
}
