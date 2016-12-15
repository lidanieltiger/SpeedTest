package com.example.android.hackproj;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
        ArrayList<Double[]> listDouble = (ArrayList<Double[]>) getIntent().getSerializableExtra("arraylist");
        ArrayAdapter<Double[]> arrayAdapter = new ArrayAdapter<>( //TODO: modify the array adapter later once I figure out what I want it to show
                this,
                android.R.layout.simple_list_item_1,
                listDouble );
        listview.setAdapter(arrayAdapter);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    public ArrayList<ArrayList<Double[]>> decodePreferences(String day){
        SharedPreferences sharedpreferences = getSharedPreferences(Home.MyPREFERENCES, Context.MODE_PRIVATE);
        ArrayList<ArrayList<Double[]>> daydata = new ArrayList<>();
        int numlogs = sharedpreferences.getInt("numLogsOn:"+day, 0); //number of logs in a given day
        Log.d("numlogs2", "numlogs: "+numlogs);
        for(int i = 0; i <numlogs; i++){ //problem: daydata is full of identical data sets?
            String logdate = day+"index:"+Integer.toString(i); //the key to grab a stringset
            Set <String> set=sharedpreferences.getStringSet(logdate, new HashSet<String>());
            ArrayList<Double[]> temp = new ArrayList<>();
            for (String s: set){ //traverse through the string set
                int num1 = s.indexOf('@');
                int num2 = s.indexOf('#');
                double v1 = Double.parseDouble(s.substring(0,num1));
                double v2 = Double.parseDouble(s.substring(num1+1,num2));
                double v3 = Double.parseDouble(s.substring(num2+1, s.length()));
                temp.add(new Double[]{v1, v2, v3});
            }
            daydata.add(temp);
        }
        //add the elements from set to temp
        return daydata;
    }
    @Override
    public void onMapReady(GoogleMap map) { //TODO: decode the stringset from mypreferences
        mapReady=true;
        m_map=map;
        LatLng newYork = new LatLng(37.328413, -122.058676);
        CameraPosition target = CameraPosition.builder().target(newYork).zoom(14).build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

        //ArrayList<Double[]> listDouble = (ArrayList<Double[]>) getIntent().getSerializableExtra("arraylist"); no longer using this

        Calendar c = Calendar.getInstance();
        int date = c.get(Calendar.DAY_OF_YEAR);
        ArrayList<ArrayList<Double[]>> daydata = decodePreferences(Integer.toString(date));
        Log.d("numlogs","day data size: "+Integer.toString(daydata.size()));
        for(int i = 0; i < daydata.size(); i++){
            Log.d("loop", "loop iteration: "+i);
            ArrayList<Double[]> listDouble = daydata.get(i);
            for (Double[] a: listDouble){
                Log.d("loop", "point in loop: "+a[0]+","+a[1]+","+a[2]);
                LatLng temp = new LatLng(a[1], a[2]);
                map.addMarker(new MarkerOptions()
                        .position(temp)
                        .title(a[0].toString())); //when you press it gives you acceleration in m/s^2
            }
        }
        //draw a line here from the speedlog arraylist from point a to point b, c, etc...
        //get the speed from the first two points of speedlog and the last two points of speedlog
        //that gives you acceleration
    }
}
