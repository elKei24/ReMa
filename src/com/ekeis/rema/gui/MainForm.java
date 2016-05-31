/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import com.ekeis.rema.engine.Machine;
import com.ekeis.rema.engine.log.LogMessage;
import com.ekeis.rema.prefs.Prefs;
import sun.swing.UIAction;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author Elias Keis (29.05.2016)
 */
public class MainForm implements Machine.MachineListener {
    private static final Logger log = Logger.getLogger(MainForm.class.getName());
    private static ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");
    ResourceBundle resNoTranslation = ResourceBundle.getBundle("com/ekeis/rema/properties/NoTranslation");

    private JPanel contentPanel;
    private JEditorPane codeArea;
    private JPanel registerOverview;
    private JMenuBar jMenuBar;
    private JButton buttonRun;
    private JButton buttonStep;
    private JButton buttonReset;
    private JTable logTable;
    private JFileChooser fileChooser;
    private JMenuItem menuFileSave;

    private Machine machine;
    private CompoundUndoManager undoManager;
    private JMenuItem menuCodeUndo, popupCodeUndo, menuCodRedo, popupCodeRedo, menuMachineRun, menuMachineStep, menuMachineReset;
    private LogTableModel logModel;

    boolean programUpToDate = false;


    public MainForm() {
        machine = new Machine();
        machine.addListener(this);

        createJMenuBar();
        createFileChooser();

        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        buttonRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        buttonStep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step();
            }
        });

        codeArea.setComponentPopupMenu(createEditorPopup());
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onCodeChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onCodeChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onCodeChange();
            }
        });

        undoManager = new CompoundUndoManager(codeArea);
        undoManager.setLimit(15);
        checkUndoEnabled();
        codeArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                checkUndoEnabled();
            }
        });
    }

    private void checkUndoEnabled() {
        boolean undo = undoManager.isSignificant() && undoManager.canUndo();
        boolean redo = undoManager.canRedo();
        menuCodeUndo.setEnabled(undo);
        popupCodeUndo.setEnabled(undo);
        menuCodRedo.setEnabled(redo);
        popupCodeRedo.setEnabled(redo);
    }

    private JPopupMenu createEditorPopup() {
        JPopupMenu menu = new JPopupMenu(res.getString("menu.code"));JMenu codeMenu = new JMenu(res.getString("menu.code"));
        popupCodeUndo = menu.add(new UIAction(res.getString("menu.code.undo")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        popupCodeRedo = menu.add(new UIAction(res.getString("menu.code.redo")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
        menu.add(new JSeparator(JSeparator.HORIZONTAL));
        menu.add(new UIAction(res.getString("menu.code.update_line_numbers")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                autolines();
            }
        });
        return menu;
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
        JMenuItem menuFileNew = fileMenu.add(new UIAction(res.getString("menu.file.new")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuFileSave.setEnabled(false);
                codeArea.setText(resNoTranslation.getString("code.default"));
            }
        });
        menuFileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        menuFileNew.setMnemonic(menuFileNew.getText().charAt(0));

        JMenuItem menuFileLoad = fileMenu.add(new UIAction(res.getString("menu.file.load")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                load();
            }
        });
        menuFileLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        menuFileLoad.setMnemonic(menuFileLoad.getText().charAt(0));

        menuFileSave = fileMenu.add(new UIAction(res.getString("menu.file.save")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        menuFileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        menuFileSave.setEnabled(false);
        menuFileSave.setMnemonic(menuFileSave.getText().charAt(0));

        JMenuItem menuFileSaveAs = fileMenu.add(new UIAction(res.getString("menu.file.save_as")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        menuFileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK));
        menuFileSaveAs.setMnemonic(menuFileSaveAs.getText().charAt(0));

        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        JMenuItem menuFileSettings = fileMenu.add(new UIAction(res.getString("menu.file.settings")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsDialog dialog = new SettingsDialog();
                dialog.pack();
                dialog.setVisible(true);
                if (dialog.getResult() == SettingsDialog.Result.OK) {
                    reset();
                }
            }
        });
        menuFileSettings.setMnemonic(menuFileSettings.getText().charAt(0));


        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        JMenuItem fileMenuExit = fileMenu.add(new UIAction(res.getString("menu.file.exit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
        fileMenuExit.setMnemonic(fileMenuExit.getText().charAt(0));

        //CODE MENU

        JMenu codeMenu = new JMenu(res.getString("menu.code"));
        codeMenu.setMnemonic(codeMenu.getText().charAt(0));
        jMenuBar.add(codeMenu);

        menuCodeUndo = codeMenu.add(new UIAction(res.getString("menu.code.undo")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        menuCodeUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        menuCodeUndo.setMnemonic(menuCodeUndo.getText().charAt(0));

        menuCodRedo = codeMenu.add(new UIAction(res.getString("menu.code.redo")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
        menuCodRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        menuCodRedo.setMnemonic(menuCodRedo.getText().charAt(0));

        codeMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        JMenuItem menuCodeAutolines = codeMenu.add(new UIAction(res.getString("menu.code.update_line_numbers")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                autolines();
            }
        });
        menuCodeAutolines.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
        menuCodeAutolines.setMnemonic(menuCodeAutolines.getText().charAt(0));

        //MACHINE MENU

        JMenu machineMenu = new JMenu(res.getString("menu.machine"));
        machineMenu.setMnemonic(machineMenu.getText().charAt(0));
        jMenuBar.add(machineMenu);

        menuMachineRun = machineMenu.add(new UIAction(res.getString("action.run")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        menuMachineRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, KeyEvent.CTRL_DOWN_MASK));
        menuMachineRun.setMnemonic(menuMachineRun.getText().charAt(0));
        menuMachineRun.setToolTipText(res.getString("action.run.tooltip"));

        menuMachineStep = machineMenu.add(new UIAction(res.getString("action.step")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                step();
            }
        });
        menuMachineStep.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK));
        menuMachineStep.setMnemonic(menuMachineStep.getText().charAt(0));
        menuMachineStep.setToolTipText(res.getString("action.step.tooltip"));

        menuMachineReset = machineMenu.add(new UIAction(res.getString("action.reset")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        menuMachineReset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, KeyEvent.CTRL_DOWN_MASK));
        menuMachineReset.setMnemonic(menuMachineReset.getText().charAt(0));
        menuMachineReset.setToolTipText(res.getString("action.reset.tooltip"));


        //HELP MENU

        JMenu helpMenu = new JMenu(res.getString("menu.help"));
        jMenuBar.add(helpMenu);
        helpMenu.setMnemonic(helpMenu.getText().charAt(0));

        JMenuItem menuHelpCommands = helpMenu.add(new UIAction(res.getString("menu.help.commands")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                CommandsDialog dialog = new CommandsDialog();
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        menuHelpCommands.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menuHelpCommands.setMnemonic(menuHelpCommands.getText().charAt(0));

        if (Desktop.isDesktopSupported()) {
            JMenuItem menuHelpFeedback = helpMenu.add(new UIAction(res.getString("menu.help.feedback")) {
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
            });
            menuHelpFeedback.setMnemonic(menuHelpFeedback.getText().charAt(0));
        } else {
            log.info("Desktop not supported; will not add feedback option");
        }

        JMenuItem menuHelpAbout = helpMenu.add(new UIAction(res.getString("menu.help.about")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog dialog = new AboutDialog();
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        menuHelpAbout.setMnemonic(menuHelpAbout.getText().charAt(0));
    }

    private void load() {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(contentPanel)) {
            menuFileSave.setEnabled(true);
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))){
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                codeArea.setText(sb.toString().trim());
                log.fine("have loaded from file");
            } catch (Exception ex) {
                log.throwing(MainForm.class.toString(), "load", ex);
                log.warning("Failed to load from file.");
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
            log.throwing(MainForm.class.toString(), "save", ex);
            log.warning("Failed to save into file.");
            JOptionPane.showMessageDialog(contentPanel, res.getString("filechooser.save.fail.msg"),
                    res.getString("filechooser.save.fail.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAs() {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(contentPanel)) {
            save();
            menuFileSave.setEnabled(true);
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
            codeArea.setText(CodeHelper.updateLineNumbers(codeArea.getText()));
        }
    }

    private void run() {
        updateProgram();
        machine.run();
    }
    private void step() {
        updateProgram();
        machine.step();
    }
    private void reset() {
        machine.reset();
    }
    private void updateProgram() {
        if (!programUpToDate) {
            machine.reset();
            machine.setProgram(codeArea.getText());
            logModel.clear();
        }
    }

    private void onCodeChange() {
        machine.pause();
    }

    private void undo() {
        try {
            undoManager.undo();
        } catch (CannotUndoException cue) {
            log.throwing(MainForm.class.getName(), "undo", cue);
        }
        checkUndoEnabled();
    }
    private void redo() {
        try {
            undoManager.redo();
        } catch (CannotRedoException cre) {
            log.throwing(MainForm.class.getName(), "redo", cre);
        }
        checkUndoEnabled();
    }

    public JFrame createFrame() {
        JFrame frame = new JFrame(res.getString("app.name"));
        frame.setContentPane(contentPanel);
        frame.setJMenuBar(jMenuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 600));
        return frame;
    }

    //Machine listener
    @Override
    public void onLogMessage(LogMessage msg) {
        log.fine(msg.getCategory().toString() + ": " + msg.getMessage());
        String surrounding;
        switch (msg.getCategory()) {
            case DEBUG:
                surrounding = "<p><i>%s</i></p>";
            case ERROR:
                surrounding = "<p style=\"color:red\"><b>%s</b></p>";
            default:
                surrounding="%s";
        }
        logModel.add(msg);
        logModel.fireTableDataChanged();
    }

    private void createUIComponents() {
        createLogTable();
    }
    private void createLogTable() {
        logModel = new LogTableModel();
        logTable = new JTable(logModel);
        /*ogTable.getColumnModel().getColumn(0).setMinWidth(55);
        logTable.getColumnModel().getColumn(0).setMaxWidth(100);
        logTable.getColumnModel().getColumn(0).setPreferredWidth(70);*/
        JPopupMenu popup = new JPopupMenu(res.getString("menu.log"));
        JMenuItem menuClear = popup.add(new UIAction(res.getString("menu.log.clear")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                logModel.clear();

            }
        });
        logTable.setComponentPopupMenu(popup);
    }

    public class LogTableModel extends AbstractTableModel {
        List<LogMessage> entries = new LinkedList<>();

        public LogTableModel() {
            super();
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }
        @Override
        public String getColumnName(int column) {
            return res.getString("log.msg");
            /*switch (column) {
                case 0:
                    return res.getString("log.category");
                case 1:
                    return res.getString("log.msg");
                default:
                    return "";
            }*/
        }


        public void add(LogMessage msg) {
            entries.add(0, msg);
            fireTableRowsInserted(0, 0);
        }
        public void clear() {
            entries.clear();
            fireTableDataChanged();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ResourceBundle logRes = ResourceBundle.getBundle("com/ekeis/rema/properties/Log");
            LogMessage msg = entries.get(rowIndex);
            /*switch (columnIndex) {
                case 0:
                    switch (msg.getCategory()) {
                        case DEBUG:
                            return logRes.getString("debug");
                        case INFO:
                            return logRes.getString("info");
                        case ERROR:
                            return logRes.getString("exception");
                    }
                    return msg.getCategory();
                case 1:
                    return msg.getMessage();
                default:
                    return "";
            }*/
            String txt = msg.getMessage();
            String formatter = "%s";
            switch (msg.getCategory()) {
                case ERROR:
                    formatter = "<span style=\"color:red;\">%s</span>";
                    break;
                case DEBUG:
                    formatter = "<i></i>";
                    break;
            }
            return String.format("<html>"+formatter+"</html>", txt);
        }
    }
}
