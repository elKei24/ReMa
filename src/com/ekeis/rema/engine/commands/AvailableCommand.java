package com.ekeis.rema.engine.commands;

/**
 * @author Elias Keis (30.05.2016)
 */
public enum AvailableCommand {
    /* LOAD,
    DLOAD,
    STORE,
    ADD,
    SUB,
    MULT,
    DIV,
    JUMP,
    JGE,
    JGT,
    JLE,
    JLT,
    JEQ,
    JNE,
    END, */
    ;
    //TODO implement commands

    private Command cmd;
    AvailableCommand(Command cmd) {
        this.cmd = cmd;
    }
    private Command getCommand() {return cmd;}
}
