package com.pubmatic.sample;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.PubMaticSDK;

public class HomeActivity extends FragmentActivity implements
        ActionBar.TabListener{

    PMPagerAdapter mPMPagerAdapter;
    ViewPager mViewPager;

    private final Handler handler = new Handler();
    private int MULTIPLE_PERMISSIONS_REQUEST_CODE = 123;
    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        PubMaticSDK.setLogLevel(PMLogger.LogLevel.Debug);

        boolean isAutoLocationDetectionChecked = PubMaticPreferences.getBooleanPreference(this, PubMaticPreferences.PREFERENCE_KEY_AUTO_LOCATION_DETECTION);
        PubMaticSDK.setLocationDetectionEnabled(isAutoLocationDetectionChecked);

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

        //Invoke permission check after 500 millisec. It is required for CI invoke
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!hasPermissions(HomeActivity.this, PERMISSIONS)){
                    ActivityCompat.requestPermissions(HomeActivity.this, PERMISSIONS, MULTIPLE_PERMISSIONS_REQUEST_CODE);
                }
            }
        }, 500);

    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}