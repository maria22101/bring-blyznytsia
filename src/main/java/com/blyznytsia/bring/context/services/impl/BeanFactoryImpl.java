package com.blyznytsia.bring.context.services.impl;

import com.blyznytsia.bring.context.ApplicationContext;
import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.BeanDefinitionRegistry;
import com.blyznytsia.bring.context.services.BeanFactory;
import com.blyznytsia.bring.context.util.ObjectSetUpper;

import java.util.List;
import java.util.Map;

import lombok.Setter;

public class BeanFactoryImpl implements BeanFactory {
    private ApplicationContext context;
    @Setter
    BeanDefinitionRegistry beanDefinitionRegistry;

//    public BeanFactoryImpl(Map<String, BeanDefinition> beanDefinitionMap) {
//        this.beanDefinitionMap = beanDefinitionMap;
//    }


    public BeanFactoryImpl(ApplicationContext context) {

    }

    public Object create(String className, Map<String, Object> beanMap){
        //TODO

//        beanMap.put();
        return null;
    }
}
