package com.blyznytsia.bring.context;

import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.context.services.impl.AnnotationBeanDefinitionProcessor;
import com.blyznytsia.bring.context.services.impl.BeanFactoryImpl;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationContext extends BeanFactoryImpl {

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

        var annotationBeanProcessor = new AnnotationBeanDefinitionProcessor();
        annotationBeanProcessor.process(packagesWithConfiguration, beanDefinitionRegistry);

        traverseBeanDefinitionRegistryAndFillBeanMap();
    }

    private void traverseBeanDefinitionRegistryAndFillBeanMap() {
        beanDefinitionRegistry.getBeanDefinitionMap().values()
                .forEach(beanDefinition -> createBean(beanDefinition, beanDefinitionRegistry, beanMap));
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
