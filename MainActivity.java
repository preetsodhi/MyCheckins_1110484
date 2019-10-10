package com.example.mycheckins;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = openOrCreateDatabase("mycheckins_db", android.content.Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Receipts (id INTEGER PRIMARY KEY, title TEXT, place TEXT, details TEXT, date TEXT, longitude TEXT, latitude TEXT, image BLOB)");
        setContentView(R.layout.activity_main);
    }
}
