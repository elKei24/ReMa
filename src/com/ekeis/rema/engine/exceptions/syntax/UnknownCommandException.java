/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.syntax;

/**
 * @author Elias Keis (31.05.2016)
 */
public class UnknownCommandException extends SyntaxException {
    public UnknownCommandException(String cmd, int lineNr) {
        super("unknown_command", cmd, lineNr);
    }
}
