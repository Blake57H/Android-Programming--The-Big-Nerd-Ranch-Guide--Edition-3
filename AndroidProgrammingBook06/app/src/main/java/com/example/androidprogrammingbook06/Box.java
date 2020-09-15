package com.example.androidprogrammingbook06;

import android.graphics.PointF;

public class Box {
    private PointF mOrigin, mCurrent, mRotatePointOrigin;
    private double mAngle = 0;

    public Box(PointF origin){
        mOrigin = mCurrent = origin;
    }

    public void setRotatePointOrigin(PointF rotatePointOrigin) {
        mRotatePointOrigin = rotatePointOrigin;
    }

    public PointF getRotatePointOrigin() {
        return mRotatePointOrigin;
    }

    public void setAngle(double angle) {
        mAngle = angle;
    }

    public double getAngle() {
        return mAngle;
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public void setCurrent(PointF current) {
        mCurrent = current;
    }

    public PointF getOrigin() {
        return mOrigin;
    }
}
