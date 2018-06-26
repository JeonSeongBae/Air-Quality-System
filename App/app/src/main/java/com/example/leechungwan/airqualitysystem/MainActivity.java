package com.example.leechungwan.airqualitysystem;

import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        LatLng DEAJEON = new LatLng(36.366709, 127.344340);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEAJEON);
        markerOptions.title("충남대학교 공대 5호관");
        markerOptions.snippet("컴퓨터공학과");
        map.addMarker(markerOptions);

        map.moveCamera(CameraUpdateFactory.newLatLng(DEAJEON));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

}
