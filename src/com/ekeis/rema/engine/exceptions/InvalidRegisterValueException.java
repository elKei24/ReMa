package com.ekeis.rema.engine.exceptions;

/**
 * @author Elias Keis (30.05.2016)
 */
public class InvalidRegisterValueException extends RemaException {
    public InvalidRegisterValueException(long val, long min, long max) {
        super(String.format(res.getString("exception.invalid_register_value"), val, min, max));
    }
}
