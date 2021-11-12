package com.blyznytsia.bring.demo.classes;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;

import lombok.Getter;

@Component
public class Class2 {

    @Autowired
    private Class3 class3;

    public Class3 getClass3() {
        return class3;
    }

    public void printName(){
        System.out.println(Class2.class);
    }

}
