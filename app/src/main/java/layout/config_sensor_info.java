package layout;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.bodhileaf.agriMonitor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link config_sensor_info.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link config_sensor_info#newInstance} factory method to
 * create an instance of this fragment.
 */
public class config_sensor_info extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Integer nodeId;
    private Integer nodeType;
    private String dbFileName;
    private View view;
    private List<String> nodeList;
    private SQLiteDatabase agriDb;
    private ArrayAdapter<String> nodeAdapter;
    private Spinner nodeSpinner;
    private Integer listCurPos;
    private boolean match = false;


    private OnFragmentInteractionListener mListener;

    public config_sensor_info() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment config_sensor_info.
     */
    // TODO: Rename and change types and number of parameters
    public static config_sensor_info newInstance(String param1, String param2) {
        config_sensor_info fragment = new config_sensor_info();
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



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_config_sensor_info, container, false);
        final EditText soilMoistureLowThresh = (EditText) view.findViewById(R.id.soilMoistureLowerThresh);
        final EditText soilMoistureHighThresh = (EditText) view.findViewById(R.id.soilMositureUpperThresh);
        final EditText sampleRelWindow = (EditText) view.findViewById(R.id.sampleRelWindow);
        soilMoistureLowThresh.setText("0");
        soilMoistureHighThresh.setText("20");
        sampleRelWindow.setText("0");
        final SQLiteDatabase agriDb = getActivity().openOrCreateDatabase(dbFileName, android.content.Context.MODE_PRIVATE, null);

        Button saveButton = (Button) view.findViewById(R.id.sensorInfoSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(match) {
                    //delete existing entry
                    String deleteRowQuery = String.format("DELETE FROM params WHERE actuatorNodeId is %d",nodeId);
                    agriDb.execSQL(deleteRowQuery);
                }
                String insertRowQuery = String.format("INSERT INTO params(ActuatorNodeId,soilMoistureLowerThreshold,soilMoistureUpperThreshold,sampleRelevanceWindow) VALUES(%d,%s,'%s','%s')", nodeId, soilMoistureLowThresh.getText().toString(), soilMoistureHighThresh.getText().toString(), sampleRelWindow.getText().toString());
                agriDb.execSQL(insertRowQuery);

            }
        });

        initNodeList(agriDb);
        nodeSpinner =  view.findViewById(R.id.nodeid_spinner_node_cfg);
        nodeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, nodeList);
        nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeSpinner.setAdapter(nodeAdapter);
        nodeSpinner.setSelection(listCurPos);
        nodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String listEntry = nodeList.get(position).toString();
                // case where the selected node is newly added
                if (match) {
                    //show all default values in the UI
                } else {
                    //case where the selected node already existed
                    String query = String.format("SELECT * FROM params where actuatorNodeId is %s" ,listEntry);
                    //check if the schedule id exists for the node id
                    Cursor nodeListResults = agriDb.rawQuery(query,null);
                    nodeListResults.moveToFirst();
                    if (nodeListResults.getCount() == 0) {
                        //load default values in the UI
                    } else {
                        Integer moisture_low_thresh_value = nodeListResults.getInt(1);
                        Integer moisture_high_thresh_value = nodeListResults.getInt(2);
                        Integer sample_rel_window_size_value = nodeListResults.getInt(3);
                        soilMoistureLowThresh.setText(moisture_low_thresh_value.toString());
                        soilMoistureHighThresh.setText(moisture_high_thresh_value.toString());
                        sampleRelWindow.setText(sample_rel_window_size_value.toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button inc_low_thresh = view.findViewById(R.id.button_incr_lower_threshold);
        Button dec_low_thresh = view.findViewById(R.id.button_decr_lower_threshold);
        Button inc_high_thresh = view.findViewById(R.id.button_incr_upper_threshold);
        Button dec_high_thresh = view.findViewById(R.id.button_decr_upper_threshold);
        Button inc_rel_wind = view.findViewById(R.id.button_incr_sample_rel_window);
        Button dec_rel_wind = view.findViewById(R.id.button_decr_sample_rel_window);
        inc_low_thresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer val = Integer.valueOf(soilMoistureLowThresh.getText().toString());
                val++;
                soilMoistureLowThresh.setText(val.toString());

            }
        });
        dec_low_thresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer val = Integer.valueOf(soilMoistureLowThresh.getText().toString());
                val--;
                soilMoistureLowThresh.setText(val.toString());

            }
        });
        inc_high_thresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer val = Integer.valueOf(soilMoistureHighThresh.getText().toString());
                val++;
                soilMoistureHighThresh.setText(val.toString());
            }
        });
        dec_high_thresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer val = Integer.valueOf(soilMoistureHighThresh.getText().toString());
                val--;
                soilMoistureHighThresh.setText(val.toString());

            }
        });
        inc_rel_wind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer val = Integer.valueOf(sampleRelWindow.getText().toString());
                val++;
                sampleRelWindow.setText(val.toString());
            }
        });
        dec_rel_wind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer val = Integer.valueOf(sampleRelWindow.getText().toString());
                val--;
                sampleRelWindow.setText(val.toString());
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }

        //EditText
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
