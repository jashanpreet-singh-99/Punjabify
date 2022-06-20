package com.ck.dev.punjabify.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import com.ck.dev.punjabify.observers.HomeObservableObject;

import java.util.Objects;

public class InternetConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = Objects.requireNonNull(connectivityManager).getActiveNetwork();
            NetworkCapabilities connectedNetwork = connectivityManager.getNetworkCapabilities(network);
            if (connectedNetwork != null) {
                if (connectedNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    connected = true;
                } else if (connectedNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    connected = true;
                }
            }
        } else {
            NetworkInfo networkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
            connected = Objects.requireNonNull(networkInfo).isConnected();
        }
        HomeObservableObject.getInstance().updateValue(connected);
    }
}
