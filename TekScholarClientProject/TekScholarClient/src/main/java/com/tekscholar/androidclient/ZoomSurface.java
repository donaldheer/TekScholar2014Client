package com.tekscholar.androidclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aidandj on 2/14/14.
 */
public class ZoomSurface extends SurfaceView implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

    SurfaceHolder surfaceHolder;

    private static final String TAG = "ADJ";
    private GestureDetectorCompat mDetector;
    private ScaleGestureDetector mScaleDetector;
    Context context;
    private float mScale = 1000;
    private float xScale = 1;
    private float xScalePrev = 0;
    private float xScale1 = 1;
    private float yScale = 1;
    private float yScalePrev = 0;
    private float yScale1 = 1;
    public BluetoothConnection btConnection;
    public List<String> commandsArray = new ArrayList<String>();

    private int x_dim, y_dim;
    private int mBoundary, mGridSize;
    private int mTextSize;

    //Data variables
    private float[] dataPoints;

    /**Channel Variables that apply to all channels **/
    private Paint generalTextPaint, gridPaint;

    //Channel Paints
    private Paint channel1Paint;
    private Paint channel2Paint;
    private Paint channel3Paint;
    private Paint channel4Paint;

    private float surfaceHeight;
    private float surfaceWidth;
    private float surfaceScalarX;
    private float surfaceScalarY;

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


        /**Paint properties for Display Grid**/
        gridPaint = new Paint();
        gridPaint.setDither(true);
        gridPaint.setColor(0x7FFFFFFF);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeJoin(Paint.Join.ROUND);
        gridPaint.setStrokeCap(Paint.Cap.ROUND);
        gridPaint.setStrokeWidth(1);
        Log.d(TAG, "Constructing");

        //Paint properties for Channel 1
        channel1Paint = new Paint();
        channel1Paint.setDither(true);
        channel1Paint.setColor(0xFFFFFF01);
        channel1Paint.setStyle(Paint.Style.STROKE);
        channel1Paint.setStrokeJoin(Paint.Join.ROUND);
        channel1Paint.setStrokeCap(Paint.Cap.ROUND);
        channel1Paint.setStrokeWidth(3);

        //Paint properties for Channel 2
        channel2Paint = new Paint();
        channel2Paint.setDither(true);
        channel2Paint.setColor(0xFF01E7E7);
        channel2Paint.setStyle(Paint.Style.STROKE);
        channel2Paint.setStrokeJoin(Paint.Join.ROUND);
        channel2Paint.setStrokeCap(Paint.Cap.ROUND);
        channel2Paint.setStrokeWidth(3);

        //Paint properties for Channel 3
        channel3Paint = new Paint();
        channel3Paint.setDither(true);
        channel3Paint.setColor(0xFFA500A5);
        channel3Paint.setStyle(Paint.Style.STROKE);
        channel3Paint.setStrokeJoin(Paint.Join.ROUND);
        channel3Paint.setStrokeCap(Paint.Cap.ROUND);
        channel3Paint.setStrokeWidth(3);

        //Paint properties for Channel 4
        channel4Paint = new Paint();
        channel4Paint.setDither(true);
        channel4Paint.setColor(0xFF00A600);
        channel4Paint.setStyle(Paint.Style.STROKE);
        channel4Paint.setStrokeJoin(Paint.Join.ROUND);
        channel4Paint.setStrokeCap(Paint.Cap.ROUND);
        channel4Paint.setStrokeWidth(3);

        mTextSize = 15; // Default size of Oscope text
        mBoundary = 10; // Default distance from the edge of the surface view
        mGridSize = 50; // Default number of pixels between grid lines on the surface view




        //surfaceHolder = getHolder();

        //Get datapoints
        //dataPoints = new float[5 * 2];
    }
