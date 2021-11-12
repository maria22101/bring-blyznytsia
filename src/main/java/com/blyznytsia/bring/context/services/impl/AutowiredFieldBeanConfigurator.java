package com.blyznytsia.bring.context.services.impl;

import java.util.Arrays;
import java.util.Map;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.exceptions.BeanCreationException;
import com.blyznytsia.bring.context.services.BeanConfigurator;

import lombok.SneakyThrows;

public class AutowiredFieldBeanConfigurator implements BeanConfigurator {

    @Override
    @SneakyThrows
    public Object configure(Object objectToConfigure, Map<String, Object> beanMap) {

        Arrays.stream(objectToConfigure.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .forEach(type -> {
                    type.setAccessible(true);
                    try {
                        type.set(objectToConfigure, beanMap.get(type.getType().getTypeName()));
                    } catch (IllegalAccessException e) {
                        throw new BeanCreationException("Unable to set @Autowired field");
                    }
                });

        return objectToConfigure;
    }
}
