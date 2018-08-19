package com.example.leechungwan.testversion;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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
    private List<RegistedNode> registedNodeList;    // Line을 그릴 PIN 객체를 저장함.
    private List<EndDevice> endDeviceList;          // 미세먼지 농도가 저장된 노드를 저장함.
    private List<MapCoordinate> orderedPair;        // Line을 그릴 PIN의 순서쌍을 저장.
    private LatLng[] registedCoordinates;
    private LatLng[] endDeviceCoordinates;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseDatabaseRef;
    private GoogleMap mGoogleMap = null;
    private MapFragment mapFragment;
    private EndDevice updateDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registedNodeList = new ArrayList<>();
        endDeviceList = new ArrayList<>();
        orderedPair = makeOrderedPair();

        android.app.FragmentManager fragmentManager = getFragmentManager();
        mapFragment = (MapFragment) fragmentManager
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

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleMap != null) {
            mGoogleMap.clear();
            drawLine(mGoogleMap);
        }
    }

    private void initNode(GoogleMap googleMap) {
        addMarker();
        LatLng startLocation = new LatLng(36.369906, 127.345907);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        drawLine(mGoogleMap);
    }

    private void addMarker(){
        for (int i = 0; i < endDeviceCoordinates.length; i++) {
            MarkerOptions node = new MarkerOptions();
            node.position(endDeviceCoordinates[i])
                    .title(endDeviceList.get(i).getID());
            mGoogleMap.addMarker(node);
        }

        for (int idx = 0; idx < registedNodeList.size(); idx++) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(registedCoordinates[idx])
                    //.title(list_EndDevice.get(idx).getID())
                    .alpha(0.01f);
            mGoogleMap.addMarker(markerOptions);
        }
    }

    private void setRegistCoordinates(int size) {
        registedCoordinates = new LatLng[size];
        for (int i = 0; i < size; i++) {
            registedCoordinates[i] = new LatLng(registedNodeList.get(i).getLatitude(), registedNodeList.get(i).getLongitude());
        }
    }

    private void setEndDeviceCoordinates(int size) {
        endDeviceCoordinates = new LatLng[size];
        for (int i = 0; i < size; i++) {
            endDeviceCoordinates[i] = new LatLng(endDeviceList.get(i).getLatitude(), endDeviceList.get(i).getLongitude());
        }
    }

    // registedNode를 연결하기 위한 순서쌍
    private List<MapCoordinate> makeOrderedPair() {
        orderedPair = new ArrayList<>();
        orderedPair.add(new MapCoordinate(0, 1));
        orderedPair.add(new MapCoordinate(1, 2));
        orderedPair.add(new MapCoordinate(2, 3));
        orderedPair.add(new MapCoordinate(6, 7));
        orderedPair.add(new MapCoordinate(7, 8));
        orderedPair.add(new MapCoordinate(8, 9));
        orderedPair.add(new MapCoordinate(10, 11));
        orderedPair.add(new MapCoordinate(11, 12));
        orderedPair.add(new MapCoordinate(12, 13));
        orderedPair.add(new MapCoordinate(13, 14));
        orderedPair.add(new MapCoordinate(0, 6));
        orderedPair.add(new MapCoordinate(6, 10));
        orderedPair.add(new MapCoordinate(1, 4));
        orderedPair.add(new MapCoordinate(4, 5));
        orderedPair.add(new MapCoordinate(5, 8));
        orderedPair.add(new MapCoordinate(8, 13));
        orderedPair.add(new MapCoordinate(7, 11));
        orderedPair.add(new MapCoordinate(3, 9));
        orderedPair.add(new MapCoordinate(9, 14));
        return orderedPair;
    }

    private void drawLine(GoogleMap googleMap) {
        for (int i = 0; i < orderedPair.size(); i++) {
            int color = determineColor(endDeviceList.get(i).getDensity());
            googleMap.addPolyline(new PolylineOptions().add(registedCoordinates[orderedPair.get(i).getX_dot()], registedCoordinates[orderedPair.get(i).getY_dot()]).width(10).color(color));
        }
    }

    private void updateLine() {
        mGoogleMap.addPolyline(new PolylineOptions().add(registedCoordinates[orderedPair.get(Integer.parseInt(updateDevice.getID()) - 1).getX_dot()], registedCoordinates[orderedPair.get(Integer.parseInt(updateDevice.getID()) - 1).getY_dot()]));
    }

    private int determineColor(int concentration) {
        if (concentration >= 0 && concentration < 30) {
            return Color.BLUE;
        } else if (concentration >= 30 && concentration < 80) {
            return Color.GREEN;
        } else if (concentration >= 80 && concentration < 150) {
            return -100000;
        } else if (concentration >= 150) {
            return Color.RED;
        }

        return -1;
    }


    private void updateNode() {
        firebaseDatabaseRef.child("registedNode").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RegistedNode registedNode = dataSnapshot.getValue(RegistedNode.class);
                registedNodeList.add(registedNode);
                if (registedNode.getID().equals("24")) {
                    onMapReady(mGoogleMap);
                    setRegistCoordinates(registedNodeList.size());
                    setEndDeviceCoordinates(endDeviceList.size());
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

        firebaseDatabaseRef.child("Node").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                EndDevice endDevice = dataSnapshot.getValue(EndDevice.class);
                endDeviceList.add(endDevice);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("Change", "바뀜감지.!!");
                updateDevice =  dataSnapshot.getValue(EndDevice.class);
                onResume();
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