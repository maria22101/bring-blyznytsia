package com.blyznytsia.bring.context.services;

import com.blyznytsia.bring.context.BeanDefinitionRegistry;

import java.util.List;
import java.util.Map;

public interface BeanConfigurator {
    Object configure(Object objectToConfigure, Map<String, Object> beanMap);
}
