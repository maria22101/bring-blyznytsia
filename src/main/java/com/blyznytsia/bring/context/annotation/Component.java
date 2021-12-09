package com.blyznytsia.bring.context.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a class indicating that an object of its type to be created by Bring
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    String value() default "";
}
