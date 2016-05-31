/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * @author Elias Keis (31.05.2016)
 */
public class InternalException extends RemaException {
    public InternalException() {
        super(String.format(EX_RES.getString("exception.internal")));
    }
    public InternalException(@NotNull Exception ex) {
        super(String.format(EX_RES.getString("exception.internal.details"), ex.toString()));
    }
}
