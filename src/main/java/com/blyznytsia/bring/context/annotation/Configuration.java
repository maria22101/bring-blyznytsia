package com.blyznytsia.bring.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class and indicates that the class declares:
 * either {@link ComponentScan} value and/or {@link Bean} annotated methods -
 * that further processed by Bring aiming at objects creation data
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
}
