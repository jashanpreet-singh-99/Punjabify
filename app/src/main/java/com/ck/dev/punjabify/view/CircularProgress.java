package com.ck.dev.punjabify.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.ck.dev.punjabify.R;

public class CircularProgress extends View {

    private Paint paint;

    private int progress = 50;

    private int MAX = 100;

    private TypedArray typedArray;
    private int progressColor;

    public CircularProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet set) {
        if (set != null) {
            typedArray = getContext().obtainStyledAttributes(set, R.styleable.CircularProgress);
        }
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        progressColor = typedArray.getColor(R.styleable.CircularProgress_circular_progress_color, getResources().getColor(R.color.colorAccent));
        progress = typedArray.getInt(R.styleable.CircularProgress_circular_progress, 0);
        MAX = typedArray.getInt(R.styleable.CircularProgress_circular_progress_max, 100);
        paint.setColor(progressColor);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int WIDTH = getWidth();
        int angle = (int) ((double)progress * 3.6);
        canvas.drawArc(0, 0, WIDTH, WIDTH, 0, angle, true, paint);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        postInvalidate();
    }

    public void setMAX(int MAX) {
        this.MAX = MAX;
        postInvalidate();
    }
}
