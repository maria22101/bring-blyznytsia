package com.blyznytsia.bring.demo.classes;

import com.blyznytsia.bring.context.annotation.Bean;

@Bean("class2")
public class Class2 {
    public void printName(){
        System.out.println(Class2.class);
    }

}
