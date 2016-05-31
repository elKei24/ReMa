/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.runtime;

import com.ekeis.rema.engine.exceptions.RemaException;

/**
 * @author Elias Keis (31.05.2016)
 */
public class RuntimeException extends RemaException
{
    public RuntimeException(int lineNr) {
        super(String.format(EX_RES.getString("exception.runtime"), lineNr));
    }
    protected RuntimeException(int lineNr, String id, Object... formatContent) {
        this(lineNr, String.format(EX_RES.getString("exception.runtime." + id), formatContent));
    }
    public RuntimeException(int lineNr, String extraMessage) {
        super(String.format(EX_RES.getString("exception.runtime.details"), lineNr, extraMessage));
    }
}
