/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.syntax;

/**
 * @author Elias Keis (31.05.2016)
 */
public class MissingLineNumberException extends SyntaxException {
    public MissingLineNumberException(String line) {
        super("missing_line_number", line);
    }
}
