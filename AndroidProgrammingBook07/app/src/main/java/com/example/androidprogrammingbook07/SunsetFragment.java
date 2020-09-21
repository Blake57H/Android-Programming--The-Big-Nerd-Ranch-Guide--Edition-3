package com.example.androidprogrammingbook07;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SunsetFragment extends Fragment {
    private static final String TAG = "SunsetFragment";
    private static final int ANIMATION_DURATION_3000 = 3000, ANIMATION_DURATION_1500 = 1500;
    private static final int
            STATE_DAYTIME = 0, STATE_SUNSETTING = 1, STATE_SKY_DUSKING = 2, STATE_NIGHTTIME = 3,
            STATE_SKY_DAWMING = 4, STATE_SUNRISING = 5;
    private static int sState = STATE_DAYTIME;
    private static boolean sIsAtNightSwitch = false;
    private static Integer sSunOriginPosition, sReflectSunOriginalPosition, sSunCurrentPosition, sReflectSunCurrentPosition, sSkyCurrentColor;

    private View mSceneView, mSunView, mReflectSunView, mSkyView;
    private int mBlueSkyColor, mSunsetSkyColor, mNightSkyColor;
    AnimatorSet mSunsetAnimatorSet = new AnimatorSet(), mSunriseAnimatorSet = new AnimatorSet(), mSunHeatAnimator = new AnimatorSet();


    public static SunsetFragment newInstance() {

        Bundle args = new Bundle();

        SunsetFragment fragment = new SunsetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sunset, container, false);
        mSceneView = v;
        mSunView = v.findViewById(R.id.sun);
        mSkyView = v.findViewById(R.id.sky);
        mReflectSunView = v.findViewById(R.id.reflect_sun);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);

        Log.d(TAG, "mBlueSkyColor=" + mBlueSkyColor + ", mSunsetSkyColor=" + mSunsetSkyColor + ", mNightSkyColor=" + mNightSkyColor);

        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sState == STATE_DAYTIME) {
                    sSkyCurrentColor = null;
                    startSunsetAnimation(null, 0);
                    //sIsAtNightSwitch = !sIsAtNightSwitch;
                } else if (sState == STATE_NIGHTTIME) {
                    sSkyCurrentColor = null;
                    startSunriseAnimation(null);
                } else {
                    if (mSunriseAnimatorSet.isRunning()) mSunriseAnimatorSet.cancel();
                    if (mSunsetAnimatorSet.isRunning()) mSunsetAnimatorSet.cancel();

                    if (sState == STATE_SUNSETTING || sState == STATE_SKY_DUSKING) {
                        startSunriseAnimation(sSkyCurrentColor);
                    } else if (sState == STATE_SUNRISING || sState == STATE_SKY_DAWMING) {
                        startSunsetAnimation(sSkyCurrentColor, sState);
                    }
                }
            }
        });


        return v;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                sSunOriginPosition = mSunView.getTop();
                Log.d(TAG, "onGlobalLayout: sSunOriginPosition = sunPosition = " + mSunView.getTop());

                ObjectAnimator heatAnimator = ObjectAnimator.ofPropertyValuesHolder(mSunView,
                        PropertyValuesHolder.ofFloat("scaleX", 0.7f)
                        , PropertyValuesHolder.ofFloat("scaleY", 0.7f))
                        .setDuration(ANIMATION_DURATION_3000);
                heatAnimator.setRepeatCount(ObjectAnimator.INFINITE);
                heatAnimator.setRepeatMode(ObjectAnimator.REVERSE);
                heatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                Log.d(TAG, "onGlobalLayout: reflectSun.getTop = " + mReflectSunView.getTop());
                sReflectSunOriginalPosition = mSkyView.getHeight() - mSunView.getBottom();
                mReflectSunView.setY(sReflectSunOriginalPosition);
                Log.d(TAG, "onGlobalLayout: reflectSun.getY = " + mReflectSunView.getY());
                ObjectAnimator heatAnimator1 = ObjectAnimator.ofPropertyValuesHolder(mReflectSunView,
                        PropertyValuesHolder.ofFloat("scaleX", 0.7f)
                        , PropertyValuesHolder.ofFloat("scaleY", 0.7f))
                        .setDuration(ANIMATION_DURATION_3000);
                heatAnimator1.setRepeatCount(ObjectAnimator.INFINITE);
                heatAnimator1.setRepeatMode(ObjectAnimator.REVERSE);
                heatAnimator1.setInterpolator(new AccelerateDecelerateInterpolator());

                if (mSunHeatAnimator.isRunning()) mSunHeatAnimator.end();
                mSunHeatAnimator = new AnimatorSet();
                mSunHeatAnimator.playTogether(heatAnimator, heatAnimator1);
                mSunHeatAnimator.start();

                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void startSunsetAnimation(final Integer skyCurrentColor, int state) {
        final float sunYStart = mSunView.getY(), sunYEnd = mSkyView.getHeight();
        float rSunYStart = mReflectSunView.getY(), rSunYEnd = -mSunView.getHeight();

        final ObjectAnimator heightAnimator =
                ObjectAnimator.ofFloat(mSunView, "y", sunYStart, sunYEnd).setDuration(ANIMATION_DURATION_3000);
        heightAnimator.setInterpolator(new AccelerateInterpolator());
        heightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Log.d(TAG, "sunsetAnimationStart, sunPosition = " + mSunView.getTop());
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setSunCurrentPosition();
                Log.d(TAG, "sunsetAnimationEnd, SunView.GetTop = " + mSunView.getTop());
                Log.d(TAG, "sunsetAnimationEnd, sunYEnd = " + sunYEnd);
                Log.d(TAG, "sunsetAnimationEnd, SunView.getY = " + mSunView.getY());
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSunCurrentPosition();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        ObjectAnimator reflectHeightAnimator =
                ObjectAnimator.ofFloat(mReflectSunView, "y", rSunYStart, rSunYEnd).setDuration(ANIMATION_DURATION_3000);
        reflectHeightAnimator.setInterpolator(new AccelerateInterpolator());
        reflectHeightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSunCurrentPosition();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        int phase1StartColor = skyCurrentColor == null ? mBlueSkyColor : skyCurrentColor;
        ObjectAnimator sunsetSkyAnimator =
                ObjectAnimator.ofInt(mSkyView, "backgroundColor", phase1StartColor, mSunsetSkyColor).setDuration(ANIMATION_DURATION_3000);
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());
        sunsetSkyAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                sState = STATE_SUNSETTING;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                sState = STATE_SKY_DUSKING;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSkyCurrentBackgroundColor();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        int phase2StartColor = skyCurrentColor == null ? mSunsetSkyColor : skyCurrentColor;
        ObjectAnimator nightSkyAnimator =
                ObjectAnimator.ofInt(mSkyView, "backgroundColor", phase2StartColor, mNightSkyColor).setDuration(ANIMATION_DURATION_1500);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());
        nightSkyAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                sState = STATE_SKY_DUSKING;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                sState = STATE_NIGHTTIME;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSkyCurrentBackgroundColor();
                Log.d(TAG, "sunset phase2 sSkyCurrentColor = " + skyCurrentColor);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        mSunsetAnimatorSet = new AnimatorSet();
        if (state == STATE_DAYTIME || state == STATE_SUNRISING)
            mSunsetAnimatorSet.play(heightAnimator).with(sunsetSkyAnimator).with(reflectHeightAnimator).before(nightSkyAnimator);
        else if (state == STATE_SKY_DAWMING)
            mSunsetAnimatorSet.play(nightSkyAnimator);
        else {
            Log.e(TAG, "startSunsetAnimation: input state error, state=" + state);
            return;
        }
        mSunsetAnimatorSet.start();
    }

    private void startSunriseAnimation(Integer skyCurrentColor) {
        final float sunYEnd = sSunOriginPosition, sunYStart = mSunView.getY();
        float rSunYEnd = sReflectSunOriginalPosition;
        float rSunYStart = mReflectSunView.getY();

        ObjectAnimator heightAnimator =
                ObjectAnimator.ofFloat(mSunView, "y", sunYStart, sunYEnd).setDuration(ANIMATION_DURATION_3000);
        heightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        heightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Log.d(TAG, "sunriseStart, sunPosition = " + mSunView.getY());
                Log.d(TAG, "sunriseStart, sSunOriginalPosition = " + sSunOriginPosition);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setSunCurrentPosition();
                Log.d(TAG, "sunriseEnd, sSunCurrentPosition = sunPosition = " + mSunView.getY());
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSunCurrentPosition();
                Log.d(TAG, "sunriseCancel, sSunCurrentPosition = sunPosition = " + mSunView.getY());
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        ObjectAnimator reflectHeightAnimator = ObjectAnimator.ofFloat(mReflectSunView, "y", rSunYStart, rSunYEnd).setDuration(ANIMATION_DURATION_3000);
        reflectHeightAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        reflectHeightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSunCurrentPosition();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        int phase2Color = skyCurrentColor == null ? mSunsetSkyColor : skyCurrentColor;
        ObjectAnimator sunriseSkyAnimator =
                ObjectAnimator.ofInt(mSkyView, "backgroundColor", phase2Color, mBlueSkyColor).setDuration(ANIMATION_DURATION_3000);
        sunriseSkyAnimator.setEvaluator(new ArgbEvaluator());
        sunriseSkyAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                sState = STATE_SUNRISING;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                sState = STATE_DAYTIME;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSkyCurrentBackgroundColor();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        int phase1Color = skyCurrentColor == null ? mNightSkyColor : skyCurrentColor;
        ObjectAnimator dawnSkyAnimator =
                ObjectAnimator.ofInt(mSkyView, "backgroundColor", phase1Color, mSunsetSkyColor).setDuration(ANIMATION_DURATION_1500);
        dawnSkyAnimator.setEvaluator(new ArgbEvaluator());
        dawnSkyAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                sState = STATE_SKY_DAWMING;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                sState = STATE_SUNRISING;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                setSkyCurrentBackgroundColor();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        mSunriseAnimatorSet = new AnimatorSet();
        if (sState == STATE_NIGHTTIME || sState == STATE_SKY_DUSKING)
            mSunriseAnimatorSet.play(dawnSkyAnimator).before(sunriseSkyAnimator).before(heightAnimator).before(reflectHeightAnimator);
        else if (sState == STATE_SUNSETTING)
            mSunriseAnimatorSet.play(sunriseSkyAnimator).with(heightAnimator).with(reflectHeightAnimator);
        mSunriseAnimatorSet.start();
    }

    private void setSkyCurrentBackgroundColor() {
        ColorDrawable colorDrawable = (ColorDrawable) mSkyView.getBackground();
        Integer color = colorDrawable.getColor();
        Log.d(TAG, "getBackgroundColor: color = " + color);
        sSkyCurrentColor = color;
        Log.d(TAG, "getBackgroundColor: sSkyCurrentColor = " + sSkyCurrentColor);
    }

    private void setSunCurrentPosition() {
        sSunCurrentPosition = (int) mSunView.getY();
        sReflectSunCurrentPosition = (int) mReflectSunView.getY();
    }

}
