package com.example.android.hackproj;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*; //for arraylists

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int UPDATE_THRESHOLD = 0;
    private static final int SAFETY_THRESHOLD = 4;

    public ArrayList<Double> times = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){ //stuff for the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_id:
                return true;
            case R.id.log_id:
                Intent intent = new Intent(MainActivity.this, AccelerationLog.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void unsafeDriving(double speed, double x, double y, double z){
        TextView speedDisplay = (TextView)findViewById(R.id.isReckless);
        TextView accelerometer = (TextView)findViewById(R.id.AccelDisplay);
        if(speed<SAFETY_THRESHOLD){
            speedDisplay.setText("OK");
        }else{
            speedDisplay.setText("Driving Unsafely!");
        }
        //SEND ACCELERATION LOG SOME DATA NOW
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.DOWN);
        accelerometer.setText("Accelerating at: "+df.format(speed)+" m/s^2"+" x: "+df.format(x)+" y: "+df.format(y)+" z: "+df.format(z));

    }
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            //calculate speed here
            double speed = Math.sqrt(Math.pow(x, 2)+Math.pow(y,2)+Math.pow((z),2));
            //calculate whether or not to update the number now
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                lastUpdate = curTime;
                unsafeDriving(speed, x, y, z);
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
