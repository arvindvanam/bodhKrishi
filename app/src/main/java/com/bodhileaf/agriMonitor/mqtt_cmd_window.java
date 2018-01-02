package com.bodhileaf.agriMonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class mqtt_cmd_window extends AppCompatActivity {
    private static final String TAG = "mqtt_cmd_window";
    private String dbFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// get data via the key

        setContentView(R.layout.activity_mqtt_cmd_window);

        //String clientId = MqttClient.generateClientId();
        String clientId;
            final String mqtt_link ;
            Bundle extras = getIntent().getExtras();
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

        Button sendMqttQuery = (Button) findViewById(R.id.button_mqtt_send_query );
        final EditText mqtt_topic = (EditText) findViewById(R.id.mqtt_topic);
        final EditText mqtt_msg = (EditText) findViewById(R.id.mqtt_msg);
        sendMqttQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = mqtt_topic.getText().toString();
                String payload = mqtt_msg.getText().toString();
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }
                Toast.makeText(mqtt_cmd_window.this,"MQTT message sent",Toast.LENGTH_LONG).show();
                mqtt_topic.setText("");
                mqtt_msg.setText("");
            }
        });
    }

}
