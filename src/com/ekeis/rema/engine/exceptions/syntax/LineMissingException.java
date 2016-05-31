/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.syntax;

/**
 * @author Elias Keis (31.05.2016)
 */
public class LineMissingException extends SyntaxException {
    public LineMissingException(int lineNr) {
        super("line_missing", lineNr);
    }
}
