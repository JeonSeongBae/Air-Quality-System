package com.example.leechungwan.testversion;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private List<EndDevice> list_EndDevice;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseRef;
    private GoogleMap mGoogleMap = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list_EndDevice = new ArrayList<EndDevice>();


        android.app.FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
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
    private void initNode(GoogleMap googleMap){
        LatLng[] latLngList = new LatLng[list_EndDevice.size()];
        for(int i=0; i<list_EndDevice.size();i++){
            latLngList[i] = new LatLng(list_EndDevice.get(i).getLatitude(), list_EndDevice.get(i).getLongitude());
        }

        for(int idx = 0; idx<5; idx++){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(latLngList[idx])
                    .title(list_EndDevice.get(idx).getID());
            googleMap.addMarker(markerOptions);
        }
        LatLng startLocation = new LatLng(36.369906, 127.345907);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        // map에 선 그리는 함수.
        // googleMap.addPolyline(new PolylineOptions().add(latLngList[0],latLngList[1]).width(5).color(Color.GREEN));

    }
    private void updateNode() {
        firebaseDatabaseRef.child("registedNode").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                EndDevice a = dataSnapshot.getValue(EndDevice.class);
                list_EndDevice.add(a);
                if (a.getID().equals("충대후문")){
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