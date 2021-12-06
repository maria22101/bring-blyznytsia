package com.blyznytsia.bring.context;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class1;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class2;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class3;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class4;
import com.blyznytsia.bring.context.services.impl.AutowiredConstructorBeanCreator;
import com.blyznytsia.bring.context.services.impl.AutowiredFieldBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredSetterBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.EmptyConstructorBeanCreator;
import com.blyznytsia.bring.context.util.BeanDefinitionGenerator;

class BeanFactoryTest {

    BeanFactory factory;
    BeanDefinitionRegistry registry;
    Map<String, Object> beanMap;

    @BeforeEach
    void setUp() {
        factory = new BeanFactory();
        registry = getBeanDefinitionRegistry();
        beanMap = new HashMap<>();
    }

    @Test
    @DisplayName("Number of Beans created is equal to the number of BeanDefinitions")
    void beanFactorySize() {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        assertThat(beanMap.values(), hasSize(registry.getBeanDefinitionMap().values().size()));
    }

    @Test
    @DisplayName("Bean without dependent fields")
    void noFieldsBean() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var class1Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class1";
        var createdBean = beanMap.get(class1Name);

        assertThat(createdBean, instanceOf(Class.forName(class1Name)));
        assertThat(createdBean.getClass().getName(), is(class1Name));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with one autowired field")
    void oneAutowiredField() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var class2Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class2";
        var createdBean = beanMap.get(class2Name);

        assertThat(createdBean, instanceOf(Class.forName(class2Name)));
        assertThat(createdBean.getClass().getName(), is(class2Name));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with one field autowired via setter")
    void oneFieldAutowiredViaSetter() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var class3Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class3";
        var createdBean = beanMap.get(class3Name);

        assertThat(createdBean, instanceOf(Class.forName(class3Name)));
        assertThat(createdBean.getClass().getName(), is(class3Name));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with fields some of which autowired via constructor")
    void someFieldsAutowiredViaConstructor() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var class4Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class4";
        var createdBean = beanMap.get(class4Name);

        assertThat(createdBean, instanceOf(Class.forName(class4Name)));
        assertThat(createdBean.getClass().getName(), is(class4Name));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with all fields autowired via constructor")
    void allFieldsAutowiredViaConstructor() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var class5Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class5";
        var createdBean = beanMap.get(class5Name);

        assertThat(createdBean, instanceOf(Class.forName(class5Name)));
        assertThat(createdBean.getClass().getName(), is(class5Name));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    private void validateConstructor(Object createdBean) {
        var beanCreator = registry.getBeanDefinitionMap().get(createdBean.getClass().getName()).getBeanCreator();

        if (beanCreator instanceof EmptyConstructorBeanCreator) {
            var emptyConstructor = createdBean.getClass().getConstructors()[0];
            assertThat(emptyConstructor, notNullValue());
        } else {
            var autowiredConstructor = Arrays.stream(createdBean.getClass().getConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                    .findFirst()
                    .orElseThrow();
            assertThat(autowiredConstructor, notNullValue());
        }
    }

    private void validateFields(Object createdBean) {
        var dependsOn = registry.getBeanDefinitionMap().get(createdBean.getClass().getName()).getDependsOnFields();
        var configurators = registry.getBeanDefinitionMap().get(createdBean.getClass().getName()).getBeanConfigurators();
        var beanCreator = registry.getBeanDefinitionMap().get(createdBean.getClass().getName()).getBeanCreator();

        var autowiredFields = Arrays.stream(createdBean.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Autowired.class))
                .map(field -> field.getType().getName())
                .collect(toList());
        var fieldsAutowiredViaSetter = BeanDefinitionGenerator.scanAutowiredMethods(createdBean.getClass());
        var fieldsAutowiredViaConstructor = Arrays.stream(createdBean.getClass().getConstructors())
                .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                .map(Constructor::getParameterTypes)
                .flatMap(Stream::of)
                .map(Class::getName)
                .collect(Collectors.toList());

        if(dependsOn.isEmpty()) {
            assertThat(autowiredFields, emptyCollectionOf(String.class));
            assertThat(fieldsAutowiredViaSetter, emptyCollectionOf(String.class));
        } else {
            configurators.stream().filter(AutowiredFieldBeanConfigurator.class::isInstance)
                    .findFirst()
                    .ifPresent(c -> assertThat(dependsOn, containsInAnyOrder(autowiredFields.toArray(new String[0]))));
            configurators.stream().filter(AutowiredSetterBeanConfigurator.class::isInstance)
                    .findFirst()
                    .ifPresent(c -> assertThat(dependsOn, containsInAnyOrder(fieldsAutowiredViaSetter.toArray(new String[0]))));
        }
        if (beanCreator instanceof AutowiredConstructorBeanCreator) {
            assertThat(dependsOn, containsInAnyOrder(fieldsAutowiredViaConstructor.toArray(new String[0])));
        }
    }

    private BeanDefinitionRegistry getBeanDefinitionRegistry() {
        var registry = new BeanDefinitionRegistry();

        var class1Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class1";
        var class1BeanDefinition = noFields(class1Name, Collections.emptyList());
        registry.registerBeanDefinition(class1Name, class1BeanDefinition);

        var class2Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class2";
        var class2BeanDefinition = autowiredFields(class2Name, List.of(Class3.class.getName()));
        registry.registerBeanDefinition(class2Name, class2BeanDefinition);

        var class3Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class3";
        var class3BeanDefinition = autowiredSetter(class3Name, List.of(Class1.class.getName()));
        registry.registerBeanDefinition(class3Name, class3BeanDefinition);

        var class4Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class4";
        var class4BeanDefinition = autowiredConstructor(class4Name, List.of(Class1.class.getName(), Class3.class.getName()));
        registry.registerBeanDefinition(class4Name, class4BeanDefinition);

        var class5Name = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class5";
        var class5BeanDefinition = autowiredConstructor(class5Name, List.of(Class1.class.getName(), Class2.class.getName(), Class3.class.getName(), Class4.class.getName()));
        registry.registerBeanDefinition(class5Name, class5BeanDefinition);

        return registry;
    }

    private BeanDefinition noFields(String className, List<String> dependsOn) {
        var beanDefinition = new BeanDefinition();
        beanDefinition.setClassName(className);
        beanDefinition.setDependsOnFields(dependsOn);
        beanDefinition.setBeanCreator(new EmptyConstructorBeanCreator());
        beanDefinition.setBeanConfigurators(Collections.emptyList());
        return beanDefinition;
    }

    private BeanDefinition autowiredFields(String className, List<String> dependsOn) {
        var beanDefinition = noFields(className, dependsOn);
        beanDefinition.setBeanConfigurators(List.of(new AutowiredFieldBeanConfigurator()));
        return beanDefinition;
    }

    private BeanDefinition autowiredSetter(String className, List<String> dependsOn) {
        var beanDefinition = noFields(className, dependsOn);
        beanDefinition.setBeanConfigurators(List.of(new AutowiredSetterBeanConfigurator()));
        return beanDefinition;
    }

    private BeanDefinition autowiredConstructor(String className, List<String> dependsOn) {
        var beanDefinition = noFields(className, dependsOn);
        beanDefinition.setBeanCreator(new AutowiredConstructorBeanCreator());
        return beanDefinition;
    }
}
