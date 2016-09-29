/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import com.ekeis.rema.engine.Program;
import com.ekeis.rema.engine.exceptions.syntax.SyntaxException;
import com.ekeis.rema.prefs.Prefs;
import javafx.util.Pair;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Elias Keis (28.09.2016)
 */
public class CodeDocument extends DefaultStyledDocument {
    private static final Logger log = Logger.getLogger(CodeDocument.class.getName());

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
        StyleConstants.setBackground(currentLineStyle, Color.getHSBColor((float) 50.0 / 360, (float) 0.5, (float) 1.0));

        //linenr style
        Style linenrStyle = c.addStyle(STYLE_LINENR, defaultStyle);
        StyleConstants.setForeground(linenrStyle, Color.getHSBColor((float) 25.0 / 360, (float) 1, (float) 0.8));

        //command style
        c.addStyle(STYLE_COMMAND, defaultStyle);

        styleContext = c;
    }

    //non-static stuff
    private int curLine = -1;
    private boolean styleCode = true;
    private DocumentListener onCodeChangeStyler = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            onCodeChange(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            onCodeChange(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            //should be working without calling onCodeChange() here
        }

        private void onCodeChange(DocumentEvent e) {
            log.log(Level.FINER, "code style after code change");

            if (e.getDocument() != CodeDocument.this) {
                log.warning("Will not style document. The given event was not caused by this document!");
                return;
            }

            if (!styleCode) return;

            final int start = e.getOffset();
            final int end = e.getLength() + start;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    styleCode(start, end);
                }
            });
        }
    };

    public CodeDocument(String text, boolean styleCode) {
        super();
        this.styleCode = styleCode;
        try {
            insertString(0, text, styleContext.getStyle(STYLE_DEFAULT));
        } catch (BadLocationException e) {
            log.throwing(CodeDocument.class.getName(), "CodeDocument(String)", e);
        }
        addDocumentListener(onCodeChangeStyler);
    }

    public void updateLineNumbers() {
        recommendCombineEverything(true);

        String code = getText();
        int lineNr = 1;
        java.util.List<String> lines = Arrays.asList(code.trim().split("\n"));
        java.util.List<String> linesNew = new ArrayList<>(lines);

        //go through the lines
        forLine:
        for (int i = 0; i < lines.size(); i++) {
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

        setText(codeNew.trim());
        recommendCombineEverything(false);
    }

    public void styleCode() {
        styleCode(0, CodeDocument.this.getLength() - 1);
    }

    protected void styleCode(int start, int end) {
        if (styleCode) {
            styleCodeFancy(start, end);
        } else {
            styleCodeDefault(start, end);
        }
    }

    public void styleCodeDefault(int start, int end) {
        recommendIgnoreChanges(true);
        setCharacterAttributes(start, end, styleContext.getStyle(STYLE_DEFAULT), true);
        recommendIgnoreChanges(false);
    }

    protected void styleCodeFancy(int start, int end) {
        recommendIgnoreChanges(true);

        if (start > end) return;
        String code;
        try {
            code = getText(0, getLength());
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

        //style lines
        int lineStart = start;
        while (lineStart < end) {
            int lineEnd = code.indexOf('\n', lineStart + 1);
            if (lineEnd > end || lineEnd < 0) lineEnd = end;

            styleCodeLine(lineStart, lineEnd);

            lineStart = lineEnd;
            while (lineStart < end && code.charAt(lineStart) == '\n') lineStart++;
        }

        recommendIgnoreChanges(false);
    }

    private void styleCodeLine(int startLine, int endLine) {
        String code;
        try {
            code = getText(0, getLength());
        } catch (BadLocationException e) {
            log.log(Level.SEVERE, "Failed to format code, could not obtain whole text", e);
            return;
        }

        String line = code.substring(startLine, endLine);

        //reset style for whole line
        setCharacterAttributes(startLine, endLine - startLine, styleContext.getStyle(STYLE_DEFAULT), true);

        //style parts
        if (Program.isCommentLine(line)) {
            //comment styling
            setCharacterAttributes(startLine, endLine - startLine, styleContext.getStyle(STYLE_COMMENT), false);
        } else {
            //again comment styling
            int commentStart = Program.findCommentStart(line);
            if (commentStart >= 0) {
                setCharacterAttributes(startLine + commentStart, endLine - startLine - commentStart,
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
                setCharacterAttributes(startLine, lineNrPos + 1, styleContext.getStyle(STYLE_LINENR), false);

                //current line
                if (lineNr == curLine) {
                    setCharacterAttributes(startLine, endLine - startLine, styleContext.getStyle(STYLE_CURRENT_LINE), false);
                }
            }

            //command styling
            commandStyling:
            {
                //find beginning
                int commandPos = startLine + (lineNrPos < 0 ? 0 : lineNrPos + 1);
                while (commandPos < endLine && code.charAt(commandPos) == ' ')
                    commandPos++; //ignore space in front of command
                if (commandPos >= endLine) break commandStyling; //cancel if there is no command

                //find end
                int commandEnd = code.indexOf(' ', commandPos + 1);
                if (commandEnd > endLine || commandEnd < 0) commandEnd = endLine;

                //do styling
                if (commandPos >= 0 && commandPos < endLine && commandEnd > commandPos) {
                    setCharacterAttributes(commandPos, commandEnd - commandPos, styleContext.getStyle(STYLE_COMMAND), false);
                    if (Prefs.getInstance().getUpperCommands()) {
                        String command = code.substring(commandPos, commandEnd);
                        String commandUpper = command.toUpperCase(Locale.ROOT);
                        if (!command.equals(commandUpper)) {
                            try {
                                remove(commandPos, commandEnd - commandPos);
                                insertString(commandPos, commandUpper, styleContext.getStyle(STYLE_COMMAND));
                            } catch (BadLocationException e) {
                                log.throwing(CodeDocument.class.getName(), "styleCodeLine()", e);
                            }
                        }
                    }
                }
            }
        }
    }

    public int getCurLine() {
        return curLine;
    }

    public void setCurLine(int curLine) {
        if (this.curLine != curLine) {
            this.curLine = curLine;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    styleCode();
                }
            });
        }
    }

    public boolean isStyleCode() {
        return styleCode;
    }

    public void setStyleCode(boolean styleCode) {
        if (this.styleCode != styleCode) {
            this.styleCode = styleCode;
            styleCode();
        }
    }

    public String getText() {
        try {
            return getText(0, getLength());
        } catch (BadLocationException ble) {
            throw new AssertionError("Should be no problem to fetch whole text of document", ble);
        }
    }
    public void setText(String text) {
        recommendCombineEverything(true);
        try {
            remove(0, getLength());
            insertString(0, text, styleContext.getStyle(STYLE_DEFAULT));
        } catch (BadLocationException ble) {
            throw new AssertionError("Should be no problem to change whole text", ble);
        } finally {
            recommendCombineEverything(false);
            styleCode();
        }
    }

    //HANDLER STUFF
    public interface UndoRecommendationListener {
        void combineEverythingRecommendation(boolean combineEverything);
        void ignoreChangesRecommendation(boolean ignoreChanges);
    }
    private List<UndoRecommendationListener> recListeners = new LinkedList<UndoRecommendationListener>();
    public void addUndoRecommendationListener(UndoRecommendationListener listener) {
        recListeners.add(listener);
    }
    public void removeUndoRecommendationListener(UndoRecommendationListener listener) {
        recListeners.remove(listener);
    }
    private void recommendCombineEverything(boolean combineEverything) {
        for (UndoRecommendationListener l : recListeners) {
            l.combineEverythingRecommendation(combineEverything);
        }
    }

    private void recommendIgnoreChanges(boolean ignoreChanges) {
        for (UndoRecommendationListener l : recListeners) {
            l.ignoreChangesRecommendation(ignoreChanges);
        }
    }
}
