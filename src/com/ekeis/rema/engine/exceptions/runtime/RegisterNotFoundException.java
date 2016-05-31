/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.runtime;

/**
 * @author Elias Keis (31.05.2016)
 */
public class RegisterNotFoundException extends RuntimeException {
    public RegisterNotFoundException(int lineNr, int regNumber) {
        super(lineNr, "register_not_found", regNumber);
    }
}
