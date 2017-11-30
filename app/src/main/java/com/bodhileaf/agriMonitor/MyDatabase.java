package com.bodhileaf.agriMonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by shwetathareja on 11/5/17.
 */


public class MyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "golden.db";
    private static final int DATABASE_VERSION = 1;
    private static MyDatabase instance;

    public MyDatabase(Context  context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SQLiteDatabase getDatabase() {
        return getWritableDatabase();
    }
}
