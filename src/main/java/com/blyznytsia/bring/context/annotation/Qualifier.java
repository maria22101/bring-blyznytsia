package com.blyznytsia.bring.context.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an {@link Autowired} annotated field.
 * Annotation to be used if injection-candidate-field-type is an interface that has more than one implementation
 * eligible for injection. In this case one implementation to be chosen and indicated as follows:
 * - add a name as the annotation {@link Component} value of the chosen implementation class;
 * - annotate with {@link Qualifier} the {@link Autowired} field of the interface type;
 * - add the name as the annotation {@link Qualifier} value
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {
    String value();
}
