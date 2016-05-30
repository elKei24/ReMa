package com.ekeis.rema.gui;

import com.ekeis.rema.prefs.Prefs;

import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class SettingsDialog extends JDialog {
    private static final Logger log = Logger.getLogger(SettingsDialog.class.getName());
    private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner registersSpinner;

    private Result result = Result.NONE;
    public enum Result {
        NONE, OK, CANCEL
    }

    //TODO can not yet edit register min/max values

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
        result = Result.CANCEL;
        dispose();
    }

    private void onOK() {
        store();
        result = Result.OK;
        dispose();
    }

    private void load() {
        registersSpinner.setModel(new SpinnerNumberModel(Prefs.getInstance().getNumberRegisters(), 3, 200, 1));
    }

    private void store() {
        Prefs.getInstance().setNumberRegisters((int) registersSpinner.getValue());
    }

    public Result getResult() {
        return result;
    }
}
