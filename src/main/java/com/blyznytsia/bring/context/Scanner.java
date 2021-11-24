package com.blyznytsia.bring.context;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.exceptions.BeanCreationException;
import com.blyznytsia.bring.context.exceptions.InterfaceAnnotationException;
import com.blyznytsia.bring.context.services.BeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredConstructorBeanCreator;
import com.blyznytsia.bring.context.services.impl.AutowiredFieldBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredSetterBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.EmptyConstructorBeanCreator;
import com.blyznytsia.bring.context.util.AutowiredConstructorHelper;
import com.blyznytsia.bring.context.util.InterfaceAnalyzer;

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
                var classesAnnotatedWithComponent = scanPackageReflections.getTypesAnnotatedWith(Component.class);
                classesAnnotatedWithComponent
                        .forEach(type -> {
                            rejectInterfaceAnnotatedWithComponent(type);
                            registerBeanDefinition(registry, type, classesAnnotatedWithComponent);
                        });
            });
        });
    }

    private void rejectInterfaceAnnotatedWithComponent(Class<?> type) {
        if(type.isInterface()) {
            throw new InterfaceAnnotationException(String.format(
                    "%s can not be annotated as Component", type));
        }
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> type,
                                        Set<Class<?>> classesAnnotatedWithComponent) {

        var dependsOnFields = scanAutowiredFields(type, classesAnnotatedWithComponent);
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

    // find Autowired fields, if they are Interface type -> define implementation
    private List<String> scanAutowiredFields(Class<?> targetClass,
                                             Set<Class<?>> classesAnnotatedWithComponent) {
        return Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .map(field -> InterfaceAnalyzer.getImplementation(targetClass, field, classesAnnotatedWithComponent))
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
            return;
        }
        if(isSingleAutowiredConstructorPresent(type)) {
            beanDefinition.setBeanCreator(new AutowiredConstructorBeanCreator());
            AutowiredConstructorHelper.validateAndSetUpDependsOnFields(beanDefinition);
            return;
        }
        throw new BeanCreationException(String.format(
                "Context creation error: no Default or Autowired constructor found for bean %s", type.getName()));
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
