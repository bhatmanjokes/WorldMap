package com.task.worldmap.model;

public class Locality {


    private int savedOneOrNot;
    private String locality;

    public int getSavedOneOrNot() {
        return savedOneOrNot;
    }

    public Locality(int savedOneOrNot, String locality) {
        this.savedOneOrNot = savedOneOrNot;
        this.locality = locality;
    }

    public void setSavedOneOrNot(int savedOneOrNot) {
        this.savedOneOrNot = savedOneOrNot;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }
}
