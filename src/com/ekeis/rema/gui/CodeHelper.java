/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import com.ekeis.rema.engine.Program;
import com.ekeis.rema.engine.exceptions.syntax.SyntaxException;
import com.ekeis.rema.prefs.Prefs;
import javafx.util.Pair;

import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Elias Keis (30.05.2016)
 */
public class CodeHelper {
    private static final Logger log = Logger.getLogger(CodeHelper.class.getName());

    //styles
    private static final String STYLE_DEFAULT = "defaultStyle";
    private static final String STYLE_COMMENT = "comment";
    private static final String STYLE_LINENR = "lineNr";
    private static final String STYLE_CURRENT_LINE = "currentLine";
    private static final String STYLE_COMMAND = "command";
    private static final StyleContext styleContext;
    static {
        StyleContext c = new StyleContext();

        //default style
        Style defaultStyle = c.addStyle(STYLE_DEFAULT, c.getStyle(StyleContext.DEFAULT_STYLE));

        //comment style
        Style commentStyle = c.addStyle(STYLE_COMMENT, defaultStyle);
        StyleConstants.setForeground(commentStyle, Color.DARK_GRAY);
        StyleConstants.setItalic(commentStyle, true);

        //current line style
        Style currentLineStyle = c.addStyle(STYLE_CURRENT_LINE, defaultStyle);
        StyleConstants.setBackground(currentLineStyle, Color.getHSBColor((float) 50.0/360, (float) 0.5, (float) 1.0));

        //linenr style
        Style linenrStyle = c.addStyle(STYLE_LINENR, defaultStyle);
        StyleConstants.setForeground(linenrStyle, Color.getHSBColor((float) 25.0 / 360, (float) 1, (float) 0.8));

        //command style
        c.addStyle(STYLE_COMMAND, defaultStyle);

        styleContext = c;
    }

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

    public static void styleCodeDefault(StyledDocument document) {
        document.setCharacterAttributes(0, document.getLength(), styleContext.getStyle(STYLE_DEFAULT), true);
    }
    public static void styleCode(StyledDocument document, int currentLine) {
        styleCode(document, 0, document.getLength() - 1, currentLine);
    }
    public static void styleCodeAfterChange(DocumentEvent e, int currentLine) throws IllegalArgumentException {
        if (e.getDocument() == null || !(e.getDocument() instanceof StyledDocument)) {
            throw new IllegalArgumentException("Document must be a StyledDocument");
        }
        StyledDocument doc = (StyledDocument) e.getDocument();

        int start = e.getOffset();
        int end = e.getLength() + start;

        //stlye
        styleCode(doc, start, end, currentLine);
    }
    protected static void styleCode(StyledDocument doc, int start, int end, int currentLine) {
        if (start > end) return;
        String code;
        try {
            code = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            log.log(Level.SEVERE, "Failed to format code, could not obtain whole text", e);
            return;
        }

        //don´t style only part of a line
        start = code.lastIndexOf('\n', start);
        if (start < 0) {
            start = 0;
        } else {
            start++;
        }
        end = code.indexOf('\n', end);
        if (end < 0) {
            end = code.length();
        }
        //start is inclusive, end not!

        //stlye lines
        int lineStart = start;
        while (lineStart < end) {
            int lineEnd = code.indexOf('\n', lineStart + 1);
            if (lineEnd > end || lineEnd < 0) lineEnd = end;

            styleCodeLine(doc, lineStart, lineEnd, currentLine);

            lineStart = lineEnd;
            while (lineStart < end && code.charAt(lineStart) == '\n') lineStart++;
        }
    }
    protected static void styleCodeLine(StyledDocument doc, int startLine, int endLine, int currentLine) {
        String code;
        try {
            code = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            log.log(Level.SEVERE, "Failed to format code, could not obtain whole text", e);
            return;
        }

        String line = code.substring(startLine, endLine);

        //reset style for whole line
        doc.setCharacterAttributes(startLine, endLine - startLine, styleContext.getStyle(STYLE_DEFAULT), true);

        //style parts
        if (Program.isCommentLine(line)) {
            //comment styling
            doc.setCharacterAttributes(startLine, endLine - startLine, styleContext.getStyle(STYLE_COMMENT), false);
        } else {
            //again comment styling
            int commentStart = Program.findCommentStart(line);
            if (commentStart >= 0) {
                doc.setCharacterAttributes(startLine + commentStart, endLine - startLine - commentStart,
                        styleContext.getStyle(STYLE_COMMENT), false);
            }

            //lineNr styling
            int lineNr;
            int lineNrPos = -1;
            try {
                lineNr = Program.cutLineNumber(line).getKey();
            } catch (SyntaxException se) {
                lineNr = -1;
            }
            if (lineNr > 0) {
                //lineNr itsself
                lineNrPos = line.indexOf(':');
                doc.setCharacterAttributes(startLine, lineNrPos + 1, styleContext.getStyle(STYLE_LINENR), false);

                //current line
                if (lineNr == currentLine) {
                    doc.setCharacterAttributes(startLine, endLine - startLine, styleContext.getStyle(STYLE_CURRENT_LINE), false);
                }
            }

            //command styling
            commandStyling:
            {
                //find beginning
                int commandPos = startLine + (lineNrPos < 0 ? 0 : lineNrPos + 1);
                while (commandPos < endLine && code.charAt(commandPos) == ' ') commandPos++; //ignore space in front of command
                if (commandPos >= endLine) break commandStyling; //cancel if there is no command

                //find end
                int commandEnd = code.indexOf(' ', commandPos + 1);
                if (commandEnd > endLine || commandEnd < 0) commandEnd = endLine;

                //do styling
                if (commandPos >= 0 && commandPos < endLine && commandEnd > commandPos) {
                    doc.setCharacterAttributes(commandPos, commandEnd - commandPos, styleContext.getStyle(STYLE_COMMAND), false);
                    if (Prefs.getInstance().getUpperCommands()) {
                        String command = code.substring(commandPos, commandEnd);
                        String commandUpper = command.toUpperCase(Locale.ROOT);
                        if (!command.equals(commandUpper)) {
                            try {
                                doc.remove(commandPos, commandEnd - commandPos);
                                doc.insertString(commandPos, commandUpper, styleContext.getStyle(STYLE_COMMAND));
                            } catch (BadLocationException e) {
                                log.throwing(CodeHelper.class.getName(), "styleCodeLine()", e);
                            }
                        }
                    }
                }
            }
        }
    }

}
