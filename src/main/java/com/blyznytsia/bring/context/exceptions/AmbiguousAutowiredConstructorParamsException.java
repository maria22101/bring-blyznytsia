package com.blyznytsia.bring.context.exceptions;

public class AmbiguousAutowiredConstructorParamsException extends RuntimeException {

    public AmbiguousAutowiredConstructorParamsException(String message) {
        super(message);
    }
}
