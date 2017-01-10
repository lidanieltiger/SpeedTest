package com.example.android.hackproj;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
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
public class AccelerationLog extends AppCompatActivity { //changed from extends fragment activity + implements OnMapReadyCallback
    private LinearLayout mLayout;
    private ListView listview;

    //variables used in the class
    public String DAY_OF_YEAR;
    SharedPreferences sharedpreferences;
    ArrayList<ArrayList<Double[]>> logList;

    //listview elements
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;
    private int numlistlogs = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceleration_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.log_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //getting rid of the title
        //toolbar.setNavigationIcon(R.drawable.ic_toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("");
        //toolbar.setLogo(R.drawable.ic_toolbar);


        mLayout = (LinearLayout) findViewById(R.id.logLayout);
        listview = (ListView) findViewById(R.id.speedList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listItems );
        listview.setAdapter(arrayAdapter);

        //access sharedpreferences
        sharedpreferences = getSharedPreferences(Home.MyPREFERENCES, Context.MODE_PRIVATE);

        //init day of the year to be the current day
        Calendar c = Calendar.getInstance();
        DAY_OF_YEAR= Integer.toString(c.get(Calendar.DAY_OF_YEAR));
        logList = decodePreferences(DAY_OF_YEAR);                       //set logList (the list of logs for the day) to the current day of year
                                                                        //TODO: refresh DAY_OF_YEAR and logList based on different days
        for(int i = 0; i < logList.size(); i++){                        //create the listview based on number of logs for a given day  TODO: refresh it based on different days
            arrayAdapter.add("log: "+numlistlogs);
            numlistlogs++;
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                Log.d("list", Long.toString(id));
                ArrayList<Double[]> sendData = logList.get((int)id);                //get the correct log data
                Intent intent = new Intent(AccelerationLog.this, displaydata.class);//start a new intent and send them the data for the day
                intent.putExtra("log", sendData);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){ //stuff for the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_log, menu);
        return true;
    }
    public ArrayList<ArrayList<Double[]>> decodePreferences(String day){
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
}
