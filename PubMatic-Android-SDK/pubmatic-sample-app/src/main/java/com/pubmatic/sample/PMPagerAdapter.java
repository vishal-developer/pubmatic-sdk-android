package com.pubmatic.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 *
 */

public class PMPagerAdapter extends FragmentStatePagerAdapter {

    private HomeActivity activity;

    public PMPagerAdapter(FragmentManager fm, HomeActivity homeActivity) {
        super(fm);
        activity = homeActivity;
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
            case 2:
                LogFragment logsFragment = new LogFragment();
                logsFragment.setLogs(activity.getLogs());
                return logsFragment;
            default:
                Fragment fragment = new HomeFragment();
                return fragment;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}