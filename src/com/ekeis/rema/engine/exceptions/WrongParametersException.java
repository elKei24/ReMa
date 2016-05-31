/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions;

/**
 * @author Elias Keis (30.05.2016)
 */
public class WrongParametersException extends RemaException {
    public WrongParametersException(int line) {
        super(EX_RES.getString("exception.invalid_parameters"), line);
    }
}
