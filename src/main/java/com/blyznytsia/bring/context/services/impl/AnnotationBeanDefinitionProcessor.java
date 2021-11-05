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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnotationBeanDefinitionProcessor implements BeanDefinitionProcessor {

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
                        .forEach(type -> registerBeanDefinition(registry, type));
            });
        });
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> type) {
        var beanDefinition = new BeanDefinition();
        beanDefinition.setClassName(type.getName());
        var dependsOn = new ArrayList<String>();
        Arrays.stream(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(Field::getName)
                .forEach(dependsOn::add);
        Arrays.stream(type.getDeclaredMethods())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(Method::getReturnType)
                .map(Class::getName)
                .forEach(dependsOn::add);
        beanDefinition.setDependsOn(dependsOn);
        registry.registerBeanDefinition(type.getName(), beanDefinition);
    }
}
