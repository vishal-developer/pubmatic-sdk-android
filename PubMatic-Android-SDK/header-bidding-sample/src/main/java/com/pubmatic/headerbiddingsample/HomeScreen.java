package com.pubmatic.headerbiddingsample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.PubMaticSDK;


/**
 *
 */
public class HomeScreen extends Activity implements View.OnClickListener{

    private int MULTIPLE_PERMISSIONS_REQUEST_CODE = 123;
    private static String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        PubMaticSDK.setLogLevel(PMLogger.LogLevel.Debug);
        ((Button)findViewById(R.id.banner_demo)).setOnClickListener(this);
        ((Button)findViewById(R.id.interstitial_demo)).setOnClickListener(this);

        if(!hasPermissions(HomeScreen.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(HomeScreen.this, PERMISSIONS, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        }
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
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()){
            case R.id.banner_demo:
                intent = new Intent(HomeScreen.this, BannerDemoScreen.class);
                break;
            case R.id.interstitial_demo:
                intent = new Intent(HomeScreen.this, InterstitialDemoScreen.class);
                break;
        }
        if(intent!=null)
            HomeScreen.this.startActivity(intent);
    }
}
