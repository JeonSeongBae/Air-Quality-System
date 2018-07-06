package com.example.leechungwan.testgooglemap;

public class Node {
    String id;
    double latitide;    // 위도
    double longitude;   // 경도

    public Node(String id, double latitide, double longitude){
        this.id = id;
        this.latitide = latitide;
        this.longitude = longitude;
    }
}
