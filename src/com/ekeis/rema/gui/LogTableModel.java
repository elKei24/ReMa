/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import com.ekeis.rema.engine.log.LogMessage;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Elias Keis (02.06.2016)
 */
public class LogTableModel extends AbstractTableModel {
    private static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/GUIBundle");

    List<LogMessage> entries = new LinkedList<>();

    public LogTableModel() {
        super();
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(int column) {
        return res.getString("log.msg");
    }


    public void add(LogMessage msg) {
        entries.add(0, msg);
        fireTableRowsInserted(0, 0);
    }

    public void clear() {
        entries.clear();
        fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ResourceBundle logRes = ResourceBundle.getBundle("com/ekeis/rema/properties/Log");
        LogMessage msg = entries.get(rowIndex);
        String txt = msg.getMessage();
        String formatter = "%s";
        switch (msg.getCategory()) {
            case ERROR:
                formatter = "<span style=\"color:red;\">%s</span>";
                break;
        }
        return String.format("<html>" + formatter + "</html>", txt);
    }
}
