package com.bodhileaf.agriMonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG = "StasticsActivity" ;
    private String dbFileName;
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
        if (dbFileName == null) {
            Log.d(TAG, "onreate: db filename missing" );
            return;
        }
        Log.d(TAG, "onreate: db filename "+dbFileName );

    }
}
