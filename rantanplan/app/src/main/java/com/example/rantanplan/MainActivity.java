package com.example.rantanplan;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;

import android.content.pm.PackageManager;


import android.os.Bundle;

import android.util.Log;

import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity{
    public static final String BROADCAST_SEEKBAR = "com.gm.sendseekbar";

    public SeekBar seekBar1;
    public SeekBar seekBar2;
    public SeekBar seekBar3;

    public TextView textView1;
    public TextView textView2;
    public TextView textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
        textView1 = (TextView) findViewById(R.id.textView1);
        seekBar1.setMax(30);
        seekBar1.setProgress(1);
        textView1.setText("Minimum idle motion duration: " + seekBar1.getProgress() + " minutes");

        seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        textView2 = (TextView) findViewById(R.id.textView2);
        final int val = 1000;
        seekBar2.setMax(val);
        seekBar2.setProgress(5);
        textView2.setText("Maximum scan round: " + seekBar2.getProgress());

        seekBar3 = (SeekBar) findViewById(R.id.seekBar3);
        textView3 = (TextView) findViewById(R.id.textView3);
        seekBar3.setMax(10);
        seekBar3.setProgress(5);
        textView3.setText("Empty-handed scan rounds: " + seekBar3.getProgress());


        startService(new Intent(MainActivity.this, BackgroundService.class));

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;
            ActivityCompat.requestPermissions(
                        MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
        }

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress1 = 0;

            // When Progress value changed.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress1 = progressValue;
                textView1.setText("Minimum idle motion duration: " + progress1 + " minutes");
                Log.d("log", "Changing seekbar1's progress");
                Intent seekBar_intent = new Intent(MainActivity.this, BackgroundService.class);
                seekBar_intent.putExtra("sendSeekBar1", progress1);
                seekBar_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(seekBar_intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress2 = 0;

            // When Progress value changed.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress2 = progressValue;
                textView2.setText("Maximum scan round: " + progress2);
                Intent seekIntent2 = new Intent("seekBar");
                seekIntent2.putExtra("sendSeekBar2", progress2);
                sendBroadcast(seekIntent2);
                Log.d("log", "Changing seekbar2's progress");
            }

            // Notification that the user has started a touch gesture.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("log", "Tracking seekbar2's progress");
            }

            // Notification that the user has finished a touch gesture
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar2.getProgress()==1000) {
                    textView2.setText("Maximum scan round: infinities");
                } else {
                    textView2.setText("Maximum scan round: " + progress2);
                }
                Log.d("log", "Stopped tracking seekbar");

            }
        });

        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress3 = 0;

            // When Progress value changed.
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress3 = progressValue;
                textView3.setText("Empty-handed scan rounds: " + progress3);
                Intent seekIntent3 = new Intent("seekBar");
                seekIntent3.putExtra("sendSeekBar3", progress3);
                sendBroadcast(seekIntent3);
                Log.d("log", "Changing seekbar's progress");
            }

            // Notification that the user has started a touch gesture.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("log", "Tracking seekbar's progress");
            }

            // Notification that the user has finished a touch gesture
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView3.setText("Empty-handed scan rounds: " + progress3);
                Log.d("log", "Stopped tracking seekbar");

            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "App Stopped", Toast.LENGTH_SHORT).show();
    }

}

