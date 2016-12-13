package com.example.android.hackproj;

import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.*;
public class AccelerationLog extends FragmentActivity implements OnMapReadyCallback {
    private LinearLayout mLayout;
    private ListView listview;
    private boolean mapReady = false;
    GoogleMap m_map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceleration_log);
        mLayout = (LinearLayout) findViewById(R.id.logLayout);
        listview = (ListView) findViewById(R.id.speedList);
        ArrayList<Double> listDouble = (ArrayList<Double>) getIntent().getSerializableExtra("arraylist");
        ArrayAdapter<Double> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                listDouble );
        listview.setAdapter(arrayAdapter);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapReady=true;
        m_map=map;
        LatLng newYork = new LatLng(37.328413, -122.058676);
        CameraPosition target = CameraPosition.builder().target(newYork).zoom(14).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

        ArrayList<Double[]> locations = (ArrayList<Double[]>) getIntent().getSerializableExtra("locations");
        for (Double[] a: locations){
            LatLng temp = new LatLng(a[0], a[1]);
            map.addMarker(new MarkerOptions()
                    .position(temp)
                    .title(a[2].toString()));
        }
        //draw a line here from the speedlog arraylist from point a to point b, c, etc...
        //get the speed from the first two points of speedlog and the last two points of speedlog
        //that gives you acceleration
    }
}
