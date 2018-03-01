package com.example.dries.project;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by dries on 27/02/2018.
 */

public class Herinnering {

    private int id;
    private String name;
    private String description;
    private String coordlat;
    private String coordlong;


    public Herinnering() {}

    public Herinnering(String name, String description, String coordlat, String coordlong) {
        this.name = name;
        this.description = description;
        this.coordlat = coordlat;
        this.coordlong = coordlong;
    }

    public int getId() {
        return id;
    }

    //wordt niet gebruikt
    public void setId(int id) {this.id = id; }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    //ADDED

    public String getCoordlat() {
        return coordlat;
    }

    public void setCoordlat(String coordlat) {
        this.coordlat = coordlat;
    }

    public String getCoordlong() {
        return coordlong;
    }

    public void setCoordlong(String coordlong) {
        this.name = coordlong;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
