package com.pubmatic.sampleapp.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.phoenix.PhoenixBannerAdRequest;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.phoenix.PhoenixAdRequest;
import com.pubmatic.sdk.common.pubmatic.PUBAdSize;

public class PhoenixRuntimeBannerActivity extends Activity {

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

        PhoenixBannerAdRequest adRequest = PhoenixBannerAdRequest.createPhoenixBannerAdRequest(
                PhoenixRuntimeBannerActivity.this,
                "555:fe29119e-894e-4122-8dcd-b5cb7350352a",
                "DIV1");
        adRequest.setAdWidth(970);
        adRequest.setAdHeight(250);
        adRequest.setAndroidAidEnabled(true);
        //SSP parameters
        adRequest.setAdFloor("AF");
        adRequest.setAdPosition(PhoenixAdRequest.PM_AD_POSITION.ABOVE_FOLD);
        adRequest.setAdTruth("AT");
        adRequest.setAid("aid_123");
        adRequest.setAppCategory("Ac_1");
        adRequest.setAppDomain("ad_test");
        adRequest.setAWT(PhoenixAdRequest.AWT_OPTION.WRAPPED_IN_JS);
        adRequest.setBlockAdDomain("block_domain_test");

        banner.setUseInternalBrowser(true);
        banner.setUpdateInterval(15);
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
