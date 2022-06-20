package com.ck.dev.punjabify.interfaces;

public interface RegistrationFragmentConnection {

    void onCreateAccountClicked();
    void onMobileNumberSubmitted(String number);
    void onOTPVerifyClicked(String otp);
    String getMobileNumber();
}
