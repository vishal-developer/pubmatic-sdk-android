package com.pubmatic.headerbiddingsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

/**
 *
 */
public class HomeScreen extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ((Button)findViewById(R.id.banner_demo)).setOnClickListener(this);
        ((Button)findViewById(R.id.interstitial_demo)).setOnClickListener(this);
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
