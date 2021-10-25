package com.blyznytsia.bring.demo.classes;

import com.blyznytsia.bring.context.annotation.Bean;

@Bean("class1")
public class Class1 {
    public void printName(){
        System.out.println(Class1.class);
    }
}
