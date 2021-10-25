package com.blyznytsia.bring.context;

import com.blyznytsia.bring.context.annotation.Bean;
import com.blyznytsia.bring.context.annotation.ComponentScan;
import com.blyznytsia.bring.context.annotation.Configuration;
import lombok.SneakyThrows;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationContext {
    private Map<String, Object> beanMap = new HashMap<>();

    public ApplicationContext() {
        init();
    }

    @SneakyThrows
    private void init() {
        //scan current package
        //find all classes that require bean creation
        //create object for each class
        //add object to the bean map
        var packages = Arrays.stream(Package.getPackages()).filter(p -> {
                    var reflections = new Reflections(p.getName());
                    return !reflections.getTypesAnnotatedWith(Configuration.class).isEmpty();
                })
                .map(Package::getName).collect(Collectors.toList());

        var packageWithConfig = packages.stream().findFirst();
        if (packageWithConfig.isPresent()) {
            var reflections = new Reflections(packageWithConfig.get());
            var packageWithComponentScan= reflections.getTypesAnnotatedWith(ComponentScan.class).stream().findFirst();
            if(packageWithComponentScan.isPresent()){
                var typeComponentScan = packageWithComponentScan.get();
                var beanAnnotation = typeComponentScan.getAnnotation(ComponentScan.class);
                var packageToScan = beanAnnotation.value();

                var scanPackageReflections = new Reflections(packageToScan);
                scanPackageReflections.getTypesAnnotatedWith(Bean.class)
                        .forEach(this::registerBean);

            }
        }
    }

    @SneakyThrows
    private void registerBean(Class<?> type) {
        var beanAnnotation = type.getAnnotation(Bean.class);
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
                .findAny().
                map(type::cast)
                .orElseThrow();
    }
}
