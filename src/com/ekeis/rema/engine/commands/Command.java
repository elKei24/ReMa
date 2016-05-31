/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;

/**
 * @author Elias Keis (31.05.2016)
 */
public abstract class Command {
    Machine machine;
    int index;

    protected Command(Machine machine, int line) {
        this.machine = machine;
        this.index = line;
    }

    public abstract void perform();
}
