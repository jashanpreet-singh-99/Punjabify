package com.ck.dev.punjabify.interfaces;

import android.view.View;

import com.ck.dev.punjabify.utils.SwipeDetector;

public interface OnSwipeEvent {
    void SwipeEventDetected(View v, SwipeDetector.SwipeTypeEnum SwipeType);
}
