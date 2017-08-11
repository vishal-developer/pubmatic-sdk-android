package com.pubmatic.headerbiddingsample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;



/**
 *
 */
public class HomeScreen extends Activity implements View.OnClickListener{

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 12355;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ((Button)findViewById(R.id.banner_demo)).setOnClickListener(this);
        ((Button)findViewById(R.id.interstitial_demo)).setOnClickListener(this);

        int LocationPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(LocationPermissionCheck != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, MY_PERMISSIONS_REQUEST_LOCATION);

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
                //PublisherInterstitialAd adView1 = new PublisherInterstitialAd(this);
                break;
        }
        if(intent!=null)
            HomeScreen.this.startActivity(intent);
    }
}
