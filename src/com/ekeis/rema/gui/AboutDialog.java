package com.ekeis.rema.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class AboutDialog extends JDialog {
    private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel infoIconLabel;
    private JPanel aboutInfos;
    private JLabel labelLicenses;

    public AboutDialog() {
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
// add your code here
        dispose();
    }
}
