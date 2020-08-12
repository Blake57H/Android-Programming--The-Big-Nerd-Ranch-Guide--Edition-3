package com.example.androidprogrammingbook02;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Date;

public class DateTimePickerActivity extends SingleFragmentActivity {
    private static final String EXTRA_FRAGMENT_TYPE = "com.example.androidprogrammingbook02.picker_type";
    private static final String EXTRA_DATE = "com.example.androidprogrammingbook02.picker_date";

    public static final int EXTRA_DATE_FRAG = 0, EXTRA_TIME_FRAG = 1;


    public static Intent newIntent(Context packageContext, Date date, int fragment_type){
        Intent intent = new Intent(packageContext, DateTimePickerActivity.class);
        intent.putExtra(EXTRA_DATE, date);
        intent.putExtra(EXTRA_FRAGMENT_TYPE, fragment_type);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        // 0 = Date, 1 = Time
        int fragmentType = getIntent().getIntExtra(EXTRA_FRAGMENT_TYPE, -1);
        Date date = (Date) getIntent().getSerializableExtra(EXTRA_DATE);

        if(fragmentType == EXTRA_DATE_FRAG) {
            return DatePickerFragment.newInstance(date);
        }
        else if(fragmentType == EXTRA_TIME_FRAG)
            return TimePickerFragment.newInstance(date);
        else
            return null;

    }
}
