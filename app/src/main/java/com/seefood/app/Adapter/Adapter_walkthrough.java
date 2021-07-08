package com.seefood.app.Adapter;

import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.seefood.app.fragment.Fragment_walkthroughone;
import com.seefood.app.fragment.Fragment_walkthroughthree;
import com.seefood.app.fragment.Fragment_walkthroughtwo;


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
                Fragment_walkthroughone tab1 = new Fragment_walkthroughone();
                return tab1;

            case 1:
                Fragment_walkthroughtwo tab2 = new Fragment_walkthroughtwo();
                return tab2;


            case 2:
                Fragment_walkthroughthree tab3 = new Fragment_walkthroughthree();
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
