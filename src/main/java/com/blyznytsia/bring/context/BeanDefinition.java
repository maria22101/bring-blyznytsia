package com.blyznytsia.bring.context;

import com.blyznytsia.bring.context.services.BeanConfigurator;

import java.util.List;

import lombok.Data;

@Data
public class BeanDefinition {
    private String className;
    private String scope;
    private List<BeanConfigurator> beanConfigurators; // BeanConfigurator responsible for different object creation modes: fields autowiring, different constructors, multiple impl of same interface
    private List<String> dependsOnFields;
}
