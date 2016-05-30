package com.ekeis.rema.engine.exceptions;

/**
 * @author Elias Keis (30.05.2016)
 */
public class UnknownCommandException extends RemaException {
    public UnknownCommandException(int line, String command) {
        super(String.format(res.getString("exception.unknown_command"), command), line);
    }
}
