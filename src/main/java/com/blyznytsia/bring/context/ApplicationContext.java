package com.blyznytsia.bring.context;

import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.context.services.impl.AnnotationBeanDefinitionProcessor;
import com.blyznytsia.bring.context.services.impl.BeanFactoryImpl;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationContext {

    private BeanDefinitionRegistry beanDefinitionRegistry = new BeanDefinitionRegistry();
    private Map<String, Object> beanMap = new HashMap<>();

    public ApplicationContext() {
        init();
    }

    @SneakyThrows
    private void init() {
        var packages = Arrays.stream(Package.getPackages()).filter(p -> {
                    var reflections = new Reflections(p.getName());
                    return !reflections.getTypesAnnotatedWith(Configuration.class).isEmpty();
                })
                .map(Package::getName).collect(Collectors.toList());

        var annotationBeanProcessor = new AnnotationBeanDefinitionProcessor();
        annotationBeanProcessor.process(packages, beanDefinitionRegistry);

        var beanFactory = new BeanFactoryImpl(beanDefinitionRegistry.getBeanDefinitionMap());
    }

    @SneakyThrows
    private void registerBean(Class<?> type) {
        var beanAnnotation = type.getAnnotation(Component.class);
        var beanId = beanAnnotation.value();
        var constructor = type.getConstructor();
        var beanInstance = constructor.newInstance();
        beanMap.put(beanId, beanInstance);
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

    public void processBeanDefinition() {
        //TODO: calling bean factory create for each bean definition item if need
    }
}
