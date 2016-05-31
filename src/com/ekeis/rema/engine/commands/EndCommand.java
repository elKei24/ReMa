/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;

/**
 * @author Elias Keis (31.05.2016)
 */
public class EndCommand extends Command {
    public enum Type {END, PAUSE}

    Type type;

    public EndCommand(Machine machine, int line, Type type) {
        super(machine, line);
        this.type = type;
    }

    @Override
    public void perform() {
        switch (type) {
            case END:
                machine.end();
                machine.increaseIP();
                break;
            case PAUSE:
                machine.pause();
                machine.increaseIP();
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        logExecution("end");
    }

}
