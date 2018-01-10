package com.bodhileaf.agriMonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
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
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FarmDetailsDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FarmDetailsDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FarmDetailsDialogFragment extends android.app.DialogFragment {

    private static final String TAG = FarmDetailsDialogFragment.class.getSimpleName();
    // Use this instance of the interface to deliver action events
    FarmDetailsDialogListener mListener;
    private Activity mActivity;



    @Override
        public Dialog  onCreateDialog(Bundle savedInstanceState) {
            // Build the dialog and set up the button click handlers
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            Log.d(TAG, "onCreateView: spinner creator");
            View view = inflater.inflate(R.layout.newfarmdetails, null);


        // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(view)
                    .setPositiveButton(R.string.saveMarker, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the positive button event back to the host activity
                            mListener.onDialogPositiveClick(FarmDetailsDialogFragment.this);
                        }
                    })
                    .setNegativeButton(R.string.cancelMarker, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Send the negative button event back to the host activity
                            mListener.onDialogNegativeClick(FarmDetailsDialogFragment.this);
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
            mListener = (FarmDetailsDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    public FarmDetailsDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MarkerDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FarmDetailsDialogFragment newInstance() {
        FarmDetailsDialogFragment fragment = new FarmDetailsDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
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
    public interface FarmDetailsDialogListener {
        public void onDialogPositiveClick(android.app.DialogFragment dialog);
        public void onDialogNegativeClick(android.app.DialogFragment dialog);
    }
}
