package com.blyznytsia.bring.context;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.services.BeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredConstructorBeanCreator;
import com.blyznytsia.bring.context.services.impl.AutowiredFieldBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredSetterBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.EmptyConstructorBeanCreator;

/**
 * Class scans packages indicated in @ComponentScan annotation,
 * finds classes annotated with @Component and collects metadata relevant for further bean creation.
 * These metadata is placed into BeanDefinition instances that are stored in
 * BeanDefinitionRegistry's storage
 */
public class Scanner {

    //TODO: add posibility to scan multiple packages indicated in @ComponentScan property
    public void scan(List<String> packages, BeanDefinitionRegistry registry) {
        packages.forEach(currentPackage -> {
            var reflections = new Reflections(currentPackage);

            var typesWithComponentScan =
                    reflections.getTypesAnnotatedWith(ComponentScan.class);

            typesWithComponentScan.forEach(currentComponentScanClass -> {
                var componentScanAnnotation = currentComponentScanClass.getAnnotation(ComponentScan.class);
                var packageToScan = componentScanAnnotation.value();

                var scanPackageReflections = new Reflections(packageToScan);
                scanPackageReflections.getTypesAnnotatedWith(Component.class)
                        .forEach(type -> registerBeanDefinition(registry, type));
            });
        });
    }

    //TODO: check if interface -> if yes -> check that 1 impl class exist
    //if not found / > than 1 implementations without qualifier(or primary) found => exception
    //


    private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> type) {
        var dependsOnFields = scanAutowiredFields(type);
        var dependsOnFromSetters = scanAutowiredMethods(type);

        var beanConfigurators = new ArrayList<BeanConfigurator>();
        if(!dependsOnFields.isEmpty()){
            beanConfigurators.add(new AutowiredFieldBeanConfigurator());
        }
        if(!dependsOnFromSetters.isEmpty()){
            beanConfigurators.add(new AutowiredSetterBeanConfigurator());
        }
        dependsOnFields.addAll(dependsOnFromSetters);

        var beanDefinition = new BeanDefinition();
        beanDefinition.setClassName(type.getName());
        beanDefinition.setDependsOnFields(dependsOnFields);
        beanDefinition.setBeanConfigurators(beanConfigurators);

        setUpBeanCreators(beanDefinition, type);

        registry.registerBeanDefinition(type.getName(), beanDefinition);
    }

    // find Autowired fields
    private List<String> scanAutowiredFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(Field::getType)
                .map(Type::getTypeName)
                .collect(Collectors.toList());
    }

    // find Autowired methods(setters)
    //TODO: process exception if there are two method parameters
    private List<String> scanAutowiredMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(method -> Arrays.stream(method.getParameterTypes()).findFirst())
                .flatMap(Optional::stream)
                .map(Type::getTypeName)
                .collect(Collectors.toList());
    }

    private void setUpBeanCreators(BeanDefinition beanDefinition,
                                   Class<?> type) {
        if(isDefaultConstructorPresent(type)) {
            beanDefinition.setBeanCreator(new EmptyConstructorBeanCreator());
        }
        if(isSingleAutowiredConstructorPresent(type)) {
            beanDefinition.setBeanCreator(new AutowiredConstructorBeanCreator());
        }
    }

    private boolean isDefaultConstructorPresent(Class<?> type) {
        return type.getConstructors()[0].getParameterCount() == 0;
    }

    private boolean isSingleAutowiredConstructorPresent(Class<?> type) {
        return Arrays.stream(type.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .count() == 1;
    }
}
