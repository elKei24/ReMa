/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.engine.log;

/**
 * @author Elias Keis (30.05.2016)
 */
public interface LogMessage {
    enum Category {ERROR, DEBUG, COMMAND}

    String getMessage();
    Category getCategory();
}
