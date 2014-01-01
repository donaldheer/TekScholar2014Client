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
 * Created by Chase on 4/13/14.
 */
public class ChannelSelect extends ImageButton implements OnClickListener {

    private static final String TAG = "ADJ";
    int imageSwitchCount;
    int state = 0;
    ArrayList<Drawable> images = new ArrayList<Drawable>();
    Context context;
    Iterator<Drawable> itr;


    public ChannelSelect(Context context) {
        super(context);
        this.context = context;
        setOnClickListener(this);
    }
    public ChannelSelect(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setBackgroundColor(Color.TRANSPARENT);
        initImages(attrs);
        setOnClickListener(this);

    }

    private void initImages(AttributeSet attrs) {
        TypedArray params = getContext().obtainStyledAttributes(attrs,R.styleable.style_channelSelectSwitch);
        imageSwitchCount = params.getInteger(R.styleable.style_channelSelectSwitch_img_count, 1);
        Log.d(TAG, Integer.toString(imageSwitchCount));
        //Looping through strings of resources is really freaking hard so here is something that works
        images.add(params.getDrawable(R.styleable.style_channelSelectSwitch_img0));
        images.add(params.getDrawable(R.styleable.style_channelSelectSwitch_img1));
        params.recycle();
        itr = images.iterator();
        //Set Default Image
        //if(itr.hasNext()){
        //    setImageDrawable(itr.next());
        //}
        setImageDrawable(images.get(state));
    }

    public Integer getState(){
        return state;
    }

    public void setState(int mState) {
        setImageDrawable(images.get(mState));
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "Clicked!");
        if(state != 1){
            state++;
            setState(state);
        } else {
            state = 0;
            setState(state);
        }
        Log.d(TAG, Integer.toString(getState()));
    }
}
