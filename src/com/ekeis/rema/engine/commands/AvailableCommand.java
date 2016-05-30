package com.ekeis.rema.engine.commands;

import com.ekeis.rema.engine.commands.descriptions.CommandDescription;

/**
 * @author Elias Keis (30.05.2016)
 */
public enum AvailableCommand {
    LOAD(LoadCmd.class, new CommandDescription("cmd.storage.load")),
    DLOAD(LoadCmd.class, new CommandDescription("cmd.storage.dload")),
    STORE(LoadCmd.class, new CommandDescription("cmd.storage.store")),
    ADD(LoadCmd.class, new CommandDescription("cmd.math.add")),
    SUB(LoadCmd.class, new CommandDescription("cmd.math.sub")),
    MULT(LoadCmd.class, new CommandDescription("cmd.math.mult")),
    DIV(LoadCmd.class, new CommandDescription("cmd.math.div")),
    JUMP(LoadCmd.class, new CommandDescription("cmd.jump.jump")),
    JGE(LoadCmd.class, new CommandDescription("cmd.jump.jge")),
    JGT(LoadCmd.class, new CommandDescription("cmd.jump.jgt")),
    JLE(LoadCmd.class, new CommandDescription("cmd.jump.jle")),
    JLT(LoadCmd.class, new CommandDescription("cmd.jump.jlt")),
    JEQ(LoadCmd.class, new CommandDescription("cmd.jump.jeq")),
    JNE(LoadCmd.class, new CommandDescription("cmd.jump.jne")),
    END(LoadCmd.class, new CommandDescription("cmd.end"));
    //TODO implement commands

    private Class<? extends Command> cmd;
    private CommandDescription description;
    AvailableCommand(Class<? extends Command> cmd, CommandDescription cd) {
        this.cmd = cmd;
        this.description = cd;
    }
    private Class<? extends Command> getCommand() {return cmd;}
    public CommandDescription getDescription() {
        return description;
    }
}
