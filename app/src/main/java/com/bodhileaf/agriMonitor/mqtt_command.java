package com.bodhileaf.agriMonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class mqtt_command extends AppCompatActivity {
    private static final String TAG = "mqtt_command";
    private String mqtt_link;
    private String clientId;
    private String dbFilename;
    private String statsFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, " onCreate");
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
// get data via the key
         dbFilename = extras.getString("configfilename");
         statsFilename = extras.getString("statsfilename");

        if (dbFilename == null) {
            Log.d(TAG, "onreate: db filename missing" );
            return;
            // do something with the data
        }
        if (statsFilename == null) {
            Log.d(TAG, "onreate: db filename missing" );
            return;
            // do something with the data
        }


        setContentView(R.layout.activity_mqtt_command);
        final Button mqttConnect = (Button) findViewById(R.id.button_mqtt_server_connect);
        final Button control_nodes_gui = (Button) findViewById(R.id.control_nodes_gui_button);
        final Button control_nodes_mqtt = (Button) findViewById(R.id.control_nodes_mqtt);
        final EditText mqtt_server_ip_address = (EditText) findViewById(R.id.mqtt_ip_address);
        final EditText mqtt_server_port = (EditText) findViewById(R.id.mqtt_port);
        final TextView tv_ip = (TextView) findViewById(R.id.mqtt_tv_ip);
        final TextView tv_port = (TextView) findViewById(R.id.mqtt_tv_port);
        final Button decr_port = (Button) findViewById(R.id.button9);
        final Button incr_port = (Button) findViewById(R.id.button8);

        mqtt_server_port.setText("1883");

        control_nodes_gui.setVisibility(View.INVISIBLE);
        control_nodes_mqtt.setVisibility(View.INVISIBLE);



        mqttConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                 clientId = MqttClient.generateClientId();

                mqtt_link = "tcp://"+mqtt_server_ip_address.getText().toString()+":"+mqtt_server_port.getText().toString();
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
                            Toast.makeText(v.getContext(),"MQTT broker connected",Toast.LENGTH_LONG);
                            control_nodes_gui.setVisibility(View.VISIBLE);
                            control_nodes_mqtt.setVisibility(View.VISIBLE);
                            mqttConnect.setVisibility(View.INVISIBLE);
                            mqtt_server_ip_address.setVisibility(View.INVISIBLE);
                            mqtt_server_port.setVisibility(View.INVISIBLE);
                            decr_port.setVisibility(View.INVISIBLE);
                            incr_port.setVisibility(View.INVISIBLE);
                            tv_ip.setVisibility(View.INVISIBLE);
                            tv_port.setVisibility(View.INVISIBLE);
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


            }
        });

        decr_port.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer port_value = Integer.valueOf(mqtt_server_port.getText().toString());
                port_value--;
                mqtt_server_port.setText(port_value.toString());
            }
        });
        incr_port.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer port_value = Integer.valueOf(mqtt_server_port.getText().toString());
                port_value++;
                mqtt_server_port.setText(port_value.toString());
            }
        });
        control_nodes_mqtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mqttCmd = new Intent(mqtt_command.this, com.bodhileaf.agriMonitor.mqtt_cmd_window.class);
                mqttCmd.putExtra("link",mqtt_link);
                mqttCmd.putExtra("clientid",clientId);
                mqttCmd.putExtra("configfilename",dbFilename);
                mqttCmd.putExtra("statsfilename",statsFilename);
                startActivity(mqttCmd);
            }
        });
        control_nodes_gui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mqttGui = new Intent(mqtt_command.this, com.bodhileaf.agriMonitor.MqttGuiActivity.class);
                mqttGui.putExtra("link",mqtt_link);
                mqttGui.putExtra("clientid",clientId);
                mqttGui.putExtra("configfilename",dbFilename);
                mqttGui.putExtra("statsfilename",statsFilename);

                startActivity(mqttGui);
            }
        });
    }
}
