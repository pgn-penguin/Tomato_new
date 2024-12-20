// FocusRecordManager.java
package com.example.tomato;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class FocusRecordManager {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public FocusRecordManager(Context context) {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    public void insertFocusRecord(String date, int hours, int minutes, int seconds, String purpose) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DATE, date);
        values.put(DatabaseHelper.COLUMN_HOURS, hours);
        values.put(DatabaseHelper.COLUMN_MINUTES, minutes);
        values.put(DatabaseHelper.COLUMN_SECONDS, seconds);
        values.put(DatabaseHelper.COLUMN_PURPOSE, purpose); // Include purpose
        database.insert(DatabaseHelper.TABLE_NAME, null, values);
    }

    public void close() {
        dbHelper.close();
    }
}