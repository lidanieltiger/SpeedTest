package com.example.android.hackproj;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class DriveSession extends AppCompatActivity implements SensorEventListener{
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    GoogleApiClient mGoogleApiClient;
    int REQUEST_LOCATION =0; //I have no idea what this does..
    double latitude = 0;
    double longitude = 0;
    double GPS_speed = 0;

    private static final String MY_PREFERENCES = "my_preferences"; //used to check if it's the first time opening the app

    private long lastUpdate = 0;
    private static final int UPDATE_THRESHOLD = 0;
    private static final double SAFETY_THRESHOLD = 0.8; //TESTING THIS VALUE...

    public ArrayList<Double[]> speedlog = new ArrayList<>(); //contains a list of the average accelerations
    public ArrayList<Double[]> speedwindow = new ArrayList<>(); //temporary list of acceleration, lat, long

    long prevFailure=0;
    boolean collecting = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//this keeps the screen on
        //TODO: make this app a background process
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    //.addConnectionCallbacks(this)
                    //.addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        TextView isFirst = (TextView)findViewById(R.id.first_launch);
        boolean isFirstTime = DriveSession.isFirst(DriveSession.this);
        if(!isFirstTime){
            isFirst.setText("not first"); //TODO: MAKE A TUTORIAL
        }
        //QUIT BUTTON send data back
        Button quit = (Button) findViewById(R.id.quitbutton);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",speedlog);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
        //start the calibration
        Intent intent = new Intent(DriveSession.this, Calibrate.class);
        startActivityForResult(intent, 1); //GRAB DATA FROM THE CALIBRATION
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DriveSession.this);
        builder1.setMessage("Are you sure you want to quit?");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes, Save this session",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result",speedlog);
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                });
        builder1.setNegativeButton(
                "no, I didn't mean to",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder1.setNeutralButton(
                "Yes, but don't save this session",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                        finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public static boolean isFirst(Context context){ //checks if this is the first time I've opened the app
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        final boolean first = reader.getBoolean("is_first", true);
        if(first){
            final SharedPreferences.Editor editor = reader.edit();
            editor.putBoolean("is_first", false);
            editor.commit();
        }
        return first;
    }
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }
    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    private void updateSpeed(double speed, double x, double y, double z){
        TextView accelerometer = (TextView)findViewById(R.id.AccelDisplay);
        DecimalFormat df = new DecimalFormat("##.##");
        df.setRoundingMode(RoundingMode.DOWN);
        accelerometer.setText("Accelerating at: "+df.format(speed)+" m/s^2"+" x: "+df.format(x)+" y: "+df.format(y)+" z: "+df.format(z));

    }
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        TextView warning = (TextView)findViewById(R.id.isReckless);

        if (mySensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            //calculate speed here
            double speed = Math.sqrt(Math.pow(x, 2)+Math.pow(y,2)+Math.pow((z),2));
            //calculate whether or not to update the number now
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                //TODO: adjust the accuracy of our sensor when speeding so that it takes in more values. also change in acceleration should be within a reasonable limit so we can get rid of outliers
                //TODO: find the speed in the first part of the window, and in the last part of the window
                lastUpdate = curTime;
                if(speed>SAFETY_THRESHOLD){ //speeding
                    prevFailure=lastUpdate;
                    //set the text to say that fast acceleration reported
                    warning.setText("fast acceleration...");
                    collecting = true;
                }else{ //not speeding, so not collecting data, check if there's enough data in speedwindow
                    collecting = false;
                    if(speedwindow.size()>=4){ //500 milliseconds or more
                        /*double totalspeed=0;
                        for(Double[] a: speedwindow){
                            totalspeed+=a[0];
                        }
                        totalspeed/=speedwindow.size(); THIS CODE USES THE ACCELEROMETERS*/
                        double acceleration = (speedwindow.get(speedwindow.size()-1)[3]-speedwindow.get(0)[3]); //gives meters/second^2
                        speedlog.add(new Double[] {acceleration, speedwindow.get(speedwindow.size()-1)[1]
                        , speedwindow.get(speedwindow.size()-1)[2]}); //beginning location/speed, end location/speed
                        //set the text to say that you were speeding
                        warning.setText("you were speeding for "+speedwindow.size()*100+" consecutive milliseconds!");
                    }
                    //clear speedwindow, since we stopped accelerating so there's a break...
                    speedwindow.clear();
                }
                if(collecting){
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) //GET LOCATION DATA
                            != PackageManager.PERMISSION_GRANTED) {
                        // Check Permissions Now
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION);
                    } else {
                        // permission has been granted, continue as usual
                        Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                        latitude = myLocation.getLatitude();
                        longitude = myLocation.getLongitude();
                        GPS_speed = myLocation.getSpeed();
                    }
                    if(lastUpdate-prevFailure<200){//consecutive data collection, so add member
                        speedwindow.add(new Double[] {speed, latitude, longitude, GPS_speed} );
                    }else{//you've just started speeding so clear array and start over repopulating it
                        speedwindow.clear();
                        speedwindow.add(new Double[] {speed, latitude, longitude, GPS_speed} );
                    }
                }
                updateSpeed(speed, x, y, z);
                //compile data for acceleration log
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}