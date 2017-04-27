package com.pubmatic.sampleapp.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMBannerAdView.BannerAdViewDelegate.RequestListener;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.pubmatic.PUBAdSize;

import java.util.Map;

import static com.pubmatic.sdk.common.pubmatic.PUBAdSize.PUBBANNER_SIZE_300x250;

public class PubRuntimeBannerActivity extends Activity {

    private PMBannerAdView banner;
    private int screenWidth, screenHeight;
    private RequestListener mRequestListener;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pubmatic_activity_runtime_banner);

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

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

        PUBAdSize size1 = new PUBAdSize(300,50);
        PUBAdSize size2 = new PUBAdSize(640,100);
        PUBAdSize arr[]= {size1, size2, PUBBANNER_SIZE_300x250, PUBAdSize.PUBBANNER_SIZE_320x100};
        adRequest.setOptionalAdSizes(arr);

        banner.setUseInternalBrowser(true);
        //banner.setUpdateInterval(15);

        mRequestListener = new RequestListener() {

            @Override
            public void onReceivedThirdPartyRequest(PMBannerAdView arg0, Map<String, String> arg1,
                                                    Map<String, String> arg2) {
            }

            @Override
            public void onReceivedAd(PMBannerAdView adView) {

                //Logic to resize ad slot, if required.
                if (adView.getAdHeight() != 0) {
                    if (screenWidth < adView.getAdWidth() && screenHeight < adView.getAdHeight()) {
                        adView.setLayoutParams(
                                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    } else {
                        if (screenWidth < adView.getAdWidth()) {
                            adView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                    adView.getAdHeight()));
                        } else if (screenHeight < adView.getAdHeight()) {
                            adView.setLayoutParams(
                                    new RelativeLayout.LayoutParams(adView.getAdWidth(), LayoutParams.MATCH_PARENT));
                        } else {
                            Log.d("PM Banner Demo","Creative width/height is less than screen dimension");
                            adView.setLayoutParams(
                                    new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                        }
                    }
                }
            }

            @Override
            public void onFailedToReceiveAd(PMBannerAdView arg0,  int errorCode, String msg) {
                Toast.makeText(PubRuntimeBannerActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        };

        banner.setRequestListener(mRequestListener);
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
