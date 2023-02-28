package com.eits.smartpid.model;

public class ComponentModel {
    int compId;
    String compName;


    public ComponentModel(int compId, String compName) {
        this.compId = compId;
        this.compName = compName;
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


}
