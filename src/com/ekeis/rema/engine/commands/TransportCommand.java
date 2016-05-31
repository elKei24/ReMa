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
        switch (type) {
            case STORE:
                Rx.setValue(Ra.getValue());
                break;
            case LOAD:
                Ra.setValue(Rx.getValue());
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        machine.increaseIP();
    }
}
