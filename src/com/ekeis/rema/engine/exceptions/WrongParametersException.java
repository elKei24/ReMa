package com.ekeis.rema.engine.exceptions;

/**
 * @author Elias Keis (30.05.2016)
 */
public class WrongParametersException extends RemaException {
    public WrongParametersException(int line) {
        super(res.getString("exception.invalid_parameters"), line);
    }
}
