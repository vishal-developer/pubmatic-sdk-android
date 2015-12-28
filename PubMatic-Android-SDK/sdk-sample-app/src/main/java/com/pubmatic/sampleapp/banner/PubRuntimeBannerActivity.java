package com.pubmatic.sampleapp.banner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.pubmatic.PUBAdSize;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.CommonDelegate;

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
		banner.setLogLevel(CommonDelegate.LogLevel.Debug);
		banner.execute(adRequest);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
