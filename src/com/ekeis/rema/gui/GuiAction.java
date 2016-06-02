/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author Elias Keis (02.06.2016)
 */
public abstract class GuiAction extends AbstractAction {

    public GuiAction(String name, KeyStroke accelerator, String toolTip) {
        this(name, accelerator);
        setTooltip(toolTip);
        setMnemonic(name);
    }
    public GuiAction(String name, KeyStroke accelerator, String tooltip, int mnemonic) {
        this(name, accelerator, mnemonic);
        setTooltip(tooltip);
    }
    public GuiAction(String name, KeyStroke accelerator, int mnemonic) {
        this(name, accelerator);
        setMnemonic(mnemonic);
    }
    public GuiAction(String name, KeyStroke accelerator) {
        this(name);
        setAccelerator(accelerator);
        setMnemonic(name);
    }
    public GuiAction(String name) {
        super(name);
    }
    public GuiAction(String name, String tooltip, int mnemonic) {
        this(name);
        setTooltip(tooltip);
        setMnemonic(mnemonic);
    }
    public GuiAction(String name, String tooltip) {
        this(name);
        setMnemonic(name);
        setTooltip(tooltip);
    }
    public GuiAction(String name, int mnemonic) {
        this(name);
        setMnemonic(mnemonic);
    }
    public GuiAction(String name, KeyStroke accelerator, String toolTip, boolean enabled) {
        this(name, accelerator, enabled);
        setTooltip(toolTip);
        setMnemonic(name);
    }
    public GuiAction(String name, KeyStroke accelerator, String tooltip, int mnemonic, boolean enabled) {
        this(name, accelerator, mnemonic, enabled);
        setTooltip(tooltip);
    }
    public GuiAction(String name, KeyStroke accelerator, int mnemonic, boolean enabled) {
        this(name, accelerator, enabled);
        setMnemonic(mnemonic);
    }
    public GuiAction(String name, KeyStroke accelerator, boolean enabled) {
        this(name, enabled);
        setAccelerator(accelerator);
    }
    public GuiAction(String name, boolean enabled) {
        super(name);
        setEnabled(enabled);
    }
    public GuiAction(String name, String tooltip, int mnemonic, boolean enabled) {
        this(name, enabled);
        setTooltip(tooltip);
        setMnemonic(mnemonic);
    }
    public GuiAction(String name, String tooltip, boolean enabled) {
        this(name, enabled);
        setMnemonic(name);
        setTooltip(tooltip);
    }
    public GuiAction(String name, int mnemonic, boolean enabled) {
        this(name, enabled);
        setMnemonic(mnemonic);
    }

    public void setTooltip(String tooltip) {
        putValue(SHORT_DESCRIPTION, tooltip);
    }
    public void setMnemonic(int mnemonic) {
        putValue(MNEMONIC_KEY, mnemonic);
    }
    public void setAccelerator(KeyStroke accelerator) {
        putValue(ACCELERATOR_KEY, accelerator);
    }
    private void setMnemonic(String name) {
        setMnemonic(KeyEvent.getExtendedKeyCodeForChar(name.charAt(0)));
    }
}
