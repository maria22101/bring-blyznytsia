package com.blyznytsia.bring.context.services;

import java.util.Map;

public interface BeanCreator {

    Object create(String className, Map<String, Object> beanMap);
}
