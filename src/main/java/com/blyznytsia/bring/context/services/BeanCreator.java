package com.blyznytsia.bring.context.services;

import java.util.Map;

public interface BeanCreator {

    /**
     * Creates an {@link Object} and place it in parameter {@link Map}
     *
     * @param className     class name
     * @param beanMap       objects' storage
     * @return              created object
     */
    Object create(String className, Map<String, Object> beanMap);
}
