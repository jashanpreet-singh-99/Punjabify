package com.ck.dev.punjabify.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

public class Config {

    public static final String TAG_PERMISSION     = "message_permission";
    public static final String TAG_MEDIA          = "message_media";
    public static final String TAG_SWIPE          = "message_swipe";
    public static final String TAG_DATABASE       = "message_database";
    public static final String TAG_ALBUM_VIEW     = "message_album_view";
    public static final String TAG_MEDIA_HEADSET  = "message_media_headset";
    public static final String TAG_MEDIA_ONLINE   = "message_media_online";
    public static final String TAG_LOCAL_FILE     = "message_local_file";
    public static final String TAG_ART_CACHE      = "message_art_cache";
    public static final String TAG_INTERNET       = "message_internet";
    public static final String TAG_DOWNLOAD       = "message_download_track";


    public static final String TAG_SPLASH         = "message_activity_splash";
    public static final String TAG_REGISTRATION   = "message_activity_registration";
    public static final String TAG_HOME           = "message_activity_home";
    public static final String TAG_SPECIFIC_TRACK = "message_activity_specific_track";
    public static final String TAG_ARTIST_FOLLOW  = "message_fragment_artist_follow";

    public static final String TAG_THREAD         = "message_thread";

    public static final String TAG_UNAPPROVED     = "message_unapproved";

    public static final String DOWNLOADED_TRACK   = "track_downloaded";

    public static final int PERMISSION_REQUEST_CODE = 1001;

    public static final String CHANNEL_ID = "Media_Play_ID";
    public static final int NOTIFICATION_ID = 1002;

    public static final int SPECIFIC_GENRE_MODE    = 0;
    public static final int SPECIFIC_ARTIST_MODE   = 1;
    public static final int SPECIFIC_PLAYLIST_MODE = 2;

    public static final String ART_DIR     = "/art/";
    public static final String IMG_DIR     = "/img/";
    public static final String TRACKS_DIR  = "/tracks/";

    private static final boolean DEBUGGING = true;

    public static void LOG(String TAG, String message, boolean error) {
        if (DEBUGGING) {
            if (error)
                Log.e(TAG, message);
            else
                Log.d(TAG, message);
        }
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float r;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        float r_inner = (float) (r * 0.25);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawCircle(r, r, r_inner, paint);
        return output;
    }//getCircularBitmap

    public static Bitmap getOvalBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float r;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }//getCircularBitmap

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float r;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight();
        } else {
            r = bitmap.getWidth();
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect( 0, 0, r, r, 60, 60, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static float convertDpToPixel(float dp, Context context){ return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT); }

    public static float convertPixelsToDp(float px, Context context) { return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT); }

    public static String convertToMB(int val) { return String.format(Locale.ENGLISH ,"%.3f",((double) val)/((double) 1024 * 1024)); }
}
