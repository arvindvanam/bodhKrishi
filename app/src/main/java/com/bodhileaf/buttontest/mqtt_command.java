package com.bodhileaf.buttontest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class mqtt_command extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_command);
        Button mqttConnect = (Button) findViewById(R.id.button_mqtt_server_connect);


        mqttConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mqtt_server_ip_address = (EditText) findViewById(R.id.mqtt_ip_address);
                EditText mqtt_server_port = (EditText) findViewById(R.id.mqtt_port);
                final String mqtt_link = "tcp://"+mqtt_server_ip_address.getText().toString()+":"+mqtt_server_port.getText().toString();
                Intent mqttCmd = new Intent(mqtt_command.this, com.bodhileaf.buttontest.mqtt_cmd_window.class);
                mqttCmd.putExtra("link",mqtt_link);
                startActivity(mqttCmd);
            }
        });
    }
}
