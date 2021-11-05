package com.blyznytsia.bring.context.services;

import com.blyznytsia.bring.context.BeanDefinitionRegistry;

import java.util.List;

public interface BeanDefinitionProcessor {
    void process(List<String> packages, BeanDefinitionRegistry registry);
}
