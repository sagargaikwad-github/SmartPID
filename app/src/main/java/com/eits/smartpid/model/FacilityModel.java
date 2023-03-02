package com.eits.smartpid.model;

public class FacilityModel {
    int facID;
    String facName;
    int facFilter;

    public FacilityModel(int facID, String facName, int facFilter) {
        this.facID = facID;
        this.facName = facName;
        this.facFilter = facFilter;
    }

    public int getFacID() {
        return facID;
    }

    public void setFacID(int facID) {
        this.facID = facID;
    }

    public String getFacName() {
        return facName;
    }

    public void setFacName(String facName) {
        this.facName = facName;
    }

    public int getFacFilter() {
        return facFilter;
    }

    public void setFacFilter(int facFilter) {
        this.facFilter = facFilter;
    }
}
