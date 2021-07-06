package com.example.rantanplan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;


class WifiResults {
    public String start_time;
    public String end_time;
    public int round_count;
    public List<Signals> signals = new ArrayList<>();
}

class Signals {
    public String signal_id;
    public String bssid;
    public String ssid;
    public int frequency;
    public int signal_level;
    public int sample_count;
}

