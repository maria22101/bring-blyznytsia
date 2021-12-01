package com.blyznytsia.bring.context;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
 * populates packageComponentsMap that is storage of package name and its classes annotated with @Component,
 * processes @ComponentScan annotations of each configuration class and inside packages that are indicated as these
 * annotation value scans @Component classes and collects such classes metadata.
 * These metadata is placed into BeanDefinition instances that are stored in
 * BeanDefinitionRegistry's storage
 */
public class Scanner {
    private Map<String, Set<Class<?>>> packageComponentsMap = new HashMap<>();

    public void scan(List<String> packagesContainingConfigClasses, BeanDefinitionRegistry registry) {
        List<Class<?>> configClasses = packagesContainingConfigClasses.stream()
                .flatMap(packageContainingConfigClasses -> {
                    var reflections = new Reflections(packageContainingConfigClasses);
                    return reflections.getTypesAnnotatedWith(Configuration.class).stream();
                }).collect(Collectors.toList());

        validateConfigClasses(configClasses);
        populatePackageComponentsMap(configClasses);

        configClasses.forEach(configClass -> {
            createBeanDefinitionsFromComponentScanAnnotation(configClass, registry);
            createBeanDefinitionsFromBeanAnnotations();
        });
    }

    private void populatePackageComponentsMap(List<Class<?>> configClasses) {
        configClasses.forEach(configClass -> {
            var packagesFromComponentScan = configClass.getAnnotation(ComponentScan.class).value();

            Arrays.stream(packagesFromComponentScan)
                    .forEach(packageFromComponentScan -> {
                        var reflection = new Reflections(packageFromComponentScan);
                        var classesComponents = reflection.getTypesAnnotatedWith(Component.class);
                        packageComponentsMap.computeIfAbsent(packageFromComponentScan, packageName -> classesComponents);
                    });
        });
    }

    private void validateConfigClasses(List<Class<?>> configClasses) {
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
        var packagesFromComponentScan = configClass.getAnnotation(ComponentScan.class).value();
        Set<Class<?>> componentsForCurrentConfigClass = new HashSet<>();

        Arrays.stream(packagesFromComponentScan)
                .forEach(packageFromComponentScan ->
                        componentsForCurrentConfigClass.addAll(
                                packageComponentsMap.get(packageFromComponentScan)));

        componentsForCurrentConfigClass
                .forEach(type -> {
                    rejectInterfaceAnnotatedWithComponent(type);
                    registerBeanDefinition(registry, type, componentsForCurrentConfigClass);
                });
    }

    private void createBeanDefinitionsFromBeanAnnotations() {
        //TODO
    }

    private void rejectInterfaceAnnotatedWithComponent(Class<?> type) {
        if (type.isInterface()) {
            throw new InterfaceAnnotationException(String.format(
                    "%s can not be annotated as Component", type));
        }
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> type,
                                        Set<Class<?>> components) {
        if (!registry.containsBeanDefinition(type.getName())) {
            BeanDefinition beanDefinition = createBeanDefinition(type, components);
            registry.registerBeanDefinition(type.getName(), beanDefinition);
        }
    }

    private BeanDefinition createBeanDefinition(Class<?> type, Set<Class<?>> components) {
        var dependsOnFields = scanAutowiredFields(type, components);
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

        return beanDefinition;
    }

    // find Autowired fields, if they are Interface type -> define implementation
    private List<String> scanAutowiredFields(Class<?> targetClass,
                                             Set<Class<?>> components) {
        return Arrays.stream(targetClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .map(field -> InterfaceAnalyzer.getImplementation(targetClass, field, components))
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
