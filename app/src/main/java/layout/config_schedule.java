package layout;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bodhileaf.buttontest.EndDateFragment;
import com.bodhileaf.buttontest.R;
import com.bodhileaf.buttontest.StartDateFragment;
import com.bodhileaf.buttontest.TimeFragment;

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
        EditText timePick = (EditText) view.findViewById(R.id.timePick);
        timePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("schedule frag", "onClick: open time picker ");
                Fragment tempTime = showTimePickerDialog(getView());
                //String tempTimeValue = tempTime.getTime();

            }
        });

        EditText startDatePick = (EditText) view.findViewById(R.id.startDatePick);
        startDatePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("schedule frag", "onClick: open start Date picker ");
                DialogFragment newStartDateFragment = new StartDateFragment() ;
                newStartDateFragment.show(getFragmentManager(), "startTimePicker");

            }
        });

        EditText endDatePick = (EditText) view.findViewById(R.id.endDatePick);
        endDatePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("schedule frag", "onClick: open end date picker ");
                DialogFragment newEndDateFragment = new EndDateFragment() ;
                newEndDateFragment.show(getFragmentManager(), "endTimePicker");

            }
        });

        return view;
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
