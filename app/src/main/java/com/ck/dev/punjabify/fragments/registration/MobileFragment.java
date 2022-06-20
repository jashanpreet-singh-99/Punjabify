package com.ck.dev.punjabify.fragments.registration;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.interfaces.RegistrationFragmentConnection;
import com.ck.dev.punjabify.utils.Config;

import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class MobileFragment extends Fragment {

    private EditText mobileNumberTxt;
    private Button   mobileNumberSubmitted;
    private ScrollView scrollViewParent;

    private RegistrationFragmentConnection registrationFragmentConnection;

    public MobileFragment(RegistrationFragmentConnection registrationFragmentConnection) {
        this.registrationFragmentConnection = registrationFragmentConnection;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration_mobile_number, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Config.LOG(Config.TAG_REGISTRATION, "Opening Mobile Fragment", false);
        mobileNumberTxt       = view.findViewById(R.id.mobile_number_txt);
        mobileNumberSubmitted = view.findViewById(R.id.continue_btn);
        scrollViewParent      = view.findViewById(R.id.parent_scroll_view);

        scrollViewParent.setSmoothScrollingEnabled(true);
        onClick();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClick() {
        mobileNumberSubmitted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = mobileNumberTxt.getText().toString();
                if (data.length() == 10) {
                    registrationFragmentConnection.onMobileNumberSubmitted(data);
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Invalid Input")
                            .setMessage("The Phone Number Entered is Not a Valid Number. Recheck the Number or try Again")
                            .setCancelable(true).show();
                }
            }
        });

        mobileNumberTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollViewParent.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollViewParent.fullScroll(View.FOCUS_DOWN);
                        scrollViewParent.animate();
                    }
                }, 300);
                return false;
            }
        });

        mobileNumberTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    InputMethodManager inputManager = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(INPUT_METHOD_SERVICE);
                    assert inputManager != null;
                    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });
    }

    public static Fragment init(RegistrationFragmentConnection registrationFragmentConnection) {
        return  new MobileFragment(registrationFragmentConnection);}

}
