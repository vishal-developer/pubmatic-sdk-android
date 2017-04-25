package com.pubmatic.sample;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends FragmentActivity implements
        ActionBar.TabListener{

    PMPagerAdapter mPMPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mViewPager = (ViewPager) findViewById(R.id.pager);

        final ActionBar actionBar = getActionBar();

        actionBar.setHomeButtonEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.holo_blue_dark)));

        // Specify that tabs should be displayed in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab()
                .setContentDescription("Home")
                .setIcon(getResources().getDrawable(R.drawable.home))
                .setTabListener(this));

        actionBar.addTab(actionBar.newTab()
                .setContentDescription("Settings")
                .setIcon(getResources().getDrawable(R.drawable.settings))
                .setTabListener(this));

        mPMPagerAdapter = new PMPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPMPagerAdapter);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_info:

                HelpDialogFragment helpDialogFragment = new HelpDialogFragment();
                helpDialogFragment.show(HomeActivity.this.getFragmentManager(), "HelpFragment");

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }
}