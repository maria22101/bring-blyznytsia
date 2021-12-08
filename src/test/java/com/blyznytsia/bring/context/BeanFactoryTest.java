package com.blyznytsia.bring.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.demo.test_data_classes.exception.ClassAwithB;
import com.blyznytsia.bring.context.demo.test_data_classes.exception.ClassBwithA;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class1;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class2;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class3;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class4;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.HelloInterfaceImpl_1;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.HelloInterfaceImpl_2;
import com.blyznytsia.bring.context.exceptions.CircularDependencyException;
import com.blyznytsia.bring.context.services.BeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredConstructorBeanCreator;
import com.blyznytsia.bring.context.services.impl.AutowiredFieldBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredSetterBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.EmptyConstructorBeanCreator;
import com.blyznytsia.bring.context.util.BeanDefinitionGenerator;

class BeanFactoryTest {

    // test data
    private static final String CLASS_NO_FIELDS = "com.blyznytsia.bring.context.demo.test_data_classes.happy_path" +
            ".Class1";
    private static final String CLASS_AUTOWIRED_FIELD = "com.blyznytsia.bring.context.demo.test_data_classes" +
            ".happy_path.Class2";
    private static final String CLASS_AUTOWIRED_SETTER = "com.blyznytsia.bring.context.demo.test_data_classes" +
            ".happy_path.Class3";
    private static final String CLASS_SOME_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR = "com.blyznytsia.bring.context.demo" +
            ".test_data_classes.happy_path.Class4";
    private static final String CLASS_ALL_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR = "com.blyznytsia.bring.context.demo" +
            ".test_data_classes.happy_path.Class5";
    private static final String CLASS_FIELD_OF_INTERFACE_TYPE = "com.blyznytsia.bring.context.demo.test_data_classes" +
            ".happy_path.Class6";
    private static final String CLASS_FIELD_OF_INTERFACE_TYPE_AND_VARIOUS_INJECTION_MODES = "com.blyznytsia.bring" +
            ".context.demo.test_data_classes.happy_path.Class7";
    private static final String CLASS_INTERFACE_IMPL_1 = "com.blyznytsia.bring.context.demo.test_data_classes" +
            ".happy_path.HelloInterfaceImpl_1";
    private static final String CLASS_INTERFACE_IMPL_2 = "com.blyznytsia.bring.context.demo.test_data_classes" +
            ".happy_path.HelloInterfaceImpl_2";

    private static final String CLASS_A_CIRCULAR_DEPENDENCY = "com.blyznytsia.bring.context.demo.test_data_classes" +
            ".exception.ClassAwithB";
    private static final String CLASS_B_CIRCULAR_DEPENDENCY = "com.blyznytsia.bring.context.demo.test_data_classes" +
            ".exception.ClassBwithA";

    private static final String PACKAGE = "com.blyznytsia.bring.context.demo.test_data_classes";

    private BeanFactory factory = new BeanFactory();
    private BeanDefinitionRegistry registry;
    private Map<String, Object> beanMap;

