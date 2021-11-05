package com.blyznytsia.bring.context;

import java.util.ArrayList;

public class BeanDefinition {
    private String className;
    private String scope;
    private ArrayList<String> dependsOn;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public ArrayList<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(ArrayList<String> dependsOn) {
        this.dependsOn = dependsOn;
    }
}
