package com.ck.dev.punjabify.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ck.dev.punjabify.fragments.ErrorFragment;
import com.ck.dev.punjabify.fragments.registration.IntroFragment;
import com.ck.dev.punjabify.fragments.registration.MobileFragment;
import com.ck.dev.punjabify.fragments.registration.OTPFragment;
import com.ck.dev.punjabify.interfaces.RegistrationFragmentConnection;

import java.util.ArrayList;

public class RegistrationScreenAdapter extends FragmentStateAdapter {

    private Fragment curFragment;
    private ArrayList<Fragment> allFragments = new ArrayList<>();
    private RegistrationFragmentConnection registrationFragmentConnection;

    public RegistrationScreenAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.registrationFragmentConnection = (RegistrationFragmentConnection) fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0 : {
                curFragment = IntroFragment.init(registrationFragmentConnection);
                allFragments.add(curFragment);
                return curFragment;
            }
            case 1 : {
                curFragment = MobileFragment.init(registrationFragmentConnection);
                allFragments.add(curFragment);
                return curFragment;
            }
            case 2 : {
                curFragment = OTPFragment.init(registrationFragmentConnection);
                allFragments.add(curFragment);
                return curFragment;
            }
            default : {
                curFragment = ErrorFragment.init();
                return curFragment;
            }
        }
    }

    public Fragment getCurFragment(int pos) {
        return allFragments.get(pos);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
