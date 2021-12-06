package com.blyznytsia.bring.context.demo.test_data_classes.happy_path;

import com.blyznytsia.bring.context.annotation.Component;

@Component
public class Class1 {

    public void printName(){
        System.out.println(Class1.class);
    }
}
