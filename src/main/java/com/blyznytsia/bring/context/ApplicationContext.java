package com.blyznytsia.bring.context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Configuration;
import com.blyznytsia.bring.context.services.impl.AnnotationBeanDefinitionProcessor;
import com.blyznytsia.bring.context.services.impl.BeanFactoryImpl;

import lombok.Setter;
import lombok.SneakyThrows;

public class ApplicationContext {

    @Setter
    private BeanFactoryImpl beanFactory;
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

        // create BeanDefinitions and store them
        var annotationBeanProcessor = new AnnotationBeanDefinitionProcessor();
        annotationBeanProcessor.process(packagesWithConfiguration, beanDefinitionRegistry);

        beanFactory.setBeanDefinitionRegistry(beanDefinitionRegistry);

        traverseBeanDefinitionRegistryAndFillBeanMap();
    }

    //TODO
    private void traverseBeanDefinitionRegistryAndFillBeanMap() {
//        beanDefinitionRegistry.getBeanDefinitionMap().entrySet()
//                .forEach(beanAndDefinition -> {
//                    Object obj = beanFactory.create(beanAndDefinition.getKey());
//                    beanMap.put(obj.getClass().getTypeName(), obj);
//                });
    }


    //    @SneakyThrows
//    // Factory will do it
//    private void registerBean(Class<?> type) {
//        var beanAnnotation = type.getAnnotation(Component.class);
//        var beanId = beanAnnotation.value();
//        var constructor = type.getConstructor();
//        var beanInstance = constructor.newInstance();
//        beanMap.put(beanId, beanInstance);
//    }

//     Factory will call the beanMap and check if there is a bean
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
