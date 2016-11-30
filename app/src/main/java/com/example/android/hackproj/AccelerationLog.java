package com.example.android.hackproj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.*;
public class AccelerationLog extends AppCompatActivity {
    private RelativeLayout mLayout;
    private ListView listview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceleration_log);
        mLayout = (RelativeLayout) findViewById(R.id.logLayout);
        listview = (ListView) findViewById(R.id.speedList);
        ArrayList<Double> listDouble = (ArrayList<Double>) getIntent().getSerializableExtra("arraylist");
        ArrayAdapter<Double> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                listDouble );
        listview.setAdapter(arrayAdapter);
    }
}
