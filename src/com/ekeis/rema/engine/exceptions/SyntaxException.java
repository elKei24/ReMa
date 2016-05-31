/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions;

/**
 * @author Elias Keis (30.05.2016)
 */
public class SyntaxException extends RemaException {
    public SyntaxException() {
        super(String.format(EX_RES.getString("exception.syntax")));
    }
    public SyntaxException(String details) {
        super(String.format(EX_RES.getString("exception.syntax.details"), details));
    }
}
