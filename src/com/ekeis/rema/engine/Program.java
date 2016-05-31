/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine;

import com.ekeis.rema.engine.commands.Command;
import com.ekeis.rema.engine.exceptions.SyntaxException;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Elias Keis (30.05.2016)
 */
public class Program {
    private static final String[] commentPrefixes = {"--", "#", "//"};

    List<Command> commands = new LinkedList<Command>();

    public Program() {}
    public Program(String code) {
        this.commands = compile(code);
    }
    protected List<Command> compile(String code) throws SyntaxException {
        List<Command> program = new LinkedList<>();

        lineFor: for (String line : code.trim().split("\n")) {
            if (isCommentLine(line)) continue lineFor;
            Pair<Integer, String> result = cutLineNumber(line);
            int lineNr = result.getKey();
            line = result.getValue();
            Command cmd = findCommand(lineNr, line);
            //heck if the place is already taken
            if (program.size() >= lineNr && program.get(lineNr - 1) != null) {
                throw new SyntaxException(String.format(
                        SyntaxException.EX_RES.getString("exception.syntax.line_number_double"), lineNr))
            }
            program.add(lineNr-1, cmd);
        }
        return program;
    }
    protected Command findCommand(int lineNr, String line) throws SyntaxException {
        List<String> parts = new LinkedList<>();
        for (String part : Arrays.asList(line.trim().split(" "))) {
            if (part != null && !part.trim().isEmpty()) parts.add(part.trim());
        }

        if (parts.size() == 0)
            throw new SyntaxException(String.format(
                    SyntaxException.EX_RES.getString("exception.syntax.missing_command"), lineNr));

        String cmd = parts.get(0).toUpperCase();
        switch (cmd) {
            case "DLOAD":

                break;
            case "LOAD":

                break;
            case "STORE":

                break;
            case "ADD":

                break;
            case "SUB":

                break;
            case "MULT":

                break;
            case "DIV":

                break;
            case "JUMP":

                break;
            case "JEQ":

                break;
            case "JNE":

                break;
            case "JLE":

                break;
            case "JLT":

                break;
            case "JGE":

                break;
            case "JGT":

                break;
            case "END":

                break;
            case "PAUSE":

                break;
            case "LOG":

                break;
            default:
                throw new SyntaxException(String.format(
                        SyntaxException.EX_RES.getString("exception.syntax.unkown_command"), cmd, lineNr));
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
     * @throws com.ekeis.rema.engine.exceptions.SyntaxException if the line number is missing
     */
    public static Pair<Integer, String> cutLineNumber(String line) throws SyntaxException {
        ////Erkennen, ob Zeilennummer vorhanden
        hasNumberCheck: {
            int splitpos = line.indexOf(' ');
            String firstPart;
            if (splitpos < 0) {
                firstPart = line;
            } else {
                firstPart = line.substring(0, splitpos);
            }
            if (!firstPart.endsWith(":")) break hasNumberCheck;
            int lineNrOld;
            try {
                lineNrOld = Integer.valueOf(firstPart.substring(0, firstPart.length() - 1));
                assert lineNrOld > 0;
            } catch (NumberFormatException | AssertionError e) {
                break hasNumberCheck;
            }
            //nicht abgebrochen, also Zeilennummer da → löschen
            line = line.substring(splitpos + 1);
            return new Pair<>(lineNrOld, line);
        }
        //canceled → line number missing/malformed
        throw new SyntaxException(String.format(SyntaxException.EX_RES.getString("exception.syntax.missing_line_number"), line));
    }
}
