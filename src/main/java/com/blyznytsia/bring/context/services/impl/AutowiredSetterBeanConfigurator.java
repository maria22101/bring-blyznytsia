package com.blyznytsia.bring.context.services.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.exceptions.BeanCreationException;
import com.blyznytsia.bring.context.services.BeanConfigurator;

import lombok.SneakyThrows;

/**
 * {@link AutowiredSetterBeanConfigurator} implements functionality of injecting into an object
 * fields that are set via {@link Autowired} setters
 */
public class AutowiredSetterBeanConfigurator implements BeanConfigurator {

    @Override
    @SneakyThrows
    public void configure(Object objectToConfigure, BeanDefinition beanDefinition, Map<String, Object> beanMap) {

        Arrays.stream(objectToConfigure.getClass().getDeclaredMethods())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(method -> Arrays.stream(method.getParameterTypes()).findFirst())
                .flatMap(Optional::stream)
                .forEach(type -> {
                    Field field = getFieldCorrespondingToSetterParameter(type, objectToConfigure);
                    field.setAccessible(true);
                    try {
                        field.set(objectToConfigure, beanMap.get(field.getType().getTypeName()));
                    } catch (IllegalAccessException e) {
                        throw new BeanCreationException("Unable to set field that corresponds to @Autowired setter");
                    }
                });
    }

    private Field getFieldCorrespondingToSetterParameter(Class<?> setterParameterType,
                                                         Object objectToConfigure) {
        return Arrays.stream(objectToConfigure.getClass().getDeclaredFields())
                .filter(field -> (field.getType()).equals(setterParameterType))
                .findFirst()
                .orElseThrow();
    }
}
