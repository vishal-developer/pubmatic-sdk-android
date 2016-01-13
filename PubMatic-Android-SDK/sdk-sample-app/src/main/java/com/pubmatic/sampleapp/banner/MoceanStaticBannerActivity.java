package com.pubmatic.sampleapp.banner;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.banner.PMInterstitialAdView;
import com.pubmatic.sdk.banner.mocean.MoceanBannerAdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMLogger;

public class MoceanStaticBannerActivity extends Activity {

	PMInterstitialAdView banner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mocean_activity_static_banner);
		loadInterstitial();
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

	public void showInterstitial(View view) {

		if(banner!=null)
			banner.showInterstitial();
	}

	public void loadInterstitial() {

		PMLogger.setLogLevel(PMLogger.LogLevel.Debug);

		if(banner==null) {
			banner = new PMInterstitialAdView(this);

			RelativeLayout layout = (RelativeLayout) findViewById(R.id.parent);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				params.setLayoutDirection(RelativeLayout.ALIGN_PARENT_TOP);
			}
			layout.addView(banner, params);


			banner.setUseInternalBrowser(true);
		}

		MoceanBannerAdRequest adRequest = MoceanBannerAdRequest
				.createMoceanBannerAdRequest(this, "88269");//279722 88269 156037
		banner.execute(adRequest);
	}
}
