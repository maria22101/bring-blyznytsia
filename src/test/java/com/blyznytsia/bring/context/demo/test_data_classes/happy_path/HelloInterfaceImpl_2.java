package com.blyznytsia.bring.context.demo.test_data_classes.happy_path;

import com.blyznytsia.bring.context.annotation.Component;

@Component("impl_2")
public class HelloInterfaceImpl_2 implements HelloInterface {

    public void printName() {
        System.out.println(HelloInterfaceImpl_2.class);
    }

    @Override
    public void sayHello() {
        System.out.println("Hello from HelloInterfaceImpl_2");
    }
}
