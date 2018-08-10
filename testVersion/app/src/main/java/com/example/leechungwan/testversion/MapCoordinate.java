package com.example.leechungwan.testversion;

public class MapCoordinate {
    private int x_dot;
    private int y_dot;

    public MapCoordinate(int x, int y) {
        this.x_dot = x;
        this.y_dot = y;
    }

    public int getX_dot(){
        return x_dot;
    }

    public int getY_dot(){
        return y_dot;
    }
}
