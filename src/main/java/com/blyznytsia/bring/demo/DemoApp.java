package com.blyznytsia.bring.demo;

import com.blyznytsia.bring.context.ApplicationContext;
import com.blyznytsia.bring.demo.classes.Class1;
import com.blyznytsia.bring.demo.classes.Class2;
import com.blyznytsia.bring.demo.classes.Class3;

public class DemoApp {
    public static void main(String[] args) {
        var context = new ApplicationContext();

        var class1 = context.getBean(Class1.class);
        var class2 = context.getBean(Class2.class);
        var class3 = context.getBean(Class3.class);

        // beans creation check:
        class1.printName();
        class2.printName();
        class3.printName();

        System.out.println();

        // bean configurators check:
        System.out.println(" -----class2 bean with autowired field class3: ");
        System.out.println("hello from class3 received from autowired field: ");
        class2.getClass3().printName();

        System.out.println();

        System.out.println(" -----class3 field autowired in setter class1: ");
        System.out.println("hello from class1 received from autowired setter: ");
        class3.getClass1().printName();
    }
}
