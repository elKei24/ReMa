/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine;

import com.ekeis.rema.engine.commands.*;
import com.ekeis.rema.engine.exceptions.InternalException;
import com.ekeis.rema.engine.exceptions.RemaException;
import com.ekeis.rema.engine.exceptions.runtime.LineNotFoundException;
import com.ekeis.rema.engine.exceptions.syntax.*;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Elias Keis (30.05.2016)
 */
public class Program {
    private static final Logger log = Logger.getLogger(Program.class.getName());
    private static final String[] commentPrefixes = {"--", "#", "//"};

    List<Command> commands = new LinkedList<Command>();
    Machine machine;

    public Program(Machine machine) {
        this.machine = machine;
    }
    public Program(Machine machine, String code) {
        this(machine);
        this.commands = compile(code);
    }
    protected List<Command> compile(String code) throws SyntaxException {
        log.fine("compiling program");
        List<Command> program = new LinkedList<>();

        lineFor: for (String line : code.trim().split("\n")) {
            if (isCommentLine(line)) continue lineFor;
            Pair<Integer, String> result = cutLineNumber(line);
            int lineNr = result.getKey();
            line = result.getValue();
            Command cmd = findCommand(lineNr, line);
            //heck if the place is already taken
            if (program.size() >= lineNr && program.get(lineNr - 1) != null) {
                throw new LineNumberTwiceException(lineNr);
            }
            while (program.size() < lineNr) {
                program.add(null);
            }
            program.set(lineNr-1, cmd);
        }
        for (int i = 0; i < program.size(); i++) {
            if (program.get(i) == null) {
                throw new LineMissingException(i+1);
            }
        }
        return program;
    }
    protected Command findCommand(int lineNr, String line) throws SyntaxException {
        List<String> parts = new LinkedList<>();
        for (String part : Arrays.asList(line.trim().split(" "))) {
            if (part != null && !part.trim().isEmpty()) parts.add(part.trim());
        }

        if (parts.size() == 0)
            throw new MissingCommandException(lineNr);

        String cmd = parts.get(0).toUpperCase();
        try {
            switch (cmd) {
                case "DLOAD":
                    return new DloadCommand(machine, lineNr, Long.decode(parts.get(1)));
                case "LOAD":
                    return new TransportCommand(machine, lineNr, Integer.decode(parts.get(1)), TransportCommand.Type.LOAD);
                case "STORE":
                    return new TransportCommand(machine, lineNr, Integer.decode(parts.get(1)), TransportCommand.Type.STORE);
                case "ADD":
                    return new ArithmeticCommand(machine, lineNr, Integer.decode(parts.get(1)), ArithmeticCommand.Type.ADD);
                case "SUB":
                    return new ArithmeticCommand(machine, lineNr, Integer.decode(parts.get(1)), ArithmeticCommand.Type.SUB);
                case "MULT":
                    return new ArithmeticCommand(machine, lineNr, Integer.decode(parts.get(1)), ArithmeticCommand.Type.MULT);
                case "DIV":
                    return new ArithmeticCommand(machine, lineNr, Integer.decode(parts.get(1)), ArithmeticCommand.Type.DIV);
                case "JUMP":
                    return new JumpCommand(machine, lineNr, Integer.decode(parts.get(1)), JumpCommand.Type.JUMP);
                case "JEQ":
                    return new JumpCommand(machine, lineNr, Integer.decode(parts.get(1)), JumpCommand.Type.JEQ);
                case "JNE":
                    return new JumpCommand(machine, lineNr, Integer.decode(parts.get(1)), JumpCommand.Type.JNE);
                case "JLE":
                    return new JumpCommand(machine, lineNr, Integer.decode(parts.get(1)), JumpCommand.Type.JLE);
                case "JLT":
                    return new JumpCommand(machine, lineNr, Integer.decode(parts.get(1)), JumpCommand.Type.JLT);
                case "JGE":
                    return new JumpCommand(machine, lineNr, Integer.decode(parts.get(1)), JumpCommand.Type.JGE);
                case "JGT":
                    return new JumpCommand(machine, lineNr, Integer.decode(parts.get(1)), JumpCommand.Type.JGT);
                case "END":
                    return new EndCommand(machine, lineNr, EndCommand.Type.END);
                case "PAUSE":
                    return new EndCommand(machine, lineNr, EndCommand.Type.PAUSE);
                case "LOG":
                    List<String> partsTwo = new LinkedList<>();
                    for (String part : Arrays.asList(line.trim().split(" ", 2))) {
                        if (part != null && !part.trim().isEmpty()) partsTwo.add(part.trim());
                    }
                    return new LogCommand(machine, lineNr, partsTwo.get(1));
                default:
                    throw new UnknownCommandException(cmd, lineNr);
            }
        } catch (IndexOutOfBoundsException ioobe) {
            throw new MissingParametersException(cmd, lineNr);
        } catch (NumberFormatException nfe) {
            throw new ParameterNoNumberException(lineNr);
        }
    }

    //static stuff
    public static boolean isCommentLine(String line) {
        for (String commentPrefix : commentPrefixes) {
            if (line.startsWith(commentPrefix)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Removes the line number from <code>line</code> and returns it
     * @param line the line with line number. Pass by reference, value will be changed!
     * @return the line number
     * @throws SyntaxException if the line number is missing
     */
    public static Pair<Integer, String> cutLineNumber(final String line) throws SyntaxException {
        ////Erkennen, ob Zeilennummer vorhanden
        hasNumberCheck: {
            int splitpos = line.indexOf(' ');
            String firstPart;
            String rest;
            if (splitpos < 0) {
                firstPart = line;
                rest = "";
            } else {
                firstPart = line.substring(0, splitpos);
                rest = line.substring(splitpos + 1);
            }
            if (!firstPart.endsWith(":")) break hasNumberCheck;
            int lineNrOld;
            try {
                lineNrOld = Integer.valueOf(firstPart.substring(0, firstPart.length() - 1));
            } catch (NumberFormatException | AssertionError e) {
                break hasNumberCheck;
            }
            if (lineNrOld < 0) {
                throw new MissingLineNumberException(line);
            }
            //nicht abgebrochen, also Zeilennummer da → löschen
            return new Pair<>(lineNrOld, rest);
        }
        //canceled → line number missing/malformed
        throw new MissingLineNumberException(line);
    }

    public void execute(int l) {
        try {
            commands.get(l - 1).perform();
        } catch (RemaException re) {
            throw re;
        } catch (IndexOutOfBoundsException ioobe) {
            throw new LineNotFoundException(l);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Could not execute command", ex);
            throw new InternalException(ex);
        }
    }
}
