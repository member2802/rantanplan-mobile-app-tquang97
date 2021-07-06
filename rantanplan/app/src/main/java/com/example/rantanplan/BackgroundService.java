package com.example.rantanplan;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import android.os.Handler;
import android.os.IBinder;

import android.provider.BaseColumns;
import android.util.Log;

import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;

import java.util.List;


import static com.example.rantanplan.DatabaseHelper.Database.TABLE_NAME;


public class BackgroundService extends Service implements SensorEventListener{
    long tStart = new Date().getTime();
    public SensorManager sensorManager;
    public Sensor mLight;

    private long now = 0;
    private long timeDiff = 0;
    private long lastUpdate = 0;
    private long lastShake = 0;

    public float x = 0;
    public float y = 0;
    public float z = 0;
    public float lastX = 0;
    public float lastY = 0;
    public float lastZ = 0;

    private float force = 0;

    public float threshold= 0.5f;
    public int interval = 1;
    public Boolean onMove=false;

    public WifiManager wifiManager;
    public List<ScanResult> results;
    public ArrayList<String> arrayList = new ArrayList<>();
    public int MAX_SCAN_ROUND = 0;


    public Handler handler = new Handler();
    public Handler mHandler = new Handler();

    public WifiResults wifi = new WifiResults();
    FileOutputStream outputStream;

    String filename = "JSON_file.json";

    IntentFilter filter = new IntentFilter("seekBar");

    DatabaseHelper databaseHelper = new DatabaseHelper(this);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("Service Started");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        System.out.println("DB ");
        readDB();
        return START_STICKY;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
            // use the event timestamp as reference
            // so the manager precision won't depends
            // on the AccelerometerListener implementation
            // processing time
        now = event.timestamp;

        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

//        Intent intent = new getIntent();
//        int seekBar1 = intent.getIntExtra("sendSeekBar1", 0);
//        System.out.println("seekBar1: "+seekBar1);
        if (lastUpdate == 0) {
            lastUpdate = now;
            lastShake = now;
            lastX = x;
            lastY = y;
            lastZ = z;
            System.out.println("No Motion detected1");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.out.println("scanning1");
                    mScanWifi.run();
                }
            }, (10*1000));
        } else {
            timeDiff = now - lastUpdate;
            if (timeDiff > 0) {
                force = Math.abs(x + y + z - lastX - lastY - lastZ);
                if (Float.compare(force, threshold) > 0) {
                    if (now - lastShake >= interval) {
                        // trigger shake event
                        System.out.println("Motion detected");

                    } else {
                        System.out.println("No Motion detected2");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("scanning2");
                                mScanWifi.run();
                            }
                        }, (10*1000));
                    }
                    lastShake = now;
                }
                lastX = x;
                lastY = y;
                lastZ = z;
                lastUpdate = now;
            } else {
                System.out.println("No Motion detected3");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("scanning3");
                        mScanWifi.run();
                    }
                }, (10*1000));
            }
        }
