package com.example.android.hackproj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AccelerationLog extends AppCompatActivity {
    private RelativeLayout mLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceleration_log);
        mLayout = (RelativeLayout) findViewById(R.id.logLayout);
    }
    public void accelerationLog(double time){
        mLayout.addView(createNewTextView(Double.toString(time)));
    }
    private TextView createNewTextView(String text) {
        final TextView textView = new TextView(this);
        textView.setText(text);
        return textView;
    }
}
