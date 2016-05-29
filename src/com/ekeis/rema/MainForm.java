package com.ekeis.rema;

import sun.swing.UIAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Elias Keis (29.05.2016)
 */
public class MainForm {
    private static final Logger log = Logger.getLogger(MainForm.class.getName());

    private JPanel contentPanel;
    private JTextArea codeArea;
    private JPanel registerOverview;
    private JTextArea outputArea;
    private JMenuBar jMenuBar;
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("com/ekeis/rema/properties/MainFormBundle");

    private void createUIComponents() {
        log.fine("create");
        createJMenuBar();
    }

    private void createJMenuBar() {
        log.finest("menuBar");
        jMenuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(new UIAction(resourceBundle.getString("fileMenu")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.finest("file menu");
            }
        });
        jMenuBar.add(fileMenu);

        JMenu settingsMenu = new JMenu(new UIAction(resourceBundle.getString("settingsMenu")) {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }


    public JFrame getFrame() {
        log.severe("Test");
        JFrame frame = new JFrame("MainForm");
        frame.setContentPane(contentPanel);
        frame.setJMenuBar(jMenuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }
}
