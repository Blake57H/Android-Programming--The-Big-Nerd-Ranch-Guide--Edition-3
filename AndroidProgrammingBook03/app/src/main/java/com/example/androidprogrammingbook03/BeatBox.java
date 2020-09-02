package com.example.androidprogrammingbook03;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BeatBox {
    private static final String TAG = "BeatBox";
    private static final String SOUND_FOLDER = "sample_sounds";
    private static final int MAX_SOUNDS = 5;

    private AssetManager mAssetManager;
    private List<Sound> mSounds = new ArrayList<>();
    private SoundPool mSoundPool;
    private float rate = 1f;

    public BeatBox(Context context) {
        mAssetManager = context.getAssets();
        mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        loadSounds();
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public void play(Sound sound) {
        Integer soundID = sound.getSoundID();
        if (soundID == null) return;
        mSoundPool.play(soundID, 1.0f, 1.0f, 1, 0, rate);
    }

    public void release(){
        mSoundPool.release();
    }

    private void loadSounds() {
        String[] soundNames;
        try {
            soundNames = mAssetManager.list(SOUND_FOLDER);
            Log.i(TAG, "Found " + soundNames.length + " sounds");
        } catch (IOException ioException) {
            Log.e(TAG, "Could not list assets", ioException);
            return;
        }

        for (String filename : soundNames) {
            try {
                String assetPath = SOUND_FOLDER + "/" + filename;
                Sound sound = new Sound(assetPath);
                load(sound);
                mSounds.add(sound);
                Log.i(TAG, "Sound [" + filename + "] loaded");
            } catch (IOException e) {
                Log.e(TAG, "Could not load sound " + filename, e);
            }
        }
    }

    private void load(Sound sound) throws IOException {
        AssetFileDescriptor aFD = mAssetManager.openFd(sound.getAssetPath());
        Log.i(TAG, "Asset path is " + sound.getAssetPath());
        int soundID = mSoundPool.load(aFD, 1);
        sound.setSoundID(soundID);
    }

    public List<Sound> getSounds() {
        return mSounds;
    }


}
