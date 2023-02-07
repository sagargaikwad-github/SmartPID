package com.eits.smartpid.model;

public class FileModel {
    String fileName;
    String fileDateTime;
    int compID;
    int facID;
    String fileSiteLocation;
    float fileMin;
    float fileMax;
    float fileAverage;
    String filePath;
    String  fileDuration;
    String fileNote;

    public FileModel(String fileName, String fileDateTime, int compID, int facID, String fileSiteLocation, float fileMin, float fileMax, float fileAverage, String filePath, String fileDuration, String fileNote) {
        this.fileName = fileName;
        this.fileDateTime = fileDateTime;
        this.compID = compID;
        this.facID = facID;
        this.fileSiteLocation = fileSiteLocation;
        this.fileMin = fileMin;
        this.fileMax = fileMax;
        this.fileAverage = fileAverage;
        this.filePath = filePath;
        this.fileDuration = fileDuration;
        this.fileNote = fileNote;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDateTime() {
        return fileDateTime;
    }

    public void setFileDateTime(String fileDateTime) {
        this.fileDateTime = fileDateTime;
    }

    public int getCompID() {
        return compID;
    }

    public void setCompID(int compID) {
        this.compID = compID;
    }

    public int getFacID() {
        return facID;
    }

    public void setFacID(int facID) {
        this.facID = facID;
    }

    public String getFileSiteLocation() {
        return fileSiteLocation;
    }

    public void setFileSiteLocation(String fileSiteLocation) {
        this.fileSiteLocation = fileSiteLocation;
    }

    public float getFileMin() {
        return fileMin;
    }

    public void setFileMin(float fileMin) {
        this.fileMin = fileMin;
    }

    public float getFileMax() {
        return fileMax;
    }

    public void setFileMax(float fileMax) {
        this.fileMax = fileMax;
    }

    public float getFileAverage() {
        return fileAverage;
    }

    public void setFileAverage(float fileAverage) {
        this.fileAverage = fileAverage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileDuration() {
        return fileDuration;
    }

    public void setFileDuration(String fileDuration) {
        this.fileDuration = fileDuration;
    }

    public String getFileNote() {
        return fileNote;
    }

    public void setFileNote(String fileNote) {
        this.fileNote = fileNote;
    }
}
