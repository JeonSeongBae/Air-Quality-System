package com.example.leechungwan.testversion;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class RegistedNode {
    private String ID;
    private double latitude;
    private double longitude;

    public RegistedNode() {
        // Defalut constructor required for calls to DataSnapshot.getValue(EndDevice.class)
    }

    public RegistedNode(String ID, double latitude, double longitude) {
        this.ID = ID;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ID", ID);
        result.put("latitude", latitude);
        result.put("longitude", longitude);

        return result;
    }

    public void setID(String ID) {
        this.ID = ID;
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
