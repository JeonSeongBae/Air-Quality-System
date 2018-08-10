package com.example.leechungwan.testversion;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private List<EndDevice> list_EndDevice;
    private List<MapCoordinate> coordinates;
    private LatLng[] latLngList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseRef;
    private GoogleMap mGoogleMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list_EndDevice = new ArrayList<EndDevice>();
        coordinates = makeCoordinate();

        android.app.FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 데이터베이스 Instance 생성
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseRef = firebaseDatabase.getReference();
        updateNode();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        LatLng startLocation = new LatLng(36.369906, 127.345907);
        // 카메라를 위치로 옮긴다.
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void initNode(GoogleMap googleMap) {
        latLngList = new LatLng[list_EndDevice.size()];
        for (int i = 0; i < list_EndDevice.size(); i++) {
            latLngList[i] = new LatLng(list_EndDevice.get(i).getLatitude(), list_EndDevice.get(i).getLongitude());
        }

        for (int idx = 0; idx < list_EndDevice.size(); idx++) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(latLngList[idx])
                    //.title(list_EndDevice.get(idx).getID())
                    .alpha(0.01f);
            googleMap.addMarker(markerOptions);
        }
        LatLng startLocation = new LatLng(36.369906, 127.345907);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        drawLine(mGoogleMap);
        // map에 선 그리는 함수.
//        for (int i = 0; i < latLngList.length - 1; i++) {
//            googleMap.addPolyline(new PolylineOptions().add(latLngList[i], latLngList[i + 1]).width(5).color(Color.GREEN));
//        }
        //googleMap.addPolyline(new PolylineOptions().add(latLngList[1], latLngList[6]).width(5).color(Color.GREEN));


    }
    private List<MapCoordinate> makeCoordinate(){
        coordinates = new ArrayList<>();
        coordinates.add(new MapCoordinate(0, 1));
        coordinates.add(new MapCoordinate(1, 2));
        coordinates.add(new MapCoordinate(2, 3));
        coordinates.add(new MapCoordinate(6, 7));
        coordinates.add(new MapCoordinate(7, 8));
        coordinates.add(new MapCoordinate(8, 9));
        coordinates.add(new MapCoordinate(10, 11));
        coordinates.add(new MapCoordinate(11, 12));
        coordinates.add(new MapCoordinate(12, 13));
        coordinates.add(new MapCoordinate(13, 14));


        coordinates.add(new MapCoordinate(0, 6));
        coordinates.add(new MapCoordinate(6, 10));
        coordinates.add(new MapCoordinate(1, 4));
        coordinates.add(new MapCoordinate(4, 5));
        coordinates.add(new MapCoordinate(5, 8));
        coordinates.add(new MapCoordinate(8, 13));
        coordinates.add(new MapCoordinate(7, 11));
        coordinates.add(new MapCoordinate(3, 9));
        coordinates.add(new MapCoordinate(9, 14));
        return coordinates;
    }

    private void drawLine(GoogleMap googleMap){
        for(int i=0; i<coordinates.size(); i++){
            googleMap.addPolyline(new PolylineOptions().add(latLngList[coordinates.get(i).getX_dot()], latLngList[coordinates.get(i).getY_dot()]).width(5).color(Color.GREEN));
        }
    }

    private void updateNode() {
        firebaseDatabaseRef.child("registedNode").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                EndDevice a = dataSnapshot.getValue(EndDevice.class);
                list_EndDevice.add(a);
                if (a.getID().equals("24")) {
                    onMapReady(mGoogleMap);
                    initNode(mGoogleMap);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}