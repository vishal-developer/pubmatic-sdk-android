package com.pubmatic.headerbiddingsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.pubmatic.sdk.headerbidding.PubMaticPrefetchManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BannerDemoScreen extends AppCompatActivity {

    // To track all adViews on this page.
    private Set<PublisherAdView> adViews = new HashSet<>();

    private HashMap<String, PublisherAdView> adSlotAdViewMap = new HashMap<>();

    private PubMaticPrefetchManager headerBiddingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        PublisherAdView adView1 = (PublisherAdView) findViewById(R.id.publisherAdView1);
        PublisherAdView adView2 = (PublisherAdView) findViewById(R.id.publisherAdView2);

        adViews.add(adView1);
        adViews.add(adView2);

        adSlotAdViewMap.put("impression1", adView1);
        adSlotAdViewMap.put("impression2", adView2);

        // For Adapter
        HeaderBiddingBannerAdapter headerBiddingBannerAdapter = new HeaderBiddingBannerAdapter(this, adSlotAdViewMap);
        headerBiddingBannerAdapter.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        adSlotAdViewMap.clear();

        for (PublisherAdView adView : adViews)
            adView.destroy();

        adViews.clear();
    }
}