//    public ZoomSurface(Context context) {
//        super(context);
//        this.context = context;
//        // Instantiate the gesture detector with the
//        // application context and an implementation of
//        // GestureDetector.OnGestureListener
//        mDetector = new GestureDetectorCompat(context, this);
//        // Set the gesture detector as the double tap
//        // listener.
//        mDetector.setOnDoubleTapListener(this);
//
//        mScaleDetector = new ScaleGestureDetector(context, this);
//
//
//        /**Paint properties for Display Grid**/
//        gridPaint = new Paint();
//        gridPaint.setDither(true);
//        gridPaint.setColor(0x7FFFFFFF);
//        gridPaint.setStyle(Paint.Style.STROKE);
//        gridPaint.setStrokeJoin(Paint.Join.ROUND);
//        gridPaint.setStrokeCap(Paint.Cap.ROUND);
//        gridPaint.setStrokeWidth(1);
//        Log.d(TAG, "Constructing");
//
//        //Paint properties for Channel 1
//        channel1Paint = new Paint();
//        channel1Paint.setDither(true);
//        channel1Paint.setColor(0xFFFF01);
//        channel1Paint.setStyle(Paint.Style.STROKE);
//        channel1Paint.setStrokeJoin(Paint.Join.ROUND);
//        channel1Paint.setStrokeCap(Paint.Cap.ROUND);
//        channel1Paint.setStrokeWidth(1);
//
//        mTextSize = 15; // Default size of Oscope text
//        mBoundary = 10; // Default distance from the edge of the surface view
//        mGridSize = 50; // Default number of pixels between grid lines on the surface view
//
//        surfaceHolder = getHolder();
//
//        //Get datapoints
//        dataPoints = new float[5 * 2];
//    }

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
        //Log.d(TAG, "It scrolled me!");
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
        //Log.d(TAG, "It scaled me!");
        //Log.d(TAG, Float.toString(scaleGestureDetector.getScaleFactor()));
        //mScale = mScale * scaleGestureDetector.getScaleFactor();
        //Log.d(TAG, "X current: " + Float.toString(scaleGestureDetector.getCurrentSpanX()) + " X previous: " + Float.toString(scaleGestureDetector.getPreviousSpanX()));
//        xScalePrev = scaleGestureDetector.getPreviousSpanX();
//        yScalePrev = scaleGestureDetector.getPreviousSpanY();
        xScale = (scaleGestureDetector.getCurrentSpanX() - scaleGestureDetector.getPreviousSpanX())/scaleGestureDetector.getPreviousSpanX() + 1;
        yScale = (scaleGestureDetector.getCurrentSpanY() - scaleGestureDetector.getPreviousSpanY())/scaleGestureDetector.getPreviousSpanY() + 1;
