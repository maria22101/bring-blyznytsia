package com.blyznytsia.bring.context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.context.services.impl.AutowiredConstructorBeanCreator;
import com.blyznytsia.bring.context.services.impl.EmptyConstructorBeanCreator;

import lombok.SneakyThrows;

public class ApplicationContext extends BeanFactory {

    private BeanDefinitionRegistry beanDefinitionRegistry = new BeanDefinitionRegistry();
    private Map<String, Object> beanMap = new HashMap<>();

    public ApplicationContext() {
        init();
    }

    // Scanner
    @SneakyThrows
    private void init() {
        // scan packages annotated with @Configuration into the list
        var packagesWithConfiguration = Arrays.stream(Package.getPackages()).filter(p -> {
                    var reflections = new Reflections(p.getName());
                    return !reflections.getTypesAnnotatedWith(Configuration.class).isEmpty();
                })
                .map(Package::getName).collect(Collectors.toList());

        var scanner = new Scanner();
        scanner.scan(packagesWithConfiguration, beanDefinitionRegistry);

        traverseBeanDefinitionRegistryAndFillBeanMap();
    }

    private void traverseBeanDefinitionRegistryAndFillBeanMap() {
        beanDefinitionRegistry.getBeanDefinitionMap().values()
                .stream()
                .filter(beanDefinition -> beanDefinition.getBeanCreator() instanceof EmptyConstructorBeanCreator)
                .forEach(beanDefinition ->
                        createBean(beanDefinition, beanDefinitionRegistry, beanMap));

        beanDefinitionRegistry.getBeanDefinitionMap().values()
                .stream()
                .filter(beanDefinition -> beanDefinition.getBeanCreator() instanceof AutowiredConstructorBeanCreator)
                .forEach(beanDefinition ->
                        createBean(beanDefinition, beanDefinitionRegistry, beanMap));
    }

    public <T> T getBean(Class<T> type) {
        return beanMap
                .values()
                .stream()
                .filter(type::isInstance)
                .findAny()
                .map(type::cast)
                .orElseThrow();
    }
}
