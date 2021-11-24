package com.blyznytsia.bring.demo.classes;

import com.blyznytsia.bring.context.annotation.Component;

@Component("_class6_")
public class Class6_HelloInterfaceImpl implements HelloInterface {

    @Override
    public void sayHello() {
        System.out.println("Hello from Class6_InterfaceImpl");
    }
}
