package com.blyznytsia.bring.context;

import com.blyznytsia.bring.context.constants.CreationMode;

import java.util.List;

public class BeanDefinition {
    private String className;
    private String scope;
    private List<CreationMode> creationModes; // multiple-parameters constructor, circular dependency, multiple impl of same interface, constructor, setter, reflection,  ? not sure if necessary - maybe factory will detect it
    private List<String> dependsOnFields;
    private List<String> dependsOnSetters;

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

    public List<CreationMode> getCreationModes() {
        return creationModes;
    }

    public void setCreationModes(List<CreationMode> creationMode) {
        this.creationModes = creationMode;
    }

    public List<String> getDependsOnFields() {
        return dependsOnFields;
    }

    public void setDependsOnFields(List<String> dependsOnFields) {
        this.dependsOnFields = dependsOnFields;
    }

    public List<String> getDependsOnSetters() {
        return dependsOnSetters;
    }

    public void setDependsOnSetters(List<String> dependsOnSetters) {
        this.dependsOnSetters = dependsOnSetters;
    }
}
