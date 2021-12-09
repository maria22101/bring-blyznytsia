package com.blyznytsia.bring.context;

import java.util.List;

import com.blyznytsia.bring.context.constants.BeanStatus;
import com.blyznytsia.bring.context.services.BeanConfigurator;
import com.blyznytsia.bring.context.services.BeanCreator;

import lombok.Data;

/**
 * {@link BeanDefinition} holds a class metadata.
 */
@Data
public class BeanDefinition {
    private String className;
    private BeanCreator beanCreator;
    private List<BeanConfigurator> beanConfigurators;
    private List<String> dependsOnFields;
    private BeanStatus status = BeanStatus.INITIALIZING;
}
