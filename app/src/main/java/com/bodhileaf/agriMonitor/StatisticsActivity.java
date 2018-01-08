package com.bodhileaf.agriMonitor;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG = "StatisticsActivity" ;
    private String dbFileName;
    private Integer nodeID=0;
    private List<String> nodeList;
    private Integer nodeType=0;
    private SQLiteDatabase agriDb;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
         // get database filename via the key
        dbFileName  = extras.getString("filename");
        nodeID =extras.getInt("nodeId");
        if (dbFileName == null) {
            Log.d(TAG, "oncreate: db filename missing" );
            return;
        }
        Log.d(TAG, "onreate: db filename "+dbFileName );
        agriDb = openOrCreateDatabase(dbFileName,MODE_PRIVATE,null);
        initNodeList(agriDb);

        //create UI references
        iv_air_temp = findViewById(R.id.stats_image_air_temp);
        iv_air_humid = findViewById(R.id.stats_image_air_humid);
        iv_soil_temp = findViewById(R.id.stats_image_soil_temp);
        iv_soil_moisture = findViewById(R.id.stats_image_soil_mositure);
        iv_wifi_strength = findViewById(R.id.stats_image_wifi_strength);
        iv_water_flow_rate = findViewById(R.id.stats_image_air_temp);
        iv_water_level = findViewById(R.id.stats_image_soil_temp);
        iv_actuation_status = findViewById(R.id.stats_image_soil_temp);
        val_air_temp = findViewById(R.id.stats_air_temp_val);
        val_air_humid = findViewById(R.id.stats_air_humid_val);
        val_soil_temp = findViewById(R.id.stats_soil_temp_val);
        val_soil_moisture = findViewById(R.id.stats_soil_moisture_val);
        val_wifi_strength = findViewById(R.id.stats_wifi_strength_val);
        val_water_flow_rate = findViewById(R.id.stats_air_temp_val);
        val_water_level = findViewById(R.id.stats_soil_temp_val);
        val_actuation_status = findViewById(R.id.stats_soil_temp_val);
        title_air_temp = findViewById(R.id.stats_title_air_temp);
        title_air_humid = findViewById(R.id.stats_title_air_humid);
        title_soil_temp = findViewById(R.id.stats_title_soil_temp);
        title_soil_moisture = findViewById(R.id.stats_title_soil_mositure);
        title_wifi_strength = findViewById(R.id.stats_title_wifi_strength);
        title_water_flow_rate = findViewById(R.id.stats_title_air_temp);
        title_water_level = findViewById(R.id.stats_title_soil_temp);
        title_actuation_status = findViewById(R.id.stats_title_soil_temp);

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
                        query = String.format("SELECT airTemperature,airHumidity,soilTemperature,soilMoisture,wifiStrength FROM sensorData WHERE nodeID=%d ORDER BY CAST(strftime('%%s', timestamp) as int) DESC LIMIT 1  ", nodeID);
                        Log.d(TAG, "onItemSelected: "+query);
                        nodeListResults = agriDb.rawQuery(query, null);
                        nodeListResults.moveToFirst();
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
                            //case where the selected node already existed
                            query = String.format("SELECT * FROM sensorData where NodeID is %s", listEntry);
                            //check if the schedule id exists for the node id
                            nodeListResults = agriDb.rawQuery(query, null);
                            nodeListResults.moveToFirst();
                            if (nodeListResults.getCount() != 0) {
                                //load default values in the UI
                            } else {
                                //TODO: add toast to show node info doesnt exist
                            }
                            break;
                        case 2:
                            //case where the selected node already existed
                            query = String.format("SELECT * FROM actuatorData where NodeID is %s", listEntry);
                            //check if the schedule id exists for the node id
                            nodeListResults = agriDb.rawQuery(query, null);
                            nodeListResults.moveToFirst();
                            if (nodeListResults.getCount() == 0) {
                                //load default values in the UI
                            } else {
                                //TODO: add toast to show node info doesnt exist
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
                hourlyChart.putExtra("filename",dbFileName);
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
