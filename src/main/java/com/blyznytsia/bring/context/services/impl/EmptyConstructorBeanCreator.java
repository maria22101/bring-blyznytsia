package com.blyznytsia.bring.context.services.impl;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.blyznytsia.bring.context.exceptions.NoDefaultConstructorException;
import com.blyznytsia.bring.context.services.BeanCreator;

import lombok.SneakyThrows;

public class EmptyConstructorBeanCreator implements BeanCreator {

    @Override
    @SneakyThrows
    public Object create(String className, Map<String, Object> beanMap) {

        return beanMap.computeIfAbsent(className, this::createBeanFromEmptyConstructor);

    }

    @SneakyThrows
    private Object createBeanFromEmptyConstructor(String className) {
        var targetClass = Class.forName(className);

        Constructor<?> constructor;
        try {
            constructor = targetClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new NoDefaultConstructorException("Unable to find empty constructor");
        }
        return constructor.newInstance();
    }
}
