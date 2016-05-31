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

    protected Command(Machine machine) {
        this.machine = machine;
    }

    public abstract void perform();
}
