package com.example.android.hackproj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorEventListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    GoogleApiClient mGoogleApiClient;
    int REQUEST_LOCATION =0; //I have no idea what this does..
    double latitude = 0;
    double longitude = 0;

    private static final String MY_PREFERENCES = "my_preferences"; //used to check if it's the first time opening the app

    private long lastUpdate = 0;
    private static final int UPDATE_THRESHOLD = 0;
    private static final double SAFETY_THRESHOLD = 0.5; //TESTING THIS VALUE...

    public ArrayList<Double> speedlog = new ArrayList<>(); //contains a list of the average accelerations
    public ArrayList<Double> speedwindow = new ArrayList<>(); //temporary list of acceleration
    public ArrayList<Double[]> locations = new ArrayList<>();//contains the latitudes and longitudes of the speeding locations

    long prevFailure=0;
    boolean collecting = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        boolean isFirstTime = MainActivity.isFirst(MainActivity.this);
        if(!isFirstTime){
            isFirst.setText("not first"); //TODO: SET CALIBRATION
        }
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
                intent.putExtra("arraylist", speedlog);
                intent.putExtra("locations", locations);
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
                        double totalspeed=0;
                        for(double a: speedwindow){
                            totalspeed+=a;
                        }
                        totalspeed/=speedwindow.size();
                        speedlog.add(totalspeed);
                        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Check Permissions Now
                            ActivityCompat.requestPermissions(this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION);
                        } else {
                            // permission has been granted, continue as usual
                            Location myLocation =
                                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            latitude = myLocation.getLatitude();
                            longitude = myLocation.getLongitude();
                        }
                        locations.add(new Double[]{latitude, longitude, totalspeed});
                        //set the text to say that you were speeding
                        warning.setText("you were speeding for "+speedwindow.size()*100+" consecutive milliseconds!");
                    }
                    //clear speedwindow, since we stopped accelerating so there's a break...
                    speedwindow.clear();
                }
                if(collecting){
                    if(lastUpdate-prevFailure<200){//consecutive data collection, so add member
                        speedwindow.add(speed);
                        //TODO: add location too
                    }else{//you've just started speeding so clear array and start over repopulating it
                        speedwindow.clear();
                        speedwindow.add(speed);
                        //TODO: add location too
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