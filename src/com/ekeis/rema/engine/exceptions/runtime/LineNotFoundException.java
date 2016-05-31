/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.runtime;

/**
 * @author Elias Keis (31.05.2016)
 */
public class LineNotFoundException extends RuntimeException {
    public LineNotFoundException(int lineNr) {
        super(lineNr, "line_not_found");
    }
}
