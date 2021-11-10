package com.blyznytsia.bring.context.services.impl;

import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.BeanDefinitionRegistry;
import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.constants.CreationMode;
import com.blyznytsia.bring.context.services.BeanFactory;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class BeanFactoryImpl implements BeanFactory {

    @SneakyThrows
    public Object createBean(BeanDefinition beanDefinition, BeanDefinitionRegistry beanDefinitionRegistry, Map<String, Object> beanMap) {
        beanDefinition.getDependsOnFields().forEach(typeName -> {
            if (!beanMap.containsKey(typeName)) {
                var childBeanDefinition = beanDefinitionRegistry.getBeanDefinition(typeName);
                if (childBeanDefinition != null) {
                    createBean(childBeanDefinition, beanDefinitionRegistry, beanMap);
                } else {
//                    throw new NotFoundException("Can`t found bean definition for type " + typeName);
                }
            }
        });

        beanDefinition.getDependsOnSetters().forEach(typeName -> {
            if (!beanMap.containsKey(typeName)) {
                var childBeanDefinition = beanDefinitionRegistry.getBeanDefinition(typeName);
                if (childBeanDefinition != null) {
                    createBean(childBeanDefinition, beanDefinitionRegistry, beanMap);
                } else {
//                    throw new NotFoundException("Can`t found bean definition for type " + typeName);
                }
            }
        });

        if (beanDefinition.getCreationModes().contains(CreationMode.FIELD)) {
            var beanClass = Class.forName(beanDefinition.getClassName());
            var constructor = beanClass.getDeclaredConstructor();
            if (constructor == null) {
                throw new Exception("Unable to find constructor");
            }

            var instance = constructor.newInstance();
            Arrays.stream(instance.getClass().getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(Autowired.class)).forEach(type -> {
                        type.setAccessible(true);
                        try {
                            type.set(instance, beanMap.get(type.getType().getTypeName()));
                        } catch (IllegalAccessException e) {
//                            throw new Exception("Unable to set @Autowired field");
                        }
                    });
            beanMap.put(beanDefinition.getClassName(), instance);
        }

        if (beanDefinition.getCreationModes().contains(CreationMode.SETTER)) {
            var beanClass = Class.forName(beanDefinition.getClassName());
            var constructor = beanClass.getDeclaredConstructor();
            if (constructor == null) {
                throw new Exception("Unable to find constructor");
            }

            var instance = constructor.newInstance();
//            Arrays.stream(instance.getClass().getDeclaredMethods())
//                    .filter(f -> f.isAnnotationPresent(Autowired.class))
//                    .map(method -> Arrays.stream(method.getParameterTypes()).findFirst())
//                    .flatMap(Optional::stream)
//                    .forEach(type -> {
//
//                        type.setAccessible(true);
//                        try {
//                            type.set(instance, beanMap.get(type.getType().getTypeName()));
//                        } catch (IllegalAccessException e) {
////                            throw new Exception("Unable to set @Autowired field");
//                        }
//                    });
            beanMap.put(beanDefinition.getClassName(), instance);
        }

        if (beanDefinition.getCreationModes().contains(CreationMode.EMPTY_CONSTRUCTOR)) {
            var beanClass = Class.forName(beanDefinition.getClassName());
            var constructor = beanClass.getDeclaredConstructor();
            if (constructor == null) {
                throw new Exception("Unable to find constructor");
            }

            var instance = constructor.newInstance();
            beanMap.put(beanDefinition.getClassName(), instance);
        }

        return null;
    }
}
