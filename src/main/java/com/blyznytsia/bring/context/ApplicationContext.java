package com.blyznytsia.bring.context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.context.exceptions.ConfigurationNotFoundException;

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

        if (packagesWithConfiguration.isEmpty()) {
            throw new ConfigurationNotFoundException(
                    "No package with class annotated with @Configuration found. Context creation not possible");
        }

        var scanner = new Scanner();
        scanner.scan(packagesWithConfiguration, beanDefinitionRegistry);

        traverseBeanDefinitionRegistryAndFillBeanMap(beanDefinitionRegistry, beanMap);
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
