package com.ck.dev.punjabify;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.ck.dev.punjabify.broadcast.InternetConnectionReceiver;

public class MasterApplication extends Application {

    InternetConnectionReceiver internetConnectionReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerConnectivityReceiver();
    }

    private void registerConnectivityReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        internetConnectionReceiver = new InternetConnectionReceiver();
        registerReceiver(internetConnectionReceiver, intentFilter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(internetConnectionReceiver);
    }
}
