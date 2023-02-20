package com.eits.smartpid.model;

public class FacilityModel {
    int facID;
    String facName;

    public FacilityModel(int facID, String facName) {
        this.facID = facID;
        this.facName = facName;

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

}
