package com.example.androidprogrammingbook02;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

public class _Deprecated_CrimeActivity extends SingleFragmentActivity {

    private static final String EXTRA_CRIME_ID = "com.example.androidprogrammingbook02.crime_id";
    private static final String EXTRA_CRIME_INDEX = "com.example.androidprogrammingbook02.crime_index";

    @Override
    protected Fragment createFragment() {
        //UUID crimeID = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);
        int crimeIndex = getIntent().getIntExtra(EXTRA_CRIME_INDEX, 0);
        //return CrimeFragment.newInstance(crimeID);
        return CrimeFragment.newInstance(crimeIndex);
    }

    public static Intent newIntent(Context packageContext, int crimeIndex){
        Intent intent = new Intent(packageContext, _Deprecated_CrimeActivity.class);
        intent.putExtra(EXTRA_CRIME_INDEX, crimeIndex);
        return intent;
    }
}
