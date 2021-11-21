package com.blyznytsia.bring.context;

import java.util.Map;

import com.blyznytsia.bring.context.constants.BeanStatus;
import com.blyznytsia.bring.context.exceptions.CircularDependencyException;

import lombok.SneakyThrows;

public class BeanFactory {

    @SneakyThrows
    public void traverseBeanDefinitionRegistryAndFillBeanMap(BeanDefinitionRegistry beanDefinitionRegistry,
                                                             Map<String, Object> beanMap) {

        createBeansWithoutDependsOnFields(beanDefinitionRegistry, beanMap);
        createBeansWithDependsOnFields(beanDefinitionRegistry, beanMap);
        notifyAboutCircularDependency(beanDefinitionRegistry, beanMap);
    }

    private void createBeansWithoutDependsOnFields(BeanDefinitionRegistry beanDefinitionRegistry,
                                                   Map<String, Object> beanMap) {
        beanDefinitionRegistry.getBeanDefinitionMap().values().stream()
                .filter(this::isBeanWithoutDependsOnFields)
                .forEach(beanDefinition -> createBeanWithoutDependsOnFields(beanDefinition, beanMap));
    }

    private void createBeansWithDependsOnFields(BeanDefinitionRegistry beanDefinitionRegistry,
                                                Map<String, Object> beanMap) {
        while (beansCreationNotCompleted(beanDefinitionRegistry) &&
                atLeastOneBeanCreationIsPossible(beanDefinitionRegistry, beanMap)) {

            beanDefinitionRegistry.getBeanDefinitionMap().values().stream()
                    .filter(beanDefinition -> isBeanCreationRequiredAndPossible(beanDefinition, beanMap))
                    .forEach(beanDefinition -> createAndConfigureBean(beanDefinition, beanMap));
        }
    }

    private boolean isBeanWithoutDependsOnFields(BeanDefinition beanDefinition) {
        return beanDefinition.getDependsOnFields().isEmpty();
    }

    private void createBeanWithoutDependsOnFields(BeanDefinition beanDefinition,
                                                  Map<String, Object> beanMap) {
        beanDefinition.getBeanCreator().create(beanDefinition.getClassName(), beanMap);
        beanDefinition.setStatus(BeanStatus.CREATED);
    }

    private boolean beansCreationNotCompleted(BeanDefinitionRegistry beanDefinitionRegistry) {
        return beanDefinitionRegistry.getBeanDefinitionMap().values().stream()
                .anyMatch(beanDefinition -> beanDefinition.getStatus().equals(BeanStatus.INITIALIZING));
    }

    private boolean atLeastOneBeanCreationIsPossible(BeanDefinitionRegistry beanDefinitionRegistry,
                                                     Map<String, Object> beanMap) {
        return beanDefinitionRegistry.getBeanDefinitionMap().values().stream()
                .anyMatch(beanDefinition -> isBeanCreationRequiredAndPossible(beanDefinition, beanMap));
    }

    private boolean isBeanCreationRequiredAndPossible(BeanDefinition beanDefinition,
                                                      Map<String, Object> beanMap) {
        return beanDefinition.getStatus().equals(BeanStatus.INITIALIZING) &&
                beansForDependsOnFieldsCreated(beanDefinition, beanMap);
    }

    private boolean beansForDependsOnFieldsCreated(BeanDefinition beanDefinition,
                                                   Map<String, Object> beanMap) {
        return beanDefinition.getDependsOnFields().stream()
                .allMatch(beanMap::containsKey);
    }

    private void createAndConfigureBean(BeanDefinition beanDefinition,
                                        Map<String, Object> beanMap) {
        Object initialBean = beanDefinition.getBeanCreator().create(beanDefinition.getClassName(), beanMap);
        beanDefinition.getBeanConfigurators().forEach(
                configurator -> configurator.configure(initialBean, beanMap));
        beanDefinition.setStatus(BeanStatus.CREATED);
    }

    private void notifyAboutCircularDependency(BeanDefinitionRegistry beanDefinitionRegistry,
                                               Map<String, Object> beanMap) {
        if (beansCreationNotCompleted(beanDefinitionRegistry)) {

            beanDefinitionRegistry.getBeanDefinitionMap().values().stream()
                    .filter(beanDefinition ->
                            isBeanDefinitionCircularDependent(beanDefinition, beanDefinitionRegistry, beanMap))
                    .findFirst()
                    .ifPresent(beanDefinition -> {
                        beanDefinition.getDependsOnFields().stream()
                                .filter(dependsOnField -> !beanMap.containsKey(dependsOnField) &&
                                        isBeanDefinitionFieldOfDependsOn(
                                                dependsOnField, beanDefinition, beanDefinitionRegistry))
                                .findFirst()
                                .ifPresent(dependOnField -> {
                                    throw new CircularDependencyException(String.format(
                                            "Context creation error: circular dependency when creating bean %s on bean %s",
                                            beanDefinition.getClassName(), dependOnField));
                                });
                    });
        }
    }

    private boolean isBeanDefinitionCircularDependent(BeanDefinition beanDefinition,
                                                      BeanDefinitionRegistry beanDefinitionRegistry,
                                                      Map<String, Object> beanMap) {
        boolean notCreated = beanDefinition.getStatus().equals(BeanStatus.INITIALIZING);
        boolean circularDependent = beanDefinition.getDependsOnFields().stream()
                .anyMatch(dependsOnField ->
                        !beanMap.containsKey(dependsOnField) &&
                        isBeanDefinitionFieldOfDependsOn(dependsOnField, beanDefinition, beanDefinitionRegistry));
        return notCreated && circularDependent;
    }

    private boolean isBeanDefinitionFieldOfDependsOn(String dependsOnField,
                                                     BeanDefinition beanDefinition,
                                                     BeanDefinitionRegistry beanDefinitionRegistry) {
        return beanDefinitionRegistry.getBeanDefinition(dependsOnField).getDependsOnFields()
                .contains(beanDefinition.getClassName());
    }
}
