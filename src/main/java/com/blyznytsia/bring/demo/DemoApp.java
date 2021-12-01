package com.blyznytsia.bring.demo;

import com.blyznytsia.bring.context.ApplicationContext;
import com.blyznytsia.bring.demo.classes.Class1;
import com.blyznytsia.bring.demo.classes.Class2;
import com.blyznytsia.bring.demo.classes.Class3;
import com.blyznytsia.bring.demo.classes.Class4;
import com.blyznytsia.bring.demo.classes.Class5;
import com.blyznytsia.bring.demo.classes.Class5_HelloInterfaceImpl;
import com.blyznytsia.bring.demo.classes.Class6_HelloInterfaceImpl;
import com.blyznytsia.bring.demo.classes_x.ClassX;
import com.blyznytsia.bring.demo.classes_y.ClassY;

public class DemoApp {
    public static void main(String[] args) {
        var context = new ApplicationContext();

        var class1 = context.getBean(Class1.class);
        var class2 = context.getBean(Class2.class);
        var class3 = context.getBean(Class3.class);
        var class4 = context.getBean(Class4.class);
        var class5 = context.getBean(Class5.class);
        var class5InterfaceImpl = context.getBean(Class5_HelloInterfaceImpl.class);
        var class6InterfaceImpl = context.getBean(Class6_HelloInterfaceImpl.class);
        var classX = context.getBean(ClassX.class);
        var classY = context.getBean(ClassY.class);

        // beans creation check:
        System.out.println(" -----beans created: ");
        class1.printName();
        class2.printName();
        class3.printName();
        class4.printName();
        class5.printName();
        class5InterfaceImpl.printName();
        class6InterfaceImpl.printName();
        classX.printName();
        classY.printName();

        System.out.println();
        // bean configurators check:
        System.out.println(" -----class2 (with autowired fields class3 and HelloInterface): ");
        class2.printFields();

        System.out.println();
        System.out.println(" -----class3 (with autowired via setter field class1): ");
        class3.printFields();

        System.out.println();
        System.out.println(" -----Class4 (with fields Class1, Class3 injected via autowired constructor): ");
        class4.printFields();

        System.out.println();
        System.out.println(" -----Class5 (with fields Class1, Class2, Class3, Class4 injected via autowired constructor): ");
        class5.printFields();

        System.out.println();
        System.out.println(" -----ClassX (from 2nd package indicated in @ComponentScan; with autowired field Class2): ");
        classX.printFields();

        System.out.println();
        System.out.println(" -----ClassY (from 3rd package indicated in @ComponentScan; with fields Class3, Class5 injected via autowired constructor): ");
        classY.printFields();
    }
}
