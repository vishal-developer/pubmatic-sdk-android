package com.pubmatic.headerbiddingsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.pubmatic.sdk.headerbidding.PubMaticDecisionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class InterstitialDemoScreen extends AppCompatActivity {

    // To track all adViews on this page.
    private Set<PublisherInterstitialAd> adViews = new HashSet<>();

    private HashMap<String, PublisherInterstitialAd> adSlotAdViewMap = new HashMap<>();

    private PubMaticDecisionManager headerBiddingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        PublisherInterstitialAd adView = new PublisherInterstitialAd(this);
        adView.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));

        adSlotAdViewMap.put("impression1", adView);

        // For Adapter
        HeaderBiddingInterstitialAdapter headerBiddingInterstitialAdapter = new HeaderBiddingInterstitialAdapter(this, adSlotAdViewMap);
        headerBiddingInterstitialAdapter.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        adSlotAdViewMap.clear();
        adViews.clear();
    }
}

