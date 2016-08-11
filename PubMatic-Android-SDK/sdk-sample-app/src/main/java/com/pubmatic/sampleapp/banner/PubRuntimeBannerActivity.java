package com.pubmatic.sampleapp.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.pubmatic.PUBAdSize;

public class PubRuntimeBannerActivity extends Activity {

    PMBannerAdView banner;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pubmatic_activity_runtime_banner);

        banner = new PMBannerAdView(this);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.parent);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                                               LayoutParams.WRAP_CONTENT);
        params.setLayoutDirection(RelativeLayout.ALIGN_PARENT_TOP);
        layout.addView(banner, params);
        PMLogger.setLogLevel(PMLogger.LogLevel.Debug);

        PubMaticBannerAdRequest adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(
                PubRuntimeBannerActivity.this,
                "31400",
                "32504",
                "439662");
        adRequest.setAdSize(PUBAdSize.PUBBANNER_SIZE_320x50);
        banner.setUseInternalBrowser(true);
        banner.setUpdateInterval(15);

        Location location = new Location("");
        location.setLatitude(1.0);
        location.setLongitude(2.0);

        adRequest.setLocation(location);

        banner.execute(adRequest);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (banner != null) {
            // Note: It is mandatory to call reset() method before activity gets destroyed
            banner.reset();
        }
    }

}
