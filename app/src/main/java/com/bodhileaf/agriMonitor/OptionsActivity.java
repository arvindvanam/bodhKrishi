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
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.OpenFileActivityOptions;

import java.io.File;
import java.util.Collections;
import java.util.Locale;

public class OptionsActivity extends AppCompatActivity implements FarmListFragment.FarmSourceListener {
static int a=0;

    private static final int FILE_SELECT_CODE = 0;
    private String dbfilename;
    final private String TAG = "OptionsActivity";

    /**
     * Request code for the file opener activity.
     */
    private static final int REQUEST_CODE_OPENER = 1;



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
        Log.d("main activity", "showFileChooser: exit ");

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

                    path = FileUtils.getPath(OptionsActivity.this, uri);
                    Log.d("OptionsActivity", "Actual File Path: " + path);
                    // Get the file instance

                    Cursor  cursor = null;

                    try {
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                        if (cursor.moveToFirst()) {
                            filename = cursor.getString(column_index);
                        }
                        dbfilename = path;
                        Log.d("onActivity", "File path: " + path+ "Name:" + filename);
                        SQLiteDatabase agridb = openOrCreateDatabase(path,MODE_PRIVATE ,null ) ;
                        if (!qualifyDB(agridb)) {
                            Toast.makeText(getApplicationContext(), "INCORRECT TYPE OF DATABASE. OPEN ANOTHER" , Toast.LENGTH_LONG).show();
                            // TODO: return to open db activity;
                            Log.d("database tables", "qualifyDB: FAILED");
                            return;
                        }
                        agridb.close();
                        //agridb.execSQL("insert into nodesInfo(nodeID,nodeType) values(\"104\",\"122\") ");
                        //Cursor  resultSet = agridb.rawQuery("Select nodeID from nodesInfo",null);
                        //resultSet.moveToFirst();
                        //String username = resultSet.getString(0);
                        //String password = resultSet.getString(1);
                        //Toast.makeText(getApplicationContext(), username , Toast.LENGTH_LONG).show();



                    } catch (Exception e) {
                        // Eat it
                    }
                }

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("main activity", "on activityresult: exit ");
        if (dbfilename != null) {
            Intent mapsScreen  = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.FarmMapsActivity.class);
            mapsScreen.putExtra("filename",dbfilename );
            startActivity(mapsScreen);
        }
    }
    // Sanity check if the opened db is not corrupted and matches the required table schemas
    private boolean qualifyDB(SQLiteDatabase selectDB) {

        MyDatabase myDB = new MyDatabase(getApplicationContext());
        SQLiteDatabase compareDB = myDB.getDatabase();
        boolean result = true;


        //SQLiteDatabase compareDB = openOrCreateDatabase("golden_new.db",MODE_PRIVATE,null);
        Cursor results = compareDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null);
        Cursor resultsSelect = selectDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null);
        results.moveToFirst();
        resultsSelect.moveToFirst();
        do {
            Log.d("database tables", "qualifyDB: "+results.getString(0));
            if (results.getString(0) != "android_metadata") {
                if (results.getString(0) == resultsSelect.getString(0)) {
                    String query = "SELECT sql FROM sqlite_master WHERE name ='" + results.getString(0) + "'";
                    Cursor schemaResults = compareDB.rawQuery(query, null);
                    Cursor schemaSelectResults = selectDB.rawQuery(query, null);
                    schemaResults.moveToFirst();
                    schemaSelectResults.moveToFirst();
                    if (schemaResults.getString(0) != schemaSelectResults.getString(0)) {
                        compareDB.close();
                        return false;
                    }
                }
            } else {
                compareDB.close();
                return false;
            }
                //Log.d("database tables", "schema query result: " + schemaResults.getString(0));

        }while (results.moveToNext());
        compareDB.close();
        return result;
    }

    // Create new DB which copies table schemas from golden db
    private void createNewDB(String path) {
        SQLiteDatabase baseDB = openOrCreateDatabase("databases/golden.db",MODE_PRIVATE,null);
        //File fileMetadata = new File();
        //fileMetadata.setName("Project plan");
        //fileMetadata.setMimeType("application/vnd.google-apps.drive-sdk");

        //File file = driveService.files().create(fileMetadata)
         //       .setFields("id")
          //      .execute();
        //System.out.println("File ID: " + file.getId());





    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final com.bodhileaf.agriMonitor.config config_page = new config();
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
                Intent configScreen = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.config.class);
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
                DialogFragment newFarmListFragment = new FarmListFragment();
                newFarmListFragment.show(getFragmentManager(), "FarmSelect");

                //Intent mapsScreen  = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.FarmMapsActivity.class);
                //startActivity(mapsScreen);
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
                Intent mqttScreen = new Intent(OptionsActivity.this, com.bodhileaf.agriMonitor.mqtt_command.class);
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
        Intent refresh = new Intent(this, OptionsActivity.class);

        startActivity(refresh);



    }



    @Override
    public void onFarmSourceSelect(int result) {
        Log.d("Main Activity", "onFarmSourceSelect: result: "+Integer.toString(result));
        switch (result) {
            case 0:


               
                //TODO: Open create new file dialog box
                DriveClient mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
                // Create the initial metadata - MIME type and title.
                // Note that the user will be able to change the title later.
                MetadataChangeSet metadataChangeSet =
                        new MetadataChangeSet.Builder()
                                .setMimeType("*/*")
                                .setTitle("Android .db")
                                .build();
                // Set up options to configure and display the create file activity.
                OpenFileActivityOptions openFileActivityOptions =
                        new OpenFileActivityOptions.Builder()
                                .setMimeType(Collections.singletonList("*/*"))
                                .build();

                    mDriveClient.newOpenFileActivityIntentSender(openFileActivityOptions)
                            .addOnSuccessListener(new OnSuccessListener<IntentSender>() {
                                @Override
                                public void onSuccess(IntentSender intentSender) {
                                    try {
                                        startIntentSenderForResult(
                                                intentSender,
                                                REQUEST_CODE_OPENER,
                            /* fillInIntent= */ null,
                            /* flagsMask= */ 0,
                            /* flagsValues= */ 0,
                            /* extraFlags= */ 0);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.w(TAG, "Unable to send intent.", e);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Unable to create OpenFileActivityIntent.", e);
                        }
                    });



                break;


            case 1:
                if(dbfilename == null) {
                    showFileChooser();
                }
                Log.d("main Activity", "onFarmSourceSelect: file chooser done");


                break;

            case 2:
                break;
            default:
                break;

        }
    }
}
