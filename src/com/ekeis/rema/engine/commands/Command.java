/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.log.LogMessage;

import java.util.ResourceBundle;

/**
 * @author Elias Keis (31.05.2016)
 */
public abstract class Command {
    private static final ResourceBundle LOG_RES = ResourceBundle.getBundle("com/ekeis/rema/properties/Log");;

    Machine machine;
    int index;

    protected Command(Machine machine, int line) {
        this.machine = machine;
        this.index = line;
    }

    public abstract void perform();

    protected void logExecution(String id, Object... info) {
        logExecution(LogMessage.Category.COMMAND, String.format(LOG_RES.getString("command." + id), info));
    }
    protected void logExecution(final LogMessage.Category category, String details) {
        final String txt = String.format(LOG_RES.getString("command.format"), index, details);
        machine.log(new LogMessage() {
            @Override
            public String getMessage() {
                return txt;
            }
            @Override
            public Category getCategory() {
                return category;
            }
        });
    }
}
