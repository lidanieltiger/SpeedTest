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

public class Home extends AppCompatActivity {
    HashMap <String, Collection<Double[]>> driveLogs = new HashMap<>();
    ArrayList <Double[]> temp = new ArrayList<>();
    //date/time, arraylist of arrays (speedlog)
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE); //this contains my data

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
                temp = (ArrayList<Double[]>) data.getSerializableExtra("result");
                //SharedPreferences.Editor editor = sharedpreferences.edit();
                //Set<Double[]> foo = new HashSet<Double[]>(temp);
                //editor.putDoubleSet
                //convert list to hashset
                //TODO: write to sharedpreferences
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_id:
                return true;
            case R.id.log_id:
                Intent intent = new Intent(Home.this, AccelerationLog.class);
                intent.putExtra("arraylist", temp); //TODO: change accelerationlog so that it works with a hashmap
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
