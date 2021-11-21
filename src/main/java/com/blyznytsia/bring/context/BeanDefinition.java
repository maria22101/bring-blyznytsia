package com.blyznytsia.bring.context;

import java.util.List;

import com.blyznytsia.bring.context.constants.BeanStatus;
import com.blyznytsia.bring.context.services.BeanConfigurator;
import com.blyznytsia.bring.context.services.BeanCreator;

import lombok.Data;

@Data
public class BeanDefinition {
    private String className;
    private String scope;
    private List<BeanConfigurator> beanConfigurators; // responsible for configuring already created bean: fields autowiring, fields autowiring via setter ...
    private BeanCreator beanCreator; //responsible for creation modes
    private List<String> dependsOnFields;
    private BeanStatus status = BeanStatus.INITIALIZING;
}
