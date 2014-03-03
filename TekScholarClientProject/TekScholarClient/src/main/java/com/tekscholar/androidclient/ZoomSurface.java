package com.tekscholar.androidclient;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;

/**
 * Created by aidandj on 2/14/14.
 */
public class ZoomSurface extends SurfaceView implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

    private static final String TAG = "ADJ";
    private GestureDetectorCompat mDetector;
    private ScaleGestureDetector mScaleDetector;
    Context context;
    private float mScale = 1000;

    public ZoomSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        // Instantiate the gesture detector with the 
        // application context and an implementation of 
        // GestureDetector.OnGestureListener 
        mDetector = new GestureDetectorCompat(context, this);
        // Set the gesture detector as the double tap 
        // listener. 
        mDetector.setOnDoubleTapListener(this);

        mScaleDetector = new ScaleGestureDetector(context, this);
        Log.d(TAG, "Constructing");
    }
    public ZoomSurface(Context context) {
        super(context);
        this.context = context;
        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(context, this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);

        mScaleDetector = new ScaleGestureDetector(context, this);
        Log.d(TAG, "Constructing");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(TAG, "onTouchEvent");
        this.mScaleDetector.onTouchEvent(event);
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        //return super.onTouchEvent(event);
        return true;
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
//        Log.d(TAG, "It tapped me!");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
//        Log.d(TAG, "It double tapped me!");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
//        Log.d(TAG, "It double tapped me!");
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
//        Log.d(TAG, "onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        Log.d(TAG, "It scrolled me!");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
//        Log.d(TAG, "It flung me!");
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        Log.d(TAG, "It scaled me!");
        Log.d(TAG, Float.toString(scaleGestureDetector.getScaleFactor()));
        mScale = mScale * scaleGestureDetector.getScaleFactor();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        Log.d(TAG, "It started scaling me!");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
    Log.d(TAG, "It scaled me to " + Float.toString(mScale));
    }
}
