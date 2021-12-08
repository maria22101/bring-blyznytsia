package com.blyznytsia.bring.context.util;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.blyznytsia.bring.context.BeanDefinition;
import com.blyznytsia.bring.context.constants.BeanStatus;
import com.blyznytsia.bring.context.demo.test_data_classes.exception.NoRequiredConstructors;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class1;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class2;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class3;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class4;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class5;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class6;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class7;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.HelloInterfaceImpl_1;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.HelloInterfaceImpl_2;
import com.blyznytsia.bring.context.exceptions.BeanCreationException;
import com.blyznytsia.bring.context.services.BeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredConstructorBeanCreator;
import com.blyznytsia.bring.context.services.impl.AutowiredFieldBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.AutowiredSetterBeanConfigurator;
import com.blyznytsia.bring.context.services.impl.EmptyConstructorBeanCreator;

class BeanDefinitionGeneratorTest {

    @Test
    @DisplayName("For Bean without dependent fields")
    void noFields() {
        var testClass = Class1.class;

        var beanDefinition = BeanDefinitionGenerator.generate(testClass, Collections.emptySet());

        beanCreatorIsEmptyConstructor(beanDefinition);
        noBeanConfigurators(beanDefinition);
        noDependsOnFields(beanDefinition);
    }

    @Test
    @DisplayName("For Bean with one autowired field")
    void oneAutowiredField() {
        var testClass = Class2.class;
        var testClassFieldsToInject = List.of(Class3.class.getName());

        var beanDefinition = BeanDefinitionGenerator.generate(testClass, Collections.emptySet());

        beanCreatorIsEmptyConstructor(beanDefinition);
        beanConfiguratorsIsAutowiredField(beanDefinition);
        validateDependOnFields(beanDefinition, testClassFieldsToInject);
    }

    @Test
    @DisplayName("For Bean with one field autowired via setter")
    void oneFieldAutowiredViaSetter() {
        var testClass = Class3.class;
        var testClassFieldsToInject = List.of(Class1.class.getName());

        var beanDefinition = BeanDefinitionGenerator.generate(testClass, Collections.emptySet());

        beanCreatorIsEmptyConstructor(beanDefinition);
        beanConfiguratorsIsAutowiredSetter(beanDefinition);
        validateDependOnFields(beanDefinition, testClassFieldsToInject);
    }

    @Test
    @DisplayName("For Bean with fields some of which autowired via constructor")
    void someFieldsAutowiredViaConstructor() {
        var testClass = Class4.class;
        var testClassFieldsToInject = List.of(Class1.class.getName(), Class3.class.getName());

        var beanDefinition = BeanDefinitionGenerator.generate(testClass, Collections.emptySet());

        beanCreatorIsAutowiredConstructor(beanDefinition);
        noBeanConfigurators(beanDefinition);
        validateDependOnFields(beanDefinition, testClassFieldsToInject);
    }

    @Test
    @DisplayName("For Bean with all fields autowired via constructor")
    void allFieldsAutowiredViaConstructor() {
        var testClass = Class5.class;
        var testClassFieldsToInject = List.of(Class1.class.getName(), Class2.class.getName(),
                Class3.class.getName(), Class4.class.getName());

        var beanDefinition = BeanDefinitionGenerator.generate(testClass, Collections.emptySet());

        beanCreatorIsAutowiredConstructor(beanDefinition);
        noBeanConfigurators(beanDefinition);
        validateDependOnFields(beanDefinition, testClassFieldsToInject);
    }

    @Test
    @DisplayName("For Bean with a field of an interface type")
    void oneFieldOfInterfaceType() {
        var testClass = Class6.class;
        var testClassFieldsToInject = List.of(HelloInterfaceImpl_2.class.getName());

        var beanDefinition = BeanDefinitionGenerator.generate(testClass,
                Set.of(HelloInterfaceImpl_1.class, HelloInterfaceImpl_2.class));

        beanCreatorIsEmptyConstructor(beanDefinition);
        beanConfiguratorsIsAutowiredField(beanDefinition);
        validateDependOnFields(beanDefinition, testClassFieldsToInject);
    }

