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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.*;

import static java.util.Calendar.DAY_OF_YEAR;

public class Home extends AppCompatActivity {
    ArrayList <Double[]> driveLog = new ArrayList<>();
    //date/time, arraylist of arrays (speedlog)
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "ACCELERATION_DATA" ;
    public String DAY_OF_YEAR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE); //this contains my data

        //TESTING BELOW... TODO: remove this eventually
        SharedPreferences preferences = getSharedPreferences(MyPREFERENCES, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        //CLEAR MYPREFERENCES FOR TESTING PURPOSES

        //setting the number of logs recorded today on startup
        Calendar c = Calendar.getInstance();
        DAY_OF_YEAR= Integer.toString(c.get(Calendar.DAY_OF_YEAR));
        TextView intro = (TextView)findViewById(R.id.introduction);
        int numlogs = sharedpreferences.getInt("numLogsOn:"+DAY_OF_YEAR, 0); //number of logs in a given day
        intro.setText("\t Welcome, Driver \n \n \t Sessions Recorded Today: "+numlogs);

        //init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button quit = (Button) findViewById(R.id.startbutton);
        quit.setOnClickListener(new View.OnClickListener() {
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
        TextView intro = (TextView)findViewById(R.id.introduction);
        SharedPreferences sharedpreferences = getSharedPreferences(Home.MyPREFERENCES, Context.MODE_PRIVATE);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                //saving the logs to sharedpreferences via savelist
                driveLog = (ArrayList<Double[]>) data.getSerializableExtra("result");
                saveList(driveLog);

                //changing the message for number of sessions recorded
                int numlogs = sharedpreferences.getInt("numLogsOn:"+DAY_OF_YEAR, 0); //number of logs in a given day
                intro.setText("\t Welcome, Driver \n \n \t Sessions Recorded Today: "+numlogs);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //nothing happens!
            }
        }
    }
    public void saveList (ArrayList<Double[]> logs){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Set<String> set = new HashSet<>();
        for(Double[] a : logs){
            Log.d("stringloop", "iterate1");
            set.add(a[0].toString()+"@"+a[1].toString()+"#"+a[2].toString());
            //NOTE: a lot of the time there are duplicate points that are being overriden here. need to tweak my algorithm so this happens
            //LESS OFTEN...
        }
        int numlogs = sharedpreferences.getInt("numLogsOn:"+DAY_OF_YEAR, 0);
        String logdate = DAY_OF_YEAR+"index:"+Integer.toString(numlogs); //(day)index:0,1,2...
        editor.putStringSet(logdate, set);
        editor.putInt("numLogsOn:"+DAY_OF_YEAR, ++numlogs);
        editor.commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_id:
                Intent intent = new Intent(Home.this, AccelerationLog.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
