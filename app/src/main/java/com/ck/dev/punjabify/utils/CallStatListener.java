package com.ck.dev.punjabify.utils;

import android.os.Build;
import android.telephony.TelephonyCallback;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.S)
public abstract class CallStatListener extends TelephonyCallback implements TelephonyCallback.CallStateListener {

    @Override
    public void onCallStateChanged(int state) {
    }
}
