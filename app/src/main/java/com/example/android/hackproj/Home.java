package com.example.android.hackproj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.*;

import static java.util.Calendar.DAY_OF_YEAR;

public class Home extends AppCompatActivity {
    ArrayList <Double[]> driveLog = new ArrayList<>();
    //date/time, arraylist of arrays (speedlog)
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "ACCELERATION_DATA" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE); //this contains my data

        SharedPreferences preferences = getSharedPreferences(MyPREFERENCES, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        //CLEAR MYPREFERENCES FOR TESTING PURPOSES

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, DriveSession.class);
                startActivityForResult(intent, 1); //GRAB DATA FROM THE DRIVE SESSION
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){ //stuff for the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                driveLog = (ArrayList<Double[]>) data.getSerializableExtra("result");
                saveList(driveLog);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //nothing happens!
            }
        }
    }
    public void saveList (ArrayList<Double[]> logs){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Set<String> set = new HashSet<String>();
        for(Double[] a : logs){
            set.add(a[0].toString()+"-"+a[1].toString()+"+"+a[2].toString()); //change this to char arrays[4] so that i don't have to do toString, that'll make it faster
        }
        Calendar c = Calendar.getInstance();
        String day = Integer.toString(c.get(Calendar.DAY_OF_YEAR));
        int numlogs = sharedpreferences.getInt("numLogsOn:"+day, 0);
        String logdate = day+"index:"+Integer.toString(numlogs); //(day)index:0,1,2...
        editor.putStringSet(logdate, set);
        editor.putInt("numLogsOn:"+day, ++numlogs);
        editor.commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_id:
                return true;
            case R.id.log_id:
                Intent intent = new Intent(Home.this, AccelerationLog.class);
                intent.putExtra("arraylist", driveLog);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
