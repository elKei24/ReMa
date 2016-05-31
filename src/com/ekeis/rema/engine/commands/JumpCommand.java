/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.Machine;

/**
 * @author Elias Keis (31.05.2016)
 */
public class JumpCommand extends Command {
    public enum Type {JUMP, JGE, JGT, JLE, JLT, JEQ, JNE};

    private long line;
    private Type type;

    public JumpCommand(Machine machine, int lineNr, long line, Type type) {
        super(machine, lineNr);
        this.line = line;
        this.type = type;
    }

    @Override
    public void perform() {
        boolean ok;
        long a = machine.getAkku().getValue();
        switch (type) {
            case JUMP:
                ok = true;
                break;
            case JGE:
                ok = a >= 0;
                break;
            case JGT:
                ok = a > 0;
                break;
            case JLE:
                ok = a <= 0;
                break;
            case JLT:
                ok = a < 0;
                break;
            case JEQ:
                ok = a == 0;
                break;
            case JNE:
                ok = a != 0;
                break;
            default:
                throw new UnsupportedOperationException("No action for the given type defined");
        }
        if (ok) {
            machine.getCounter().setValue(line);
        } else {
            machine.increaseIP();
        }
    }
}
