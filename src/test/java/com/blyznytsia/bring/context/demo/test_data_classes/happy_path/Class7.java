package com.blyznytsia.bring.context.demo.test_data_classes.happy_path;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.Qualifier;

@Component
public class Class7 {

    @Autowired
    Class2 class2;

    @Autowired
    @Qualifier("impl_1")
    HelloInterface helloInterface;

    Class3 class3;

    @Autowired
    public void setClass3(Class3 class3) {
        this.class3 = class3;
    }
}
