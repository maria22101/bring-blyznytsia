package com.blyznytsia.bring.context.exceptions;

public class CircularDependencyException extends RuntimeException {

    public CircularDependencyException(String message) {
        super(message);
    }
}