//        Log.d(TAG, "xScalePrev = " + Float.toString(xScalePrev) + " yScalePrev = " + Float.toString(yScalePrev));
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        //Log.d(TAG, "It started scaling me!");
        xScalePrev = scaleGestureDetector.getPreviousSpanX();
        yScalePrev = scaleGestureDetector.getPreviousSpanY();
        Log.d(TAG, "xScalePrev = " + Float.toString(xScalePrev) + " yScalePrev = " + Float.toString(yScalePrev));
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.x_dim = w;
        this.y_dim = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void generatePoints(){
        String data = null;
        String _data = null;
        byte[] dataBytes = new byte[2000];
        int[] dataInts = new int[2000];
        dataPoints = new float[dataBytes.length * 2 + 2];

        int X = 0;
        int Y = 1;
//
//        for(int i = 0; i < 5; i++){
//            dataPoints[i*2 + X] = i*30 + 10;
//            dataPoints[i*2 + Y] = i*30 + 10;
//        }
        MainActivity.btConnection.readMessage();
        MainActivity.btConnection.sendMessage(":DATA:SOURCE CH1;:DATA:START 1;:DATA:STOP 1000;:DATA:WIDTH 1;:CURVE?\n");
        data = MainActivity.btConnection.receiveMessage();

        //Need to watch this since it might be only getting half message... need some form of catch
        int headerPosition = data.indexOf(":CURVE ");

        _data = data.substring(headerPosition + 13);
        Log.d("ADJ", _data);
        dataBytes = _data.getBytes();
        Log.d("ADJ", "String length: " + _data.length());
        Log.d("ADJ", "Byte length: " + dataBytes.length);
        for(int i = 0; i < dataBytes.length; i++){
            dataPoints[i*2 + Y] = (surfaceHeight - (surfaceScalarY * Float.parseFloat(Byte.toString(dataBytes[i]))));
            dataPoints[i*2 + X] = (surfaceScalarX *(float) i);
            Log.d("ADJ", "Point Y:" + dataPoints[i*2 + X] + " x " + dataPoints[i*2 + Y]);
        }


    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        Log.d("ADJ", "onDraw");
        x_dim = this.getWidth();
        y_dim = this.getHeight();
        mGridSize = (int) (y_dim - 2*mBoundary)/8;
        int i = 0;
        int j = 0;
        int pointCount = 1000;//this.getWidth();

        float smallTextPaintSize = (float) ((.5)*(y_dim / mTextSize));
        float largeTextPaintSize = (float) ((y_dim / mTextSize));
        //generalTextPaint.setTextSize(smallTextPaintSize);

        canvas.drawColor(Color.BLACK);

        //Prepare to Draw horizontal lines
        j = (this.getWidth() % mGridSize) / 2;
        for (i = (y_dim % mGridSize) / 2; i < y_dim; i = i + mGridSize) {
            canvas.drawLine((float) j, (float) i, (float) (this.getWidth() - j),
                    (float) i, gridPaint);
        }

        canvas.drawLine((float) j, (float) i - mGridSize-5-smallTextPaintSize, (float) (this.getWidth() - j),
                (float) i - mGridSize-5-smallTextPaintSize, gridPaint);

        //Prepare to Draw vertical lines
        i = (y_dim % mGridSize) / 2;
        canvas.drawLine((float) (this.getWidth() % mGridSize) / 2, (float) i, (float) ((this.getWidth() % mGridSize) / 2),
                (float) (y_dim - i), gridPaint);
        for (j = (this.getWidth() % mGridSize) / 2; j < this.getWidth(); j = j + mGridSize) {
            canvas.drawLine((float) j, (float) i, (float) (j),
                    (float) (y_dim - i - 5-smallTextPaintSize), gridPaint);
        }
        canvas.drawLine((float) j - mGridSize, (float) i, (float) (j - mGridSize),
                (float) (y_dim - i), gridPaint);


        //Scalar stuff
        surfaceHeight = this.getHeight();
        surfaceWidth = this.getWidth();
        surfaceScalarY = surfaceHeight/255;
        surfaceScalarX = surfaceWidth/1000;


        for(int k = 0; k < dataPoints.length/2 - 1;k++){
            Log.d("ADJ", Integer.toString(k));
            canvas.drawLine((surfaceScalarX * dataPoints[k*2]), (surfaceHeight - (surfaceScalarY * dataPoints[k*2+1])), (surfaceScalarX * dataPoints[k*2+2]), (surfaceHeight - (surfaceScalarY * dataPoints[k*2+3])), channel1Paint);
            //k = k+2;
        }

        Log.d("ADJ", Float.toString((float) j - mGridSize) + " " + Float.toString((float) i) + " " + Float.toString((float) (y_dim - i)));

        Log.d("ADJ", Float.toString(dataPoints[1]));

        canvas.drawLines(dataPoints, channel1Paint);
        //canvas.drawLine(dataPoints[2], dataPoints[3], dataPoints[4], dataPoints[5], channel1Paint);

        canvas.drawPoints(dataPoints, channel1Paint);


    }

    public void paint(){
        //Canvas canvas = surfaceHolder.lockCanvas();
        //paintOnCanvas(canvas);
        //surfaceHolder.unlockCanvasAndPost(canvas);
    }

    public void paintOnCanvas(Canvas canvas) {

        x_dim = this.getWidth();
        y_dim = this.getHeight();
        mGridSize = (int) (y_dim - 2*mBoundary)/8;
        int i = 0;
        int j = 0;
        int pointCount = 1000;//this.getWidth();

        float smallTextPaintSize = (float) ((.5)*(y_dim / mTextSize));
        float largeTextPaintSize = (float) ((y_dim / mTextSize));
        //generalTextPaint.setTextSize(smallTextPaintSize);

        canvas.drawColor(Color.BLACK);

        //Prepare to Draw horizontal lines
        j = (this.getWidth() % mGridSize) / 2;
        for (i = (y_dim % mGridSize) / 2; i < y_dim; i = i + mGridSize) {
            canvas.drawLine((float) j, (float) i, (float) (this.getWidth() - j),
                    (float) i, gridPaint);
        }

        canvas.drawLine((float) j, (float) i - mGridSize-5-smallTextPaintSize, (float) (this.getWidth() - j),
                (float) i - mGridSize-5-smallTextPaintSize, gridPaint);

        //Prepare to Draw vertical lines
        i = (y_dim % mGridSize) / 2;
        canvas.drawLine((float) (this.getWidth() % mGridSize) / 2, (float) i, (float) ((this.getWidth() % mGridSize) / 2),
                (float) (y_dim - i), gridPaint);
        for (j = (this.getWidth() % mGridSize) / 2; j < this.getWidth(); j = j + mGridSize) {
            canvas.drawLine((float) j, (float) i, (float) (j),
                    (float) (y_dim - i - 5-smallTextPaintSize), gridPaint);
        }
        canvas.drawLine((float) j - mGridSize, (float) i, (float) (j - mGridSize),
                (float) (y_dim - i), gridPaint);


        for(int k = 0; k < dataPoints.length/2 - 1;k++){
            //Log.d("ADJ", Integer.toString(k));
            canvas.drawLine(dataPoints[k*2], dataPoints[k*2+1], dataPoints[k*2+2], dataPoints[k*2+3], channel1Paint);
            //k = k+2;
        }

        //Log.d("ADJ", Float.toString((float) j - mGridSize) + " " + Float.toString((float) i) + " " + Float.toString((float) (y_dim - i)));

        //Log.d("ADJ", Float.toString(dataPoints[1]));

        //canvas.drawLines(dataPoints, channel1Paint);
        //canvas.drawLine(dataPoints[2], dataPoints[3], dataPoints[4], dataPoints[5], channel1Paint);

        //canvas.drawPoints(dataPoints, channel1Paint);


    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
    //Log.d(TAG, "It scaled me to " + Float.toString(mScale));
    xScale = ((scaleGestureDetector.getCurrentSpanX() - xScalePrev)/xScalePrev) + 1;
    yScale = ((scaleGestureDetector.getCurrentSpanY() - yScalePrev)/yScalePrev) + 1;
    Log.d(TAG, "xScale = " + Float.toString(xScale) + " yScale = " + Float.toString(yScale));

    int selectedCH = 1;
    //Send scale update command to pi
    //MainActivity.btConnection.sendTestMessage();
    MainActivity.btConnection.readMessage();
    MainActivity.btConnection.sendMessage("HORIZONTAL:SCALE?\n");
    String zoomCommand  = MainActivity.btConnection.receiveMessage();
    String _zoomCommand = "";
    String _zoomCommand2 = "";
    MainActivity.btConnection.sendMessage("CH" + Integer.toString(selectedCH) + ":SCALE?\n");
    String scaleCommand = MainActivity.btConnection.receiveMessage();
    String _scaleCommand = "";
    String _scaleCommand2 = "";
    Log.d("ADJ", "zoomCommand = " + zoomCommand);
    Log.d("ADJ", "scaleCommand = " + scaleCommand);

    int vertPosition = zoomCommand.indexOf("HORIZONTAL:SCALE ");
    try {
        _zoomCommand = zoomCommand.substring(vertPosition + 17, vertPosition + 21);
        _zoomCommand2 = zoomCommand.substring(zoomCommand.length() - 3);
        if(xScale > 1) {
            _zoomCommand = Float.toString((float) 1 * 1/xScale * Float.valueOf(_zoomCommand));
        } else {
            _zoomCommand = Float.toString((float) 1 * 1/xScale * Float.valueOf(_zoomCommand));
        }
        Log.d("ADJ", "zoom command 1: " + _zoomCommand + "\nzoom command 2: " + _zoomCommand2);
//        MainActivity.btConnection.sendMessage("HORIZONTAL:SCALE " + _zoomCommand + _zoomCommand2 + '\n');
//        //MainActivity.btConnection.sendMessage("xScale = " + Float.toString(xScale) + " yScale = " + Float.toString(yScale));
    } catch(Exception e){
        e.printStackTrace();
    }

    int horizPosition = scaleCommand.indexOf("CH" + Integer.toString(selectedCH) + ":SCALE ");
    try {
        _scaleCommand = scaleCommand.substring(horizPosition + 10, horizPosition + 14);
        _scaleCommand2 = scaleCommand.substring(scaleCommand.length() - 3);
        if(yScale > 1) {
            _scaleCommand = Float.toString((float) 1 * 1/yScale * Float.valueOf(_scaleCommand));
        } else {
            _scaleCommand = Float.toString((float) 1 * 1/yScale * Float.valueOf(_scaleCommand));
        }
        Log.d("ADJ", "scale command 1: " + _scaleCommand + "\nscale command 2: " + _scaleCommand2);
//        MainActivity.btConnection.sendMessage("HORIZONTAL:SCALE " + _zoomCommand + _zoomCommand2 + '\n');
//        //MainActivity.btConnection.sendMessage("xScale = " + Float.toString(xScale) + " yScale = " + Float.toString(yScale));
        } catch(Exception e){
            e.printStackTrace();
        }



        MainActivity.btConnection.sendMessage("HORIZONTAL:SCALE " + _zoomCommand + _zoomCommand2 + ";" +
                "CH" + Integer.toString(selectedCH) + ":SCALE " + _scaleCommand + _scaleCommand2 + '\n');
        //MainActivity.btConnection.sendMessage("xScale = " + Float.toString(xScale) + " yScale = " + Float.toString(yScale));
    xScale = 1;
    yScale = 1;
    xScalePrev = 0;
    yScalePrev = 0;

    }
}
