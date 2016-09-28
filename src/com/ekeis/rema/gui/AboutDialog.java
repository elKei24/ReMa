/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class AboutDialog extends JDialog {
    private static final Logger log = Logger.getLogger(AboutDialog.class.getName());
    private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel labelLicenses;

    public AboutDialog() {
        log.entering(AboutDialog.class.getSimpleName(), "constructor");
        setTitle(res.getString("about.title"));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        String licensesWithoutWrap = labelLicenses.getText();
        labelLicenses.setText(String.format("<html><body style='width: %dpx'>%s</body></html>", 170, licensesWithoutWrap));

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        dispose();
    }
}
