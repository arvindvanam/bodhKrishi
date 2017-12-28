package layout;

import java.util.ArrayList;
import java.util.List;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.app.DialogFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import com.bodhileaf.agriMonitor.EndDateFragment;
import com.bodhileaf.agriMonitor.R;
import com.bodhileaf.agriMonitor.StartDateFragment;
import com.bodhileaf.agriMonitor.TimeFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link config_schedule.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link config_schedule#newInstance} factory method to
 * create an instance of this fragment.
 */

public class config_schedule extends Fragment  implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    private Integer nodeId;
    private Integer nodeType;
    private String dbFileName;
    private List<String> nodeList;
    private SQLiteDatabase agriDb;
    private ArrayAdapter<String> nodeAdapter;
    private Spinner nodeSpinner;
    private Integer listCurPos;
    private boolean match = false;

    private OnFragmentInteractionListener mListener;

    public Fragment showTimePickerDialog(View view) {
        DialogFragment newFragment = new TimeFragment() ;
        newFragment.show(getFragmentManager(), "timePicker");
        return newFragment;
    }
    public config_schedule() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment config_schedule.
     */
    // TODO: Rename and change types and number of parameters
    public static config_schedule newInstance(String param1, String param2) {
        config_schedule fragment = new config_schedule();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras == null) {
            Log.d("schedule frag", "onCreate: null bundle ");
            return;
        }
        nodeId = extras.getInt("nodeId");
        nodeType = extras.getInt("nodeType");
        dbFileName = extras.getString("dbFileName");
        Log.d("schedule frag Node info", "onCreate: nodeid/nodetype "+ String.valueOf(nodeId)+"/"+String.valueOf(nodeType));
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Log.d("schedule frag", "onCreate: open time picker ");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_config_schedule, container, false);
        final EditText startDatePicker = (EditText) view.findViewById(R.id.startDatePick);
        final EditText endDatePicker = (EditText) view.findViewById(R.id.endDatePick);
        final EditText startTime = (EditText) view.findViewById(R.id.timePick);
        final EditText scheduleId = (EditText) view.findViewById(R.id.scheduleId);
        final EditText scheduleDuration = (EditText) view.findViewById(R.id.durationInMin);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("schedule frag", "onClick: open time picker ");
                Fragment tempTime = showTimePickerDialog(getView());
                //String tempTimeValue = tempTime.getTime();

            }
        });

        startDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("schedule frag", "onClick: open start Date picker ");
                DialogFragment newStartDateFragment = new StartDateFragment() ;
                newStartDateFragment.show(getFragmentManager(), "startTimePicker");

            }
        });

        endDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("schedule frag", "onClick: open end date picker ");
                DialogFragment newEndDateFragment = new EndDateFragment() ;
                newEndDateFragment.show(getFragmentManager(), "endTimePicker");

            }
        });

        agriDb = getActivity().openOrCreateDatabase(dbFileName,android.content.Context.MODE_PRIVATE ,null);
        initNodeList(agriDb);
        nodeSpinner =  view.findViewById(R.id.nodeSpinner);
        nodeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, nodeList);
        nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSpinner.setAdapter(nodeAdapter);
        nodeSpinner.setSelection(listCurPos);
        nodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String listEntry = nodeList.get(position);
                // case where the selected node is newly added
                if (match) {
                    //show all default values in the UI
                } else {
                    //case where the selected node already existed
                    String query = String.format("SELECT scheduleId FROM scheduleList where nodeId=%d" ,nodeId);
                    //check if the schedule id exists for the node id
                    Cursor nodeListResults = agriDb.rawQuery(query,null);
                    nodeListResults.moveToFirst();
                    if (nodeListResults.getCount() == 0) {
                        //load default values in the UI
                    } else {
                        Integer schedule_id_value = nodeListResults.getInt(1);
                        String startDate_value = nodeListResults.getString(2);
                        String endDate_value = nodeListResults.getString(3);
                        String startTime_value = nodeListResults.getString(4);
                        Integer duration_value = nodeListResults.getInt(5);
                        scheduleId.setText(schedule_id_value.toString());
                        startDatePicker.setText(startDate_value);
                        endDatePicker.setText(endDate_value);
                        startTime.setText(startTime_value);
                        scheduleDuration.setText(duration_value.toString());
                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });
        Button saveButton = (Button) view.findViewById(R.id.configSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


    //            int start_hour;
      //          int start_min;
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          //          start_hour = startTime.getHour();
            //        start_min = startTime.getMinute();
              //  } else {
                //    start_hour = startTime.getCurrentHour();
                  //  start_min = startTime.getCurrentMinute();
                //}


                DateFormat df = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
                String startDateString = startDatePicker.getText().toString()+" "+startTime.getText().toString()+":00";
                String endDateString = endDatePicker.getText().toString()+" "+startTime.getText().toString()+":00";



                try {
                    java.util.Date mDate = df.parse(startDateString);
                    long start_time_ms = mDate.getTime();
                    Log.d("date picker", "onClick: start Date in milli :: " + start_time_ms);
                    mDate = df.parse(endDateString);
                    long end_time_ms = mDate.getTime();
                    Log.d("date picker", "onClick: End Date in milli :: " + end_time_ms);
                    String query = String.format("SELECT * FROM scheduleList where nodeId=%d and scheduleId=%d",nodeId,
                            Integer.parseInt(scheduleId.getText().toString()));
                    //check if the schedule id exists for the node id
                    Cursor nodeListResults = agriDb.rawQuery(query,null);
                    if (nodeListResults.getCount() != 0 ) {
                        query = String.format("DELETE FROM scheduleList where nodeId=%d and scheduleId=%d",nodeId,
                                Integer.parseInt(scheduleId.getText().toString()));
                        //check if the schedule id exists for the node id
                        agriDb.execSQL(query);
                        query = String.format("DELETE FROM scheduleInfo where nodeId=%d and scheduleId=%d",nodeId,
                                Integer.parseInt(scheduleId.getText().toString()));

                        //remove the entry
                    }
                        String insertRowQuery = String.format("INSERT INTO scheduleList(nodeId,scheduleId,startDate,endDate,startTime,duration) VALUES(%d,%s,'%s','%s','%s',%s)", nodeId, scheduleId.getText().toString(), startDatePicker.getText().toString(), endDatePicker.getText().toString(),startTime.getText().toString(), scheduleDuration.getText().toString());
                        agriDb.execSQL(insertRowQuery);
                    DateFormat cur_date_format= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Log.d("config schedule", "onClick: row query" +insertRowQuery);
                        for (long cur_date = start_time_ms; cur_date < end_time_ms; cur_date += 1000*60*60*24) {
                            Log.d("config_schedule", String.format("onClick: date: " + cur_date_format.format(cur_date) ));
                            insertRowQuery = String.format("INSERT INTO scheduleInfo(rowIndex,dateTimeValue,actuatorNodeId,duration,actionTaken,scheduleId) VALUES(%d,'%s',%d,'%s','PENDING',%d)", Integer.getInteger(scheduleId.getText().toString()), cur_date_format.format(cur_date), nodeId,scheduleDuration.getText().toString(),Integer.getInteger(scheduleId.getText().toString()));
                            agriDb.execSQL(insertRowQuery);
                        }
                    agriDb.execSQL("drop TABLE IF EXISTS scheduleInfo_temp");
                    agriDb.execSQL("CREATE TABLE scheduleInfo_temp(rowIndex INTEGER PRIMARY KEY, dateTimeValue TEXT NOT NULL, actuatorNodeID INT NOT NULL, duration INTEGER NOT NULL, actionTaken TEXT, scheduleId INTEGER)");
                    agriDb.execSQL("INSERT INTO scheduleInfo_temp (dateTimeValue,actuatorNodeId,duration,actionTaken,scheduleId) SELECT dateTimeValue,actuatorNodeId,duration,actionTaken,scheduleId FROM scheduleInfo ORDER BY actuatorNodeId,dateTimeValue ");
                    agriDb.execSQL("drop TABLE scheduleInfo");
                    agriDb.execSQL("ALTER TABLE scheduleInfo_temp RENAME TO scheduleInfo");


                } catch (ParseException e) {
                    e.printStackTrace();
                }



            }
        });
        return view;
    }

    private void initNodeList(SQLiteDatabase agriDb) {

        Integer curPos = 0;
        nodeList = new ArrayList<String>();
        String query = "SELECT nodeID FROM nodesInfo";
        //check if the schedule id exists for the node id
        Cursor nodeListResults = agriDb.rawQuery(query,null);
        nodeListResults.moveToFirst();
        do {

            Integer nodeID_from_list = nodeListResults.getInt(0);
            if (nodeId == nodeID_from_list) {
                match = true;
                listCurPos = curPos;
            }
            nodeList.add(nodeID_from_list.toString());
            Log.d("Config_schedule", "NodeList: "+nodeID_from_list.toString());
            curPos++;
        } while(nodeListResults.moveToNext());
        if(match == false) {
            listCurPos = curPos;
            nodeList.add(nodeId.toString());
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }




}
