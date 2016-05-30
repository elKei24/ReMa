package com.ekeis.rema.engine.log;

/**
 * @author Elias Keis (30.05.2016)
 */
public interface LogMessage {
    String getMessage();
    String getCategory();
    int getLine();
}
