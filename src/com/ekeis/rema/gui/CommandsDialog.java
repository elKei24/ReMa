package com.ekeis.rema.gui;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

public class CommandsDialog extends JDialog {
    private static final Logger log = Logger.getLogger(CommandsDialog.class.getName());

    private JPanel contentPane;
    private JButton buttonOK;
    private JTree tree1;
    private JTextPane halloTextPane;

    public CommandsDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        tree1.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                log.info("Change");
            }
        });
    }

    private void onOK() {
// add your code here
        dispose();
    }

    public static void main(String[] args) {
        CommandsDialog dialog = new CommandsDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
