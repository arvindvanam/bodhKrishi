package com.bodhileaf.agriMonitor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.lang.reflect.Field;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LineChartActivity extends LineBase implements OnSeekBarChangeListener {

    private static final String TAG = "LineChartActivity" ;
    private LineChart mChart[]= new LineChart[4];;
    private SeekBar mSeekBarX;
    private TextView tvX;
    private Integer nodeId;
    private Integer nodeType;
    private String dbFileName;
    private Cursor dataSet;
    SQLiteDatabase agriDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.d("schedule frag", "onCreate: null bundle ");
            return;
        }

        nodeId = extras.getInt("nodeId");
        dbFileName = extras.getString("filename");
        agriDB = openOrCreateDatabase(dbFileName,MODE_PRIVATE ,null);
        String query = String.format("SELECT nodeType FROM nodesInfo where nodeID=%d",nodeId);
        Cursor nodeTypeResult= agriDB.rawQuery(query,null);
        nodeTypeResult.moveToFirst();
        nodeType = nodeTypeResult.getInt(0);
        nodeTypeResult.close();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_linechart_time);
        tvX = (TextView) findViewById(R.id.tvXMax);
        mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBarX.setProgress(100);
        tvX.setText("100");

        mChart[0] = (LineChart) findViewById(R.id.air_temp_chart);
        mChart[1] = (LineChart) findViewById(R.id.air_humidity_chart);
        mChart[2] = (LineChart) findViewById(R.id.soil_temp_chart);
        mChart[3] = (LineChart) findViewById(R.id.soil_humidity_chart);
        mSeekBarX.setOnSeekBarChangeListener(this);
        for (int i=0; i <4 ; i++) {


            // no description text
            mChart[i].getDescription().setEnabled(false);

            // enable touch gestures
            mChart[i].setTouchEnabled(true);

            mChart[i].setDragDecelerationFrictionCoef(0.9f);

            // enable scaling and dragging
            mChart[i].setDragEnabled(true);
            mChart[i].setScaleEnabled(true);
            mChart[i].setDrawGridBackground(false);
            mChart[i].setHighlightPerDragEnabled(true);

            // set an alternative background color
            mChart[i].setBackgroundColor(Color.WHITE);
            mChart[i].setViewPortOffsets(0f, 0f, 0f, 0f);

            // add data
            try {
                setData(100, 30);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mChart[i].invalidate();

            // get the legend (only possible after setting data)
            Legend l = mChart[i].getLegend();
            l.setEnabled(false);

            XAxis xAxis = mChart[i].getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
            xAxis.setTypeface(mTfLight);
            xAxis.setTextSize(6f);
            xAxis.setTextColor(Color.BLUE);
            xAxis.setDrawAxisLine(true);
            xAxis.setDrawGridLines(true);
            xAxis.setTextColor(Color.rgb(0, 0, 0));
            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(1f); // 1hour
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm,dd/MM");

                @Override
                public String getFormattedValue(float value, AxisBase axis) {

                    long millis = TimeUnit.HOURS.toMillis((long) value);
                    return mFormat.format(new Date(millis));
                }
            });

            YAxis leftAxis = mChart[i].getAxisLeft();
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
            leftAxis.setTypeface(mTfLight);
            leftAxis.setTextColor(ColorTemplate.getHoloBlue());
            leftAxis.setDrawGridLines(false);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setAxisMinimum(0f);
            leftAxis.setAxisMaximum(60f);
            leftAxis.setYOffset(-9f);
            leftAxis.setTextColor(Color.rgb(0, 0, 255));

            YAxis rightAxis = mChart[i].getAxisRight();
            rightAxis.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.line, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionToggleValues: {
                List<ILineDataSet> sets = mChart[0].getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setDrawValues(!set.isDrawValuesEnabled());
                }

                mChart[0].invalidate();
                break;
            }
            case R.id.actionToggleHighlight: {
                if (mChart[0].getData() != null) {
                    mChart[0].getData().setHighlightEnabled(!mChart[0].getData().isHighlightEnabled());
                    mChart[0].invalidate();
                }
                break;
            }
            case R.id.actionToggleFilled: {

                List<ILineDataSet> sets = mChart[0].getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawFilledEnabled())
                        set.setDrawFilled(false);
                    else
                        set.setDrawFilled(true);
                }
                mChart[0].invalidate();
                break;
            }
            case R.id.actionToggleCircles: {
                List<ILineDataSet> sets = mChart[0].getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawCirclesEnabled())
                        set.setDrawCircles(false);
                    else
                        set.setDrawCircles(true);
                }
                mChart[0].invalidate();
                break;
            }
            case R.id.actionToggleCubic: {
                List<ILineDataSet> sets = mChart[0].getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.getMode() == LineDataSet.Mode.CUBIC_BEZIER)
                        set.setMode(LineDataSet.Mode.LINEAR);
                    else
                        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                }
                mChart[0].invalidate();
                break;
            }
            case R.id.actionToggleStepped: {
                List<ILineDataSet> sets = mChart[0].getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.getMode() == LineDataSet.Mode.STEPPED)
                        set.setMode(LineDataSet.Mode.LINEAR);
                    else
                        set.setMode(LineDataSet.Mode.STEPPED);
                }
                mChart[0].invalidate();
                break;
            }
            case R.id.actionTogglePinch: {
                if (mChart[0].isPinchZoomEnabled())
                    mChart[0].setPinchZoom(false);
                else
                    mChart[0].setPinchZoom(true);

                mChart[0].invalidate();
                break;
            }
            case R.id.actionToggleAutoScaleMinMax: {
                mChart[0].setAutoScaleMinMaxEnabled(!mChart[0].isAutoScaleMinMaxEnabled());
                mChart[0].notifyDataSetChanged();
                break;
            }
            case R.id.animateX: {
                mChart[0].animateX(3000);
                break;
            }
            case R.id.animateY: {
                mChart[0].animateY(3000);
                break;
            }
            case R.id.animateXY: {
                mChart[0].animateXY(3000, 3000);
                break;
            }

            case R.id.actionSave: {
                if (mChart[0].saveToPath("title" + System.currentTimeMillis(), "")) {
                    Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!",
                            Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                            .show();

                // mChart.saveToGallery("title"+System.currentTimeMillis())
                break;
            }
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText("" + (mSeekBarX.getProgress()));

        try {
            setData(mSeekBarX.getProgress(), 50);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // redraw
        mChart[0].invalidate();
    }

    private void setData(int count, float range) throws ParseException {

        // now in hours
        Date sDate = null;
        long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //long start = now - 24*30*3;
        //long end = now - 24*30*1;
        Date start_date = df.parse("2017-10-19 00:00:00");
        Date end_date = df.parse("2017-10-28 00:00:00");
        long start = TimeUnit.MILLISECONDS.toHours(start_date.getTime());
        long end = TimeUnit.MILLISECONDS.toHours(end_date.getTime());

        String query=null;
        ArrayList<Entry> values = new ArrayList<Entry>();
        ArrayList<Entry> values1 = new ArrayList<Entry>();
        ArrayList<Entry> values2 = new ArrayList<Entry>();
        ArrayList<Entry> values3 = new ArrayList<Entry>();
        Float airTemperature = 0f;
        Float airHumidity = 0f;
        Float soilTemperature = 0f;
        Float soilMoisture = 0f;



          switch(nodeType) {
            case 0:
                               break;
            case 1:
                query = String.format("SELECT timeStamp,waterLevel,waterFlowRate FROM sensorData WHERE nodeID=%d ORDER by DATE(timeStamp) ASC", nodeId);
                break;
            case 2:
                query = String.format("SELECT timeStamp, FROM actuatorData WHERE nodeID=%d ORDER by DATE(timeStamp) ASC", nodeId);
                break;
        }


        Log.d(TAG,String.format("============================setData ======================="));
        // increment by 1 hour
        for (long x = start; x < end; x+=1) {
            switch (nodeType) {
                case 0:
                    //do {
                    long start_time = TimeUnit.HOURS.toSeconds(x);
                    long end_time = TimeUnit.HOURS.toSeconds(x + 1);
                    query = String.format("SELECT AVG(airTemperature),AVG(airHumidity),AVG(soilTemperature),AVG(soilMoisture) FROM sensorData WHERE nodeID=%d AND CAST(strftime('%%s', timestamp) as int) between %d AND %d  ", nodeId, start_time, end_time);
                    Cursor dataSet = agriDB.rawQuery(query, null);
                    Log.d(TAG, "setData: query: "+query);
                    dataSet.moveToFirst();
                    //query = String.format("SELECT AVG(airTemperature) FROM sensorData WHERE nodeID=%d AND CAST(strftime('%%s', timestamp) as int) between %d AND %d  ", nodeId, start_time, end_time);
                    //Log.d(TAG, "setData: query: "+query);
                    //Cursor atemp_dataSet = agriDB.rawQuery(query, null);
                    //query = String.format("SELECT AVG(airHumidity) FROM sensorData WHERE nodeID=%d AND CAST(strftime('%%s', timestamp) as int) between %d AND %d  ", nodeId, start_time, end_time);
                    //Log.d(TAG, "setData: query: "+query);
                    //Cursor ahumid_dataSet = agriDB.rawQuery(query, null);
                    //query = String.format("SELECT AVG(soilTemperature) FROM sensorData WHERE nodeID=%d AND CAST(strftime('%%s', timestamp) as int) between %d AND %d  ", nodeId, start_time, end_time);
                    //Log.d(TAG, "setData: query: "+query);
                    //Cursor stemp_dataSet = agriDB.rawQuery(query, null);
                    //query = String.format("SELECT AVG(soilMoisture) FROM sensorData WHERE nodeID=%d AND CAST(strftime('%%s', timestamp) as int) between %d AND %d  ", nodeId, start_time, end_time);
                    //Log.d(TAG, "setData: query: "+query);
                    //Cursor shumid_dataSet = agriDB.rawQuery(query, null);
                    //atemp_dataSet.moveToFirst();
                    //ahumid_dataSet.moveToFirst();
                    //stemp_dataSet.moveToFirst();
                    //shumid_dataSet.moveToFirst();


                    if(dataSet.getCount() != 0) {
                        //String timeStamp = dataSet.getString(0);
                        //TODO: it maybe better to address a column with name rather than column number
                        // as databse might change column number in future
                        if(!dataSet.isNull(0)) {
                            airTemperature = dataSet.getFloat(0);
                            values.add(new Entry(x, airTemperature));
                        }
                        if(!dataSet.isNull(1)) {
                            airHumidity = dataSet.getFloat(1);
                            values1.add((new Entry(x, airHumidity)));
                        }
                        if(!dataSet.isNull(2)) {
                            soilTemperature = dataSet.getFloat(2);
                            values2.add((new Entry(x, soilTemperature)));
                        }
                        if(!dataSet.isNull(3)) {
                            soilMoisture = dataSet.getFloat(3);
                            values3.add((new Entry(x, soilMoisture)));
                        }


                        //   try {
                        //      sDate = df.parse(timeStamp);
                        //  } catch (ParseException e) {
                        //      e.printStackTrace();
                        //  }
                        //   time = sDate.getTime();
                        //Log.d(TAG, String.format("setData , Time:%tc", time));
                        Log.d(TAG, String.format("setData: A_temp:%f A_humid:%f S_temp:%f S_humid:%f ",airTemperature,airHumidity,soilTemperature,soilMoisture));

                    }





                    //   } while(dataSet.moveToNext());
                    break;
            }
        }

      //      float y = getRandom(range, 50);
      //      float y1 = getRandom(range, 20);
      //      values.add(new Entry(x, y)); // add one entry per hour
      //      values1.add(new Entry(x, y1));
      //  }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "Air Temperature");

        set1.setAxisDependency(AxisDependency.LEFT);
        set1.setColor(Color.GREEN);
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(true);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(255, 0, 0));
        set1.setDrawCircleHole(false);
        // create a dataset and give it a type
        LineDataSet set2 = new LineDataSet(values1, "Air Humidity");
        set2.setAxisDependency(AxisDependency.LEFT);
        set2.setColor(Color.BLUE);
        set2.setValueTextColor(ColorTemplate.getHoloBlue());
        set2.setLineWidth(1.5f);
        set2.setDrawCircles(true);
        set2.setDrawValues(false);
        set2.setFillAlpha(65);
        set2.setFillColor(Color.YELLOW);
        set2.setHighLightColor(Color.rgb(0, 255, 0));
        set2.setDrawCircleHole(true);
        LineDataSet set3 = new LineDataSet(values2, "Soil Temperature");
        set3.setAxisDependency(AxisDependency.LEFT);
        set3.setColor(ColorTemplate.getHoloBlue());
        set3.setValueTextColor(ColorTemplate.getHoloBlue());
        set3.setLineWidth(1.5f);
        set3.setDrawCircles(true);
        set3.setDrawValues(false);
        set3.setFillAlpha(65);
        set3.setFillColor(Color.GRAY);
        set3.setHighLightColor(Color.rgb(244, 0, 117));
        set3.setDrawCircleHole(false);
        LineDataSet set4 = new LineDataSet(values3, "Soil Mositure");
        set4.setAxisDependency(AxisDependency.LEFT);
        set4.setColor(ColorTemplate.getHoloBlue());
        set4.setValueTextColor(ColorTemplate.getHoloBlue());
        set4.setLineWidth(1.5f);
        set4.setDrawCircles(true);
        set4.setDrawValues(false);
        set4.setFillAlpha(65);
        set4.setFillColor(ColorTemplate.getHoloBlue());
        set4.setHighLightColor(Color.rgb(244, 0, 117));
        set4.setDrawCircleHole(false);
        //ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        //dataSets.add(set1);
        //dataSets.add(set2);
        // create a data object with the datasets
        LineData data = new LineData(set1);
        LineData data1 = new LineData(set2);
        LineData data2 = new LineData(set3);
        LineData data3 = new LineData(set4);
        data.setValueTextColor(Color.DKGRAY);
        data.setValueTextSize(3f);
        data1.setValueTextColor(Color.DKGRAY);
        data1.setValueTextSize(3f);
        data2.setValueTextColor(Color.DKGRAY);
        data2.setValueTextSize(3f);
        data3.setValueTextColor(Color.DKGRAY);
        data3.setValueTextSize(3f);
        // create a data object with the datasets

        // set data
        mChart[0].setData(data);
        mChart[1].setData(data1);
        mChart[2].setData(data2);
        mChart[3].setData(data3);


}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }
}
