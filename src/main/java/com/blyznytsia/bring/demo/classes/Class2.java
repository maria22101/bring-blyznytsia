package com.blyznytsia.bring.demo.classes;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;

@Component
public class Class2 {

    @Autowired
    private Class3 class3;



    public void printName(){
        System.out.println(Class2.class);
    }

}
