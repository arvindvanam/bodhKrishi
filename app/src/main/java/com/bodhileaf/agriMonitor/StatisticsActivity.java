package com.bodhileaf.agriMonitor;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG = "StatisticsActivity" ;
    private String dbFileName;
    private String statsFileName;
    private Integer nodeID=0;
    private List<String> nodeList;
    private Integer nodeType=0;
    private SQLiteDatabase agriDb;
    private SQLiteDatabase agriStatsDb;
    private ArrayAdapter<String> nodeAdapter;
    private Spinner nodeSpinner;
    private Integer listCurPos;
    private boolean match = false;
    private ImageView iv_soil_temp;
    private ImageView iv_soil_moisture;
    private ImageView iv_air_temp;
    private ImageView iv_air_humid;
    private ImageView iv_wifi_strength;
    private ImageView iv_water_level;
    private ImageView iv_water_flow_rate;
    private ImageView iv_actuation_status;
    private TextView val_soil_temp;
    private TextView val_soil_moisture;
    private TextView val_air_temp;
    private TextView val_air_humid;
    private TextView val_wifi_strength;
    private TextView val_water_level;
    private TextView val_water_flow_rate;
    private TextView val_actuation_status;
    private TextView title_soil_temp;
    private TextView title_soil_moisture;
    private TextView title_air_temp;
    private TextView title_air_humid;
    private TextView title_wifi_strength;
    private TextView title_water_level;
    private TextView title_water_flow_rate;
    private TextView title_actuation_status;
    private DriveResourceClient mDriveClient;
    private Task<Void> getDatabaseUpdateTask;
    private DriveFile farmDriveFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
         // get database filename via the key
        dbFileName  = extras.getString("configfilename");
        statsFileName  = extras.getString("statsfilename");
        nodeID =extras.getInt("nodeId");
        if (dbFileName == null) {
            Log.d(TAG, "oncreate: db filename missing" );
            return;
        }
        if (statsFileName == null) {
            Log.d(TAG, "oncreate: db filename missing" );
            return;
        }
        Log.d(TAG, "onreate: db filename "+dbFileName );
        mDriveClient=  Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
        getDatabaseUpdateTask =
                mDriveClient.getRootFolder()
                        .continueWithTask(new Continuation<DriveFolder, Task<MetadataBuffer>>() {
                            @Override
                            public Task<MetadataBuffer> then(@NonNull Task<DriveFolder> task) throws Exception {
                                Query query = new Query.Builder()
                                        .addFilter(Filters.eq(SearchableField.TITLE, "bodhKrishiDatabases"))
                                        .build();
                                DriveFolder rootFolder = task.getResult();
                                return mDriveClient.queryChildren(rootFolder,query);
                            }
                        })
                        .continueWithTask(new Continuation<MetadataBuffer, Task<MetadataBuffer>>() {
                            @Override
                            public Task<MetadataBuffer> then(@NonNull Task<MetadataBuffer> task) throws Exception {
                                String farmName = dbFileName.substring(dbFileName.lastIndexOf("/")+1,dbFileName.lastIndexOf("."))+"_stat.db";
                                Query query = new Query.Builder()
                                        .addFilter(Filters.eq(SearchableField.TITLE, farmName))
                                        .build();
                                DriveFolder rootFolder = task.getResult().get(0).getDriveId().asDriveFolder();;
                                return mDriveClient.queryChildren(rootFolder,query);
                            }
                        })
                        .continueWithTask(new Continuation<MetadataBuffer, Task<DriveContents>>() {
                            @Override
                            public Task<DriveContents> then(@NonNull Task<MetadataBuffer> task) throws Exception {
                                farmDriveFile = task.getResult().get(0).getDriveId().asDriveFile();
                                return mDriveClient.openFile(farmDriveFile, DriveFile.MODE_READ_WRITE);
                            }
                        })
                        .continueWithTask(new Continuation<DriveContents, Task<Void>>() {
                            @Override
                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
                                DriveContents driveContents = task.getResult();
                                ParcelFileDescriptor pfd = driveContents.getParcelFileDescriptor();
                                File oFile = new File(statsFileName);

                                try {
                                    InputStream in = new FileInputStream(pfd.getFileDescriptor());
                                    OutputStream out = new FileOutputStream(oFile);
                                    MyDatabase.writeExtractedFileToDisk(in,out);
                                }catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }


                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setStarred(true)
                                        .setLastViewedByMeDate(new Date())
                                        .build();
                                Task<Void> commitTask =
                                        mDriveClient.commitContents(driveContents, changeSet);

                                return commitTask;
                            }
                        })
                        .addOnSuccessListener(this,
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void avoid) {

                                        Log.d(TAG, "farm config updated into drive");
                                    }
                                }
                        )
                        .addOnFailureListener(this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Unable to create folder", e);
                            }
                        });
        Tasks.whenAll(getDatabaseUpdateTask);

        agriDb = openOrCreateDatabase(dbFileName,MODE_PRIVATE,null);
        agriStatsDb = openOrCreateDatabase(statsFileName,MODE_PRIVATE,null);

        initNodeList(agriDb);

        //create UI references

        iv_air_humid = findViewById(R.id.stats_image_air_humid);
        iv_air_temp = findViewById(R.id.stats_image_air_temp);
        iv_soil_moisture = findViewById(R.id.stats_image_soil_mositure);
        iv_soil_temp = findViewById(R.id.stats_image_soil_temp);
        iv_wifi_strength = findViewById(R.id.stats_image_wifi_strength);
        iv_water_flow_rate = findViewById(R.id.stats_image_air_humid);
        iv_water_level = findViewById(R.id.stats_image_soil_mositure);
        iv_actuation_status = findViewById(R.id.stats_image_soil_mositure);
        val_air_temp = findViewById(R.id.stats_air_temp_val);
        val_air_humid = findViewById(R.id.stats_air_humid_val);
        val_soil_temp = findViewById(R.id.stats_soil_temp_val);
        val_soil_moisture = findViewById(R.id.stats_soil_moisture_val);
        val_wifi_strength = findViewById(R.id.stats_wifi_strength_val);
        val_water_flow_rate = findViewById(R.id.stats_air_humid_val);
        val_water_level = findViewById(R.id.stats_soil_moisture_val);
        val_actuation_status = findViewById(R.id.stats_soil_moisture_val);
        title_air_temp = findViewById(R.id.stats_title_air_temp);
        title_air_humid = findViewById(R.id.stats_title_air_humid);
        title_soil_temp = findViewById(R.id.stats_title_soil_temp);
        title_soil_moisture = findViewById(R.id.stats_title_soil_mositure);
        title_wifi_strength = findViewById(R.id.stats_title_wifi_strength);
        title_water_flow_rate = findViewById(R.id.stats_title_air_humid);
        title_water_level = findViewById(R.id.stats_title_soil_mositure);
        title_actuation_status = findViewById(R.id.stats_title_soil_mositure);
        iv_air_temp.setVisibility(View.INVISIBLE);
        iv_air_humid.setVisibility(View.INVISIBLE);
        iv_soil_temp.setVisibility(View.INVISIBLE);
        iv_soil_moisture.setVisibility(View.INVISIBLE);
        iv_wifi_strength.setVisibility(View.INVISIBLE);
        val_air_temp.setVisibility(View.INVISIBLE);
        val_air_humid.setVisibility(View.INVISIBLE);
        val_soil_temp.setVisibility(View.INVISIBLE);
        val_soil_moisture.setVisibility(View.INVISIBLE);
        val_wifi_strength.setVisibility(View.INVISIBLE);
        title_air_temp.setVisibility(View.INVISIBLE);
        title_air_humid.setVisibility(View.INVISIBLE);
        title_soil_temp.setVisibility(View.INVISIBLE);
        title_soil_moisture.setVisibility(View.INVISIBLE);
        title_wifi_strength.setVisibility(View.INVISIBLE);

        nodeSpinner = findViewById(R.id.stats_node_spinner);
        nodeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nodeList);
        nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSpinner.setAdapter(nodeAdapter);
        nodeSpinner.setSelection(listCurPos);
        nodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String listEntry = nodeList.get(position).toString();
                Cursor nodeListResults;
                //extract nodesType
                String query = String.format("SELECT nodeType FROM nodesInfo where NodeID is %s", listEntry);
                Log.d(TAG, "onItemSelected: query" + query);
                //check if the schedule id exists for the node id
                nodeListResults = agriDb.rawQuery(query, null);
                nodeListResults.moveToFirst();
                if (nodeListResults.getCount() != 0) {
                    nodeType = nodeListResults.getInt(0);
                    //load default values in the UI
                } else {
                    Toast.makeText(getApplicationContext(),"Couldn't find nodeType Info",Toast.LENGTH_LONG).show();

                }
                switch (nodeType) {
                    case 0:
                        //case where the selected node already existed
                        query = String.format("SELECT airTemperature,airHumidity,soilTemperature,soilMoisture,wifiStrength FROM sensorData WHERE nodeID=%s ORDER BY CAST(strftime('%%s', timestamp) as int) DESC LIMIT 1  ", listEntry);
                        Log.d(TAG, "onItemSelected: "+query);
                        nodeListResults = agriStatsDb.rawQuery(query, null);
                        nodeListResults.moveToFirst();
                        iv_air_temp.setVisibility(View.VISIBLE);
                        iv_air_humid.setVisibility(View.VISIBLE);
                        iv_soil_temp.setVisibility(View.VISIBLE);
                        iv_soil_moisture.setVisibility(View.VISIBLE);
                        iv_wifi_strength.setVisibility(View.VISIBLE);
                        val_air_temp.setVisibility(View.VISIBLE);
                        val_air_humid.setVisibility(View.VISIBLE);
                        val_soil_temp.setVisibility(View.VISIBLE);
                        val_soil_moisture.setVisibility(View.VISIBLE);
                        val_wifi_strength.setVisibility(View.VISIBLE);
                        title_air_temp.setVisibility(View.VISIBLE);
                        title_air_humid.setVisibility(View.VISIBLE);
                        title_soil_temp.setVisibility(View.VISIBLE);
                        title_soil_moisture.setVisibility(View.VISIBLE);
                        title_wifi_strength.setVisibility(View.VISIBLE);
                        val_air_temp.setText("");
                        val_air_humid.setText("");
                        val_soil_temp.setText("");
                        val_soil_moisture.setText("");
                        val_wifi_strength.setText("");

                        if (nodeListResults.getCount() != 0) {
                            val_air_temp.setText(String.format("%.2f deg C",nodeListResults.getFloat(0)));
                            val_air_humid.setText(String.format("%.2f ppm",nodeListResults.getFloat(1)));
                            val_soil_temp.setText(String.format("%.2f deg C",nodeListResults.getFloat(2)));
                            val_soil_moisture.setText(String.format("%.2f ppm",nodeListResults.getFloat(3)));
                            val_wifi_strength.setText(String.format("%.2f dB",nodeListResults.getFloat(4)));
                                //load default values in the UI
                        } else {
                            Toast.makeText(getApplicationContext(),"Couldn't find sensor Info",Toast.LENGTH_LONG).show();
                        }

                            break;
                        case 1:
                            query = String.format("SELECT waterLevel,wifiStrength FROM sensorData WHERE nodeID=%s ORDER BY CAST(strftime('%%s', timestamp) as int) DESC LIMIT 1  ", listEntry);
                            Log.d(TAG, "onItemSelected: "+query);
                            title_water_level.setText("WATER LEVEL");
                            iv_air_temp.setVisibility(View.INVISIBLE);
                            iv_air_humid.setVisibility(View.INVISIBLE);
                            iv_soil_temp.setVisibility(View.INVISIBLE);
                            iv_soil_moisture.setVisibility(View.VISIBLE);
                            iv_wifi_strength.setVisibility(View.VISIBLE);
                            val_air_temp.setVisibility(View.INVISIBLE);
                            val_air_humid.setVisibility(View.INVISIBLE);
                            val_soil_temp.setVisibility(View.INVISIBLE);
                            val_soil_moisture.setVisibility(View.VISIBLE);
                            val_wifi_strength.setVisibility(View.VISIBLE);
                            title_air_temp.setVisibility(View.INVISIBLE);
                            title_air_humid.setVisibility(View.INVISIBLE);
                            title_soil_temp.setVisibility(View.INVISIBLE);
                            title_soil_moisture.setVisibility(View.VISIBLE);
                            title_wifi_strength.setVisibility(View.VISIBLE);
                            val_soil_moisture.setText("");
                            val_wifi_strength.setText("");

                            nodeListResults = agriStatsDb.rawQuery(query, null);
                            nodeListResults.moveToFirst();
                            if (nodeListResults.getCount() != 0) {
                                val_water_level.setText(String.format("%.2f cm",nodeListResults.getFloat(0)));
                                val_wifi_strength.setText(String.format("%.2f dB",nodeListResults.getFloat(1)));

                                //load default values in the UI
                            } else {
                                Toast.makeText(getApplicationContext(),"Couldn't find sensor Info",Toast.LENGTH_LONG).show();
                            }
                            break;

                        case 2:
                            iv_air_temp.setVisibility(View.INVISIBLE);
                            iv_air_humid.setVisibility(View.VISIBLE);
                            iv_soil_temp.setVisibility(View.INVISIBLE);
                            iv_soil_moisture.setVisibility(View.VISIBLE);
                            iv_wifi_strength.setVisibility(View.VISIBLE);
                            val_air_temp.setVisibility(View.INVISIBLE);
                            val_soil_temp.setVisibility(View.INVISIBLE);
                            val_air_humid.setVisibility(View.VISIBLE);
                            val_soil_moisture.setVisibility(View.VISIBLE);
                            val_wifi_strength.setVisibility(View.VISIBLE);
                            title_air_temp.setVisibility(View.INVISIBLE);
                            title_air_humid.setVisibility(View.VISIBLE);
                            title_soil_temp.setVisibility(View.INVISIBLE);
                            title_soil_moisture.setVisibility(View.VISIBLE);
                            title_wifi_strength.setVisibility(View.VISIBLE);
                            val_air_humid.setText("");
                            val_soil_moisture.setText("");
                            val_wifi_strength.setText("");
                            query = String.format("SELECT waterFlowRate,wifiStrength FROM sensorData WHERE nodeID=%s ORDER BY CAST(strftime('%%s', timestamp) as int) DESC LIMIT 1  ", listEntry);
                            Log.d(TAG, "onItemSelected: "+query);
                            title_water_flow_rate.setText("WATER FLOW RATE");
                            title_actuation_status.setText("WATER VALVE STATUS");
                            nodeListResults = agriStatsDb.rawQuery(query, null);
                            nodeListResults.moveToFirst();
                            if (nodeListResults.getCount() != 0) {
                                val_water_flow_rate.setText(String.format("%.2f L/min",nodeListResults.getFloat(0)));
                                val_wifi_strength.setText(String.format("%.2f dB",nodeListResults.getFloat(1)));
                            } else {
                                Toast.makeText(getApplicationContext(),"Couldn't find sensor Info",Toast.LENGTH_LONG).show();
                            }
                            query = String.format("SELECT actuationStatus FROM actuatorData WHERE nodeID=%s ORDER BY CAST(strftime('%%s', timestamp) as int) DESC LIMIT 1  ", listEntry);
                            nodeListResults = agriStatsDb.rawQuery(query, null);
                            nodeListResults.moveToFirst();
                            if (nodeListResults.getCount() != 0) {
                                if(nodeListResults.getInt(0) == 1) {
                                    val_actuation_status.setText("ON");
                                } else {
                                    val_actuation_status.setText("OFF");
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),"Couldn't find actuator Info",Toast.LENGTH_LONG).show();
                            }
                            break;
                        default:
                            //TODO: toast to print that its an invalid case
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageButton hourly_chart_button = (ImageButton) findViewById(R.id.button_stats_hourly_chart);
        hourly_chart_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent hourlyChart = new Intent(StatisticsActivity.this, com.bodhileaf.agriMonitor.LineChartActivity.class);
                hourlyChart.putExtra("nodeId",nodeID);
                hourlyChart.putExtra("configfilename",dbFileName);
                hourlyChart.putExtra("statsfilename",statsFileName);
                startActivity(hourlyChart);
            }
        });
    }

    private void initNodeList(SQLiteDatabase agriDb) {

        Integer curPos = 0;
        nodeList = new ArrayList<String>();
        String query = "SELECT nodeID FROM nodesInfo";
        //check if the schedule id exists for the node id
        Cursor nodeListResults = agriDb.rawQuery(query,null);
        nodeListResults.moveToFirst();
        do {

            Integer nodeID_from_list = nodeListResults.getInt(0);
            if (nodeID == nodeID_from_list) {
                match = true;
                listCurPos = curPos;
            }
            nodeList.add(nodeID_from_list.toString());
            Log.d(TAG, "NodeList: "+nodeID_from_list.toString());
            curPos++;
        } while(nodeListResults.moveToNext());
        if(match == false) {
            listCurPos = curPos;
            nodeList.add(nodeID.toString());
        }
    }

}
