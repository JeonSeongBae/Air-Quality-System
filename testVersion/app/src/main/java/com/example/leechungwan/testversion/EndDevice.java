package com.example.leechungwan.testversion;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tjdqo_000 on 2018-06-02.
 */

public class EndDevice {

    private String color;
    private String ID;
    private double density;
    private double latitude;
    private double longitude;

    public EndDevice(){
        // Defalut constructor required for calls to DataSnapshot.getValue(EndDevice.class)
    }

    public EndDevice(String color, String ID, double density, double latitude, double longitude) {
        this.color = color;
        this.ID = ID;
        this.density = density;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("density", density);
        result.put("latitude", latitude);
        result.put("longitude", longitude);

        return result;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getColor() {
        return color;
    }

    public String getID() {
        return ID;
    }

    public double getDensity() {
        return density;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}

