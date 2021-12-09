package com.blyznytsia.bring.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link Configuration} annotated class.
 * {@link ComponentScan} annotation value is package(s) to be scanned through by Bring for objects creation indicators
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    String[] value();
}
