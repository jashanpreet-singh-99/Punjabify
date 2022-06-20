package com.ck.dev.punjabify.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.adapter.RegistrationScreenAdapter;
import com.ck.dev.punjabify.interfaces.RegistrationFragmentConnection;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.FirebaseConfig;
import com.ck.dev.punjabify.utils.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegistrationScreen extends FragmentActivity implements RegistrationFragmentConnection {

    private ViewPager2 registrationViewPager;

    private FirebaseAuth firebaseAuth;

    private String MOBILE_NUMBER = "";
    private String verificationID = "";

    private Boolean manualOTP = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_registration_screen);
        Config.LOG(Config.TAG_REGISTRATION, "Opening Registration Screen", false);
        initView();
    }

    private void initView() {
        registrationViewPager = this.findViewById(R.id.registration_data_pager);

        RegistrationScreenAdapter registrationScreenAdapter = new RegistrationScreenAdapter(this);
        registrationViewPager.setAdapter(registrationScreenAdapter);
        registrationViewPager.setUserInputEnabled(false);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateAccountClicked() {
        registrationViewPager.setCurrentItem(1);
    }

    @Override
    public void onMobileNumberSubmitted(String number) {
        MOBILE_NUMBER = "+91" + number;
        registrationViewPager.setCurrentItem(2);
        getOTP(MOBILE_NUMBER);
    }

    @Override
    public void onOTPVerifyClicked(String otp) {
        manualOTP = true;
        if (!verificationID.equals("")) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, otp);
            signInWithPhoneAuthCredential(credential);
        } else {
            new AlertDialog.Builder(RegistrationScreen.this)
                    .setTitle("Server Error")
                    .setMessage("Their seems to be a network error. Plz retry after some time.")
                    .setCancelable(true).show();
        }
    }

    @Override
    public String getMobileNumber() {
        return MOBILE_NUMBER;
    }

    @Override
    public void onBackPressed() {
        switch (registrationViewPager.getCurrentItem()) {
            case 2:
                registrationViewPager.setCurrentItem(1);
                break;
            case 1:
                registrationViewPager.setCurrentItem(0);
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    private void getOTP(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120,
                TimeUnit.SECONDS,
                this,
                onVerifyCallBack);
    }//getOTP

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerifyCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            Config.LOG(Config.TAG_REGISTRATION, "SIGN IN ", false);
            if (!manualOTP) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
                Config.LOG(Config.TAG_REGISTRATION, "AUTO SIGN IN ", false);
            }
        }
        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Config.LOG(Config.TAG_REGISTRATION, "SIGN IN FAIL ", true);
            Config.LOG(Config.TAG_REGISTRATION, " Error " + e, true);
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                new AlertDialog.Builder(RegistrationScreen.this)
                        .setTitle("Invalid Input")
                        .setMessage("The Phone Number Entered is Not a Valid Number. Recheck the Number or try Again")
                        .setCancelable(true).show();
            } else {
                new AlertDialog.Builder(RegistrationScreen.this)
                        .setTitle("OTP Timeout")
                        .setMessage("Unable to Verify the OTP. Try Again Later.")
                        .setCancelable(true).show();
            }
        }
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            verificationID = s;
        }
    };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String id = FirebaseAuth.getInstance().getUid();
                            FirebaseDatabase.getInstance().getReference("user").child(Objects.requireNonNull(id)).setValue("yes");
                            PreferenceManager.setString(getApplicationContext(), FirebaseConfig.USER_ID, id);
                            new AlertDialog.Builder(RegistrationScreen.this)
                                    .setTitle("OTP Verified")
                                    .setMessage("Welcome to Punjabify. We hope, you will enjoy our music collections.")
                                    .setCancelable(true).show();
                            startActivity(new Intent(getApplicationContext(), SplashScreen.class));
                            finish();
                        } else {
                            Config.LOG(Config.TAG_REGISTRATION, "VERIFICATION FAILED " , true);
                            new AlertDialog.Builder(RegistrationScreen.this)
                                    .setTitle("Verification Failed")
                                    .setMessage("Incorrect OTP. Try again later.")
                                    .setCancelable(true).show();
                        }
                    }
                });
    }//signInWithPhoneAuthCredential

}
