package com.bodhileaf.buttontest;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimeFragment extends android.app.DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private TimePickerDialog tempTimePickerDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        tempTimePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        return tempTimePickerDialog;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        // Do something with the time chosen by the user
        EditText timePick = (EditText) getActivity().findViewById(R.id.timePick);
        String timeSet = String.format("%02d", hourOfDay)+":"+String.format("%02d", minute);
        timePick.setText(timeSet);
    }

}
