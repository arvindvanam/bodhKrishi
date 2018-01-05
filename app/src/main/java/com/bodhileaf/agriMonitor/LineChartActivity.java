package com.bodhileaf.agriMonitor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LineChartActivity extends LineBase implements OnSeekBarChangeListener {

    private LineChart mChart[]= new LineChart[4];;
    private SeekBar mSeekBarX;
    private TextView tvX;
    private Integer nodeId;
    private Integer nodeType;
    private String dbFileName;
    private Cursor dataSet;

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
        SQLiteDatabase agriDB = openOrCreateDatabase(dbFileName,MODE_PRIVATE ,null);
        String query = String.format("SELECT nodeType FROM nodesInfo where nodeID=%d",nodeId);
        Cursor nodeTypeResult= agriDB.rawQuery(query,null);
        nodeTypeResult.moveToFirst();
        nodeType = nodeTypeResult.getInt(0);
        switch(nodeType) {
            case 0:
                query = String.format("SELECT timeStamp,airTemperature,airHumidity,soilTemperature,soilMoisture FROM sensorData WHERE nodeID=%d ORDER by DATE(timeStamp) ASC ",nodeId);
                break;
            case 1:
                query = String.format("SELECT timeStamp,waterLevel,waterFlowRate FROM sensorData WHERE nodeID=%d ORDER by DATE(timeStamp) ASC", nodeId);
                break;
            case 2:
                query = String.format("SELECT timeStamp, FROM actuatorData WHERE nodeID=%d ORDER by DATE(timeStamp) ASC", nodeId);
                break;
        }
        dataSet = agriDB.rawQuery(query,null);
        dataSet.moveToFirst();

        Log.d("schedule frag Node info", "onCreate: nodeid/nodetype "+ String.valueOf(nodeId)+"/"+String.valueOf(nodeType));
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
            setData(100, 30);
            mChart[i].invalidate();

            // get the legend (only possible after setting data)
            Legend l = mChart[i].getLegend();
            l.setEnabled(false);

            XAxis xAxis = mChart[i].getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
            xAxis.setTypeface(mTfLight);
            xAxis.setTextSize(10f);
            xAxis.setTextColor(Color.BLUE);
            xAxis.setDrawAxisLine(true);
            xAxis.setDrawGridLines(true);
            xAxis.setTextColor(Color.rgb(0, 0, 0));
            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(1f); // one hour
            xAxis.setValueFormatter(new IAxisValueFormatter() {

                private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM HH:mm");

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
            leftAxis.setDrawGridLines(true);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setAxisMinimum(-20f);
            leftAxis.setAxisMaximum(60f);
            leftAxis.setYOffset(-9f);
            leftAxis.setTextColor(Color.rgb(0, 0, 0));

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

        setData(mSeekBarX.getProgress(), 50);

        // redraw
        mChart[0].invalidate();
    }

    private void setData(int count, float range) {

        // now in hours
        Date sDate = null;
        //long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
        long time;
        ArrayList<Entry> values = new ArrayList<Entry>();
        ArrayList<Entry> values1 = new ArrayList<Entry>();
        ArrayList<Entry> values2 = new ArrayList<Entry>();
        ArrayList<Entry> values3 = new ArrayList<Entry>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dataSet.moveToFirst();
        switch(nodeType) {
            case 0:
                do {
                    String timeStamp = dataSet.getString(0);
                    Float airTemperature = dataSet.getFloat(1);
                    Float airHumidity = dataSet.getFloat(2);
                    Float soilTemperature = dataSet.getFloat(3);
                    Float soilMoisture = dataSet.getFloat(4);
                    try {
                        sDate = df.parse(timeStamp);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    time = sDate.getTime();
                    
                    values.add(new Entry(time,airTemperature));
                    values1.add((new Entry(time,airHumidity)));
                    values2.add((new Entry(time,soilTemperature)));
                    values3.add((new Entry(time,soilMoisture)));
                } while(dataSet.moveToNext());
             break;
        }
      //  float from = now;
//
        // count = hours
      //  float to = now + count;

        // increment by 1 hour
     //   for (float x = from; x < to; x++) {
//
      //      float y = getRandom(range, 50);
      //      float y1 = getRandom(range, 20);
      //      values.add(new Entry(x, y)); // add one entry per hour
      //      values1.add(new Entry(x, y1));
      //  }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "Air Temperature");

        set1.setAxisDependency(AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(false);
        set1.setDrawValues(true);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);
        // create a dataset and give it a type
        LineDataSet set2 = new LineDataSet(values1, "Air Humidity");
        set2.setAxisDependency(AxisDependency.LEFT);
        set2.setColor(ColorTemplate.getHoloBlue());
        set2.setValueTextColor(ColorTemplate.getHoloBlue());
        set2.setLineWidth(1.5f);
        set2.setDrawCircles(false);
        set2.setDrawValues(true);
        set2.setFillAlpha(65);
        set2.setFillColor(ColorTemplate.getHoloBlue());
        set2.setHighLightColor(Color.rgb(244, 0, 117));
        set2.setDrawCircleHole(false);
        LineDataSet set3 = new LineDataSet(values2, "Soil Temperature");
        set3.setAxisDependency(AxisDependency.LEFT);
        set3.setColor(ColorTemplate.getHoloBlue());
        set3.setValueTextColor(ColorTemplate.getHoloBlue());
        set3.setLineWidth(1.5f);
        set3.setDrawCircles(false);
        set3.setDrawValues(true);
        set3.setFillAlpha(65);
        set3.setFillColor(ColorTemplate.getHoloBlue());
        set3.setHighLightColor(Color.rgb(244, 0, 117));
        set3.setDrawCircleHole(false);
        LineDataSet set4 = new LineDataSet(values3, "Soil Mositure");
        set4.setAxisDependency(AxisDependency.LEFT);
        set4.setColor(ColorTemplate.getHoloBlue());
        set4.setValueTextColor(ColorTemplate.getHoloBlue());
        set4.setLineWidth(1.5f);
        set4.setDrawCircles(false);
        set4.setDrawValues(true);
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
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);
        data1.setValueTextColor(Color.BLACK);
        data1.setValueTextSize(9f);
        data2.setValueTextColor(Color.BLUE);
        data2.setValueTextSize(9f);
        data3.setValueTextColor(Color.GREEN);
        data3.setValueTextSize(9f);
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
