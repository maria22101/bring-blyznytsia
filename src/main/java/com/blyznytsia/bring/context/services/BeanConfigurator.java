package com.blyznytsia.bring.context.services;

import java.util.Map;

public interface BeanConfigurator {
    Object configure(Object objectToConfigure, Map<String, Object> beanMap);
}
