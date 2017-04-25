package com.pubmatic.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 *
 */

public class PMPagerAdapter extends FragmentStatePagerAdapter {
    public PMPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i)
        {
            case 0:
                Fragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                Fragment settingsFragment = new SettingsFragment();
                return settingsFragment;
            default:
                Fragment fragment = new HomeFragment();
                return fragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}