    @Test
    @DisplayName("For Bean with a field of an interface type and various injection modes")
    void oneFieldOfInterfaceTypeAndMixedAutowiringModes() {
        var testClass = Class7.class;
        var testClassFieldsToInject = List.of(Class2.class.getName(),
                HelloInterfaceImpl_1.class.getName(), Class3.class.getName());

        var beanDefinition = BeanDefinitionGenerator.generate(testClass,
                Set.of(HelloInterfaceImpl_1.class, HelloInterfaceImpl_2.class));

        beanCreatorIsEmptyConstructor(beanDefinition);
        beanConfiguratorsAreAutowiredFieldAndSetter(beanDefinition);
        validateDependOnFields(beanDefinition, testClassFieldsToInject);
    }

    @Test
    @DisplayName("Exception if no default or autowired constructors present")
    void withoutDefaultOrAutowiredConstructor() {
        var testClass = NoRequiredConstructors.class;

        assertThrows(BeanCreationException.class,
                () -> BeanDefinitionGenerator.generate(testClass, Collections.emptySet()));
    }

    private void beanCreatorIsEmptyConstructor(BeanDefinition beanDefinition) {
        assertThat(beanDefinition.getBeanCreator().getClass().getName(), is(EmptyConstructorBeanCreator.class.getName()));
        assertThat(beanDefinition.getStatus(), is(BeanStatus.INITIALIZING));
    }

    private void beanCreatorIsAutowiredConstructor(BeanDefinition beanDefinition) {
        assertThat(beanDefinition.getBeanCreator().getClass().getName(), is(AutowiredConstructorBeanCreator.class.getName()));
        assertThat(beanDefinition.getStatus(), is(BeanStatus.INITIALIZING));
    }

    private void noBeanConfigurators(BeanDefinition beanDefinition) {
        assertThat(beanDefinition.getBeanConfigurators(), emptyCollectionOf(BeanConfigurator.class));
    }

    private void beanConfiguratorsIsAutowiredField(BeanDefinition beanDefinition) {
        assertThat(beanDefinition.getBeanConfigurators(), hasSize(1));
        assertThat(beanDefinition.getBeanConfigurators().get(0).getClass().getName(),
                is(AutowiredFieldBeanConfigurator.class.getName()));
    }

    private void beanConfiguratorsIsAutowiredSetter(BeanDefinition beanDefinition) {
        assertThat(beanDefinition.getBeanConfigurators(), hasSize(1));
        assertThat(beanDefinition.getBeanConfigurators().get(0).getClass().getName(),
                is(AutowiredSetterBeanConfigurator.class.getName()));
    }

    private void beanConfiguratorsAreAutowiredFieldAndSetter(BeanDefinition beanDefinition) {
        var configuratorsClassNames = beanDefinition.getBeanConfigurators().stream()
                .map(c -> c.getClass().getName())
                .collect(toList());
        assertThat(configuratorsClassNames, hasSize(2));
        assertThat(configuratorsClassNames, hasItem(AutowiredSetterBeanConfigurator.class.getName()));
        assertThat(configuratorsClassNames, hasItem(AutowiredFieldBeanConfigurator.class.getName()));
    }

    private void noDependsOnFields(BeanDefinition beanDefinition) {
        assertThat(beanDefinition.getDependsOnFields(), emptyCollectionOf(String.class));
    }

    private void validateDependOnFields(BeanDefinition beanDefinition, List<String> testClassFieldsToInject) {
        assertThat(beanDefinition.getDependsOnFields(), hasSize(testClassFieldsToInject.size()));
        assertThat(beanDefinition.getDependsOnFields(),
                containsInAnyOrder(testClassFieldsToInject.toArray(new String[0])));
    }
}
