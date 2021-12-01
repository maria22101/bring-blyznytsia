package com.blyznytsia.bring.context;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Bean;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.context.exceptions.ConfigurationInsufficientException;
import com.blyznytsia.bring.context.exceptions.ConfigurationNotFoundException;
import com.blyznytsia.bring.context.exceptions.InterfaceAnnotationException;
import com.blyznytsia.bring.context.util.BeanDefinitionGenerator;

/**
 * Class scans all packages and finds classes annotated with @Configuration, then in these classes
 * gets value of @ComponentScan annotation, in packages indicated as value of this annotation finds classes
 * annotated with @Component and populates packageComponentsMap where key is the package name,
 * value - collection of @Component classes of this package.
 * For each @Configuration class and for each package indicated in @ComponentScan and for each @Component
 * of this package BeanDefinition is generated and stored in BeanDefinitionRegistry's storage
 */
public class Scanner {
    private Map<String, Set<Class<?>>> packageComponentsMap = new HashMap<>();

    public void scan(BeanDefinitionRegistry registry) {
        var configClasses = getConfigClasses();
        validateConfigClasses(configClasses);
        populatePackageComponentsMap(configClasses);

        configClasses.forEach(configClass -> {
            createBeanDefinitionsFromComponentScanAnnotation(configClass, registry);
            createBeanDefinitionsFromBeanAnnotations();
        });
    }

    private List<Class<?>> getConfigClasses() {
        return Arrays.stream(Package.getPackages())
                .flatMap(p -> {
                    var reflections = new Reflections(p.getName());
                    return reflections.getTypesAnnotatedWith(Configuration.class).stream();
                }).collect(toList());
    }

    private void populatePackageComponentsMap(List<Class<?>> configClasses) {
        configClasses.forEach(configClass -> {
            var packagesFromComponentScan = configClass.getAnnotation(ComponentScan.class).value();

            Arrays.stream(packagesFromComponentScan)
                    .forEach(packageFromComponentScan -> {
                        var reflection = new Reflections(packageFromComponentScan);
                        var classesComponents = reflection.getTypesAnnotatedWith(Component.class);
                        packageComponentsMap.putIfAbsent(packageFromComponentScan, classesComponents);
                    });
        });
    }

    private void validateConfigClasses(List<Class<?>> configClasses) {
        if (configClasses.isEmpty()) {
            throw new ConfigurationNotFoundException(
                    "No class annotated with @Configuration found. Context creation not possible");
        }
        configClasses.forEach(configClass -> {
            boolean isConfigClassWithComponentScan = configClass.isAnnotationPresent(ComponentScan.class);
            boolean isConfigClassWithBeans = Arrays.stream(configClass.getMethods())
                    .anyMatch(method -> method.isAnnotationPresent(Bean.class));

            if (!isConfigClassWithComponentScan && !isConfigClassWithBeans) {
                throw new ConfigurationInsufficientException(String.format(
                        "No @ComponentScan or @Bean annotations found in %s", configClass.getName()));
            }
        });
    }

    private void createBeanDefinitionsFromComponentScanAnnotation(Class<?> configClass,
                                                                  BeanDefinitionRegistry registry) {
        var packagesToScanForCurrentConfigClass = configClass.getAnnotation(ComponentScan.class).value();
        var componentsForCurrentConfigClass = getComponents(packagesToScanForCurrentConfigClass);

        componentsForCurrentConfigClass
                .forEach(targetClass -> {
                    rejectInterfaceAnnotatedWithComponent(targetClass);
                    registerBeanDefinition(registry, targetClass, componentsForCurrentConfigClass);
                });
    }

    private HashSet<Class<?>> getComponents(String[] packages) {
        var components = new HashSet<Class<?>>();
        Arrays.stream(packages)
                .forEach(p -> {
                    var packageComponents = packageComponentsMap.get(p);
                    components.addAll(packageComponents);
                });
        return components;
    }

    private void createBeanDefinitionsFromBeanAnnotations() {
        //TODO
    }

    private void rejectInterfaceAnnotatedWithComponent(Class<?> targetClass) {
        if (targetClass.isInterface()) {
            throw new InterfaceAnnotationException(String.format(
                    "%s can not be annotated as Component", targetClass));
        }
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> targetClass,
                                        Set<Class<?>> componentsForCurrentConfigClass) {
        if (!registry.containsBeanDefinition(targetClass.getName())) {
            BeanDefinition beanDefinition = BeanDefinitionGenerator.generate(targetClass, componentsForCurrentConfigClass);
            registry.registerBeanDefinition(targetClass.getName(), beanDefinition);
        }
    }
}
