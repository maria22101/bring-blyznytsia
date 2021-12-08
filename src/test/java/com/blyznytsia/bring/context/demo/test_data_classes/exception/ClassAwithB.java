package com.blyznytsia.bring.context.demo.test_data_classes.exception;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;

@Component
public class ClassAwithB {

    @Autowired
    private ClassBwithA classBwithA;
}
