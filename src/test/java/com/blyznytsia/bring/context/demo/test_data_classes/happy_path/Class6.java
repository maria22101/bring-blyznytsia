package com.blyznytsia.bring.context.demo.test_data_classes.happy_path;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.Qualifier;

@Component
public class Class6 {

    @Autowired
    @Qualifier("impl_2")
    private HelloInterface hello;
}
