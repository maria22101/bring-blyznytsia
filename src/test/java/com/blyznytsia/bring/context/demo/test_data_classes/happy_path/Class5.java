package com.blyznytsia.bring.context.demo.test_data_classes.happy_path;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;

@Component
public class Class5 {

    Class1 class1;
    Class2 class2;
    Class3 class3;
    Class4 class4;

    @Autowired
    public Class5(Class1 class1, Class2 class2, Class3 class3, Class4 class4) {
        this.class1 = class1;
        this.class2 = class2;
        this.class3 = class3;
        this.class4 = class4;
    }

    public void printName() {
        System.out.println(Class5.class);
    }

    public void printFields(){
        System.out.println("--My fields injected via autowired constructor: ");
        class1.printName();
        class2.printName();
        class3.printName();
        class4.printName();
    }
}
