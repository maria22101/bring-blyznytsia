package com.blyznytsia.bring.demo.classes_x;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.demo.classes.Class2;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class ClassX {

    @Autowired
    Class2 class2;

    public void printName(){
        System.out.println(ClassX.class);
    }

    public void printFields(){
        System.out.println("--My autowired fields: ");
        class2.printName();
    }
}
