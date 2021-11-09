package com.blyznytsia.bring.context;

import java.util.ArrayList;
import java.util.List;

import com.blyznytsia.bring.context.services.BeanDefinitionProcessor;

public class BeanDefinition {
    private String className;
    private String scope;
    private String creationMode; // multiple-parameters constructor, circular dependency, multiple impl of same interface, constructor, setter, reflection,  ? not sure if necessary - maybe factory will detect it
    private List<String> dependsOn;

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

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public String getCreationMode() {
        return creationMode;
    }

    public void setCreationMode(String creationMode) {
        this.creationMode = creationMode;
    }

    public void setDependsOn(ArrayList<String> dependsOn) {
        this.dependsOn = dependsOn;
    }
}
