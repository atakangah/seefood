package com.seefood.app.Adapter;

import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.seefood.app.fragment.OnboardingFramgment1;
import com.seefood.app.fragment.OnboardingFragment3;
import com.seefood.app.fragment.OnboardingFragment2;


public class Adapter_walkthrough extends FragmentStatePagerAdapter {

    public static TextView mFragLabel;


    public Adapter_walkthrough(FragmentManager fm, TextView fragLabel) {
        super(fm);
        mFragLabel = fragLabel;
    }


    @Override
    public Fragment getItem(int position) {


        switch (position) {
            case 0:
                OnboardingFramgment1 tab1 = new OnboardingFramgment1();
                return tab1;

            case 1:
                OnboardingFragment2 tab2 = new OnboardingFragment2();
                return tab2;


            case 2:
                OnboardingFragment3 tab3 = new OnboardingFragment3();
                return tab3;


            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }


    public static void updateTab(String label) {
        if(mFragLabel != null) {
            mFragLabel.setText(label);
            mFragLabel.invalidate();
        }
    }
}
