package com.blyznytsia.bring.context;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Bean;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.context.exceptions.BeanCreationException;
import com.blyznytsia.bring.context.exceptions.ConfigurationInsufficientException;
import com.blyznytsia.bring.context.exceptions.InterfaceAnnotationException;
import com.blyznytsia.bring.context.services.BeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredConstructorBeanCreator;
import com.blyznytsia.bring.context.services.impl.AutowiredFieldBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredSetterBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.EmptyConstructorBeanCreator;
import com.blyznytsia.bring.context.util.AutowiredConstructorHelper;
import com.blyznytsia.bring.context.util.InterfaceAnalyzer;

/**
 * Class scans packages that contain classes annotated with @Configuration, finds those classes,
 * reads @ComponentScan annotations value - for getting packages which are scanned
 * for classes annotated with @Component - which metadata are collected for further beans creation.
 * These metadata is placed into BeanDefinition instances that are stored in
 * BeanDefinitionRegistry's storage
 */
public class Scanner {

    public void scan(List<String> packagesContainingConfigClasses, BeanDefinitionRegistry registry) {
        packagesContainingConfigClasses
                .forEach(packageContainingConfigClasses -> {
                    var reflections = new Reflections(packageContainingConfigClasses);
                    var configClasses = reflections.getTypesAnnotatedWith(Configuration.class);
                    configClasses
                            .forEach(configClass ->
                                    scanConfigClassAndCreateBeanDefinitions(configClass, registry));
                });
    }

    private void scanConfigClassAndCreateBeanDefinitions(Class<?> configClass,
                                                         BeanDefinitionRegistry registry) {
        boolean isConfigClassWithComponentScan = configClass.isAnnotationPresent(ComponentScan.class);
        boolean isConfigClassWithBeans = Arrays.stream(configClass.getMethods())
                .anyMatch(method -> method.isAnnotationPresent(Bean.class));

        if (isConfigClassWithComponentScan) {
            var packagesFromComponentScan = configClass.getAnnotation(ComponentScan.class).value();
            Arrays.stream(packagesFromComponentScan)
                    .forEach(packageFromComponentScan ->
                            scanPackageAndCreateBeanDefinitions(packageFromComponentScan, registry));
        }

        if (isConfigClassWithBeans) {
            //TODO: implement creating BeanDefinition from @Bean annotation
        }

        if (!isConfigClassWithComponentScan && !isConfigClassWithBeans) {
            throw new ConfigurationInsufficientException(String.format(
                    "No @ComponentScan or @Bean annotations found in %s", configClass.getName()));
        }
    }

    private void scanPackageAndCreateBeanDefinitions(String packageFromComponentScan,
                                                     BeanDefinitionRegistry registry) {
        var reflection = new Reflections(packageFromComponentScan);
        var classesAnnotatedWithComponent = reflection.getTypesAnnotatedWith(Component.class);
        classesAnnotatedWithComponent.forEach(type -> {
            rejectInterfaceAnnotatedWithComponent(type);
            registerBeanDefinition(registry, type, classesAnnotatedWithComponent);
        });
    }

    private void rejectInterfaceAnnotatedWithComponent(Class<?> type) {
        if (type.isInterface()) {
            throw new InterfaceAnnotationException(String.format(
                    "%s can not be annotated as Component", type));
        }
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> type,
                                        Set<Class<?>> classesAnnotatedWithComponent) {

        var dependsOnFields = scanAutowiredFields(type, classesAnnotatedWithComponent);
        var dependsOnFromSetters = scanAutowiredMethods(type);

        var beanConfigurators = new ArrayList<BeanConfigurator>();
        if (!dependsOnFields.isEmpty()) {
            beanConfigurators.add(new AutowiredFieldBeanConfigurator());
        }
        if (!dependsOnFromSetters.isEmpty()) {
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
                .collect(toList());
    }

    // find Autowired methods(setters)
    //TODO: process exception if there are two method parameters
    private List<String> scanAutowiredMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(f -> f.isAnnotationPresent(Autowired.class))
                .map(method -> Arrays.stream(method.getParameterTypes()).findFirst())
                .flatMap(Optional::stream)
                .map(Type::getTypeName)
                .collect(toList());
    }

    private void setUpBeanCreators(BeanDefinition beanDefinition,
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

    private boolean isDefaultConstructorPresent(Class<?> type) {
        return type.getConstructors()[0].getParameterCount() == 0;
    }

    private boolean isSingleAutowiredConstructorPresent(Class<?> type) {
        return Arrays.stream(type.getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .count() == 1;
    }
}
