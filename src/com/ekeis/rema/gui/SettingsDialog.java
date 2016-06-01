/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import com.ekeis.rema.prefs.Prefs;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private JSpinner maxValueSpinner;
    private JSpinner minValueSpinner;
    private JCheckBox lifeCompilationCheck;
    private JCheckBox syntaxHighlightingCheck;
    private SpinnerNumberModel minValModel, maxValModel;

    private Result result = Result.NONE;
    public enum Result {
        NONE, OK, CANCEL
    }

    public SettingsDialog() {
        setTitle(res.getString("settings.title"));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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

        maxValModel = new SpinnerNumberModel((double) 0, (double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE, (double) 1);
        minValModel = new SpinnerNumberModel((double) 0, (double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE, (double) 1);
        maxValueSpinner.setModel(maxValModel);
        minValueSpinner.setModel(minValModel);
        maxValModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double max = (double) maxValModel.getValue();
                if ((double) minValModel.getValue() > max) minValModel.setValue(max);
                minValModel.setMaximum(max);
            }
        });
        minValModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double min = (double) minValModel.getValue();
                if ((double) maxValModel.getValue() < min) maxValModel.setValue(min);
                maxValModel.setMinimum(min);
            }
        });

        load();
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
        Prefs prefs = Prefs.getInstance();
        registersSpinner.setModel(new SpinnerNumberModel(prefs.getNumberRegisters(), 3, 200, 1));

        maxValModel.setValue((double) prefs.getRegisterMax());
        minValModel.setValue((double) prefs.getRegisterMin());

        lifeCompilationCheck.setSelected(prefs.getLifeCompileEnabled());
        syntaxHighlightingCheck.setSelected(prefs.getStyleCode());
    }

    private void store() {
        Prefs prefs = Prefs.getInstance();
        prefs.setNumberRegisters((int) registersSpinner.getValue());
        prefs.setRegisterMin((long)(double) minValModel.getValue());
        prefs.setRegisterMax((long)(double) maxValModel.getValue());
        prefs.setLifeCompileEnabled(lifeCompilationCheck.isSelected());
        prefs.setStyleCode(syntaxHighlightingCheck.isSelected());
        prefs.sync();
    }

    public Result getResult() {
        return result;
    }
}
