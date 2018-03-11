package com.example.dries.project;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "myHerinneringDB";

    // Herinnering table name
    private static final String TABLE_FRIEND = "herinnering";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_JOB = "description";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LONG = "longtitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FRIEND_TABLE = "CREATE TABLE " + TABLE_FRIEND + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_JOB + " TEXT," + KEY_LAT + " TEXT," + "longtitude" + " TEXT" + " )";
        db.execSQL(CREATE_FRIEND_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIEND);

        // Create tables again
        onCreate(db);
    }

    // Adding a new record (herinnering) to table
    public long addNewHerinnering(Herinnering herinnering) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, herinnering.getName());
        values.put(KEY_JOB, herinnering.getDescription());
        values.put(KEY_LAT, herinnering.getCoordlat());
        values.put(KEY_LONG, herinnering.getCoordlong());

        // inserting this record
        long id = db.insert(TABLE_FRIEND, null, values);
        db.close(); // Closing database connection
        return id;
    }

    // Getting All Herinnerings in Table of Database
    public List<Herinnering> getAllHerinnerings() {
        List<Herinnering> herinneringList = new ArrayList<>();

        // select query
        String selectQuery = "SELECT  * FROM " + TABLE_FRIEND;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all table records and adding to list
        if (cursor.moveToFirst()) {
            do {
                Herinnering herinnering = new Herinnering();
                herinnering.setId(Integer.parseInt(cursor.getString(0)));
                herinnering.setName(cursor.getString(1));

                herinnering.setDescription(cursor.getString(2));
                herinnering.setCoordlat(cursor.getString(3));
                herinnering.setCoordlong(cursor.getString(4));

                // Adding herinnering to list
                herinneringList.add(herinnering);
            } while (cursor.moveToNext());
        }

        return herinneringList;
    }

    // Updating a record in database table
    public int updateHerinnering(Herinnering herinnering) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, herinnering.getName());
        values.put(KEY_JOB, herinnering.getDescription());
        values.put(KEY_LAT, herinnering.getCoordlat());
        values.put(KEY_LONG, herinnering.getCoordlong());

        // updating row
        return db.update(TABLE_FRIEND, values, KEY_ID + " = ?", new String[]{String.valueOf(herinnering.getId())});
    }

    // Deleting a record in database table
    public void deleteHerinnering(Herinnering herinnering) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FRIEND, KEY_ID + " = ?", new String[]{String.valueOf(herinnering.getId())});
        db.close();
    }

    // getting number of records in table
    public int getContactsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor dataCount = db.rawQuery("select " + KEY_ID + " from " + TABLE_FRIEND, null);

        int count = dataCount.getCount();
        dataCount.close();
        db.close();

        return count;
    }
}