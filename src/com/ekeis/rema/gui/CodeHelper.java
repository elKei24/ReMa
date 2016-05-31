/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import com.ekeis.rema.engine.Program;
import com.ekeis.rema.engine.exceptions.syntax.SyntaxException;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Elias Keis (30.05.2016)
 */
public class CodeHelper {
    public static String updateLineNumbers(String code) {
        int lineNr = 1;
        List<String> lines = Arrays.asList(code.trim().split("\n"));
        List<String> linesNew = new ArrayList<>(lines);

        //go through the lines
        forLine: for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            //Zeile allgemein optimieren
            line.trim();
            if (Program.isCommentLine(line)) continue forLine;

            //Zeilennummer anpassen
            try {
                Pair<Integer, String> result = Program.cutLineNumber(line);
                line = result.getValue();
            } catch (SyntaxException se) {
                //line number probably missing → just go on
            }
            ////Zeilennummer vorne anfügen
            line = String.format("%d: %s", lineNr, line);
            linesNew.set(i, line);
            lineNr++;
        }

        //concat the lines
        String codeNew = "";
        for (String line : linesNew) {
            codeNew += line + "\n";
        }
        return codeNew.trim();
    }
}
