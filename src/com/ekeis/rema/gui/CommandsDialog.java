package com.ekeis.rema.gui;

import com.ekeis.rema.engine.commands.AvailableCommand;
import com.ekeis.rema.engine.commands.descriptions.CommandDescription;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
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

    public CommandsDialog() {
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
        List<AvailableCommand> commands = Arrays.asList(AvailableCommand.values());
        CommandDescription desc;
        HashMap<String, DefaultMutableTreeNode> map = new HashMap<>(commands.size());
        for (AvailableCommand command : commands) {
            desc = command.getDescription();
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
}
