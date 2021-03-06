package com.blyznytsia.bring.demo.classes;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.context.annotation.Qualifier;
import com.blyznytsia.bring.demo.interfaces.HelloInterface;

@Component
public class Class2 {

    @Autowired
    private Class3 class3;

    @Autowired
    @Qualifier("_class6_")
    private HelloInterface hello;

    public Class3 getClass3() {
        return class3;
    }

    public void printName(){
        System.out.println(Class2.class);
    }

    public void printFields(){
        System.out.println("--My autowired fields: ");
        class3.printName();
        hello.sayHello();
    }

}
