package com.ck.dev.punjabify.utils;

import android.view.MotionEvent;
import android.view.View;

import com.ck.dev.punjabify.interfaces.OnSwipeEvent;

public class SwipeDetector implements View.OnTouchListener{

    private int min_distance = 50;
    private float downX, downY, upX, upY;
    private final View parent;

    private OnSwipeEvent swipeEventListener;

    public SwipeDetector(View parent){
        this.parent = parent;
        parent.setOnTouchListener(this);
    }

    public void setOnSwipeListener(OnSwipeEvent listener)
    {
        try{
            swipeEventListener = listener;
        }
        catch(ClassCastException e)
        {
            Config.LOG(Config.TAG_SWIPE,"SwipeDetector.onSwipeEvent " + e, false);
        }
    }


    public void onRightToLeftSwipe(){
        if(swipeEventListener != null)
            swipeEventListener.SwipeEventDetected(parent, SwipeTypeEnum.RIGHT_TO_LEFT);
        else
            Config.LOG(Config.TAG_SWIPE,"SwipeDetector.onSwipeEvent instance Error", false);
    }

    public void onLeftToRightSwipe(){
        if(swipeEventListener != null)
            swipeEventListener.SwipeEventDetected(parent, SwipeTypeEnum.LEFT_TO_RIGHT);
        else
            Config.LOG(Config.TAG_SWIPE,"SwipeDetector.onSwipeEvent instance Error", false);
    }

    public void onTopToBottomSwipe(){
        if(swipeEventListener != null)
            swipeEventListener.SwipeEventDetected(parent, SwipeTypeEnum.TOP_TO_BOTTOM);
        else
            Config.LOG(Config.TAG_SWIPE,"SwipeDetector.onSwipeEvent instance Error", false);
    }

    public void onBottomToTopSwipe(){
        if(swipeEventListener != null)
            swipeEventListener.SwipeEventDetected(parent, SwipeTypeEnum.BOTTOM_TO_TOP);
        else
            Config.LOG(Config.TAG_SWIPE,"SwipeDetector.onSwipeEvent instance Error", false);
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                //HORIZONTAL SCROLL
                if(Math.abs(deltaX) > Math.abs(deltaY))
                {
                    if(Math.abs(deltaX) > min_distance){
                        // left or right
                        if(deltaX < 0)
                        {
                            this.onLeftToRightSwipe();
                            return true;
                        }
                        if(deltaX > 0) {
                            this.onRightToLeftSwipe();
                            return true;
                        }
                    }
                    else {
                        //not long enough swipe...
                        return false;
                    }
                }
                //VERTICAL SCROLL
                else
                {
                    if(Math.abs(deltaY) > min_distance){
                        // top or down
                        if(deltaY < 0)
                        { this.onTopToBottomSwipe();
                            return true;
                        }
                        if(deltaY > 0)
                        { this.onBottomToTopSwipe();
                            return true;
                        }
                    }
                    else {
                        //not long enough swipe...
                        return false;
                    }
                }

                return true;
            }
        }
        return false;
    }

    public SwipeDetector setMinDistanceInPixels(int min_distance)
    {
        this.min_distance = min_distance;
        return this;
    }

    public enum SwipeTypeEnum
    {
        RIGHT_TO_LEFT,LEFT_TO_RIGHT,TOP_TO_BOTTOM,BOTTOM_TO_TOP
    }

}