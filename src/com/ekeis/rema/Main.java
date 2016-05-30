package com.ekeis.rema;

import com.ekeis.rema.gui.MainForm;

import javax.swing.*;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Elias Keis (29.05.2016)
 */
public class Main {

    public static void main(String[] args) {
        //configure logging
        String logFile = System.getProperty("java.util.logging.config.file");
        if(logFile == null){
            try {
                LogManager.getLogManager().readConfiguration(MainForm.class.getClassLoader().getResourceAsStream("com/ekeis/rema/properties/logging.properties"));
                System.out.println("Using own logging properties");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Using default logging properties");
            }
        } else {
            System.out.println("Using external logging properties");
        }
        Logger log = Logger.getLogger(Main.class.getName());

        //configure GUI
        System.setProperty("java.awt.Window.locationByPlatform", "true");
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            log.warning("Could not switch to Nimbus look and feel. Will stay with default.");
        }

        //show main frame
        JFrame frame = (new MainForm()).createFrame();
        frame.pack();
        frame.setVisible(true);
    }
}
