/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class AboutDialog extends JDialog {
    private static final Logger log = Logger.getLogger(AboutDialog.class.getName());
    private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel infoIconLabel;
    private JPanel aboutInfos;
    private JLabel labelLicenses;
    private JLabel icons8Link;

    public AboutDialog() {
        log.info("constructor");
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
        icons8Link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("http://icons8.com"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AboutDialog.this, res.getString("about.link_not_working"),
                            res.getString("about.link_not_working.title"), JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        icons8Link.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void onOK() {
// add your code here
        dispose();
    }
}
