/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class CommandsDialog extends JDialog {
    private static final Logger log = Logger.getLogger(CommandsDialog.class.getName());
    private static final ResourceBundle commandRes = ResourceBundle.getBundle("com/ekeis/rema/properties/CommandDescriptions");
    private static final ResourceBundle guiRes = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");

    private JPanel contentPane;
    private JButton buttonOK;
    private JTree commandsTree;
    private JTextArea descriptionPane;
    private JLabel titlePane;

    private static final String[] commands = new String[] {
            "cmd.storage.dload",
            "cmd.storage.load",
            "cmd.storage.store",
            "cmd.math.add",
            "cmd.math.sub",
            "cmd.math.mult",
            "cmd.math.div",
            "cmd.jump.jump",
            "cmd.jump.jge",
            "cmd.jump.jgt",
            "cmd.jump.jle",
            "cmd.jump.jlt",
            "cmd.jump.jeq",
            "cmd.jump.jne",
            "cmd.end",
            "cmd.debug.pause",
            "cmd.debug.log",
            "cmd.debug.comment"};

    public CommandsDialog() {
        super((Dialog) null);
        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonOK);
        setTitle(guiRes.getString("commands.title"));

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        commandsTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                Object selection = e.getNewLeadSelectionPath().getLastPathComponent();
                if (selection != null && selection instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selection;
                    Object userObject = node.getUserObject();
                    if (userObject != null && userObject instanceof CommandDescription){
                        CommandDescription desc = (CommandDescription) userObject;
                        titlePane.setText(desc.getTitle());
                        descriptionPane.setText(desc.getDescription());
                    }
                }
            }
        });
    }

    private void createUIComponents() {
        //init tree
        CommandDescription desc;
        HashMap<String, DefaultMutableTreeNode> map = new HashMap<>(commands.length);
        for (String command : Arrays.asList(commands)) {
            desc = new CommandDescription(command);
            addCommand(map, desc);
        }

        TreeNode root = new DefaultMutableTreeNode();
        for (String key : map.keySet()) {
            if (!key.contains(".")) {
                root = map.get(key);
                break;
            }
        }
        commandsTree = new JTree(root);
    }
    private void addCommand(Map<String, DefaultMutableTreeNode> map, CommandDescription desc) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(desc, false);
        map.put(desc.getId(), node);
        DefaultMutableTreeNode father = getFather(map, desc.getId());
        if (father != null) {
            father.add(node);
        }
    }
    private DefaultMutableTreeNode getFather(Map<String, DefaultMutableTreeNode> map, String childId) {
        int splitpoint = childId.lastIndexOf('.');
        if (splitpoint < 0) return null;
        String ownId = childId.substring(0, splitpoint);
        if (map.containsKey(ownId)) {
            return map.get(ownId);
        } else {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(commandRes.getString(ownId), true);
            map.put(ownId, node);
            DefaultMutableTreeNode father = getFather(map, ownId);
            if (father != null) {
                father.add(node);
            }
            return node;
        }
    }

    private void createCategories(Map<String, TreeNode> map, String category) {
        while (category.contains(".")) {
            boolean added = addCategory(map, category);
            if (!added) return; //other categories already there
            int splitpoint = category.lastIndexOf('.');
            category = category.substring(0, splitpoint);
        }
        addCategory(map, category);
    }
    private boolean addCategory(Map<String, TreeNode> map, String category) {
        return addMapEntry(map, category, commandRes.getString(category));
    }
    private boolean addMapEntry(Map<String, TreeNode> map, String key, Object content) {
        if (map.containsKey(key)) return false;
        map.put(key, new DefaultMutableTreeNode(content));
        return true;
    }

    private void onOK() {
        dispose();
    }

    /**
     * @author Elias Keis (30.05.2016)
     */
    public static final class CommandDescription {
        private static final Logger log = Logger.getLogger(CommandDescription.class.getName());
        private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/commandDescriptions");

        private String id;

        public CommandDescription(String id) {
            this.id = id;
        }
        public String getTitle() {
            return res.getString(id + ".title");
        }
        public String getDescription() {
            return res.getString(id + ".text");
        }
        public String getId() {
            return id;
        }
        public String getKey() {
            return res.getString(id);
        }

        @Override
        public String toString() {
            return getKey();
        }
    }

}
