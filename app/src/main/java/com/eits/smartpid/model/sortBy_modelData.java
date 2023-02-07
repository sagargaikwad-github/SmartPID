package com.eits.smartpid.model;

public class sortBy_modelData {
    String sortByName;
    boolean isShow=false;

    public sortBy_modelData(String sortByName, boolean isShow) {
        this.sortByName = sortByName;
        this.isShow = isShow;
    }

    public String getSortByName() {
        return sortByName;
    }

    public void setSortByName(String sortByName) {
        this.sortByName = sortByName;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
