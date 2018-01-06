package com.bodhileaf.agriMonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG = "StatisticsActivity" ;
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
}
