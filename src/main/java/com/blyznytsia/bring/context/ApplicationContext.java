package com.blyznytsia.bring.context;

import java.util.HashMap;
import java.util.Map;

import com.blyznytsia.bring.context.exceptions.NoSuchBeanException;

import lombok.Setter;

/**
 * {@link ApplicationContext} is a class that triggers population of objects' storage {@link Map}, holds it
 *  and has functionality to retrieve objects from it.
 */
@Setter
public class ApplicationContext {

    private Scanner scanner = new Scanner();
    private BeanFactory factory = new BeanFactory();
    private BeanDefinitionRegistry beanDefinitionRegistry = new BeanDefinitionRegistry();
    private Map<String, Object> beanMap = new HashMap<>();

    public void init() {
        scanner.scanAndFillBeanDefinitionRegistry(beanDefinitionRegistry);
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(beanDefinitionRegistry, beanMap);
    }

    public <T> T getBean(Class<T> type) {
        return beanMap
                .values()
                .stream()
                .filter(type::isInstance)
                .findAny()
                .map(type::cast)
                .orElseThrow(() ->
                        new NoSuchBeanException(String.format(
                                "No Bean of type %s created", type.getName())));
    }
}

