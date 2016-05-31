/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.log.LogMessage;

/**
 * @author Elias Keis (31.05.2016)
 */
public class LogCommand extends Command {
    private String msg;

    public LogCommand(Machine machine, int lineNr, String msg) {
        super(machine, lineNr);
        this.msg = msg;
    }

    @Override
    public void perform() {
        logExecution(LogMessage.Category.DEBUG, msg);
        machine.increaseIP();
    }
}
