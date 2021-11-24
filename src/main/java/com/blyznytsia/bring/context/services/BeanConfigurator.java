package com.blyznytsia.bring.context.services;

import java.util.Map;

import com.blyznytsia.bring.context.BeanDefinition;

public interface BeanConfigurator {
    Object configure(Object objectToConfigure, BeanDefinition beanDefinition, Map<String, Object> beanMap);
}
