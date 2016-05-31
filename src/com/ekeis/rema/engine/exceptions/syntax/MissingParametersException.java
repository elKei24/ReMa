/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.syntax;

/**
 * @author Elias Keis (31.05.2016)
 */
public class MissingParametersException extends SyntaxException {
    public MissingParametersException(String cmd, int line) {
        super("missing_parameters", cmd, line);
    }
}
