package com.bodhileaf.agriMonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Arvind Vanam on 11/5/17.
 */


public class MyDatabase {
    private static final String TAG = MyDatabase.class.getSimpleName() ;
    private String databasePath;
    private String assetPath;
    private String databasePathStats;
    private String assetPathStats;
    private Context mContext;
    private static final String DATABASE_NAME = "golden.db";
    private static final String DATABASE_NAME_STATS = "golden_stat.db";
    private static final String ASSET_DB_PATH = "databases";
    private static MyDatabase instance;
    private SQLiteDatabase goldenDb=null;
    private SQLiteDatabase goldenStatsDb=null;

    public MyDatabase(Context  context) {
        assetPath = ASSET_DB_PATH + "/" + DATABASE_NAME;
        assetPathStats = ASSET_DB_PATH + "/" + DATABASE_NAME_STATS;
        databasePath = context.getApplicationInfo().dataDir + "/databases";
        mContext = context;


//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SQLiteDatabase getDatabase() {
        InputStream inputFile = null;
        InputStream inputFileStats = null;
        if (goldenDb != null && goldenDb.isOpen()) {
            return goldenDb;  // The database is already open
        }


        File outFile = new File (databasePath + "/" + DATABASE_NAME);
        File outFileStats = new File (databasePath + "/" + DATABASE_NAME_STATS);
        //Check if file already exists in local folder
        if (outFile.exists()) {
            try {
                goldenDb = SQLiteDatabase.openDatabase(databasePath+"/"+DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
                goldenStatsDb = SQLiteDatabase.openDatabase(databasePath+"/"+DATABASE_NAME_STATS, null, SQLiteDatabase.OPEN_READONLY);
                Log.i(TAG, "successfully opened database " + DATABASE_NAME);
                return goldenDb;
            } catch (SQLiteException e) {
                Log.w(TAG, "could not open database " + outFile + " - " + e.getMessage());
                return null;
            }
        }

        //create output directory in app folder if doesnt exist
        File f = new File(databasePath + "/");
        if (!f.exists()) { f.mkdir(); }

        //get input file stream
        try {
            inputFile =  mContext.getAssets().open(assetPath);
            inputFileStats = mContext.getAssets().open(assetPathStats);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write to output file in internal storage
        try {
            writeExtractedFileToDisk(inputFile, new FileOutputStream(outFile));
            writeExtractedFileToDisk(inputFileStats, new FileOutputStream(outFileStats));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //check now if the database is created
        if (outFile.exists()) {
            try {
                goldenDb = SQLiteDatabase.openDatabase(databasePath+"/"+DATABASE_NAME, null, SQLiteDatabase.OPEN_READONLY);
                goldenStatsDb = SQLiteDatabase.openDatabase(databasePath+"/"+DATABASE_NAME_STATS, null, SQLiteDatabase.OPEN_READONLY);
                Log.i(TAG, "successfully opened database " + DATABASE_NAME);
                return goldenDb;
            } catch (SQLiteException e) {
                Log.w(TAG, "could not open database " + outFile + " - " + e.getMessage());
                return null;
            }
        }

        return goldenDb;
    }

    public static void writeExtractedFileToDisk(InputStream in, OutputStream outs) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer))>0){
            outs.write(buffer, 0, length);
        }
        outs.flush();
        outs.close();
        in.close();
    }
}
