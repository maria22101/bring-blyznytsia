package com.blyznytsia.bring.context.demo.test_data_classes.happy_path;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;

import lombok.Getter;

@Component
@Getter
public class Class3 {

    Class1 class1;

    @Autowired
    public void setClass1(Class1 class1) {
        this.class1 = class1;
    }

    public void printName(){
        System.out.println(Class3.class);
    }

    public void printFields(){
        System.out.println("--My fields autowired via setter: ");
        class1.printName();
    }
}
