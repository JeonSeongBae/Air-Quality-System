package com.example.leechungwan.testversion;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tjdqo_000 on 2018-06-02.
 */

public class EndDevice {

    private String ID;
    private double density;
    private double latitude;
    private double longitude;
    private String time;
    public EndDevice(){
        // Defalut constructor required for calls to DataSnapshot.getValue(EndDevice.class)
    }

    public EndDevice(String ID, double density, double latitude, double longitude, String time) {
        this.ID = ID;
        this.density = density;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("density", density);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("time", time);
        return result;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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