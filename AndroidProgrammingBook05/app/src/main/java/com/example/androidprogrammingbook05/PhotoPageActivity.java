package com.example.androidprogrammingbook05;

import android.content.Context;
import android.content.Intent;
import android.icu.number.CompactNotation;
import android.net.Uri;
import android.nfc.Tag;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class PhotoPageActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoPageActivity";
    private static  Fragment sFragment;

    public static Intent newIntent(Context context, Uri photoPageUri){
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        sFragment = PhotoPageFragment.newInstance(getIntent().getData());
        return sFragment;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_photo_page);
        if(sFragment != null){
            Log.d(TAG, "onBackPressed: fragment != null");
            if(((PhotoPageFragment) sFragment).webViewCanGoBack()){
                ((PhotoPageFragment) sFragment).webViewGoBack();
                return;
            }
        }else{
            Log.e(TAG, "onBackPressed: fragment == null");
        }
        super.onBackPressed();
    }
}
