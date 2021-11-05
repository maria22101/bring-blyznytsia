package com.blyznytsia.bring.context.services.impl;

import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.services.BeanFactory;

import java.util.Map;

public class BeanFactoryImpl implements BeanFactory {
    private Map<String, BeanDefinition> beanDefinitionMap;

    public BeanFactoryImpl(Map<String, BeanDefinition> beanDefinitionMap) {
        this.beanDefinitionMap = beanDefinitionMap;
    }

    public Object create(String className, Map<String, Object> beanMap){
        //TODO

//        beanMap.put();
        return null;
    }
}
