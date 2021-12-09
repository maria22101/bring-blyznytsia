package com.blyznytsia.bring.context.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.Qualifier;
import com.blyznytsia.bring.context.exceptions.InterfaceInjectionException;

/**
 * {@link InterfaceAnalyzer} is an util class that defines a class type for a field
 */
public class InterfaceAnalyzer {

    public static String getType(Class<?> targetClass,
                                 Field field,
                                 Set<Class<?>> interfaceImplementationsRange) {
        if(fieldIsNotInterface(field)) {
            return field.getType().getName();
        }
        if (fieldIsWithoutQualifierAnnotation(field)) {
            return getImplementationOfNotAnnotatedInterfaceField(targetClass, field, interfaceImplementationsRange);
        }
        return getImplementationOfAnnotatedInterfaceField(targetClass, field, interfaceImplementationsRange);
    }

    private static String getImplementationOfNotAnnotatedInterfaceField(Class<?> targetClass,
                                                                        Field field,
                                                                        Set<Class<?>> interfaceImplementationsRange) {
        var implementations = getInterFaceImplementations(field.getType(), interfaceImplementationsRange);

        if (implementations.isEmpty()) {
            throw new InterfaceInjectionException(String.format(
                    "%s creation error: no %s implementation found",
                    targetClass, field.getType().getName()));
        }
        if (implementations.size() > 1) {
            throw new InterfaceInjectionException(String.format(
                    "%s creation ambiguity: more than one %s implementation found",
                    targetClass, field.getType().getName()));
        }
        return implementations.get(0).getName();
    }

    private static String getImplementationOfAnnotatedInterfaceField(Class<?> targetClass,
                                                                     Field field,
                                                                     Set<Class<?>> interfaceImplementationsRange) {
        var qualifierValue = field.getAnnotation(Qualifier.class).value();
        var fieldInterface = field.getType();

        var implementations =
                getInterfaceImplWithAnnotationValue(fieldInterface, interfaceImplementationsRange, qualifierValue);

        if (implementations.isEmpty()) {
            throw new InterfaceInjectionException(String.format(
                    "%s creation error: no bean of %s named as indicated in @Qualifier annotation found",
                    targetClass, field.getType().getName()));
        }
        if (implementations.size() > 1) {
            throw new InterfaceInjectionException(String.format(
                    "%s creation ambiguity: more than one %s implementation found",
                    targetClass, fieldInterface.getName()));
        }
        return implementations.get(0).getName();
    }

    private static boolean fieldIsNotInterface(Field field) {
        return !field.getType().isInterface();
    }

    private static boolean fieldIsWithoutQualifierAnnotation(Field field) {
        return !field.isAnnotationPresent(Qualifier.class);
    }

    private static List<Class<?>> getInterFaceImplementations(Class<?> fieldInterface,
                                                              Set<Class<?>> interfaceImplementationsRange) {
        return interfaceImplementationsRange.stream()
                .filter(aClass -> isClassInterfaceImplementation(aClass, fieldInterface))
                .collect(Collectors.toList());
    }

    private static boolean isClassInterfaceImplementation(Class<?> aClass,
                                                          Class<?> fieldInterface) {
        return Arrays.stream(aClass.getInterfaces())
                .anyMatch(aInterface -> aInterface.getName().equals(fieldInterface.getName()));
    }

    private static List<Class<?>> getInterfaceImplWithAnnotationValue(Class<?> fieldInterface,
                                                                      Set<Class<?>> interfaceImplementationsRange,
                                                                      String annotationValue) {
        return interfaceImplementationsRange.stream()
                .filter(aClass -> isClassInterfaceImplementation(aClass, fieldInterface) &&
                        aClass.getAnnotation(Component.class).value().equals(annotationValue))
                .collect(Collectors.toList());
    }
}
