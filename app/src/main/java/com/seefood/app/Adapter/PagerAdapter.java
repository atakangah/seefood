package com.seefood.app.Adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class PagerAdapter extends FragmentStatePagerAdapter {

    ArrayList<Fragment> pagerFragments = new ArrayList<>();
    ArrayList<String> fragmentTitles = new ArrayList<>();

    public PagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return pagerFragments.get(position);
    }

    @Override
    public int getCount() {
        return pagerFragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }

    public void addFragment(Fragment fragment, String fragmentTitle) {
        this.pagerFragments.add(fragment);
        this.fragmentTitles.add(fragmentTitle);
    }
}
