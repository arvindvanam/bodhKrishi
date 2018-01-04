package com.bodhileaf.agriMonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by shwetathareja on 1/4/18.
 */

public class sensorItemAdapter extends ArrayAdapter<String> {
    private HashMap<Integer, Integer> sensorTypeHash;
    private List<String> sensorList;

    public  sensorItemAdapter(Context context, List<String> sensor_list, HashMap<Integer, Integer> sensor_type_hash) {
        super(context,R.layout.row_item,R.id.node_name,sensor_list);
        sensorTypeHash = sensor_type_hash;
        sensorList = sensor_list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = super.getView(position, convertView, parent);
        ImageButton sensorTypeButton = (ImageButton) listItem.findViewById(R.id.button_sensor_type_navigate);
        switch (sensorTypeHash.get(Integer.valueOf(sensorList.get(position)))){
            case 0:
                sensorTypeButton.setBackgroundResource(R.drawable.sensor);
                break;
            case 1:
                sensorTypeButton.setBackgroundResource(R.drawable.water_level);
                break;
        }
        sensorTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //goto maps
            }
        });
        return listItem;
    }
}
