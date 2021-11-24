package com.blyznytsia.bring.context.services.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.services.BeanCreator;

import lombok.SneakyThrows;

public class AutowiredConstructorBeanCreator implements BeanCreator {

    @Override
    @SneakyThrows
    public Object create(String className, Map<String, Object> beanMap) {

        return beanMap.computeIfAbsent(className,
                classNameKey -> createBeanFromAutowiredConstructor(classNameKey, beanMap));
    }

    @SneakyThrows
    public Object createBeanFromAutowiredConstructor(String className, Map<String, Object> beanMap) {
        var targetClass = Class.forName(className);
        var paramsTypesArray = createParamsTypesArray(targetClass);
        var constructor = targetClass.getConstructor(paramsTypesArray);

        return createObject(constructor, paramsTypesArray, beanMap);
    }

    private Class<?>[] createParamsTypesArray(Class<?> targetClass) {
        var paramsTypesList = new ArrayList<Class<?>>();
        Arrays.stream(targetClass.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .map(Constructor::getParameterTypes)
                .flatMap(Stream::of)
                .forEach(paramsTypesList::add);

        return paramsTypesList.toArray(new Class<?>[0]);
    }

    @SneakyThrows
    private Object createObject(Constructor<?> constructor,
                                Class<?>[] paramTypesArray,
                                Map<String, Object> beanMap) {
        Object[] params = new Object[paramTypesArray.length];
        for (int i = 0; i < paramTypesArray.length; i++) {
            params[i] = beanMap.get(paramTypesArray[i].getName());
        }
        return constructor.newInstance(params);
    }
}
