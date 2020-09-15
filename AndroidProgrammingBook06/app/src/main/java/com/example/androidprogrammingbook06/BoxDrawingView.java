package com.example.androidprogrammingbook06;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BoxDrawingView extends View {
    private static final String TAG = "BoxDrawingView";

    private Box mCurrentBox;
    private List<Box> mBoxes = new ArrayList<>();
    private Paint mBoxPaint, mBackgroundPaint;

    public BoxDrawingView(Context context) {
        this(context, null);
    }

    public BoxDrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = null, current2 = null;
        String action = "";
        for (int count = 0; count < event.getPointerCount(); count++) {
            if (event.getPointerId(count) == 0)
                current = new PointF(event.getX(count), event.getY(count));
            if (event.getPointerId(count) == 1)
                current2 = new PointF(event.getX(count), event.getY(count));
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                if (current != null) {
                    mCurrentBox = new Box(current);
                    mBoxes.add(mCurrentBox);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                action = "ACTION_POINTER_DOWN";
                if (current2 != null)
                    mCurrentBox.setRotatePointOrigin(current2);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (current != null) {
                    mCurrentBox.setCurrent(current);
                }
                if (current2 != null) {
                    double angleOrigin =
                            Math.atan2(mCurrentBox.getRotatePointOrigin().y - mCurrentBox.getOrigin().y, mCurrentBox.getRotatePointOrigin().x - mCurrentBox.getOrigin().x);
                    double angleCurrent =
                            Math.atan2(current2.y - mCurrentBox.getOrigin().y, current2.x - mCurrentBox.getOrigin().x);
                    double angle = Math.toDegrees(angleCurrent - angleOrigin);
                    //if (angle < 0) angle += 360;
                    mCurrentBox.setAngle(angle);
                    action += "; Angle=" + angle;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
        }

        if (current != null)
            Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);
        else {
            Log.i(TAG, action);
        }
        return true;

//        PointF touchPoint  = null;
//        PointF touchPoint2 = null;
//        for (int i=0;i<event.getPointerCount();i++) {
//            if(event.getPointerId(i)==0)
//                touchPoint = new PointF(event.getX(i), event.getY(i));
//            if(event.getPointerId(i)==1)
//                touchPoint2 = new PointF(event.getX(i), event.getY(i));
//        }
//
//
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                mCurrentBox = new Box(touchPoint);
//                mBoxes.add(mCurrentBox);
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                mCurrentBox.setRotatePointOrigin(touchPoint2);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                if(touchPoint  != null )
//                    mCurrentBox.setCurrent(touchPoint);
//                if(touchPoint2 != null ) {
//                    PointF boxOrigin     = mCurrentBox.getOrigin();
//                    PointF pointerOrigin = mCurrentBox.getRotatePointOrigin();
//                    float angle2 = (float) Math.atan2(touchPoint2.y   - boxOrigin.y, touchPoint2.x   - boxOrigin.x);
//                    float angle1 = (float) Math.atan2(pointerOrigin.y - boxOrigin.y, pointerOrigin.x - boxOrigin.x);
//                    float calculatedAngle = (float) Math.toDegrees(angle2 - angle1);
//                    if (calculatedAngle < 0) calculatedAngle += 360;
//                    mCurrentBox.setAngle(calculatedAngle);
//                    Log.d(TAG, "Set Box Angle " + calculatedAngle);
//                }
//                invalidate();
//                break;
//            case MotionEvent.ACTION_UP:
//                Log.d(TAG, "Finger UP Box Set");
//                mCurrentBox = null;
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                Log.d(TAG, "Action Cancel Box Set");
//                mCurrentBox = null;
//                break;
//        }
//
//        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxes) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            double angle = box.getAngle();
            canvas.rotate((float) angle, (left + right) / 2, (top + bottom) / 2);
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.rotate((float) -angle, (left + right) / 2, (top + bottom) / 2);

        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.d(TAG, "Saving Instance State");
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.mBoxList = mBoxes;


        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        Log.d(TAG, "Restoring Instance State");
        mBoxes = ss.mBoxList;
    }

    private static class SavedState extends BaseSavedState {
        private List<Box> mBoxList;

        public SavedState(Parcelable superState) {
            super(superState);
            Log.d(TAG, "Saving parcelable");
        }
    }
}
