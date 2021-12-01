package com.blyznytsia.bring.demo.classes;

import com.blyznytsia.bring.context.annotation.Component;

@Component("_class5_")
public class Class5_HelloInterfaceImpl implements HelloInterface {

    public void printName() {
        System.out.println(Class5_HelloInterfaceImpl.class);
    }

    @Override
    public void sayHello() {
        System.out.println("Hello from Class5_InterfaceImpl");
    }
}
