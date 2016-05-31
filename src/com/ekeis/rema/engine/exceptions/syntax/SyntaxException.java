/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.exceptions.syntax;

import com.ekeis.rema.engine.exceptions.RemaException;

/**
 * @author Elias Keis (30.05.2016)
 */
public class SyntaxException extends RemaException {
    public SyntaxException() {
        super(String.format(EX_RES.getString("exception.syntax")));
    }
    protected SyntaxException(String id, Object... formatContent) {
        super(String.format(EX_RES.getString("exception.syntax.details"),
                String.format(EX_RES.getString("exception.syntax." + id), formatContent)));
    }
}
