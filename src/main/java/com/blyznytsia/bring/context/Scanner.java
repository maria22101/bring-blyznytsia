package com.blyznytsia.bring.context;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Bean;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.context.exceptions.ConfigurationInsufficientException;
import com.blyznytsia.bring.context.exceptions.ConfigurationNotFoundException;
import com.blyznytsia.bring.context.exceptions.InterfaceAnnotationException;
import com.blyznytsia.bring.context.util.BeanDefinitionGenerator;

import lombok.Setter;

/**
 * Class scans all packages and finds classes annotated with @Configuration, then in these classes
 * gets value of @ComponentScan annotation, in packages indicated as value of this annotation finds classes
 * annotated with @Component and populates packageComponentsMap where key is the package name,
 * value - collection of @Component classes of this package.
 * For each @Configuration class and for each package indicated in @ComponentScan and for each @Component
 * of this package BeanDefinition is generated and stored in BeanDefinitionRegistry's storage
 */
public class Scanner {

    private final Map<String, Set<Class<?>>> packagesAndTheirComponentsFromAllConfigs = new HashMap<>();
    private Set<Class<?>> componentsFromAllConfigs;

    public void scanAndFillBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        var configs = getConfigs();

        validateConfigs(configs);

        collectPackagesAndTheirComponents(configs);

        collectComponents(packagesAndTheirComponentsFromAllConfigs);

        configs.forEach(config -> fillBeanDefinitionRegistry(config, registry));
    }

    private List<Class<?>> getConfigs() {
        return Arrays.stream(Package.getPackages())
                .flatMap(p -> {
                    var reflections = new Reflections(p.getName());
                    return reflections.getTypesAnnotatedWith(Configuration.class).stream();
                }).collect(toList());
    }

    private void validateConfigs(List<Class<?>> configs) {
        if (configs.isEmpty()) {
            throw new ConfigurationNotFoundException(
                    "No class annotated with @Configuration found. Context creation not possible");
        }
        configs.forEach(config -> {
            boolean isConfigClassWithComponentScan = config.isAnnotationPresent(ComponentScan.class);
            boolean isConfigClassWithBeans = Arrays.stream(config.getMethods())
                    .anyMatch(method -> method.isAnnotationPresent(Bean.class));

            if (!isConfigClassWithComponentScan && !isConfigClassWithBeans) {
                throw new ConfigurationInsufficientException(String.format(
                        "No @ComponentScan or @Bean annotations found in %s", config.getName()));
            }
        });
    }

    private void collectPackagesAndTheirComponents(List<Class<?>> configs) {
        configs.forEach(config -> {
            var packagesFromComponentScan = config.getAnnotation(ComponentScan.class).value();

            Arrays.stream(packagesFromComponentScan)
                    .forEach(packageFromComponentScan -> {
                        var reflection = new Reflections(packageFromComponentScan);
                        var packageComponents = reflection.getTypesAnnotatedWith(Component.class);
                        packagesAndTheirComponentsFromAllConfigs.putIfAbsent(packageFromComponentScan, packageComponents);
                    });
        });
    }

    private void collectComponents(Map<String, Set<Class<?>>> packagesAndTheirComponentsFromAllConfigs) {
        componentsFromAllConfigs = packagesAndTheirComponentsFromAllConfigs.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private void fillBeanDefinitionRegistry(Class<?> config,
                                            BeanDefinitionRegistry registry) {
        var packagesForCurrentConfig = config.getAnnotation(ComponentScan.class).value();

        getAllClassesForCurrentConfig(config, packagesForCurrentConfig)
                .forEach(targetClass -> {
                    rejectInterfaceAnnotatedWithComponent(targetClass);
                    registerBeanDefinition(targetClass, registry, componentsFromAllConfigs);
                });
    }

    private Set<Class<?>> getAllClassesForCurrentConfig(Class<?> config,
                                                        String[] packages) {
        var classes = new HashSet<>(getClassesAnnotatedWithComponent(packages));
        classes.addAll(getClassesAnnotatedWithBean(config));
        return classes;
    }

    private Set<Class<?>> getClassesAnnotatedWithComponent(String[] packages) {
        return packagesAndTheirComponentsFromAllConfigs.entrySet().stream()
                .filter(entry -> List.of(packages).contains(entry.getKey()))
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toSet());
    }

    private Set<Class<?>> getClassesAnnotatedWithBean(Class<?> config) {
        var classes = Arrays.stream(config.getMethods())
                .filter(method -> method.isAnnotationPresent(Bean.class))
                .collect(toSet());
        return classes.stream()
                .map(Method::getReturnType)
                .collect(toSet());
    }

    private void rejectInterfaceAnnotatedWithComponent(Class<?> targetClass) {
        if (targetClass.isInterface()) {
            throw new InterfaceAnnotationException(String.format(
                    "%s can not be annotated as Component", targetClass));
        }
    }

    private void registerBeanDefinition(Class<?> targetClass,
                                        BeanDefinitionRegistry registry,
                                        Set<Class<?>> componentsFromAllConfigs) {
        if (!registry.containsBeanDefinition(targetClass.getName())) {
            var beanDefinition = BeanDefinitionGenerator.generate(targetClass, componentsFromAllConfigs);
            registry.registerBeanDefinition(targetClass.getName(), beanDefinition);
        }
    }
}
