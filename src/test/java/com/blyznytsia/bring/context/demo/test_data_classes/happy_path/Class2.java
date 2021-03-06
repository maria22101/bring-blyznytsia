package com.blyznytsia.bring.context.demo.test_data_classes.happy_path;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;

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

    public void printFields(){
        System.out.println("--My autowired fields: ");
        class3.printName();
    }

}
