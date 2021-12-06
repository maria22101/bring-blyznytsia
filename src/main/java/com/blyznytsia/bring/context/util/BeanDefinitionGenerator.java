package com.blyznytsia.bring.context.util;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.exceptions.BeanCreationException;
import com.blyznytsia.bring.context.services.BeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredConstructorBeanCreator;
import com.blyznytsia.bring.context.services.impl.AutowiredFieldBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredSetterBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.EmptyConstructorBeanCreator;

/**
 *  Class generates BeanDefinition for the target class based of its class name
 *  and set of Class names that indicates a range where interface implementations to be found if the target class
 *  field is of an interface type
 */
public class BeanDefinitionGenerator {

    public static BeanDefinition generate(Class<?> targetClass, Set<Class<?>> interfaceImplementationsRange) {
        var dependsOnFields = scanAutowiredFields(targetClass, interfaceImplementationsRange);
        var dependsOnFromSetters = scanAutowiredMethods(targetClass);

        var beanConfigurators = new ArrayList<BeanConfigurator>();
        if (!dependsOnFields.isEmpty()) {
            beanConfigurators.add(new AutowiredFieldBeanConfigurator());
        }
        if (!dependsOnFromSetters.isEmpty()) {
            beanConfigurators.add(new AutowiredSetterBeanConfigurator());
        }
        dependsOnFields.addAll(dependsOnFromSetters);

        var beanDefinition = new BeanDefinition();
        beanDefinition.setClassName(targetClass.getName());
        beanDefinition.setDependsOnFields(dependsOnFields);
        beanDefinition.setBeanConfigurators(beanConfigurators);

        setUpBeanCreators(beanDefinition, targetClass);

        return beanDefinition;
    }

    // find Autowired fields, if they are Interface type -> define implementation
    private static List<String> scanAutowiredFields(Class<?> targetClass,
                                                    Set<Class<?>> interfaceImplementationsRange) {
        return Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .map(field -> InterfaceAnalyzer.getImplementation(targetClass, field, interfaceImplementationsRange))
                .collect(toList());
    }

    // find Autowired methods(setters)
    //TODO: process exception if there are two method parameters
    public static List<String> scanAutowiredMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(method -> Arrays.stream(method.getParameterTypes()).findFirst())
                .flatMap(Optional::stream)
                .map(Type::getTypeName)
                .collect(toList());
    }

    private static void setUpBeanCreators(BeanDefinition beanDefinition,
                                   Class<?> type) {
        if (isDefaultConstructorPresent(type)) {
            beanDefinition.setBeanCreator(new EmptyConstructorBeanCreator());
            return;
        }
        if (isSingleAutowiredConstructorPresent(type)) {
            beanDefinition.setBeanCreator(new AutowiredConstructorBeanCreator());
            AutowiredConstructorHelper.validateAndSetUpDependsOnFields(beanDefinition);
            return;
        }
        throw new BeanCreationException(String.format(
                "Context creation error: no Default or Autowired constructor found for bean %s", type.getName()));
    }

    private static boolean isDefaultConstructorPresent(Class<?> type) {
        return type.getConstructors()[0].getParameterCount() == 0;
    }

    private static boolean isSingleAutowiredConstructorPresent(Class<?> type) {
        return Arrays.stream(type.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .count() == 1;
    }
}
