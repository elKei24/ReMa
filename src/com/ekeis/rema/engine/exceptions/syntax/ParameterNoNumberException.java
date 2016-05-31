/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.syntax;

/**
 * @author Elias Keis (31.05.2016)
 */
public class ParameterNoNumberException extends SyntaxException {
    public ParameterNoNumberException(int line) {
        super("parameter_no_number", line);
    }
}
