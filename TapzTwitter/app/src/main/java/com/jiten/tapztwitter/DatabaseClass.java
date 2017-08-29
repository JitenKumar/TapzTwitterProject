package com.example.jiten.tapztwitter;

/**
 * Created by jiten on 25/8/17.
 */
import twitter4j.Status;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseClass extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "home.db";
    private static final String TABLE_TEST = "JitenData";

    private static final String DATABASE_CREATE = "create table if not exists" + TABLE_TEST +
            "(ID NUMBER PRIMARY KEY," +
            "userid TEXT," +
            "tweetid TEXT," +
            "url TEXT," +
            "timestamp Date," +
            "unique(userid,tweetid)";

//    CREATE TABLE " + TABLE_TEST + "("
//            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
//            + KEY_AGE + " TEXT" + ")";

    public DatabaseClass(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS home");
        db.execSQL("VACUUM");
        onCreate(db);
    }
}