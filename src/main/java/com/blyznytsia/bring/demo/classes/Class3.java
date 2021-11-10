package com.blyznytsia.bring.demo.classes;

import com.blyznytsia.bring.context.annotation.Component;

@Component
public class Class3 {
    public void printName(){
        System.out.println(Class3.class);
    }
}
