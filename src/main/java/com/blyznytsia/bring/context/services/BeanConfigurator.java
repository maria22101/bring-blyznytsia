package com.blyznytsia.bring.context.services;

import java.util.Map;

import com.blyznytsia.bring.context.BeanDefinition;

public interface BeanConfigurator {
    /**
     * Configures {@link Object} using information from {@link BeanDefinition} and {@link Map}
     *
     * @param objectToConfigure  object to be configured
     * @param beanDefinition     class' metadata
     * @param beanMap            objects' storage
     */
    void configure(Object objectToConfigure, BeanDefinition beanDefinition, Map<String, Object> beanMap);
}
