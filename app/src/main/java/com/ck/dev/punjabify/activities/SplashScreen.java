package com.ck.dev.punjabify.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.utils.Config;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends Activity {

    private FirebaseUser user;

    private String[] permissionArray = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission())
                requestPermissions(permissionArray, Config.PERMISSION_REQUEST_CODE);
            else
                startNextActivity();
        } else {
            startNextActivity();
        }
    }

    /**
     * check if the Permission required are granted or not
     * @return Boolean :-> are runtime permission granted
     */
    private boolean checkPermission() {
        Config.LOG(Config.TAG_SPLASH, "Checking Permissions.", false);
        int PERMISSION_EXTERNAL_READ = PackageManager.PERMISSION_DENIED;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PERMISSION_EXTERNAL_READ = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            Config.LOG(Config.TAG_PERMISSION, String.valueOf(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)), false);
        }
        return PERMISSION_EXTERNAL_READ == PackageManager.PERMISSION_GRANTED;
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
        if (requestCode == Config.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean storageR = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageR) {
                    startNextActivity();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissionArray, Config.PERMISSION_REQUEST_CODE);
                    }
                }
            }
        }
    }
}
