package com.ck.dev.punjabify.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ck.dev.punjabify.R;

public class VolumeBar extends View {

    private Paint paint;

    private int progress = 50;

    private int MAX = 100;

    private TypedArray typedArray;

    private int progressColor;
    private int progressSecondaryColor;
    private int backgroundColor;

    private boolean background = true;

    public VolumeBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet set) {
        if (set != null) {
            typedArray = getContext().obtainStyledAttributes(set, R.styleable.VolumeBar);
        }
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        progressColor          = typedArray.getColor(R.styleable.VolumeBar_prime_progress_color, getResources().getColor(R.color.colorAccent));
        progressSecondaryColor = typedArray.getColor(R.styleable.VolumeBar_second_progress_color, getResources().getColor(R.color.colorSecondary));
        backgroundColor        = typedArray.getColor(R.styleable.VolumeBar_background_color, getResources().getColor(R.color.colorDarkBackground));
        progress               = typedArray.getInt(R.styleable.VolumeBar_volume_progress, 0);
        MAX                    = typedArray.getInt(R.styleable.VolumeBar_volume_progress_max, 100);
        background             = typedArray.getBoolean(R.styleable.VolumeBar_volume_background, true);

        paint.setColor(progressColor);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int WIDTH = getWidth();
        int HEIGHT = getHeight();
        if (!background) {
            paint.setColor(progressSecondaryColor);
            canvas.drawRoundRect(0, 0, WIDTH, HEIGHT, 0, 0, paint);
            paint.setColor(progressColor);
            int WIDTH_P = (int)(WIDTH * ((double)progress/(double)MAX));
            canvas.drawRoundRect(0, 0, WIDTH_P, HEIGHT, 0, 0, paint);
            return;
        }
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(0, 0, WIDTH, HEIGHT, 40.0f, 40.0f, paint);
        paint.setColor(progressSecondaryColor);
        canvas.drawRoundRect(20, 20, WIDTH - 20, HEIGHT - 20, 40.0f, 40.0f, paint);
        paint.setColor(progressColor);
        int WIDTH_P = (int)(WIDTH * ((double)progress/(double)MAX));
        canvas.drawRoundRect(20, 20, WIDTH_P - 20, HEIGHT - 20, 40.0f, 40.0f, paint);
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
