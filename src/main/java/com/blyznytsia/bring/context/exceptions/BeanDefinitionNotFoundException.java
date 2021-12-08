package com.blyznytsia.bring.context.exceptions;

public class BeanDefinitionNotFoundException extends RuntimeException {

    public BeanDefinitionNotFoundException(String message) {
        super(message);
    }
}
