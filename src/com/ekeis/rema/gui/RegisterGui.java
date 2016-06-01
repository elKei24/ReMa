/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import com.ekeis.rema.engine.Register;

import javax.swing.*;
import java.awt.*;

/**
 * @author Elias Keis (31.05.2016)
 */
public class RegisterGui extends JPanel implements Register.RegisterListener {
    Register register;
    JLabel valueLabel = new JLabel();

    public RegisterGui(Register register, String label) {
        this.register = register;
        register.addListener(this);
        setValue(register.getValue());
        setBorder(BorderFactory.createTitledBorder(label));
        setMinimumSize(new Dimension(180, -1));
        add(valueLabel);
        valueLabel.setHorizontalAlignment(JLabel.CENTER);
        valueLabel.setVerticalAlignment(JLabel.CENTER);

        setValue(register.getValue());
    }

    private void setValue(long value) {
        valueLabel.setText(String.format("%d", value));
    }

    @Override
    public void onValueSet(final RegisterValueSetEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setValue(e.getNewVal());
            }
        });
    }
}
