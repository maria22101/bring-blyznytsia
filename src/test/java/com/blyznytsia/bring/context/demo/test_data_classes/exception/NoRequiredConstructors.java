package com.blyznytsia.bring.context.demo.test_data_classes.exception;

import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.demo.test_data_classes.happy_path.Class2;

@Component
public class NoRequiredConstructors {

    Class2 class2;

    public NoRequiredConstructors(Class2 class2) {
        this.class2 = class2;
    }
}
