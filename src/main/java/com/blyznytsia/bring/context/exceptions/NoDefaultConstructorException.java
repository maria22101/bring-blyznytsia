package com.blyznytsia.bring.context.exceptions;

public class NoDefaultConstructorException extends RuntimeException {

    public NoDefaultConstructorException(String message) {
        super(message);
    }
}
