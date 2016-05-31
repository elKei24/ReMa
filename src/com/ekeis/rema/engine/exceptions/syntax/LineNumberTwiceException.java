/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.syntax;

/**
 * @author Elias Keis (31.05.2016)
 */
public class LineNumberTwiceException extends SyntaxException {
    public LineNumberTwiceException(int lineNr) {
        super("line_number_double", lineNr);
    }
}
