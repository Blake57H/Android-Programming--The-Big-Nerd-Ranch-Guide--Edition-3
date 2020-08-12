package com.example.androidprogrammingbook02;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.Callbacks{
    private static final String EXTRA_CRIME_INDEX = "com.example.androidprogrammingbook02.crime_index";
    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private Button mJumpToStartButton, mJumpToEndButton;


    public static Intent newIntent(Context packageContext, int crimeIndex) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_INDEX, crimeIndex);
        return intent;
    }

    @Override
    public void onCrimeUpdated(Crime crime) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_crime_pager);


        mViewPager = findViewById(R.id.crime_view_pager);
        final int index = getIntent().getIntExtra(EXTRA_CRIME_INDEX, -1);


        mCrimes = CrimeLab.get(CrimePagerActivity.this).getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                //Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(position);
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                JumpButtonEnabler(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.setCurrentItem(index);

        mJumpToStartButton = findViewById(R.id.button_jumpToStart);
        mJumpToEndButton = findViewById(R.id.button_jumpToEnd);

        mJumpToStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
                JumpButtonEnabler(0);
            }
        });

        mJumpToEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(mCrimes.size() - 1);
                JumpButtonEnabler(mCrimes.size() - 1);
            }
        });


        JumpButtonEnabler(index);
    }

    private void JumpButtonEnabler(int index) {
        if (mCrimes.size() == 1) {
            mJumpToStartButton.setEnabled(false);
            mJumpToEndButton.setEnabled(false);
        } else if (index == 0) {
            mJumpToStartButton.setEnabled(false);
            mJumpToEndButton.setEnabled(true);
        } else if (index == mCrimes.size() - 1) {
            mJumpToEndButton.setEnabled(false);
            mJumpToStartButton.setEnabled(true);
        } else {
            mJumpToStartButton.setEnabled(true);
            mJumpToEndButton.setEnabled(true);
        }
    }

}
