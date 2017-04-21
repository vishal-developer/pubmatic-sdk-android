package com.pubmatic.headerbiddingsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.pubmatic.sdk.headerbidding.PMAdSize;
import com.pubmatic.sdk.headerbidding.PMPrefetchManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterstitialDemoScreen extends AppCompatActivity {

    // To track all adViews on this page.
    private Set<PublisherInterstitialAd> adViews = new HashSet<>();

    private List<HeaderBiddingInterstitialAdapter.AdSlotInfo> adSlotInfoList;
    private HeaderBiddingInterstitialAdapter headerBiddingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        PublisherInterstitialAd adView = new PublisherInterstitialAd(this);
        adView.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));

        adViews.add(adView);

        List<PMAdSize>   adSizes1 = new ArrayList<>(1);
        adSizes1.add(new PMAdSize(320, 480));
        HeaderBiddingInterstitialAdapter.AdSlotInfo adSlotInfo1 = new HeaderBiddingInterstitialAdapter.AdSlotInfo("/15671365/mobile_app_hb", adSizes1, adView);

        adSlotInfoList = new ArrayList<>(2);
        adSlotInfoList.add(adSlotInfo1);

        // For Adapter
        headerBiddingHelper = new HeaderBiddingInterstitialAdapter(this, adSlotInfoList);
        headerBiddingHelper.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        adSlotInfoList.clear();

        adViews.clear();

        if(headerBiddingHelper !=null) {
            //hbBannerHelper.destroy();
            headerBiddingHelper = null;
        }
    }
}

