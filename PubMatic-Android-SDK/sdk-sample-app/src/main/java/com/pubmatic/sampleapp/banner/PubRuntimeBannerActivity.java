package com.pubmatic.sampleapp.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.common.pubmatic.PUBAdSize;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;

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

		PubMaticBannerAdRequest adRequest = PubMaticBannerAdRequest
				.createPubMaticBannerAdRequest(PubRuntimeBannerActivity.this,
											   "31400",
											   "32504",
											   "439662");
		adRequest.setAdSize(PUBAdSize.PUBBANNER_SIZE_320x50);
		banner.setUseInternalBrowser(true);
		banner.execute(adRequest);

	}

}
