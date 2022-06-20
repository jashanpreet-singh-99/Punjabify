package com.ck.dev.punjabify.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.utils.Config;

public class MusicProgressBar extends View {

    private Paint paint;

    private int LEVEL_PIX = 20;

    private int cur_Pix = 0;
    private int curLevel = 0;
    private int progLevel = 0;

    private int progress = 50;

    private int MAX = 100;

    private TypedArray typedArray;
    private int progressColor;
    private int progressSecondaryColor;

    public MusicProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet set) {
        if (set != null) {
            typedArray = getContext().obtainStyledAttributes(set, R.styleable.MusicProgressBar);
        }
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        progressColor = typedArray.getColor(R.styleable.MusicProgressBar_progress_color, Color.RED);
        progressSecondaryColor = typedArray.getColor(R.styleable.MusicProgressBar_secondary_progress_color, Color.BLACK);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int HEIGHT_E = (getHeight() - 100) / 2;
        int WIDTH = getWidth() - (2 * level1H.length * LEVEL_PIX);
        int extra = WIDTH % ((2 * level2H.length * LEVEL_PIX) + (level1H.length * LEVEL_PIX));
        cur_Pix = extra / 2;
        //Config.LOG(Config.TAG_ALBUM_VIEW, " - " + extra, false);
        int iteration = WIDTH / ((2 * level2H.length * LEVEL_PIX) + (level1H.length * LEVEL_PIX));
        int totalLevel = ((WIDTH - extra) / 20) + 10;
        if (progress > 0) {
            progLevel = (int) (((double) progress / MAX) * totalLevel);
            paint.setColor(progressColor);
        } else {
            progLevel = 0;
            paint.setColor(progressSecondaryColor);
        }
        curLevel = 0;
        cur_Pix += drawP1(canvas, cur_Pix, HEIGHT_E);
        for (int i = 0 ; i < iteration; i ++) {
            cur_Pix += drawP2(canvas, cur_Pix, HEIGHT_E);
            cur_Pix += drawP1(canvas, cur_Pix, HEIGHT_E);
            cur_Pix += drawP2(canvas, cur_Pix, HEIGHT_E);
        }
        cur_Pix += drawP1(canvas, cur_Pix, HEIGHT_E);
    }

    private int drawP1(Canvas canvas, int x, int y) {
        for (int i = 0 ; i < level1H.length; i ++) {
            canvas.drawRoundRect(x + (i * 20), y + 50 - (level1H[i] * 5), x + (((2 * i) + 1) * 10), y + 50 + (level1H[i] * 5), 5, 5, paint);
            if (curLevel < progLevel) {
                curLevel++;
            } else {
                paint.setColor(progressSecondaryColor);
            }
        }
        return level1H.length * LEVEL_PIX;
    }

    int[] level1H = {5, 7, 10, 6, 4};

    int[] level2H = {6, 5, 7, 10, 6, 3};

    private int drawP2(Canvas canvas, int x, int y) {
        for (int i = 0 ; i < level2H.length; i ++) {
            canvas.drawRoundRect(x + (i * 20), y + 50 - (level2H[i] * 5), x + (((2 * i) + 1) * 10), y + 50 + (level2H[i] * 5), 5, 5, paint);
            if (curLevel < progLevel) {
                curLevel++;
            } else {
                paint.setColor(progressSecondaryColor);
            }
        }
        return level2H.length * LEVEL_PIX;
    }

    public void setProgress(int val) {
        progress = val;
        postInvalidate();
    }

    public int getProgress() {
        return progress;
    }

    public int getMAX() {
        return MAX;
    }

    public void setMAX(int val) {
        MAX = val;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int WIDTH = getWidth();
        int extra = (WIDTH - (2 * level1H.length * LEVEL_PIX)) % ((2 * level2H.length * LEVEL_PIX) + (level1H.length * LEVEL_PIX));
        float t_w = event.getX() - (extra/2);
        double percent = t_w/ (WIDTH - extra);
        if (percent < 0.00) {
            percent = 0.00;
        } else if (percent > 1.00) {
            percent = 1.00;
        }
        progress = (int) (MAX * percent);
        setProgress(progress);
        return super.onTouchEvent(event);
    }

}
