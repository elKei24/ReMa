package com.ekeis.rema;

import javax.swing.*;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.LogManager;

/**
 * @author Elias Keis (29.05.2016)
 */
public class Main {
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("com/ekeis/rema/properties/LogMessages");

    public static void main(String[] args) {
        //configure logging
        String logFile = System.getProperty("java.util.logging.config.file");
        if(logFile == null){
            try {
                LogManager.getLogManager().readConfiguration(MainForm.class.getClassLoader().getResourceAsStream("com/ekeis/rema/properties/logging.properties"));
                System.out.println(resourceBundle.getString("log.properties.own"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(resourceBundle.getString("log.properties.default"));
            }
        } else {
            System.out.println(resourceBundle.getString("log.properties.external"));
        }

        //show main frame
        JFrame frame = (new MainForm()).getFrame();
        frame.pack();
        frame.setVisible(true);
    }
}
