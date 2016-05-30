package com.ekeis.rema.engine.exceptions;

import com.ekeis.rema.engine.log.LogMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * @author Elias Keis (30.05.2016)
 */
public abstract class RemaException extends RuntimeException implements LogMessage {
    protected static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/Log");
    public RemaException(@NotNull String msg) {
        super(msg);
    }

    @Override
    public String getCategory() {
        return res.getString("exception");
    }
}
