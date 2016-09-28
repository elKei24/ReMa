/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.Register;
import com.ekeis.rema.engine.log.LogMessage;
import com.ekeis.rema.prefs.Prefs;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Elias Keis (29.05.2016)
 */
public class MainForm implements Machine.MachineListener {
    //constants
    private static final Logger log = Logger.getLogger(MainForm.class.getName());
    private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");
    private static final ResourceBundle resNoTranslation = ResourceBundle.getBundle("com/ekeis/rema/properties/NoTranslation");

    //generated fields
    private JPanel contentPanel;
    private JTextPane codeArea;
    private JPanel registerOverview;
    private JMenuBar jMenuBar;
    private JButton buttonRun;
    private JButton buttonStep;
    private JButton buttonReset;
    private JTable logTable;
    private JButton buttonPause;
    private JPanel registerOverviewVIP;
    private JScrollPane registerScrollPane;
    private JScrollPane editorScrollPane;
    private JFileChooser fileChooser;

    //own fields
    private Machine machine;
    private CompoundUndoManager undoManager;
    private LogTableModel logModel;
    private boolean compilationScheduled;
    private int curLine = -1;
    private boolean styleCode = Prefs.getInstance().getStyleCode();

    //----------
    // Actions
    //----------

    //file actions
    private final AbstractAction actionFileNew = new GuiAction(res.getString("menu.file.new"),
            KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            actionFileSave.setEnabled(false);
            setCode(resNoTranslation.getString("code.default"));
        }
    };
    private final AbstractAction actionFileLoad = new GuiAction(res.getString("menu.file.load"),
            KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            load();
        }
    };
    private final AbstractAction actionFileSave = (new GuiAction(res.getString("menu.file.save"),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), false) {
        @Override
        public void actionPerformed(ActionEvent e) {
            save();
        }
    });
    private final AbstractAction actionFileSaveAs = new GuiAction(res.getString("menu.file.save_as"),
            KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveAs();
        }
    };
    private final AbstractAction actionFileSettings = new GuiAction(res.getString("menu.file.settings")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            showSettings();
        }
    };
    private final AbstractAction actionFileExit = new GuiAction(res.getString("menu.file.exit"),
            KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    };

    //code actions
    private final AbstractAction actionCodePaste = new GuiAction(res.getString("menu.code.paste"),
            KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            codeArea.paste();
        }
    };
    private final AbstractAction actionCodeCopy = new GuiAction(res.getString("menu.code.copy"),
            KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), false) {
        @Override
        public void actionPerformed(ActionEvent e) {
            codeArea.copy();
        }
    };
    private final AbstractAction actionCodeCut = new GuiAction(res.getString("menu.code.cut"),
            KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), false) {
        @Override
        public void actionPerformed(ActionEvent e) {
            codeArea.cut();
        }
    };
    private final AbstractAction actionCodeUndo = new GuiAction(res.getString("menu.code.undo"),
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            undo();
        }
    };
    private final AbstractAction actionCodeRedo = new GuiAction(res.getString("menu.code.redo"),
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            redo();
        }
    };
    private final AbstractAction actionCodeAutolines = new GuiAction(res.getString("menu.code.update_line_numbers"),
            KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            autolines();
        }
    };

    //machine actions
    private final AbstractAction actionMachineRun = new GuiAction(res.getString("action.run"),
            KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK), res.getString("action.run.tooltip"), false) {
        @Override
        public void actionPerformed(ActionEvent e) {
            machine.run();
        }
    };
    private final AbstractAction actionMachineStep = new GuiAction(res.getString("action.step"),
            KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK), res.getString("action.step.tooltip"), false) {
        @Override
        public void actionPerformed(ActionEvent e) {
            machine.step();
        }
    };
    private final AbstractAction actionMachineReset = new GuiAction(res.getString("action.reset"),
            KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK), res.getString("action.reset.tooltip")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            reset();
        }
    };
    private final AbstractAction actionMachinePause = new GuiAction(res.getString("action.pause"),
            KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.CTRL_DOWN_MASK), res.getString("action.pause.tooltip"), false) {
        @Override
        public void actionPerformed(ActionEvent e) {
            pause();
        }
    };

    //help actions
    private final AbstractAction actionHelpCommands = new GuiAction(res.getString("menu.help.commands"),
            KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.entering(MainForm.class.getSimpleName() + ".actionHelpCommands", "actionPerformed");
            CommandsDialog dialog = new CommandsDialog();
            dialog.pack();
            dialog.setVisible(true);
            log.exiting(MainForm.class.getSimpleName() + ".actionHelpCommands", "actionPerformed");
        }
    };
    private final AbstractAction actionHelpFeedback = new GuiAction(res.getString("menu.help.feedback")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            String mail = resNoTranslation.getString("feedback.mail");
            try {
                Desktop.getDesktop().mail(new URI(String.format(
                        resNoTranslation.getString("feedback.uri"), mail)));
                log.info("Opening mail program for feedback mail");
            } catch (IOException | URISyntaxException ex) {
                log.warning("Failed to open mail program for feedback mail");
                ex.printStackTrace();
                JOptionPane.showMessageDialog(contentPanel, String.format(
                        res.getString("feedback.not_supported.msg"), mail),
                        res.getString("feedback.not_supported.title"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    };
    private final AbstractAction actionHelpAbout = new GuiAction(res.getString("menu.help.about")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            AboutDialog dialog = new AboutDialog();
            dialog.pack();
            dialog.setVisible(true);
        }
    };

    //log
    private final AbstractAction actionLogClear = new GuiAction(res.getString("menu.log.clear")) {
        @Override
        public void actionPerformed(ActionEvent e) {
            logModel.clear();

        }
    };

    //-----------
    // creation
    //-----------

    public MainForm() {
        //init machine
        machine = new Machine();
        machine.addListener(this);
        machine.setNumRegisters(Prefs.getInstance().getNumberRegisters());
        registerOverview.setLayout(new WrapLayout(FlowLayout.LEFT));

        //create rest of GUI
        createJMenuBar();
        createFileChooser();
        createRegisters();

        //buttons
        buttonReset.setAction(actionMachineReset);
        buttonRun.setAction(actionMachineRun);
        buttonStep.setAction(actionMachineStep);
        buttonPause.setAction(actionMachinePause);

        //set up code area
        if (styleCode) CodeHelper.styleCode(codeArea.getStyledDocument(), curLine);
        codeArea.setComponentPopupMenu(createEditorPopup());
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
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
                //onCodeChange(e);
            }
        });
        codeArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                //clipboard actions
                boolean selection = e.getDot() != e.getMark();
                actionCodeCopy.setEnabled(selection);
                actionCodeCut.setEnabled(selection);
            }
        });

        //set up undo manager
        undoManager = new CompoundUndoManager(codeArea);
        undoManager.setLimit(25);
        checkUndoEnabled();
        undoManager.addListener(new CompoundUndoManager.CompoundUndoManagerListener() {
            @Override
            public void onEnabledChanged() {
                checkUndoEnabled();
            }
        });

        //reset machine
        reset();
    }

    public JFrame createFrame() {
        JFrame frame = new JFrame(res.getString("app.name"));
        frame.setContentPane(contentPanel);
        frame.setJMenuBar(jMenuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(640, 640));
        frame.setMinimumSize(new Dimension(450, 360));
        return frame;
    }

    private void createUIComponents() {
        createLogTable();
    }

    private void createFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f == null || f.getName() == null) throw new NullPointerException("File(name) must not be null");
                return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".txt") ||
                        f.getName().toLowerCase().endsWith(".rema");
            }

            @Override
            public String getDescription() {
                return String.format("%s (%s)", res.getString("filechooser.filter.txt"), "*.txt | *.rema");
            }
        });
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }
    private void createJMenuBar() {
        jMenuBar = new JMenuBar();

        //FILE MENU

        JMenu fileMenu = new JMenu(res.getString("menu.file"));
        fileMenu.setMnemonic(fileMenu.getText().charAt(0));
        jMenuBar.add(fileMenu);

        fileMenu.add(actionFileNew);
        fileMenu.add(actionFileLoad);
        fileMenu.add(actionFileSave);
        fileMenu.add(actionFileSaveAs);
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        fileMenu.add(actionFileSettings);
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        fileMenu.add(actionFileExit);

        //CODE MENU

        JMenu codeMenu = new JMenu(res.getString("menu.code"));
        codeMenu.setMnemonic(codeMenu.getText().charAt(0));
        jMenuBar.add(codeMenu);

        codeMenu.add(actionCodeUndo);
        codeMenu.add(actionCodeRedo);
        codeMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        codeMenu.add(actionCodeCut);
        codeMenu.add(actionCodeCopy);
        codeMenu.add(actionCodePaste);
        codeMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        codeMenu.add(actionCodeAutolines);

        //MACHINE MENU

        JMenu machineMenu = new JMenu(res.getString("menu.machine"));
        machineMenu.setMnemonic(machineMenu.getText().charAt(0));
        jMenuBar.add(machineMenu);

        machineMenu.add(actionMachineRun);
        machineMenu.add(actionMachineStep);
        machineMenu.add(actionMachineReset);
        machineMenu.add(actionMachinePause);


        //HELP MENU

        JMenu helpMenu = new JMenu(res.getString("menu.help"));
        jMenuBar.add(helpMenu);
        helpMenu.setMnemonic(helpMenu.getText().charAt(0));

        helpMenu.add(actionHelpCommands);

        if (Desktop.isDesktopSupported()) {
            helpMenu.add(actionHelpFeedback);
        } else {
            log.info("Desktop not supported; will not add feedback menu item");
        }

        helpMenu.add(actionHelpAbout);
    }
    private JPopupMenu createEditorPopup() {
        JPopupMenu menu = new JPopupMenu(res.getString("menu.code"));

        menu.add(actionCodeCut);
        menu.add(actionCodeCopy);
        menu.add(actionCodePaste);

        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(actionCodeUndo);
        menu.add(actionCodeRedo);
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(actionCodeAutolines);
        return menu;
    }

    //log
    private void createLogTable() {
        logModel = new LogTableModel();
        logTable = new JTable(logModel);
        JPopupMenu popup = new JPopupMenu(res.getString("menu.log"));
        popup.add(actionLogClear);
        logTable.setComponentPopupMenu(popup);
    }

    //Registers
    private void createRegisters() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.finer("creating registers");

                //clear
                registerOverview.removeAll();
                registerOverviewVIP.removeAll();

                //find representation
                RegisterGui.NumberRepresentation repr;
                switch (Prefs.getInstance().getNumberRepresentation()) {
                    case Prefs.NUMBERREPRSENTATION_BINARY:
                        repr = RegisterGui.NumberRepresentation.BINARY;
                        break;
                    default:
                        repr = RegisterGui.NumberRepresentation.DECIMAL;
                        break;
                }

                //add VIPs
                registerOverviewVIP.add(new RegisterGui(machine.getAkku(), res.getString("overview.akku"), repr),
                        BorderLayout.WEST);
                registerOverviewVIP.add(new RegisterGui(machine.getCounter(), res.getString("overview.ip"), repr),
                        BorderLayout.EAST);

                //add rest
                List<Register> registers = machine.getRegisters();
                for (int i = 0; i < registers.size(); i++) {
                    Register r = registers.get(i);
                    if (r != null) {
                        registerOverview.add(new RegisterGui(r, String.format(res.getString("overview.register"), i), repr));
                    }
                }
                registerScrollPane.validate();
                registerScrollPane.repaint();
            }
        });
    }

    //------------------
    // action handles
    //------------------

    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog();
        dialog.pack();
        dialog.setVisible(true);
        if (dialog.getResult() == SettingsDialog.Result.OK) {
            reset();
            Prefs prefs = Prefs.getInstance();
            machine.setNumRegisters(prefs.getNumberRegisters());
            styleCode = prefs.getStyleCode();
            if (styleCode) {
                CodeHelper.styleCode(codeArea.getStyledDocument(), curLine);
            } else {
                CodeHelper.styleCodeDefault(codeArea.getStyledDocument());
            }
        }
    }

    private void load() {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(contentPanel)) {
            actionFileSave.setEnabled(true);
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))){
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                setCode(sb.toString().trim());
                undoManager.discardAllEdits();
                log.fine("have loaded from file");
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to load from file.", ex);
                JOptionPane.showMessageDialog(contentPanel, res.getString("filechooser.load.fail.msg"),
                        res.getString("filechooser.load.fail.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void save() {
        //add extension if there is no
        File f = fileChooser.getSelectedFile();
        if (!f.exists() && !f.getName().contains(".")) {
            f = new File(f.getPath() + ".rema");
            fileChooser.setSelectedFile(f);
        }
        //store information
        try (FileWriter fw = new FileWriter(f, false)) {
            fw.write(codeArea.getText());
            fw.flush();
            log.fine("have saved into file");
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to save into file.", ex);
            JOptionPane.showMessageDialog(contentPanel, res.getString("filechooser.save.fail.msg"),
                    res.getString("filechooser.save.fail.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAs() {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(contentPanel)) {
            save();
            actionFileSave.setEnabled(true);
        }
    }

    private void autolines() {
        boolean sure = Prefs.getInstance().getIgnoreAutolinesWarning();
        if (!sure) {
            JLabel label = new JLabel(String.format("<html><div style=\"width:%dpx;\">%s</div><html>", 300,
                    res.getString("code.autolines.warning.text")));
            JCheckBox checkbox = new JCheckBox(res.getString("dont_show_again"), false);
            checkbox.setMargin(new Insets(8, 0, 0, 0));
            JPanel msgPanel = new JPanel(new BorderLayout());
            msgPanel.add(label, BorderLayout.NORTH);
            msgPanel.add(checkbox, BorderLayout.SOUTH);
            if (JOptionPane.showConfirmDialog(contentPanel, msgPanel,
                    res.getString("code.autolines.warning.title"), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                sure = true;
                Prefs.getInstance().setIgnoreAutolinesWarning(checkbox.isSelected());
            }
        }
        if (sure) {
            setCode(CodeHelper.updateLineNumbers(codeArea.getText()));
        }
    }

    private void reset() {
        curLine = -1;
        logModel.clear();
        machine.reset();
        machine.setProgram(codeArea.getText());
    }
    private void pause() {
        machine.pause();
    }

    private void undo() {
        try {
            undoManager.undo();
        } catch (CannotUndoException cue) {
            log.log(Level.WARNING, "Failed to undo", cue);
        }
        checkUndoEnabled();
    }
    private void redo() {
        try {
            undoManager.redo();
        } catch (CannotRedoException cre) {
            log.log(Level.WARNING, "Failed to redo", cre);
        }
        checkUndoEnabled();
    }

    //----------
    // Helpers
    //----------

    private void checkUndoEnabled() {
        actionCodeRedo.setEnabled(undoManager.canRedo());
        actionCodeUndo.setEnabled(undoManager.canUndo());
    }

    private void setCode(String txt) {
        undoManager.setCombineEverything(true);
        codeArea.setText(txt);
        undoManager.setCombineEverything(false);
    }

    //-------------------
    // Document listener
    //-------------------

    private void onCodeChange(final DocumentEvent e) {
        log.log(Level.FINER, "codeChange");

        //style code
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    log.log(Level.FINER, "codeStyleRun");
                    if (styleCode) CodeHelper.styleCodeAfterChange(e, curLine);
                }
            });
        } catch (IllegalArgumentException iae) {
            log.log(Level.WARNING, "Failed to style code", iae);
        }

        //compile code
        final int DELAY = 1000;
        actionMachineStep.setEnabled(false);
        actionMachineRun.setEnabled(false);
        if (Prefs.getInstance().getLifeCompileEnabled()) {
            if (!compilationScheduled) {
                compilationScheduled = true;
                java.util.Timer t = new Timer("codeCompilationTimer");
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        compilationScheduled = false;
                        reset();
                    }
                }, new Date((new Date()).getTime() + DELAY));
                reset();
            }
        } else {
            machine.pause();
        }
    }


    //-------------------
    // Machine listener
    //-------------------

    @Override
    public void onLogMessage(Machine machine, final LogMessage msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.finer(msg.getCategory().toString() + ": " + msg.getMessage());
                logModel.add(msg);
                logModel.fireTableDataChanged();
            }
        });
    }

    @Override
    public void onCompileTried(Machine machine, boolean success) {
        actionMachineStep.setEnabled(success);
        actionMachineRun.setEnabled(success);
    }

    @Override
    public void onRunningChanged(Machine machine, boolean running) {
        buttonPause.setVisible(running);
        buttonStep.setVisible(!running);
        buttonRun.setVisible(!running);
        buttonReset.setVisible(!running);

        boolean enableRun = !running && !machine.isEnd();
        actionMachineStep.setEnabled(enableRun);
        actionMachineRun.setEnabled(enableRun);
        actionMachinePause.setEnabled(running);

        boolean enableReset = !running;
        actionMachineReset.setEnabled(enableReset);
    }

    @Override
    public void onRegistersChanged(final Machine machine) {
        createRegisters();

        machine.getCounter().addListener(new Register.RegisterListener() {
            @Override
            public void onValueSet(final RegisterValueSetEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        curLine = (int) e.getOldVal();
                        if (styleCode) CodeHelper.styleCode(codeArea.getStyledDocument(), curLine);
                    }
                });
            }
        });
    }

    @Override
    public void onReset(final Machine machine) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (styleCode) CodeHelper.styleCode(codeArea.getStyledDocument(), curLine);
            }
        });
    }

}
