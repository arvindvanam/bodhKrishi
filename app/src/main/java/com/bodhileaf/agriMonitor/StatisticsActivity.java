package com.bodhileaf.agriMonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG = "StasticsActivity" ;
    private String dbFileName;
    private Integer nodeID;
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
    }
}
