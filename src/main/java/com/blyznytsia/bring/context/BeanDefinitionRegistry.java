package com.blyznytsia.bring.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.blyznytsia.bring.context.exceptions.BeanDefinitionNotFoundException;

/**
 * {@link BeanDefinitionRegistry} class is a container that holds a class name and its {@link BeanDefinition}
 */
public class BeanDefinitionRegistry {
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }

    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

    public BeanDefinition getBeanDefinition(String beanName) {
        BeanDefinition bd = this.beanDefinitionMap.get(beanName);
        if (bd == null) {
            throw new BeanDefinitionNotFoundException(String.format("BeanDefinition for %s not found", beanName));
        }
        return bd;
    }

    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }
}
