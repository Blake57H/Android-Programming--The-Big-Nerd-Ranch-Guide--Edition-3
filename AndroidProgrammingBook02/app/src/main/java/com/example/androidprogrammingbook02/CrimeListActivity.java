package com.example.androidprogrammingbook02;

import android.content.Intent;

import androidx.fragment.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    protected int getLayoutResID(){
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        if(findViewById(R.id.detail_fragment_container)==null){
            Intent intent = CrimePagerActivity.newIntent(this, CrimeLab.get(this).getIndexByID(crime.getID()));
            startActivity(intent);
        }else{
            Fragment detail = CrimeFragment.newInstance(CrimeLab.get(this).getIndexByID(crime.getID()), true);
            getSupportFragmentManager().beginTransaction().replace(R.id.detail_fragment_container, detail).commit();
        }
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        listFragment.updateUI(0);
    }
}
