package com.bodhileaf.agriMonitor;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast ;
import android.database.sqlite.*;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Collections;
import java.util.Locale;

public class OptionsActivity extends AppCompatActivity  {
static int a=0;

    private static final int FILE_SELECT_CODE = 0;
    private String dbfilename;
    final private String TAG = "OptionsActivity";

    /**
     * Request code for the file opener activity.
     */
    private static final int REQUEST_CODE_OPENER = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
// get data via the key
        dbfilename  = extras.getString("filename");
        if (dbfilename == null) {
            Log.d(TAG, "onCreate: db filename missing" );
            return;
            // do something with the data
        }
        Log.d(TAG, "onCreate: db filename "+dbfilename );

        final com.bodhileaf.agriMonitor.config config_page = new config();
        //PreferenceManager.setDefaultValues(this, R.xml.preferences , false);
        setContentView(R.layout.activity_main);
        Locale lang = new Locale("hi","rIN");
        Configuration  config = this.getResources().getConfiguration();
        config.setLocale(lang );
        onConfigurationChanged(config);

        Button  clickme = (Button ) findViewById(R.id.openFarmInText);
        clickme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a++;
                // Toast.makeText(getApplicationContext(), String.valueOf(a) , Toast.LENGTH_LONG).show();
                Intent configScreen = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.config.class);
                startActivity(configScreen);
                //setContentView(R.layout.activity_config);
            }
        });
        Button  openMaps = (Button ) findViewById(R.id.openFarmInMaps);
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
                //DialogFragment newFarmListFragment = new FarmListFragment();
                //newFarmListFragment.show(getFragmentManager(), "FarmSelect");
                Intent mapsScreen  = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.FarmMapsActivity.class);
                mapsScreen.putExtra("filename",dbfilename );
                startActivity(mapsScreen);
                //Intent mapsScreen  = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.FarmMapsActivity.class);
                //startActivity(mapsScreen);
            }
        });

//        Button  openFile = (Button ) findViewById(R.id.button3 );
//        openFile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showFileChooser() ;
//            }
//        });

//        Button  changeLanguage = (Button ) findViewById(R.id.button4 );
//        changeLanguage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //FileUtils.changeLang(v.getContext(), "hi");  ;
//                Configuration  config = v.getContext().getResources().getConfiguration();
//                Locale lang = new Locale("hi","rIN");
//                config.setLocale(lang );
//                onConfigurationChanged(config);
//
//                Toast.makeText(v.getContext() , "main activity" + " lang:" +  v.getContext().getResources().getConfiguration().getLocales().toString(), Toast.LENGTH_LONG).show();
//
//                //restartActivity() ;
//            }
//        });

        Button  mqttConnect = (Button ) findViewById(R.id.manualMode);
        mqttConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mqttScreen = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.mqtt_command.class);
                mqttScreen.putExtra("filename",dbfilename);
                startActivity(mqttScreen);

            }
        });

        Button statsConnect = (Button) findViewById(R.id.button_options_stats);
        statsConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statsScreen = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.StatisticsActivity.class);
                statsScreen.putExtra("filename",dbfilename);
                startActivity(statsScreen);

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
        Intent refresh = new Intent(this, OptionsActivity.class);

        startActivity(refresh);



    }
}
