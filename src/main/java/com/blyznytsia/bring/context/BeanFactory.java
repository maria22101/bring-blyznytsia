package com.blyznytsia.bring.context;

import java.util.Map;

import com.blyznytsia.bring.context.exceptions.BeanCreationException;

import lombok.SneakyThrows;

public class BeanFactory {

    @SneakyThrows
    public Object createBean(BeanDefinition beanDefinition,
                           BeanDefinitionRegistry beanDefinitionRegistry,
                           Map<String, Object> beanMap) {

        // recursion
        beanDefinition.getDependsOnFields().forEach(typeName -> {
            if (!beanMap.containsKey(typeName)) {
                var childBeanDefinition = beanDefinitionRegistry.getBeanDefinition(typeName);
                if (childBeanDefinition != null) {
                    createBean(childBeanDefinition, beanDefinitionRegistry, beanMap);
                } else {
                    throw new BeanCreationException("Can`t find bean definition for type " + typeName);
                }
            }
        });

        // get initial bean created using empty constructor:
        var beanDefinitionInitialBean = getInitialBean(beanDefinition.getClassName());
        beanMap.put(beanDefinition.getClassName(), beanDefinitionInitialBean);

        // set up the bean depending on what is to configure:
        beanDefinition.getBeanConfigurators().forEach(configurator -> {
            // if interface -> 1/ get implementation or 2/choose implementation according to @Qualifier/@Primary
            Object o = configurator.configure(beanDefinitionInitialBean, beanMap);
            beanMap.put(beanDefinition.getClassName(), o);
        });

        return null;
    }

    @SneakyThrows
    private Object getInitialBean(String typeName) {
        var beanClass = Class.forName(typeName);
        var constructor = beanClass.getDeclaredConstructor();
        if (constructor == null) {
            throw new BeanCreationException("Unable to find empty constructor");
        }
        return constructor.newInstance();
    }
}
