package com.ck.dev.punjabify.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.utils.Config;

public class AlbumArtView extends ImageButton {

    private Paint paintInner;
    private Paint paintOuter;

    private int WIDTH = 0;
    private int HEIGHT = 0;

    private TypedArray typedArray;

    private boolean outerBitmap = false;


    public AlbumArtView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        WIDTH  = getWidth();
        HEIGHT = getHeight();
        init(attrs);
    }

    private void init(AttributeSet set) {
        if (set != null) {
            typedArray = getContext().obtainStyledAttributes(set, R.styleable.AlbumArtView);
        }
        paintInner = new Paint();
        paintOuter = new Paint();
        paintInner.setAntiAlias(true);
        paintOuter.setAntiAlias(true);
        paintInner.setColor(typedArray.getColor(R.styleable.AlbumArtView_inner_color, Color.RED));
        paintOuter.setColor(typedArray.getColor(R.styleable.AlbumArtView_outer_color, Color.BLACK));
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        WIDTH  = getWidth();
        HEIGHT = getHeight();
        Config.LOG(Config.TAG_ALBUM_VIEW, " Width : " + getWidth() + " Height : " + getHeight(), false);
        super.onDraw(canvas);
        if (!outerBitmap) {
            canvas.drawCircle(WIDTH/2, HEIGHT/2, WIDTH/2, paintOuter);
            canvas.drawCircle(WIDTH/2, HEIGHT/2, WIDTH/8, paintInner);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) {
            outerBitmap = false;
            postInvalidate();
        } else {
            outerBitmap = true;
            bm = Config.getCircularBitmap(bm);
            super.setImageBitmap(bm);
        }
    }
}
