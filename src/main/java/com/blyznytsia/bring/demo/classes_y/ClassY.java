package com.blyznytsia.bring.demo.classes_y;

import com.blyznytsia.bring.context.annotation.Autowired;
import com.blyznytsia.bring.context.annotation.Component;
import com.blyznytsia.bring.demo.classes.Class3;
import com.blyznytsia.bring.demo.classes.Class5;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class ClassY {

    private Class3 class3;
    private Class5 class5;

    @Autowired
    public ClassY(Class3 class3, Class5 class5) {
        this.class3 = class3;
        this.class5 = class5;
    }

    public void printName(){
        System.out.println(ClassY.class);
    }

    public void printFields(){
        System.out.println("--My fields injected via autowired constructor: ");
        class3.printName();
        class5.printName();
    }
}
