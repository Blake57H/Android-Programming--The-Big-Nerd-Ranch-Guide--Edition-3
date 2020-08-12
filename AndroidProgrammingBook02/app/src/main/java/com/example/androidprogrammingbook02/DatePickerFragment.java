package com.example.androidprogrammingbook02;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
    private static final String ATG_DATE = "date";
    static final String EXTRA_DATE = "com.example.androidprogrammingbook02.date";

    private DatePicker mDatePicker;
    private Button mPositiveButton, mNegativeButton;
    private Date date;

    public static DatePickerFragment newInstance(Date date) {

        Bundle args = new Bundle();
        args.putSerializable(ATG_DATE, date);

        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        date = (Date) getArguments().getSerializable(ATG_DATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_date, container, false);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year, month, day;
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        mDatePicker = v.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(year, month, day, null);

        mPositiveButton = v.findViewById(R.id.date_picker_dialog_positive_button);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = mDatePicker.getYear();
                int month = mDatePicker.getMonth();
                int day = mDatePicker.getDayOfMonth();
                Date date1 = new GregorianCalendar(year, month, day).getTime();
                SendResult(Activity.RESULT_OK, date1);
            }
        });

        mNegativeButton = v.findViewById(R.id.date_picker_dialog_negative_button);
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendResult(Activity.RESULT_CANCELED, null);
            }
        });
        return v;
    }

/*    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_date, null);
        final Date date = (Date) getArguments().getSerializable(ATG_DATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year, month, day;
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        mDatePicker = v.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(year, month, day, null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();
                        Date date1 = new GregorianCalendar(year, month, day).getTime();
                        SendResult(Activity.RESULT_OK, date1);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SendResult(Activity.RESULT_CANCELED, null);
                    }
                })
                .create();
    }*/

    private void SendResult(int resultCode, Date date) {
        if (date != null) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DATE, date);
            //getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
            getActivity().setResult(resultCode, intent);
        }else {
            //getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
            getActivity().setResult(resultCode);
        }
        getActivity().finish();
    }

}
