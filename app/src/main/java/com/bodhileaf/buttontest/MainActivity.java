package com.bodhileaf.buttontest;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast ;
import android.database.sqlite.*;

import java.sql.ResultSet;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
static int a=0;

    private static final int FILE_SELECT_CODE = 0;
    private void showFileChooser() {
        Intent  intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri  uri = data.getData();
                    Log.d("onActivity", "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    String filename = null;

                    path = FileUtils.getPath(MainActivity.this, uri);
                    Log.d("MainActivity", "Actual File Path: " + path);
                    // Get the file instance

                    Cursor  cursor = null;

                    try {
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                        if (cursor.moveToFirst()) {
                            filename = cursor.getString(column_index);
                        }
                        Log.d("onActivity", "File path: " + path+ "Name:" + filename);
                        SQLiteDatabase agridb = openOrCreateDatabase(path,MODE_PRIVATE ,null ) ;
                        qualifyDB(agridb);
                        agridb.execSQL("insert into nodesInfo(nodeID,nodeType) values(\"104\",\"122\") ");
                        Cursor  resultSet = agridb.rawQuery("Select nodeID from nodesInfo",null);
                        resultSet.moveToFirst();
                        String username = resultSet.getString(0);
                        String password = resultSet.getString(1);
                        Toast.makeText(getApplicationContext(), username+"  "+password , Toast.LENGTH_LONG).show();



                    } catch (Exception e) {
                        // Eat it
                    }
                }

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    // Sanity check if the opened db is not corrupted and matches the required table schemas
    private void qualifyDB(SQLiteDatabase selectDB) {
        SQLiteDatabase compareDB = openOrCreateDatabase("golden.db",MODE_PRIVATE,null);
        Cursor results = selectDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null);
        results.moveToFirst();
        do {
            Log.d("database tables", "qualifyDB: "+results.getString(0));
            if (results.getString(0) != "android_metadata") {
                String query = "SELECT sql FROM sqlite_master WHERE name ='" + results.getString(0) + "'";
                Cursor schemaResults = selectDB.rawQuery(query, null);
                schemaResults.moveToFirst();
                //Log.d("database tables", "schema query result: " + schemaResults.getString(0));
            }
        }while (results.moveToNext());

    }

    // Create new DB which copies table schemas from golden db
    private void createNewDB(String path) {
        SQLiteDatabase baseDB = openOrCreateDatabase("golden.db",MODE_PRIVATE,null);



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final com.bodhileaf.buttontest.config config_page = new config();
        //PreferenceManager.setDefaultValues(this, R.xml.preferences , false);
        setContentView(R.layout.activity_main);
        Locale lang = new Locale("hi","rIN");
        Configuration  config = this.getResources().getConfiguration();
        config.setLocale(lang );
        onConfigurationChanged(config);

        Button  clickme = (Button ) findViewById(R.id.button1 );
        clickme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a++;
                // Toast.makeText(getApplicationContext(), String.valueOf(a) , Toast.LENGTH_LONG).show();
                Intent configScreen = new Intent(MainActivity.this, com.bodhileaf.buttontest.config.class);
                startActivity(configScreen);
                //setContentView(R.layout.activity_config);
            }
        });
        Button  openMaps = (Button ) findViewById(R.id.openMaps);
        openMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SQLiteDatabase mydb = openOrCreateDatabase("arvind.db",MODE_PRIVATE ,null ) ;
                //mydb.execSQL("CREATE TABLE IF NOT EXISTS pwdlist(Username VARCHAR,Password VARCHAR);");
                //mydb.execSQL("INSERT INTO pwdlist VALUES('admin','admin');");
                //Cursor  resultSet = mydb.rawQuery("Select * from pwdlist",null);
                //resultSet.moveToFirst();
                //String username = resultSet.getString(0);
                //String password = resultSet.getString(1);
                //Toast.makeText(getApplicationContext(), username+"  "+password , Toast.LENGTH_LONG).show();
                Intent mapsScreen  = new Intent(MainActivity.this, com.bodhileaf.buttontest.FarmMapsActivity.class);
                startActivity(mapsScreen);
            }
        });

        Button  openFile = (Button ) findViewById(R.id.button3 );
        openFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser() ;
            }
        });

        Button  changeLanguage = (Button ) findViewById(R.id.button4 );
        changeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FileUtils.changeLang(v.getContext(), "hi");  ;
                Configuration  config = v.getContext().getResources().getConfiguration();
                Locale lang = new Locale("hi","rIN");
                config.setLocale(lang );
                onConfigurationChanged(config);

                Toast.makeText(v.getContext() , "main activity" + " lang:" +  v.getContext().getResources().getConfiguration().getLocales().toString(), Toast.LENGTH_LONG).show();

                //restartActivity() ;
            }
        });

        Button  mqttConnect = (Button ) findViewById(R.id.button5 );
        mqttConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mqttScreen = new Intent(MainActivity.this, com.bodhileaf.buttontest.mqtt_command.class);
                startActivity(mqttScreen);

            }
        });


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // your code here, you can use newConfig.locale if you need to check the language
        // or just re-set all the labels to desired string resource
    }

    private void restartActivity() {
        Log.d("onCreate", "Restart activity " + this);
        // Intent intent = getIntent();
        finish();
        Intent refresh = new Intent(this, MainActivity.class);

        startActivity(refresh);



    }
}
