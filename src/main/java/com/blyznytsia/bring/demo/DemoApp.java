package com.blyznytsia.bring.demo;

import com.blyznytsia.bring.context.ApplicationContext;
import com.blyznytsia.bring.demo.classes.Class1;
import com.blyznytsia.bring.demo.classes.Class2;
import com.blyznytsia.bring.demo.classes.Class3;
import com.blyznytsia.bring.demo.classes.Class4;

public class DemoApp {
    public static void main(String[] args) {
        var context = new ApplicationContext();

        var class1 = context.getBean(Class1.class);
        var class2 = context.getBean(Class2.class);
        var class3 = context.getBean(Class3.class);
        var class4 = context.getBean(Class4.class);

        // beans creation check:
        System.out.println(" -----beans created: ");
        class1.printName();
        class2.printName();
        class3.printName();
        class4.printName();

        System.out.println();

        // bean configurators check:
        System.out.println(" -----class2 (with autowired field class3): ");
        class2.printFields();

        System.out.println();

        System.out.println(" -----class3 (with autowired via setter field class1): ");
        class3.printFields();

        System.out.println();

        System.out.println(" -----Class4 (with fields Class1, Class3 injected via autowired constructor): ");
        class4.printFields();
    }
}
