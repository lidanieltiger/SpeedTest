package com.example.android.hackproj;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class displaydata extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap m_map;
    private boolean mapReady = false;
    private ArrayList<Double[]> data;
    SharedPreferences sharedpreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displaydata);
        sharedpreferences = getSharedPreferences(Home.MyPREFERENCES, Context.MODE_PRIVATE);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        data = (ArrayList<Double[]>) getIntent().getSerializableExtra("log");
    }
    @Override
    public void onMapReady(GoogleMap map) { //TODO: decode the stringset from mypreferences
        mapReady=true;
        m_map=map;
        LatLng newYork = new LatLng(34.073276, -118.452396);
        CameraPosition target = CameraPosition.builder().target(newYork).zoom(14).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

        for (Double[] a: data){
            LatLng temp = new LatLng(a[1], a[2]);
            map.addMarker(new MarkerOptions()
                    .position(temp)
                    .title(a[0].toString())); //when you press it gives you acceleration in m/s^2
        }
        //draw a line here from the speedlog arraylist from point a to point b, c, etc...
        //get the speed from the first two points of speedlog and the last two points of speedlog
        //that gives you acceleration
    }

}
