package com.pubmatic.sampleapp.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.mocean.MoceanBannerAdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMLogger;


public class MoceanRuntimeBannerActivity extends Activity {

    PMBannerAdView banner;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mocean_activity_runtime_banner);

        banner = new PMBannerAdView(this);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.parent);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params.setLayoutDirection(RelativeLayout.ALIGN_PARENT_TOP);
        layout.addView(banner, params);
        PMLogger.setLogLevel(PMLogger.LogLevel.Debug);

        MoceanBannerAdRequest adRequest = MoceanBannerAdRequest
                .createMoceanBannerAdRequest(this, "88269");//279722 88269 156037
        banner.setUseInternalBrowser(true);
        banner.execute(adRequest);

    }

}
