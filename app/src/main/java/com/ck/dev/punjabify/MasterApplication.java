package com.ck.dev.punjabify;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.ck.dev.punjabify.broadcast.InternetConnectionReceiver;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;

public class MasterApplication extends Application {

    InternetConnectionReceiver internetConnectionReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerConnectivityReceiver();
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
        firebaseAppCheck.setTokenAutoRefreshEnabled(true);
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
