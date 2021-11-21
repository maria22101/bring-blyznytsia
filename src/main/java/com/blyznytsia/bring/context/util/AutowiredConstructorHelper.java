package com.blyznytsia.bring.context.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.exceptions.AmbiguousAutowiredConstructorParamsException;

import lombok.SneakyThrows;

public class AutowiredConstructorHelper {

    @SneakyThrows
    public static void validateAndSetUpDependsOnFields(BeanDefinition beanDefinition) {
        var targetClass = Class.forName(beanDefinition.getClassName());

        var dependsOnFields = new ArrayList<String>();
        Arrays.stream(targetClass.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .map(Constructor::getParameterTypes)
                .flatMap(Stream::of)
                .forEach(constructorParamType ->
                        checkRelevantFieldExistenceAndCollectParamType(
                                targetClass, constructorParamType, dependsOnFields));

        validateConstructorParams(targetClass, dependsOnFields);

        beanDefinition.setDependsOnFields(dependsOnFields);
    }

    private static void checkRelevantFieldExistenceAndCollectParamType(Class<?> targetClass,
                                                                       Class<?> constructorParamType,
                                                                       List<String> dependsOnFields) {
        Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> field.getType().equals(constructorParamType))
                .findFirst()
                .ifPresent(field -> dependsOnFields.add(field.getType().getName()));
    }

    private static void validateConstructorParams(Class<?> targetClass,
                                                  List<String> paramTypesList) {
        long constructorParamsCount = Arrays.stream(targetClass.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .findFirst()
                .get().getParameterCount();

        if (paramTypesList.size() != constructorParamsCount) {
            throw new AmbiguousAutowiredConstructorParamsException(
                    "Autowired constructor parameters must have correspondent class fields");
        }
    }
}
