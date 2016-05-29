package com.ekeis.rema.gui;

import sun.swing.UIAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author Elias Keis (29.05.2016)
 */
public class MainForm {
    private static final Logger log = Logger.getLogger(MainForm.class.getName());

    private JPanel contentPanel;
    private JEditorPane codeArea;
    private JPanel registerOverview;
    private JTextArea outputArea;
    private JMenuBar jMenuBar;
    private JButton buttonRun;
    private JButton buttonStep;
    private JButton buttonReset;
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("com/ekeis/rema/properties/MainFormBundle");

    private void createUIComponents() {
        createJMenuBar();
    }

    private void createJMenuBar() {
        jMenuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(resourceBundle.getString("menu.file"));
        jMenuBar.add(fileMenu);
        fileMenu.add(new UIAction(resourceBundle.getString("menu.file.exit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }


    public JFrame getFrame() {
        log.fine("Building frame");
        JFrame frame = new JFrame(resourceBundle.getString("app.name"));
        frame.setContentPane(contentPanel);
        frame.setJMenuBar(jMenuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 600));
        return frame;
    }
}
