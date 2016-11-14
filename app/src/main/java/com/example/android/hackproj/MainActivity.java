package com.example.android.hackproj;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*; //for arraylists

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int UPDATE_THRESHOLD = 0;
    private static final int SAFETY_THRESHOLD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void getRandomNumber(double speed) {
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.DOWN);
        TextView text = (TextView)findViewById(R.id.Speed);
        text.setText("Accelerating at: "+df.format(speed)+" m/s^2");
    }
    private void unsafeDriving(Boolean safe){
        TextView text = (TextView)findViewById(R.id.isReckless);
        if(safe){
            text.setText("OK");
        }else
            text.setText("Driving Unsafely!");
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        Boolean safeDriving = true;

        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            double speed = Math.sqrt(Math.pow(x, 2)+Math.pow(y,2)+Math.pow(z,2))-10;

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 50) {
                lastUpdate = curTime;

                if (speed > UPDATE_THRESHOLD) {
                    getRandomNumber(speed);
                    if(speed>SAFETY_THRESHOLD){
                        safeDriving=false;
                    }else
                        safeDriving=true;

                    unsafeDriving(safeDriving);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
