package com.ekeis.rema.engine.exceptions;

import com.ekeis.rema.engine.log.LogMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * @author Elias Keis (30.05.2016)
 */
public abstract class RemaException extends RuntimeException implements LogMessage {
    protected static final ResourceBundle res = ResourceBundle.getBundle("com/ekeis/rema/properties/Log");

    private int line = -1;
    public RemaException(@NotNull String msg, int line) {
        super(msg);
        this.line = line;
    }
    public RemaException(@NotNull String msg) {
        super(msg);
    }

    @Override
    public String getCategory() {
        return res.getString("exception");
    }
    @Override
    public int getLine() {
        return line;
    }
    public void setLine(int line) {
        this.line = line;
    }
}
