package com.bodhileaf.buttontest;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast ;
import android.database.sqlite.*;

import java.net.URISyntaxException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
static int a=0;

    private static final int FILE_SELECT_CODE = 0;
    private void showFileChooser() {
        Intent  intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*.db");
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
                    try {
                        path = FileUtils.getPath(this, uri  );
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    Log.d("onActivity", "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        Button  opendb = (Button ) findViewById(R.id.button2 );
        opendb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase mydb = openOrCreateDatabase("arvind.db",MODE_PRIVATE ,null ) ;
                mydb.execSQL("CREATE TABLE IF NOT EXISTS pwdlist(Username VARCHAR,Password VARCHAR);");
                mydb.execSQL("INSERT INTO pwdlist VALUES('admin','admin');");
                Cursor  resultSet = mydb.rawQuery("Select * from pwdlist",null);
                resultSet.moveToFirst();
                String username = resultSet.getString(0);
                String password = resultSet.getString(1);
                Toast.makeText(getApplicationContext(), username+"  "+password , Toast.LENGTH_LONG).show();

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
