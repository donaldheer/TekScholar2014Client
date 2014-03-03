package com.tekscholar.androidclient;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by aidandj on 3/2/14.
 */
public class MultiImageSwitch extends ImageButton implements OnClickListener {

    private static final String TAG = "ADJ";
    int imageSwitchCount;
    int state = 0;
    ArrayList<Drawable> images = new ArrayList<Drawable>();
    Context context;
    Iterator<Drawable> itr;


    public MultiImageSwitch(Context context) {
        super(context);
        this.context = context;
        setOnClickListener(this);
    }
    public MultiImageSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setBackgroundColor(Color.TRANSPARENT);
        initImages(attrs);
        setOnClickListener(this);

    }

    private void initImages(AttributeSet attrs) {
        TypedArray params = getContext().obtainStyledAttributes(attrs,R.styleable.style_multiWaySwitch);
        imageSwitchCount = params.getInteger(R.styleable.style_multiWaySwitch_image_count, 1);
        Log.d(TAG, Integer.toString(imageSwitchCount));
        //Looping through strings of resources is really freaking hard so here is something that works
        images.add(params.getDrawable(R.styleable.style_multiWaySwitch_image0));
        images.add(params.getDrawable(R.styleable.style_multiWaySwitch_image1));
        images.add(params.getDrawable(R.styleable.style_multiWaySwitch_image2));
        params.recycle();
        itr = images.iterator();
        //Set Default Image
        if(itr.hasNext()){
            setImageDrawable(itr.next());
        }
    }

    public Integer getState(){
        return state;
    }


    @Override
    public void onClick(View view) {
        Log.d(TAG, "Clicked!");
        if(itr.hasNext()){
            setImageDrawable(itr.next());
            state++;
        } else {
            itr = images.iterator();
            setImageDrawable(itr.next());
            state = 0;
        }
        Log.d(TAG, Integer.toString(getState()));
    }
}
