/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions;

import com.ekeis.rema.engine.log.LogMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * @author Elias Keis (30.05.2016)
 */
public abstract class RemaException extends java.lang.RuntimeException implements LogMessage {
    public static final ResourceBundle EX_RES = ResourceBundle.getBundle("com/ekeis/rema/properties/Log");

    public RemaException(@NotNull String msg) {
        super(msg);
    }

    @Override
    public Category getCategory() {
        return Category.ERROR;
    }
}
