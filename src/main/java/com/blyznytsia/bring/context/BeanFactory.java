package com.blyznytsia.bring.context;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blyznytsia.bring.context.constants.BeanStatus;
import com.blyznytsia.bring.context.exceptions.CircularDependencyException;

/**
 * {@link BeanFactory} class traverses through {@link BeanDefinitionRegistry},
 * creates objects and populates the object's storage
 */
public class BeanFactory {

    /**
     * Method populates the objects' storage {@link Map}
     *
     * @param beanDefinitionRegistry    {@link BeanDefinition} storage
     * @param beanMap                   objects' storage
     */
    public void traverseBeanDefinitionRegistryAndFillBeanMap(BeanDefinitionRegistry beanDefinitionRegistry,
                                                             Map<String, Object> beanMap) {

        createBeansWithoutDependsOnFields(beanDefinitionRegistry, beanMap);
        createBeansWithDependsOnFields(beanDefinitionRegistry, beanMap);
        notifyAboutCircularDependency(beanDefinitionRegistry, beanMap);
    }

    /**
     * Method creates objects for classes with no dependencies
     * and places them into the objects' storage
     *
     * @param beanDefinitionRegistry    {@link BeanDefinition} storage
     * @param beanMap                   objects' storage
     */
    private void createBeansWithoutDependsOnFields(BeanDefinitionRegistry beanDefinitionRegistry,
                                                   Map<String, Object> beanMap) {
        List<BeanDefinition> beanDefinitionsForCreatingBeans = beanDefinitionRegistry.getBeanDefinitionMap().values()
                .stream()
                .filter(this::isBeanWithoutDependsOnFields)
                .collect(Collectors.toList());

        beanDefinitionsForCreatingBeans
                .forEach(beanDefinition -> {
                    beanDefinition.getBeanCreator().create(beanDefinition.getClassName(), beanMap);
                    beanDefinition.setStatus(BeanStatus.CREATED);
                });
    }

    /**
     * Method creates objects for classes that have dependencies
     * and places them into the objects' storage
     *
     * @param beanDefinitionRegistry    {@link BeanDefinition} storage
     * @param beanMap                   objects' storage
     */
    private void createBeansWithDependsOnFields(BeanDefinitionRegistry beanDefinitionRegistry,
                                                Map<String, Object> beanMap) {
        while (beanMapNotReady(beanDefinitionRegistry) &&
                atLeastOneBeanCreationIsPossible(beanDefinitionRegistry, beanMap)) {

            List<BeanDefinition> beanDefinitionsForCreatingBeans =
                    beanDefinitionRegistry.getBeanDefinitionMap().values().stream()
                    .filter(beanDefinition -> isBeanCreationRequiredAndPossible(beanDefinition, beanMap))
                            .collect(Collectors.toList());

            beanDefinitionsForCreatingBeans
                    .forEach(beanDefinition -> {
                        Object initialBean = beanDefinition.getBeanCreator().create(beanDefinition.getClassName(), beanMap);
                        beanDefinition.getBeanConfigurators().forEach(
                                configurator -> configurator.configure(initialBean, beanDefinition, beanMap));
                        beanDefinition.setStatus(BeanStatus.CREATED);
                    });
        }
    }

    private boolean isBeanWithoutDependsOnFields(BeanDefinition beanDefinition) {
        return beanDefinition.getDependsOnFields().isEmpty();
    }

    private boolean beanMapNotReady(BeanDefinitionRegistry beanDefinitionRegistry) {
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

    /**
     * Method iterates over {@link BeanDefinition} that do not have objects of their related classes created
     * and finds those whose dependencies contain the {@link BeanDefinition} the method is currently iterating over.
     * {@link CircularDependencyException} is thrown in case of such finding.
     *
     * @param beanDefinitionRegistry    {@link BeanDefinition} storage
     * @param beanMap                   objects' storage
     */
    private void notifyAboutCircularDependency(BeanDefinitionRegistry beanDefinitionRegistry,
                                               Map<String, Object> beanMap) {
        if (beanMapNotReady(beanDefinitionRegistry)) {

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

    // TODO: implement extended circular dependency notifier
    // case when A -> B, B -> C ... X -> A
    private void notifyAboutCircularDependencyExtended(BeanDefinition start,
                                                       BeanDefinition current) {

        //traverseDependsOnsAndGetTracePath
    }
}
