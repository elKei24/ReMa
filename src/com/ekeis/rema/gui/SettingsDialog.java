package com.ekeis.rema.gui;

import com.ekeis.rema.prefs.Prefs;

import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class SettingsDialog extends JDialog {
    private static final Logger log = Logger.getLogger(SettingsDialog.class.getName());
    private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner registersSpinner;

    public SettingsDialog() {
        setTitle(res.getString("settings.title"));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        load();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCancel() {
        dispose();
    }

    private void onOK() {
        store();
        dispose();
    }

    private void load() {
        //TODO implement
        registersSpinner.setModel(new SpinnerNumberModel(Prefs.getInstance().getNumberRegisters(), 3, 200, 1));
        log.severe("Not yet implemented");
    }

    private void store() {
        //TODO implement
        Prefs.getInstance().setNumberRegisters((int) registersSpinner.getValue());
        log.severe("Not yet implemented");
    }
}