    @BeforeEach
    void setUp() {
        registry = composeBeanDefinitionRegistry();
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

        var createdBean = beanMap.get(CLASS_NO_FIELDS);

        assertThat(createdBean, instanceOf(Class.forName(CLASS_NO_FIELDS)));
        assertThat(createdBean.getClass().getName(), is(CLASS_NO_FIELDS));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with one autowired field")
    void oneAutowiredField() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var createdBean = beanMap.get(CLASS_AUTOWIRED_FIELD);

        assertThat(createdBean, instanceOf(Class.forName(CLASS_AUTOWIRED_FIELD)));
        assertThat(createdBean.getClass().getName(), is(CLASS_AUTOWIRED_FIELD));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with one field autowired via setter")
    void oneFieldAutowiredViaSetter() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var createdBean = beanMap.get(CLASS_AUTOWIRED_SETTER);

        assertThat(createdBean, instanceOf(Class.forName(CLASS_AUTOWIRED_SETTER)));
        assertThat(createdBean.getClass().getName(), is(CLASS_AUTOWIRED_SETTER));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with fields some of which autowired via constructor")
    void someFieldsAutowiredViaConstructor() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var createdBean = beanMap.get(CLASS_SOME_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR);

        assertThat(createdBean, instanceOf(Class.forName(CLASS_SOME_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR)));
        assertThat(createdBean.getClass().getName(), is(CLASS_SOME_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with all fields autowired via constructor")
    void allFieldsAutowiredViaConstructor() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var createdBean = beanMap.get(CLASS_ALL_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR);

        assertThat(createdBean, instanceOf(Class.forName(CLASS_ALL_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR)));
        assertThat(createdBean.getClass().getName(), is(CLASS_ALL_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with a field of an interface type")
    void oneFieldOfInterfaceType() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var createdBean = beanMap.get(CLASS_FIELD_OF_INTERFACE_TYPE);

        assertThat(createdBean, instanceOf(Class.forName(CLASS_FIELD_OF_INTERFACE_TYPE)));
        assertThat(createdBean.getClass().getName(), is(CLASS_FIELD_OF_INTERFACE_TYPE));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Bean with a field of an interface type and various injection modes")
    void oneFieldOfInterfaceTypeAndMixedAutowiringModes() throws ClassNotFoundException {
        factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap);

        var createdBean = beanMap.get(CLASS_FIELD_OF_INTERFACE_TYPE_AND_VARIOUS_INJECTION_MODES);

        assertThat(createdBean, instanceOf(Class.forName(CLASS_FIELD_OF_INTERFACE_TYPE_AND_VARIOUS_INJECTION_MODES)));
        assertThat(createdBean.getClass().getName(), is(CLASS_FIELD_OF_INTERFACE_TYPE_AND_VARIOUS_INJECTION_MODES));
        validateConstructor(createdBean);
        validateFields(createdBean);
    }

    @Test
    @DisplayName("Exception thrown if circular dependency detected")
    void circularDependency() {
        registry.registerBeanDefinition(CLASS_A_CIRCULAR_DEPENDENCY,
                autowiredFields(CLASS_A_CIRCULAR_DEPENDENCY, List.of(ClassBwithA.class.getName())));

        registry.registerBeanDefinition(CLASS_B_CIRCULAR_DEPENDENCY,
                autowiredFields(CLASS_B_CIRCULAR_DEPENDENCY, List.of(ClassAwithB.class.getName())));

        assertThrows(CircularDependencyException.class,
                () -> factory.traverseBeanDefinitionRegistryAndFillBeanMap(registry, beanMap));
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
        var configurators =
                registry.getBeanDefinitionMap().get(createdBean.getClass().getName()).getBeanConfigurators();

        validateAutowiredFields(createdBean, dependsOn, configurators);
        validateFieldsAutowiredViaSetter(createdBean, dependsOn, configurators);
        validateFieldsAutowiredViaConstructor(createdBean, dependsOn);
    }

    private void validateAutowiredFields(Object createdBean, List<String> dependsOn,
                                         List<BeanConfigurator> configurators) {
        var reflection = new Reflections(PACKAGE);
        var classesComponents = reflection.getTypesAnnotatedWith(Component.class);

        var autowiredFields = BeanDefinitionGenerator
                .scanAutowiredFields(createdBean.getClass(), classesComponents).toArray(String[]::new);
        if (dependsOn.isEmpty()) {
            assertThat(autowiredFields, emptyArray());
        } else {
            configurators.stream().filter(AutowiredFieldBeanConfigurator.class::isInstance)
                    .findFirst()
                    .ifPresent(c -> assertThat(dependsOn, hasItems(autowiredFields)));
        }
    }

    private void validateFieldsAutowiredViaSetter(Object createdBean, List<String> dependsOn,
                                                  List<BeanConfigurator> configurators) {
        var fieldsAutowiredViaSetter = BeanDefinitionGenerator.
                scanAutowiredMethods(createdBean.getClass()).toArray(String[]::new);
        if (dependsOn.isEmpty()) {
            assertThat(fieldsAutowiredViaSetter, emptyArray());
        } else {
            configurators.stream().filter(AutowiredSetterBeanConfigurator.class::isInstance)
                    .findFirst()
                    .ifPresent(c -> assertThat(dependsOn, hasItems(fieldsAutowiredViaSetter)));
        }
    }

    private void validateFieldsAutowiredViaConstructor(Object createdBean, List<String> dependsOn) {
        var beanCreator = registry.getBeanDefinitionMap().get(createdBean.getClass().getName()).getBeanCreator();
        if (beanCreator instanceof AutowiredConstructorBeanCreator) {
            var fieldsAutowiredViaConstructor = Arrays.stream(createdBean.getClass().getConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(Autowired.class))
                    .map(Constructor::getParameterTypes)
                    .flatMap(Stream::of)
                    .map(Class::getName)
                    .toArray(String[]::new);
            assertThat(dependsOn, containsInAnyOrder(fieldsAutowiredViaConstructor));
        }
    }

    private BeanDefinitionRegistry composeBeanDefinitionRegistry() {
        var registry = new BeanDefinitionRegistry();

        registry.registerBeanDefinition(CLASS_NO_FIELDS,
                noFields(CLASS_NO_FIELDS, Collections.emptyList()));

        registry.registerBeanDefinition(CLASS_AUTOWIRED_FIELD,
                autowiredFields(CLASS_AUTOWIRED_FIELD, List.of(Class3.class.getName())));

        registry.registerBeanDefinition(CLASS_AUTOWIRED_SETTER,
                autowiredSetter(CLASS_AUTOWIRED_SETTER, List.of(Class1.class.getName())));

        registry.registerBeanDefinition(CLASS_SOME_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR,
                autowiredConstructor(CLASS_SOME_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR,
                        List.of(Class1.class.getName(), Class3.class.getName())));

        registry.registerBeanDefinition(CLASS_ALL_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR,
                autowiredConstructor(CLASS_ALL_FIELDS_AUTOWIRED_VIA_CONSTRUCTOR,
                        List.of(Class1.class.getName(), Class2.class.getName(), Class3.class.getName(), Class4.class.getName())));

        registry.registerBeanDefinition(CLASS_FIELD_OF_INTERFACE_TYPE,
                autowiredFields(CLASS_FIELD_OF_INTERFACE_TYPE, List.of(HelloInterfaceImpl_2.class.getName())));

        registry.registerBeanDefinition(CLASS_FIELD_OF_INTERFACE_TYPE_AND_VARIOUS_INJECTION_MODES,
                autowiredFieldsAndSetters(CLASS_FIELD_OF_INTERFACE_TYPE_AND_VARIOUS_INJECTION_MODES,
                        List.of(Class2.class.getName(), HelloInterfaceImpl_1.class.getName(), Class3.class.getName())));

        registry.registerBeanDefinition(CLASS_INTERFACE_IMPL_1,
                noFields(CLASS_INTERFACE_IMPL_1, Collections.emptyList()));

        registry.registerBeanDefinition(CLASS_INTERFACE_IMPL_2,
                noFields(CLASS_INTERFACE_IMPL_2, Collections.emptyList()));

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

    private BeanDefinition autowiredFieldsAndSetters(String className, List<String> dependsOn) {
        var beanDefinition = noFields(className, dependsOn);
        beanDefinition.setBeanConfigurators(List.of(new AutowiredFieldBeanConfigurator(), new AutowiredSetterBeanConfigurator()));
        return beanDefinition;
    }
}
