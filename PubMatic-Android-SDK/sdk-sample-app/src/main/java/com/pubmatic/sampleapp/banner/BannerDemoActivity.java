package com.pubmatic.sampleapp.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMBannerAdView.BannerAdViewDelegate.RequestListener;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.PMAdSize;
import com.pubmatic.sdk.common.PMLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.id.list;
import static com.pubmatic.sdk.common.PMAdSize.PUBBANNER_SIZE_300x250;

public class BannerDemoActivity extends Activity {

    private PMBannerAdView banner;
    private RequestListener mRequestListener;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pubmatic_activity_runtime_banner);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        PMLogger.setLogLevel(PMLogger.LogLevel.Debug);


        banner = (PMBannerAdView)findViewById(R.id.banner);

        setPrefetchIds("31400",
                "32504",
                "439662");


    }

    private void setPrefetchIds(String pubId, String siteId, String adId) {
        final EditText pubIdET = (EditText)findViewById(R.id.pubIdET);
        if(pubIdET!=null) {
            pubIdET.setText(pubId);
        }
        final EditText siteIdET = (EditText)findViewById(R.id.siteIdET);
        if(siteIdET!=null) {
            siteIdET.setText(siteId);
        }
        final EditText adIdET = (EditText)findViewById(R.id.adIdET);
        if(adIdET!=null) {
            adIdET.setText(adId);
        }

        //Handle click of load ad button
        Button loadAdBtn = (Button)findViewById(R.id.loadAdBtn);
        loadAdBtn.setText("Load Banner Ad");
        loadAdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pubId = null, siteId = null, adId = null;
                if(pubIdET!=null) {
                    pubId = pubIdET.getText().toString();
                }
                if(siteIdET!=null) {
                    siteId = siteIdET.getText().toString();
                }
                if(adIdET!=null) {
                    adId = adIdET.getText().toString();
                }

                loadAd(pubId, siteId, adId);
            }
        });
    }


    private void loadAd(String pubId, String siteId, String adId) {

        PubMaticBannerAdRequest adRequest = PubMaticBannerAdRequest.createPubMaticBannerAdRequest(
                BannerDemoActivity.this,
                pubId, siteId, adId);

        adRequest.setAdSize(PMAdSize.PUBBANNER_SIZE_320x50);

        PMAdSize size1 = new PMAdSize(300,50);
        PMAdSize size2 = new PMAdSize(640,100);
        PMAdSize arr[]= {size1, size2, PUBBANNER_SIZE_300x250, PMAdSize.PUBBANNER_SIZE_320x100};
        adRequest.setOptionalAdSizes(arr);

        //------ Setting custom parameters ------
        adRequest.setCustomParams("Key1", "value1");
        adRequest.setCustomParams("Key2", "value21");
        adRequest.setCustomParams("Key2", "value22");
        adRequest.setCustomParams("Key2", "value23");
        adRequest.setCustomParams("key3", "value3");
        //-------- End of custom parameter ---------

        banner.setUseInternalBrowser(true);
        banner.setUpdateInterval(15);

        mRequestListener = new RequestListener() {

            @Override
            public void onReceivedThirdPartyRequest(PMBannerAdView arg0, Map<String, String> arg1,
                                                    Map<String, String> arg2) {
            }

            @Override
            public void onReceivedAd(PMBannerAdView adView) {
                adView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailedToReceiveAd(PMBannerAdView arg0,  int errorCode, String msg) {
                Toast.makeText(BannerDemoActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        };

        banner.setRequestListener(mRequestListener);
        banner.execute(adRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (banner != null) {
            // Note: It is mandatory to call destroy() method before activity gets destroyed
            banner.destroy();
            banner = null;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
