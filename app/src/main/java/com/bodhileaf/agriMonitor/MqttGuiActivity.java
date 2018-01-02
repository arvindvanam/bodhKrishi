package com.bodhileaf.agriMonitor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MqttGuiActivity extends AppCompatActivity {
    private class node {
        private Integer node_id;
        private Integer node_status;
        node(Integer nid, Integer nstatus) {
            node_id = nid;
            node_status = nstatus;
        }

        public Integer getNode_id(){
            return node_id;
        }
        public Boolean getNode_status(){
            if (node_status == 1) {
                return false;
            } else {
                return true;
            }
        }
    }
    private static final String TAG = "mqtt_gui_window";
    private String dbFilename;
    private List<String> nodeList;
    private SQLiteDatabase agriDb;
    private ArrayAdapter<String > nodeAdapter;
    private Spinner nodeSpinner;
    private Integer listCurPos;
    private Integer nodeId;
    private Integer nodeType;
    private String listEntry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //String clientId = MqttClient.generateClientId();
        String clientId;
        final String mqtt_link ;

        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        //connect to mqtt broker
        //String clientId = MqttClient.generateClientId();

        if(extras !=null) {
            mqtt_link = extras.getString("link");
            clientId=extras.getString("clientid");
            dbFilename = extras.getString("filename");
            if (dbFilename == null) {
                Log.d(TAG, "onCreate: db filename missing" );
                // do something with the data
            }
        } else {
            mqtt_link = "tcp://192.168.0.3:1883";
            clientId = MqttClient.generateClientId();
        }

        final MqttAndroidClient client =
                new MqttAndroidClient(getApplicationContext(), mqtt_link,
                        clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("mqtt_command->onCreate", "onSuccess");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("mqtt_command->onCreate", "onFailure, incorrect link: "+mqtt_link);

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
// get data via the key
        dbFilename = extras.getString("filename");
        if (dbFilename == null) {
            Log.d(TAG, "onreate: db filename missing" );
            return;
            // do something with the data
        }

        setContentView(R.layout.activity_mqtt_gui);
        agriDb = openOrCreateDatabase(dbFilename,android.content.Context.MODE_PRIVATE ,null);
        initNodeList(agriDb);
        nodeSpinner =  findViewById(R.id.valve_nodeid_spinner);
        nodeAdapter = new ArrayAdapter<String>(MqttGuiActivity.this, android.R.layout.simple_spinner_item, nodeList);
        nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSpinner.setAdapter(nodeAdapter);
        nodeSpinner.setSelection(0);
        nodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listEntry = nodeList.get(position).toString();
                nodeId = Integer.getInteger(listEntry);
                Integer result;
                String query = String.format("SELECT actuationStatus FROM actuatorData where nodeID is %s ORDER by timeStamp DESC LIMIT 1",listEntry);
                //check if the schedule id exists for the node id
                Log.d(TAG,"Query: "+query);
                Switch tap_switch = (Switch) findViewById(R.id.tap_switch);

                Cursor actuatorResult = agriDb.rawQuery(query,null);
                if(actuatorResult.getCount() == 0) {
                    //TODO: Actuator status not known  assuming it to be off
                    result = 0;
                } else {
                    actuatorResult.moveToFirst();
                    result = actuatorResult.getInt(0);
                    Log.d(TAG,"node id "+listEntry+ "node status"+result.toString());
                }

                if(result == 1) {
                    tap_switch.setChecked(true);

                } else {
                    tap_switch.setChecked(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });
        Switch tap_switch = (Switch) findViewById(R.id.tap_switch);
        tap_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                String topic = String.format(Locale.ENGLISH,"domoticz/out/%s",listEntry);
                String payload = String.format("{\"idx\":%d,\"nvalue\":%d,\"switchType\":\"On/Off\"}",111, isChecked?1:0);
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private void initNodeList(SQLiteDatabase agriDb) {

        nodeList = new ArrayList<String>();
        String query = "SELECT nodeID FROM nodesInfo where nodeType is 2";
        //check if the schedule id exists for the node id
        Cursor nodeListResults = agriDb.rawQuery(query,null);
        nodeListResults.moveToFirst();
        do {
            Integer nodeID_from_list = nodeListResults.getInt(0);
            nodeList.add(nodeID_from_list.toString());
            Log.d("mqtt_node", "NodeList: "+nodeID_from_list.toString());
        } while(nodeListResults.moveToNext());

    }
}
