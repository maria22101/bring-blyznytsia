package com.blyznytsia.bring.context.services.impl;

import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.BeanDefinitionRegistry;
import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.services.BeanDefinitionProcessor;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class scans packages indicated in @ComponentScan annotation,
 * finds classes annotated with @Component and collects their @Autowired fields - these information
 * stored in BeanDefinitionRegistry's storage
 */
public class AnnotationBeanDefinitionProcessor implements BeanDefinitionProcessor {

    @Override
    public void process(List<String> packages, BeanDefinitionRegistry registry) {
        packages.forEach(currentPackage -> {
            var reflections = new Reflections(currentPackage);

            var typesWithComponentScan =
                    reflections.getTypesAnnotatedWith(ComponentScan.class);

            typesWithComponentScan.forEach(currentComponentScanClass -> {
                var beanAnnotation = currentComponentScanClass.getAnnotation(ComponentScan.class);
                var packageToScan = beanAnnotation.value();

                var scanPackageReflections = new Reflections(packageToScan);
                scanPackageReflections.getTypesAnnotatedWith(Component.class)
                        .forEach(type -> registerBeanDefinitionAutowired(registry, type));
            });
        });
    }

    private void registerBeanDefinitionAutowired(BeanDefinitionRegistry registry, Class<?> type) {
        var beanDefinition = new BeanDefinition();
        beanDefinition.setClassName(type.getName());
        var dependsOn = new ArrayList<String>();

        var dependsOnFields = registerBeanDefinitionAutowiredFields(type);
        var dependsOnSetters = registerBeanDefinitionAutowiredMethods(type);

        dependsOn.addAll(dependsOnFields);
        dependsOn.addAll(dependsOnSetters);
        beanDefinition.setDependsOn(dependsOn);
        registry.registerBeanDefinition(type.getName(), beanDefinition);
    }

    // processor for Autowiring fields
    private List<String> registerBeanDefinitionAutowiredFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(Field::getType)
                .map(Type::getTypeName)
                .collect(Collectors.toList());
    }

    // processor for Autowiring methods(setters)
    //TODO: process exception if there are two method parameters
    private List<String> registerBeanDefinitionAutowiredMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(method -> Arrays.stream(method.getParameterTypes()).findFirst())
                .flatMap(Optional::stream)
                .map(Type::getTypeName)
                .collect(Collectors.toList());
    }



    // TODO: processing for creation via constructor

    // TODO: processing for constructing via Autowired constructor and final fields
}
