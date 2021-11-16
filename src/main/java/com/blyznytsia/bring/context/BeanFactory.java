package com.blyznytsia.bring.context;

import java.util.Map;

import com.blyznytsia.bring.context.exceptions.BeanCreationException;

import lombok.SneakyThrows;

public class BeanFactory {

    @SneakyThrows
    public void createBean(BeanDefinition beanDefinition,
                           BeanDefinitionRegistry beanDefinitionRegistry,
                           Map<String, Object> beanMap) {

        // recursion
        beanDefinition.getDependsOnFields().forEach(dependsOnField -> {
            if (!beanMap.containsKey(dependsOnField)) {
                var childBeanDefinition = beanDefinitionRegistry.getBeanDefinition(dependsOnField);
                if (childBeanDefinition != null) {
                    createBean(childBeanDefinition, beanDefinitionRegistry, beanMap);
                } else {
                    throw new BeanCreationException("Can`t find bean definition for type " + dependsOnField);
                }
            }
        });

        var targetClassName = beanDefinition.getClassName();

        beanDefinition.getBeanCreator().create(targetClassName, beanMap);

        // set up the bean using configurator(s):
        beanMap.computeIfPresent(targetClassName, (className, existingBean) -> {
            // TODO: if interface -> 1/ get implementation or 2/choose implementation according to @Qualifier/@Primary

            beanDefinition.getBeanConfigurators().forEach(configurator -> configurator.configure(existingBean, beanMap));
            return existingBean;
        });
    }
}
