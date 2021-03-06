package com.blyznytsia.bring.context;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
 * {@link Scanner} class purpose is to populate {@link BeanDefinitionRegistry} with {@link BeanDefinition}
 */
public class Scanner {

    /**
     * Method populates {@link BeanDefinitionRegistry} with {@link BeanDefinition}
     *
     * @param registry      storage of {@link BeanDefinition}
     */
    public void scanAndFillBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        var configs = getConfigs();
        validateConfigs(configs);
        var allComponentsClasses = getAllClassesAnnotatedWithComponent(configs);
        var allClasses = collectAllClassesForBeanDefinitionCreation(configs, allComponentsClasses);

        allClasses.forEach(targetClass -> registerBeanDefinition(targetClass, registry, allComponentsClasses));
    }

    /**
     * Method scans all the packages
     *
     * @return a list of all {@link Configuration} annotated classes of the packages
     */
    private List<Class<?>> getConfigs() {
        return Arrays.stream(Package.getPackages())
                .flatMap(p -> {
                    var reflections = new Reflections(p.getName());
                    return reflections.getTypesAnnotatedWith(Configuration.class).stream();
                }).collect(toList());
    }

    /**
     * Validation method that checks presence of a valid config class
     *
     * @param configs    config classes
     */
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

    /**
     * Method collects all {@link Component} annotated classes eligible for {@link BeanDefinition} creation
     *
     * @param configs   config classes
     * @return          set of all {@link Component} annotated classes eligible for {@link BeanDefinition} creation
     */
    private HashSet<Class<?>> getAllClassesAnnotatedWithComponent(List<Class<?>> configs) {
        var componentsFromAllConfigs = new HashSet<Class<?>>();
        configs.forEach(config -> {
            var packagesFromComponentScan = config.getAnnotation(ComponentScan.class).value();

            Arrays.stream(packagesFromComponentScan)
                    .forEach(pckg -> {
                        var reflection = new Reflections(pckg);
                        var packageComponents = reflection.getTypesAnnotatedWith(Component.class);
                        packageComponents.forEach(this::rejectInterfaceAnnotatedWithComponent);
                        componentsFromAllConfigs.addAll(packageComponents);
                    });
        });
        return componentsFromAllConfigs;
    }

    /**
     * Method collects all classes eligible for {@link BeanDefinition} creation
     *
     * @param configs           config classes
     * @param componentClasses  set of {@link Component} annotated classes
     * @return                  set of all classes eligible for {@link BeanDefinition} creation
     */
    private Set<Class<?>> collectAllClassesForBeanDefinitionCreation(List<Class<?>> configs,
                                                                     Set<Class<?>> componentClasses) {
        var classes = new HashSet<>(componentClasses);
        var classesFromBeanAnnotation = getAllClassesFromBeanAnnotation(configs);
        classes.addAll(classesFromBeanAnnotation);
        return classes;
    }

    private Set<Class<?>> getAllClassesFromBeanAnnotation(List<Class<?>> configs) {
        var classesFromBeans = new HashSet<Class<?>>();

        configs.forEach(config -> {
            var methodsAnnotatedWithBean = Arrays.stream(config.getMethods())
                    .filter(method -> method.isAnnotationPresent(Bean.class))
                    .collect(toSet());
            methodsAnnotatedWithBean.forEach(this::rejectIfReturnTypeIsInterface);

            classesFromBeans.addAll(methodsAnnotatedWithBean.stream()
                    .map(Method::getReturnType)
                    .collect(toSet()));
        });
        return classesFromBeans;
    }

    private void rejectInterfaceAnnotatedWithComponent(Class<?> aClass) {
        if (aClass.isInterface()) {
            throw new InterfaceAnnotationException(String.format(
                    "%s can not be annotated as Component", aClass));
        }
    }

    /**
     * Method populates the provided {@link BeanDefinitionRegistry} with {@link BeanDefinition}
     * received from call to {@link BeanDefinitionGenerator}
     *
     * @param targetClass         a class that is source for {@link BeanDefinition}
     * @param registry            {@link BeanDefinition} container
     * @param componentsClasses   all {@link Component} annotated classes eligible for {@link BeanDefinition} creation
     */
    private void registerBeanDefinition(Class<?> targetClass,
                                        BeanDefinitionRegistry registry,
                                        Set<Class<?>> componentsClasses) {
        if (!registry.containsBeanDefinition(targetClass.getName())) {
            var beanDefinition = BeanDefinitionGenerator.generate(targetClass, componentsClasses);
            registry.registerBeanDefinition(targetClass.getName(), beanDefinition);
        }
    }

    private void rejectIfReturnTypeIsInterface(Method method) {
        if (method.getReturnType().isInterface()) {
            throw new InterfaceAnnotationException(String.format(
                    "Method %s return type is an interface", method));
        }
    }
}