// trigger change event
    }

    public void scanWifi() {
        if (!onMove) {
            arrayList.clear();
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
            Toast.makeText(this, "Scanning WiFi ... ", Toast.LENGTH_SHORT).show();
            MAX_SCAN_ROUND++;
        } else {
            arrayList.clear();
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            MAX_SCAN_ROUND=0;
        }
    }

    public Runnable mScanWifi = new Runnable() {
        @Override
        public void run() {
            Intent intent  = new Intent("seekBar");
            int seekBar2 = intent.getIntExtra("sendSeekBar2", 0);
            if (MAX_SCAN_ROUND < 5) {
                System.out.println("seekBar2: "+seekBar2);
                wifi.start_time  = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(tStart);
                scanWifi();
                mHandler.postDelayed(this, 5000);
            }   else {
                long tEnd = new Date().getTime();
                wifi.end_time = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(tEnd);
                wifi.round_count = MAX_SCAN_ROUND;
                createJsonFile(wifi);

                Intent activity = new Intent(BackgroundService.this, SecondActivity.class);
                activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activity);
            }
        }
    };



    @Override
    public void sendBroadcast(Intent intent) {
        super.sendBroadcast(intent);
        String message1 = intent.getStringExtra("name");
        String message2 = intent.getStringExtra("address");
        intent = new Intent(BackgroundService.this, DisplayMessageActivity.class);
        intent.putExtra("name_1", message1);
        intent.putExtra("address_1", message2);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(wifiReceiver);
            int end_count = 0;

            int seekBar3 = intent.getIntExtra("seekBar3", 0);
            System.out.println("seekBar3: "+seekBar3);
            // displaying the occurrence of elements in the arraylist
            for (ScanResult scanResult : results) {
                arrayList.add(scanResult.BSSID);
                ArrayList<String> arrayList_new = new ArrayList<String>();
                arrayList_new.add(scanResult.BSSID);
                System.out.println("result wifi_old: "+ arrayList);
                System.out.println("result wifi_new: "+ arrayList_new);
                //end_count condition
                if (arrayList_new.equals(arrayList)){
                    System.out.println("same list");
                    end_count++;
                    if (end_count >= 2) {
                        System.out.println("renew scan");
                        arrayList.clear();
                        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        MAX_SCAN_ROUND=0;
                    }
                } else {
                    System.out.println("new list");
                    arrayList = arrayList_new;
                }
                Signals signals = new Signals();
                if (MAX_SCAN_ROUND == 5) {
                    signals.bssid = scanResult.BSSID;
                    signals.ssid = scanResult.SSID;
                    signals.frequency = scanResult.frequency;
                    signals.signal_level = scanResult.level;
                    signals.sample_count = MAX_SCAN_ROUND;
                    wifi.signals.add(signals);
                }
                String title = scanResult.BSSID;
                String subtitle = scanResult.SSID;
                // Gets the data repository in write mode
                SQLiteDatabase db = databaseHelper.getWritableDatabase();

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();

                values.put(DatabaseHelper.Database.COLUMN_NAME_TITLE, title);
                System.out.println("newtitle " + title);
                values.put(DatabaseHelper.Database.COLUMN_NAME_SUBTITLE, subtitle);
                System.out.println("newsubtitle " + subtitle);
                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(TABLE_NAME, null, values);
                System.out.println("newRowId " + newRowId);
            }


        };
    };

    public void saveDatabase() {
        // Gets the data repository in write mode
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String title = "Signals";
        String subtitle = "Subtitle";
// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.Database.COLUMN_NAME_TITLE, title);
        values.put(DatabaseHelper.Database.COLUMN_NAME_SUBTITLE, subtitle);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_NAME, null, values);
    }

    public void readDB() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                DatabaseHelper.Database.COLUMN_NAME_TITLE,
                DatabaseHelper.Database.COLUMN_NAME_SUBTITLE
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = DatabaseHelper.Database.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { "title" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.Database.COLUMN_NAME_SUBTITLE + " DESC";


        Cursor cursor = db.query(
                TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        List itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.Database._ID));
            itemIds.add(itemId);
        }
        cursor.close();
        System.out.println("cursor" +cursor);
    }
    public void updateDB() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

// New value for one column
        String title = "MyNewTitle";
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.Database.COLUMN_NAME_TITLE, title);

// Which row to update, based on the title
        String selection = DatabaseHelper.Database.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { "MyOldTitle" };

        int count = db.update(
                TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    public void createJsonFile(WifiResults wifi) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        String wifi_json = gson.toJson(wifi);
        System.out.println(wifi_json);
        saveJSONFile(wifi_json);

//        readJSONFile(new File(filename));
    }

    public void saveJSONFile(String string) {
        // Save json file
        try {
            String yourFilePath = this.getFilesDir() + "/" + filename;
            System.out.println("WHERE IS IT:"+yourFilePath);
            File yourFile = new File(yourFilePath);
            System.out.println("WHAT IS FILE:"+yourFile);
            FileOutputStream outputStream = new FileOutputStream(yourFile);
//            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            System.out.println("Success Saved JSON FILE");
            outputStream.flush();
            outputStream.close();

            //make sure this is in a try catch statement
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readJSONFile(File filename) {
        Gson gson = new Gson();
        String text = "";
//Make sure to use a try-catch statement to catch any errors
        try {
            //Make your FilePath and File
            String yourFilePath = this.getFilesDir() + "/" + filename;
            File yourFile = new File(yourFilePath);
            //Make an InputStream with your File in the constructor
            InputStream inputStream = new FileInputStream(yourFile);
            StringBuilder stringBuilder = new StringBuilder();
            //Check to see if your inputStream is null
            //If it isn't use the inputStream to make a InputStreamReader
            //Use that to make a BufferedReader
            //Also create an empty String
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                //Use a while loop to append the lines from the Buffered reader
                while ((receiveString = bufferedReader.readLine()) != null){
                    stringBuilder.append(receiveString);
                }
                //Close your InputStream and save stringBuilder as a String
                inputStream.close();
                text = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            //Log your error with Log.e
            Log.e("e", "FILEERNOTRFOUNDOEOEOEOROER");
        } catch (IOException e) {
            //Log your error with Log.e
            Log.e("e", "FILEEXRNIOEXCEPTIONTRFOUNDOEOEOEOROER");
        }
        //Use Gson to recreate your Object from the text String
        System.out.println(text);
    }
}
