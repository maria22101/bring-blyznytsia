package com.blyznytsia.bring.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class1;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class2;
import com.blyznytsia.bring.context.exceptions.NoSuchBeanException;

class ApplicationContextTest {

    ApplicationContext context = new ApplicationContext();

    @Test
    public void init() {
        var scanner = mock(Scanner.class);
        var factory = mock(BeanFactory.class);
        var beanDefinitionRegistry = new BeanDefinitionRegistry();
        var beanMap = new HashMap<String, Object>();

        context.setScanner(scanner);
        context.setFactory(factory);
        context.setBeanDefinitionRegistry(beanDefinitionRegistry);
        context.setBeanMap(beanMap);

        context.init();

        InOrder inOrder = Mockito.inOrder(scanner, factory);
        inOrder.verify(scanner).scanAndFillBeanDefinitionRegistry(beanDefinitionRegistry);
        inOrder.verify(factory).traverseBeanDefinitionRegistryAndFillBeanMap(beanDefinitionRegistry, beanMap);
    }

    @Test
    public void getBean() {
        var beanMap = new HashMap<String, Object>();
        var class1Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class1";
        var class1Instance = new Class1();
        beanMap.put(class1Name, class1Instance);
        context.setBeanMap(beanMap);

        var bean1 = context.getBean(Class1.class);

        assertThat(bean1, is(class1Instance));
    }

    @Test
    public void getBeanWhenBeanNotFound() {
        assertThrows(NoSuchBeanException.class, () -> context.getBean(Class2.class));
    }
}
