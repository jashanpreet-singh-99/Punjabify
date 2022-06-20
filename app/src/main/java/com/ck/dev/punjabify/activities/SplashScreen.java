package com.ck.dev.punjabify.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.utils.Config;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends Activity {

    private FirebaseUser user;

    private final String[] permissionArray = new String[] {Manifest.permission.READ_PHONE_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash_screen);
        user = FirebaseAuth.getInstance().getCurrentUser();
        Config.LOG(Config.TAG_SPLASH, "Splash Activity Started.", false);
        Config.LOG(Config.TAG_SPLASH, "Firebase User = " + user, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkPermission()) {
            startNextActivity();
        }
    }

    /**
     * check if the Permission required are granted or not
     * @return Boolean :-> are runtime permission granted
     */
    private boolean checkPermission() {
        Config.LOG(Config.TAG_SPLASH, "Checking Permissions.", false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        List<String> pendingPermissions = new ArrayList<>();
        for (String permission: permissionArray) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    pendingPermissions.add(permission);
                }
        }
        Config.LOG(Config.TAG_SPLASH, "Pending Permissions :- " + pendingPermissions.size(), false);

        //Request the pending Permissions
        if (!pendingPermissions.isEmpty()) {
            requestPermissions(pendingPermissions.toArray(new String[0]), Config.PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    /**
     * Checks if the user is registered with firebase if not open the registration activity.
     * Else : Start the Services to Sync local tracks and fetch online tracks.
     */
    private void startNextActivity() {
        if (user == null) {
            Config.LOG(Config.TAG_SPLASH, "Opening Registration Activity.", false);
            startActivity(new Intent(getApplicationContext(), RegistrationScreen.class));
        } else {
            Config.LOG(Config.TAG_SPLASH, "Already Registered.", false);
            Config.LOG(Config.TAG_SPLASH, "Opening HomeScreen.", false);
            startActivity(new Intent(getApplicationContext(), HomeScreen.class));
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int deniedCount = 0;
        if (requestCode == Config.PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedCount++;
                }
                Config.LOG(Config.TAG_SPLASH, "Permission result :- " + permissions[i] + " " + grantResults[i], false);
            }
        }
        deniedCount++;
        if (deniedCount == 0) {
            startNextActivity();
        } else {
            checkPermission();
        }
    }
}
