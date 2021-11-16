package com.blyznytsia.bring.context.services.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.exceptions.AmbiguousAutowiredConstructorParamsException;
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
                .forEach(constructorParamType ->
                        checkRelevantFieldExistenceAndCollectParamType(
                                targetClass, constructorParamType, paramsTypesList));

        validateConstructorParams(targetClass, paramsTypesList);

        return paramsTypesList.toArray(new Class<?>[0]);
    }

    private void checkRelevantFieldExistenceAndCollectParamType(Class<?> targetClass,
                                                                Class<?> constructorParamType,
                                                                List<Class<?>> paramTypesList) {
        Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> field.getType().equals(constructorParamType))
                .findFirst()
                .ifPresent(field -> paramTypesList.add(field.getType()));
    }

    private void validateConstructorParams(Class<?> targetClass,
                                           List<Class<?>> paramTypesList) {
        long constructorParamsCount = Arrays.stream(targetClass.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .findFirst()
                .get().getParameterCount();

        if (paramTypesList.size() != constructorParamsCount) {
            throw new AmbiguousAutowiredConstructorParamsException(
                    "Autowired constructor parameters must have correspondent class fields");
        }
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
