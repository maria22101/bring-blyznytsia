package com.blyznytsia.bring.context.services.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.exceptions.BeanCreationException;
import com.blyznytsia.bring.context.services.BeanConfigurator;

import lombok.SneakyThrows;

public class AutowiredFieldBeanConfigurator implements BeanConfigurator {

    @Override
    @SneakyThrows
    public Object configure(Object objectToConfigure, BeanDefinition beanDefinition, Map<String, Object> beanMap) {

        Arrays.stream(objectToConfigure.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        String typeName = getImplementationIfFieldIsInterface(field, beanDefinition);
                        field.set(objectToConfigure, beanMap.get(typeName));
                    } catch (IllegalAccessException e) {
                        throw new BeanCreationException("Unable to set @Autowired field");
                    }
                });

        return objectToConfigure;
    }

    private String getImplementationIfFieldIsInterface(Field field, BeanDefinition beanDefinition) {
        if(fieldIsNotInterface(field)) {
            return field.getType().getName();
        }
        return beanDefinition.getDependsOnFields().stream()
                .filter(dependOn -> {
                            Class<?> dependOnClass = null;
                            try {
                                dependOnClass = Class.forName(dependOn);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            return isClassInterfaceImplementation(dependOnClass, field.getType());
                        })
                .findFirst()
                .orElseThrow();
    }

    private boolean isClassInterfaceImplementation(Class<?> aClass,
                                                   Class<?> fieldInterface) {
        return Arrays.stream(aClass.getInterfaces())
                .anyMatch(aInterface -> aInterface.getName().equals(fieldInterface.getName()));
    }

    private static boolean fieldIsNotInterface(Field field) {
        return !field.getType().isInterface();
    }
}
