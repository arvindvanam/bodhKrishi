package com.bodhileaf.agriMonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MarkerDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MarkerDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MarkerDialogFragment extends android.app.DialogFragment {

    // Use this instance of the interface to deliver action events
    MarkerDialogListener mListener;
    private Integer node_type_selection;
    private Integer n_id,n_type;
    private String dbFileName;
    private Activity mActivity;



    @Override
        public Dialog  onCreateDialog(Bundle savedInstanceState) {
            // Build the dialog and set up the button click handlers
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
             LayoutInflater inflater = getActivity().getLayoutInflater();
        Log.d("Marker Dialog Fragment", "onCreateView: spinner creator");
        View view = inflater.inflate(R.layout.add_marker, null);
        List<String>  node_type_list;
        ArrayAdapter<String> nodeAdapter;
        final EditText nodeid = (EditText) view.findViewById(R.id.markerNodeId);
        node_type_list = new ArrayList<String>();
        node_type_list.add("Soil/Air sensor");
        node_type_list.add("Water Level sensor");
        node_type_list.add("Water valve");

        //Spinner
        final Spinner nodeTypeSpinner =  view.findViewById(R.id.spinner_node_type);
        final Button config_button = (Button) view.findViewById(R.id.button_marker_config);
        nodeAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, node_type_list);
        nodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nodeTypeSpinner.setAdapter(nodeAdapter);
        nodeid.setText(n_id.toString());
        nodeTypeSpinner.setSelection(n_type);


        nodeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                node_type_selection = position;
                Log.d("Marker Dialog Fragment", "onCreateView: position="+Integer.valueOf(position).toString());
                Toast.makeText(view.getContext(),"position: "+ Integer.valueOf(position).toString(),Toast.LENGTH_SHORT);
                if(node_type_selection != 2)  { /*config only required for actuator node*/
                    config_button.setVisibility(View.INVISIBLE);
                } else {
                    config_button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button nodeid_incr = (Button) view.findViewById(R.id.increment_node_button);
        nodeid_incr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer cur_value = Integer.valueOf(nodeid.getText().toString());
                cur_value--;
                nodeid.setText(cur_value.toString());
            }
        });
        Button nodeid_decr = (Button) view.findViewById(R.id.decrement_node_button);
        nodeid_decr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer cur_value = Integer.valueOf(nodeid.getText().toString());
                cur_value++;
                nodeid.setText(cur_value.toString());
            }
        });

        config_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent configScreen = new Intent(v.getContext(), config.class);
                configScreen.putExtra("nodeId",Integer.valueOf(nodeid.getText().toString()));
                configScreen.putExtra("nodeType",nodeTypeSpinner.getSelectedItem().toString());
                configScreen.putExtra("dbFileName",dbFileName);
                startActivity(configScreen);

            }
        });

        Button control_button = (Button) view.findViewById(R.id.button_marker_control);
        control_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mqttGui = new Intent(v.getContext(), com.bodhileaf.agriMonitor.MqttGuiActivity.class);
                String mqtt_link = "tcp://192.168.0.3:1883";
                String clientId = MqttClient.generateClientId();
                mqttGui.putExtra("link",mqtt_link);
                mqttGui.putExtra("clientid",clientId);
                mqttGui.putExtra("filename",dbFileName);
                startActivity(mqttGui);
            }
        });

        Button stats_button = (Button) view.findViewById(R.id.button_marker_stats);
        stats_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statsScreen = new Intent(v.getContext(), com.bodhileaf.agriMonitor.StatisticsActivity.class);
                statsScreen.putExtra("filename",dbFileName);
                statsScreen.putExtra("nodeId",Integer.valueOf(nodeid.getText().toString()));
                startActivity(statsScreen);
            }
        });

        // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view)
                    .setPositiveButton(R.string.saveMarker, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the positive button event back to the host activity
                            mListener.onDialogPositiveClick(MarkerDialogFragment.this);
                        }
                    })
                    .setNegativeButton(R.string.cancelMarker, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the negative button event back to the host activity
                            mListener.onDialogNegativeClick(MarkerDialogFragment.this);
                        }
                    });

            return builder.create();
        }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (MarkerDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    public MarkerDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MarkerDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MarkerDialogFragment newInstance() {
        MarkerDialogFragment fragment = new MarkerDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
                n_id = getArguments().getInt("node_id");
                n_type = getArguments().getInt("node_type");
                dbFileName = getArguments().getString("filename");

        }


        // Retrieve the data from the marker.

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.add_marker, container,false);
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface MarkerDialogListener {
        public void onDialogPositiveClick(android.app.DialogFragment dialog);
        public void onDialogNegativeClick(android.app.DialogFragment dialog);
    }
}
