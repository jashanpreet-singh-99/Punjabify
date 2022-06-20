package com.ck.dev.punjabify.fragments.registration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.interfaces.RegistrationFragmentConnection;
import com.ck.dev.punjabify.utils.Config;

public class IntroFragment extends Fragment {

    private Button createAccountBtn;

    private RegistrationFragmentConnection registrationFragmentConnection;

    public IntroFragment(RegistrationFragmentConnection registrationFragmentConnection) {
        this.registrationFragmentConnection = registrationFragmentConnection;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration_intro, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Config.LOG(Config.TAG_REGISTRATION, "Opening Intro Fragment", false);
        createAccountBtn = view.findViewById(R.id.create_account_btn);

        onClick();
    }

    private void onClick() {
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrationFragmentConnection.onCreateAccountClicked();
            }
        });

    }

    public static Fragment init(RegistrationFragmentConnection registrationFragmentConnection) {
        return  new IntroFragment(registrationFragmentConnection);}

}
