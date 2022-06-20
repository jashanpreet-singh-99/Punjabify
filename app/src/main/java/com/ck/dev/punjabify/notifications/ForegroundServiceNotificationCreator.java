package com.ck.dev.punjabify.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.ck.dev.punjabify.R;

import java.util.Objects;

public class ForegroundServiceNotificationCreator {

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "Track Search notification";
    private static Notification notification;

    public static Notification getNotification(Context context) {

        if(notification == null) {

            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setChannelId(createNotificationChannel(context))
                    .setContentTitle("Fetching Records")
                    .setContentText("Updating Track Records")
                    .setSmallIcon(R.drawable.ic_sync)
                    .build();
        }

        return notification;
    }

    private static String createNotificationChannel(Context context ){
        NotificationChannel chan = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan = new NotificationChannel(CHANNEL_ID, "Notification Sync", NotificationManager.IMPORTANCE_MIN);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Objects.requireNonNull(manager).createNotificationChannel(chan);
        }
        return CHANNEL_ID;
    }

    public static int getNotificationId() {
        return NOTIFICATION_ID;
    }

}
