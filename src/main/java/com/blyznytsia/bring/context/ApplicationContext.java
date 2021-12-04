package com.blyznytsia.bring.context;

import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;

public class ApplicationContext extends BeanFactory {

    private BeanDefinitionRegistry beanDefinitionRegistry = new BeanDefinitionRegistry();
    private Map<String, Object> beanMap = new HashMap<>();

    public ApplicationContext() {
        init();
    }

    @SneakyThrows
    private void init() {
        var scanner = new Scanner();
        scanner.scan(beanDefinitionRegistry);

        traverseBeanDefinitionRegistryAndFillBeanMap(beanDefinitionRegistry, beanMap);
    }

    public <T> T getBean(Class<T> type) {
        return beanMap
                .values()
                .stream()
                .filter(type::isInstance)
                .findAny()
                .map(type::cast)
                .orElseThrow();
    }
}
