package com.ekeis.rema.gui;

import com.ekeis.rema.engine.LanguageConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
            for (String commentPrefix : LanguageConstants.commentPrefixes) {
                if (line.startsWith(commentPrefix)) {
                    continue forLine;
                }
            }

            //Zeilennummer anpassen
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
                try {
                    int lineNrOld = Integer.valueOf(firstPart.substring(0, firstPart.length() - 1));
                } catch (NumberFormatException nfe) {
                    break hasNumberCheck;
                }
                //nicht abgebrochen, also Zeilennummer da → löschen
                line = line.substring(splitpos + 1);
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
