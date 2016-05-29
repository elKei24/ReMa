package com.ekeis.rema.gui;

import sun.swing.UIAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author Elias Keis (29.05.2016)
 */
public class MainForm {
    private static final Logger log = Logger.getLogger(MainForm.class.getName());
    private static ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");

    private JPanel contentPanel;
    private JEditorPane codeArea;
    private JPanel registerOverview;
    private JTextArea outputArea;
    private JMenuBar jMenuBar;
    private JButton buttonRun;
    private JButton buttonStep;
    private JButton buttonReset;
    private JFileChooser fileChooser;
    private JMenuItem menuFileSave;

    private void createUIComponents() {
        createJMenuBar();
        createFileChooser();
    }

    private void createFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f == null || f.getName() == null) throw new NullPointerException("File(name) must not be null");
                return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".txt") ||
                        f.getName().toLowerCase().endsWith(".rema");
            }

            @Override
            public String getDescription() {
                return String.format("%s (%s)", res.getString("filechooser.filter.txt"), "*.txt | *.rema");
            }
        });
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }
    private void createJMenuBar() {
        jMenuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(res.getString("menu.file"));
        jMenuBar.add(fileMenu);
        fileMenu.add(new UIAction(res.getString("menu.file.new")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuFileSave.setEnabled(false);
                codeArea.setText(ResourceBundle.getBundle("com/ekeis/rema/properties/NoTranslation").getString("code.default"));
            }
        });
        fileMenu.add(new UIAction(res.getString("menu.file.load")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                load();
            }
        });
        menuFileSave = fileMenu.add(new UIAction(res.getString("menu.file.save")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        menuFileSave.setEnabled(false);
        fileMenu.add(new UIAction(res.getString("menu.file.save_as")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        fileMenu.add(new UIAction(res.getString("menu.file.settings")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsDialog dialog = new SettingsDialog();
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        fileMenu.add(new UIAction(res.getString("menu.file.exit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JMenu helpMenu = new JMenu(res.getString("menu.help"));
        jMenuBar.add(helpMenu);
        if (Desktop.isDesktopSupported()) {
            helpMenu.add(new UIAction(res.getString("menu.help.feedback")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().mail(new URI(
                                ResourceBundle.getBundle("com/ekeis/rema/properties/NoTranslation").getString("feedback.uri")));
                        log.info("Opening mail program for feedback mail");
                    } catch (IOException | URISyntaxException ex) {
                        log.warning("Failed to open mail program for feedback mail");
                        ex.printStackTrace();
                    }
                }
            });
        } else {
            log.info("Desktop not supported; will not add feedback option");
        }
        helpMenu.add(new UIAction(res.getString("menu.help.about")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog dialog = new AboutDialog();
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    private void load() {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(contentPanel)) {
            menuFileSave.setEnabled(true);
            File file = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(file))){
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                codeArea.setText(sb.toString().trim());
                log.fine("have loaded from file");
            } catch (Exception ex) {
                log.throwing(MainForm.class.toString(), "load", ex);
                log.warning("Failed to load from file.");
                JOptionPane.showMessageDialog(contentPanel, res.getString("filechooser.load.fail.msg"),
                        res.getString("filechooser.load.fail.title"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void save() {
        //add extension if there is no
        File f = fileChooser.getSelectedFile();
        if (!f.exists() && !f.getName().contains(".")) {
            f = new File(f.getPath() + ".rema");
            fileChooser.setSelectedFile(f);
        }
        //store information
        try (FileWriter fw = new FileWriter(f, false)) {
            fw.write(codeArea.getText());
            fw.flush();
            log.fine("have saved into file");
        } catch (Exception ex) {
            log.throwing(MainForm.class.toString(), "save", ex);
            log.warning("Failed to save into file.");
            JOptionPane.showMessageDialog(contentPanel, res.getString("filechooser.save.fail.msg"),
                    res.getString("filechooser.save.fail.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAs() {
        if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(contentPanel)) {
            save();
            menuFileSave.setEnabled(true);
        }
    }


    public JFrame getFrame() {
        JFrame frame = new JFrame(res.getString("app.name"));
        frame.setContentPane(contentPanel);
        frame.setJMenuBar(jMenuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 600));
        return frame;
    }
}
