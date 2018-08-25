package com.example.leechungwan.testversion;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
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
    private Polyline polyline;
    private List<Polyline> polylines = new ArrayList<>();

    private AppCompatActivity mActivity;
    private GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        registedNodeList = new ArrayList<>();
        endDeviceList = new ArrayList<>();
        orderedPair = makeOrderedPair();

        buildGoogleApiClient();

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 데이터베이스 Instance 생성
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabaseRef = firebaseDatabase.getReference();
        updateNode();
    }

    protected synchronized void buildGoogleApiClient(){
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setStartCoordinate();
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() == false) {

            Log.d("onStart: ", "onStart: mGoogleApiClient connect");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleMap != null) {
            updateLine();
        }
    }

    private void initNode(GoogleMap googleMap) {
        //addMarker();
        drawLine();
        lineClickListener();
    }

    private void setStartCoordinate() {
        LatLng startLocation = new LatLng(36.369906, 127.345907);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(startLocation, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    private void addMarker() {
        // 미세먼저 측정 장비 위치 Node PIN 찍기
//        for (int i = 0; i < endDeviceCoordinates.length; i++) {
//            MarkerOptions node = new MarkerOptions();
//            node.position(endDeviceCoordinates[i])
//                    .title(endDeviceList.get(i).getID());
//            mGoogleMap.addMarker(node);
//        }

        for (int idx = 0; idx < registedNodeList.size(); idx++) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(registedCoordinates[idx])
                    //.title(registedNodeList.get(idx).getID())
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
        orderedPair.add(new MapCoordinate(0, 1));   // Node 0
        orderedPair.add(new MapCoordinate(1, 2));   // Node 1
        orderedPair.add(new MapCoordinate(2, 3));   // Node 2
        orderedPair.add(new MapCoordinate(6, 7));   // Node 3
        orderedPair.add(new MapCoordinate(7, 8));   // Node 4
        orderedPair.add(new MapCoordinate(8, 9));   // Node 5
        orderedPair.add(new MapCoordinate(10, 11)); // Node 6
        orderedPair.add(new MapCoordinate(11, 12)); // Node 7
        orderedPair.add(new MapCoordinate(12, 13)); // Node 8
        orderedPair.add(new MapCoordinate(13, 14)); // Node 9
        orderedPair.add(new MapCoordinate(0, 6));   // Node 11
        orderedPair.add(new MapCoordinate(6, 10));  // Node 10
        orderedPair.add(new MapCoordinate(1, 4));   // Node 15
        orderedPair.add(new MapCoordinate(4, 5));   // Node 14
        orderedPair.add(new MapCoordinate(5, 8));   // Node 13
        orderedPair.add(new MapCoordinate(8, 13));  // Node 12
        orderedPair.add(new MapCoordinate(7, 11));  // Node 16
        orderedPair.add(new MapCoordinate(3, 9));   // Node 18
        orderedPair.add(new MapCoordinate(9, 14));  // Node 17
        return orderedPair;
    }

    private void drawLine() {
        for (int i = 0; i < orderedPair.size(); i++) {
            int color = determineColor(endDeviceList.get(i).getDensity());
            LatLng x_dot = registedCoordinates[orderedPair.get(i).getX_dot()];
            LatLng y_dot = registedCoordinates[orderedPair.get(i).getY_dot()];
            polyline = mGoogleMap.addPolyline(new PolylineOptions().add(x_dot, y_dot).width(10).color(color));
            polyline.setClickable(true);
            polylines.add(polyline);
        }
    }

    private void updateLine() {
        int color = determineColor(updateDevice.getDensity());
        int id = Integer.parseInt(updateDevice.getID());

        LatLng x_dot = registedCoordinates[orderedPair.get(id).getX_dot()];
        LatLng y_dot = registedCoordinates[orderedPair.get(id).getY_dot()];
        PolylineOptions polylineOptions = new PolylineOptions().add(x_dot, y_dot).width(10).color(color);
        polylines.get(id).remove();
        polylines.remove(id);
        polyline = mGoogleMap.addPolyline(polylineOptions);
        polyline.setClickable(true);
        polylines.add(id, polyline);
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

    private void lineClickListener() {
        mGoogleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                for (int i = 0; i < polylines.size(); i++) {
                    Polyline line = polylines.get(i);
                    if (line.equals(polyline)) {
                        String msg = "미세먼지: " + endDeviceList.get(i).getDensity() + "\n동기화시간: " + endDeviceList.get(i).getTime();
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void updateNode() {
        firebaseDatabaseRef.child("registedNode").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RegistedNode registedNode = dataSnapshot.getValue(RegistedNode.class);
                registedNodeList.add(registedNode);
                if (registedNode.getID().equals("14")) {
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
                updateDevice = dataSnapshot.getValue(EndDevice.class);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLocation);
        mGoogleMap.moveCamera(cameraUpdate);
    }
}