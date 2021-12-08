package com.blyznytsia.bring.demo.classes_beans;

public class ClassBean {
    private String name = "my name is ClassBean_for_testing_@Bean_annotation";

    public void printName(){
        System.out.println(ClassBean.class);
        System.out.println(name);
    }
}
