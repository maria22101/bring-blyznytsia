package com.blyznytsia.bring.context.demo.test_data_classes.happy_path;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;

import lombok.Getter;

@Component
@Getter
public class Class4 {

    Class1 class1;
    Class2 class2;
    Class3 class3;

    @Autowired
    public Class4(Class1 class1, Class3 class3) {
        this.class1 = class1;
        this.class3 = class3;
    }

    public void setClass2(Class2 class2) {
        this.class2 = class2;
    }

    public void printName(){
        System.out.println(Class4.class);
    }

    public void printFields(){
        System.out.println("--My fields injected via autowired constructor: ");
        class1.printName();
        class3.printName();
    }
}
