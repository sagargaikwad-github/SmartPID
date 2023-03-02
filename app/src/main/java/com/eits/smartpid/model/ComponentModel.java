package com.eits.smartpid.model;

public class ComponentModel {
    int compId;
    String compName;
    int compFilter;

    public ComponentModel(int compId, String compName, int compFilter) {
        this.compId = compId;
        this.compName = compName;
        this.compFilter = compFilter;
    }

    public int getCompId() {
        return compId;
    }

    public void setCompId(int compId) {
        this.compId = compId;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public int getCompFilter() {
        return compFilter;
    }

    public void setCompFilter(int compFilter) {
        this.compFilter = compFilter;
    }
}
