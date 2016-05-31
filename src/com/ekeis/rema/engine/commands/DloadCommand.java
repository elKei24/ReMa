/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.Register;

/**
 * @author Elias Keis (31.05.2016)
 */
public class DloadCommand extends Command {
    private long val;

    public DloadCommand(Machine machine, int lineNr, long val) {
        super(machine, lineNr);
        this.val = val;
    }

    @Override
    public void perform() {
        Register Ra = machine.getAkku();
        Ra.setValue(val);
        machine.increaseIP();
    }
}
