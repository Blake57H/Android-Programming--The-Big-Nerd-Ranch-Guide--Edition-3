package com.example.androidprogrammingbook02;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class TimePickerFragment extends Fragment {
    private static final String ARG_TIME = "time";
    static final String EXTRA_TIME = "com.example.androidprogrammingbook02.time";

    private TimePicker mTimePicker;
    private Button mPositiveButton, mNegativeButton;
    private static int year, month, day;
    private Date time;

    public static TimePickerFragment newInstance(Date time) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, time);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        time = (Date) getArguments().getSerializable(ARG_TIME);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_time, container, false);

        mTimePicker = v.findViewById(R.id.dialog_time_picker);
        mTimePicker.setIs24HourView(false);

        mPositiveButton = v.findViewById(R.id.time_picker_dialog_positive_button);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, minute;
                if (Build.VERSION.SDK_INT >= 23) {
                    hour = mTimePicker.getHour();
                    minute = mTimePicker.getMinute();
                } else {
                    hour = mTimePicker.getCurrentHour();
                    minute = mTimePicker.getCurrentMinute();
                }
                Date time = new GregorianCalendar(year, month, day, hour, minute).getTime();
                SendResult(Activity.RESULT_OK, time);
            }
        });

        mNegativeButton = v.findViewById(R.id.time_picker_dialog_negative_button);
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendResult(Activity.RESULT_CANCELED, null);
            }
        });

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        if (Build.VERSION.SDK_INT >= 23) {
            mTimePicker.setHour(calendar.get(Calendar.HOUR));
            mTimePicker.setMinute(calendar.get(Calendar.MINUTE));
        } else {
            mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR));
            mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }

        return v;
    }

/*
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);
        final Date time = (Date) getArguments().getSerializable(ARG_TIME);
        mTimePicker = v.findViewById(R.id.dialog_time_picker);
        mTimePicker.setIs24HourView(false);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        if (Build.VERSION.SDK_INT >= 23) {
            mTimePicker.setHour(calendar.get(Calendar.HOUR));
            mTimePicker.setMinute(calendar.get(Calendar.MINUTE));
        } else {
            mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR));
            mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }


        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_picker_title)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SendResult(Activity.RESULT_CANCELED, null);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hour, minute;
                        if (Build.VERSION.SDK_INT >= 23) {
                            hour = mTimePicker.getHour();
                            minute = mTimePicker.getMinute();
                        } else {
                            hour = mTimePicker.getCurrentHour();
                            minute = mTimePicker.getCurrentMinute();
                        }
                        Date time = new GregorianCalendar(year, month, day, hour, minute).getTime();
                        SendResult(Activity.RESULT_OK, time);
                    }
                })
                .create();
    }
*/


    private void SendResult(int resultCode, Date date) {
//        if (getTargetFragment() != null && date != null) {
//            Intent intent = new Intent();
//            intent.putExtra(EXTRA_TIME, date);
//            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
//        } else {
//            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
//        }
        if(date!= null){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_TIME, date);
            getActivity().setResult(resultCode, intent);
        }else{
            getActivity().setResult(resultCode);
        }
        getActivity().finish();
    }

}
