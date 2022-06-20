package com.ck.dev.punjabify.fragments.registration;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.interfaces.RegistrationFragmentConnection;
import com.ck.dev.punjabify.interfaces.ViewPagerBackPressed;
import com.ck.dev.punjabify.utils.Config;

import java.util.Locale;
import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class OTPFragment extends Fragment {

    private RegistrationFragmentConnection registrationFragmentConnection;

    private ScrollView parentScrollView;

    private EditText optTxt1;
    private EditText optTxt2;
    private EditText optTxt3;
    private EditText optTxt4;
    private EditText optTxt5;
    private EditText optTxt6;

    private TextView infoTxt;

    private Button verifyBtn;

    public OTPFragment(RegistrationFragmentConnection registrationFragmentConnection) {
        this.registrationFragmentConnection = registrationFragmentConnection;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration_otp, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Config.LOG(Config.TAG_REGISTRATION, "Opening OTP Fragment", false);
        infoTxt          = view.findViewById(R.id.activity_info);
        optTxt1          = view.findViewById(R.id.otp_number_1);
        optTxt2          = view.findViewById(R.id.otp_number_2);
        optTxt3          = view.findViewById(R.id.otp_number_3);
        optTxt4          = view.findViewById(R.id.otp_number_4);
        optTxt5          = view.findViewById(R.id.otp_number_5);
        optTxt6          = view.findViewById(R.id.otp_number_6);
        verifyBtn        = view.findViewById(R.id.verify_btn);
        parentScrollView = view.findViewById(R.id.parent_scroll_view);

        onClick();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClick() {
        optTxt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    optTxt2.requestFocus();
                }
            }
        });

        optTxt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    optTxt3.requestFocus();
                }
            }
        });

        optTxt3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    optTxt4.requestFocus();
                }
            }
        });

        optTxt4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    optTxt5.requestFocus();
                }
            }
        });

        optTxt5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    optTxt6.requestFocus();
                }
            }
        });

        optTxt6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    InputMethodManager inputManager = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(INPUT_METHOD_SERVICE);
                    assert inputManager != null;
                    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = optTxt1.getText().toString() +
                        optTxt2.getText().toString() +
                        optTxt3.getText().toString() +
                        optTxt4.getText().toString() +
                        optTxt5.getText().toString() +
                        optTxt6.getText().toString();
                Config.LOG(Config.TAG_REGISTRATION, " OTP Received = " + otp, false);
                registrationFragmentConnection.onOTPVerifyClicked(otp);
            }
        });

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                parentScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        parentScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 300);
                return false;
            }
        };

        optTxt1.setOnTouchListener(onTouchListener);
        optTxt2.setOnTouchListener(onTouchListener);
        optTxt3.setOnTouchListener(onTouchListener);
        optTxt4.setOnTouchListener(onTouchListener);
        optTxt5.setOnTouchListener(onTouchListener);
        optTxt6.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        infoTxt.setText(String.format(Locale.ENGLISH, "Enter 6 digit number that sent to \n %s",registrationFragmentConnection.getMobileNumber()));
    }

    public static Fragment init(RegistrationFragmentConnection registrationFragmentConnection) {
        return  new OTPFragment(registrationFragmentConnection);}

}
