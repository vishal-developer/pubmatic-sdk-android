/*

 * PubMatic Inc. ("PubMatic") CONFIDENTIAL

 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.

 *

 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained

 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.

 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained

 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed 

 * Confidentiality and Non-disclosure agreements explicitly covering such access.

 *

 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes  

 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, 

 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE 

 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  

 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.                

 */
package com.moceanmobile.mast.samples;

import java.util.Calendar;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moceanmobile.mast.MASTBaseAdapter.MediationNetwork;
import com.moceanmobile.mast.MASTNativeAd;
import com.moceanmobile.mast.MASTNativeAd.Image;
import com.moceanmobile.mast.MASTNativeAd.NativeRequestListener;
import com.moceanmobile.mast.NativeAdSize;

public class NativeActivity extends Activity {

	private MASTNativeAd ad = null;
	private ImageView imgLogo = null;
	private ImageView imgMain = null;
	private TextView txtTitle = null;
	private TextView txtDescription = null;
	private RatingBar ratingBar = null;
	private TextView txtLogView = null;
	private RelativeLayout mLayout = null;

	private final String AD_URL = "http://ads.mocean.mobi/ad";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_native);

		mLayout = (RelativeLayout) findViewById(R.id.layout);
		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		imgMain = (ImageView) findViewById(R.id.imgMain);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtDescription = (TextView) findViewById(R.id.txtDescription);
		ratingBar = (RatingBar) findViewById(R.id.ratingbar);
		txtLogView = (TextView) findViewById(R.id.textView);

		// Initialize the adview
		ad = new MASTNativeAd(this);
		ad.setRequestListener(new AdRequestListener());
		ad.setZone(179492); // TODO: Add your ZoneId
		
		ad.setAdNetworkURL(AD_URL);
		ad.setDescriptionLength(150);
		ad.setTitleLength(30);
		ad.setIconImageSize(NativeAdSize.ICON_IMAGE_300X300);
		ad.setLogoImageSize(NativeAdSize.LOGO_IMAGE_80X80);
		ad.setMainImageSize(NativeAdSize.MAIN_IMAGE_1200X627);
		ad.setLocationDetectionEnabled(true);
		ad.setNativeContent("1,2,3,4,5,6,7");

		// ad.setTest(true);

		// This is by default false.
		ad.overrideAdapterLoading(false);

		/*
		 * Add your device id for Facebook Audience network to get test ads.
		 * This will be printed in the logs once you launch the application for
		 * the first time.
		 */
		ad.addTestDeviceIdForNetwork(
				MediationNetwork.FACEBOOK_AUDIENCE_NETWORK, "HASHED_ID");
		// TODO: Add your test device hash id above

		ad.update();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		reset();
		ad.destroy();
	}

	public void onReloadAdClicked(View v) {
		if (ad != null) {

			reset();
			ad.reset();
			ad.update();
		}
	}

	private void reset() {
		imgLogo.setImageBitmap(null);

		imgMain.setImageBitmap(null);

		txtTitle.setText("<Native Title>");

		txtDescription.setText("<Native Description>");

		ratingBar.setRating(0f);
		ratingBar.setVisibility(View.GONE);

		txtLogView.setText("");
	}

	private void appendOutput(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (txtLogView != null && !TextUtils.isEmpty(message)) {
					txtLogView.append(java.text.DateFormat.getTimeInstance(
							java.text.DateFormat.DEFAULT).format(
							Calendar.getInstance().getTime())
							+ " : " + message + "\n");
					Log.i("Samples:NativeActivity", message);
				}
			}
		});
	}

	private class AdRequestListener implements NativeRequestListener {

		@Override
		public void onNativeAdFailed(MASTNativeAd ad, Exception ex) {
			ex.printStackTrace();
			appendOutput("Error Message/Code : " + ex.getMessage());
		}

		@Override
		public void onNativeAdReceived(final MASTNativeAd ad) {

			if (ad != null) {

				appendOutput("Native Ad Received. Response is : \n "
						+ ad.getAdResponse());

				ad.trackViewForInteractions(mLayout);

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Image iconImage = ad.getIconImage();
						Image mainImage = ad.getMainImage();
						if (iconImage != null) {
							imgLogo.setImageBitmap(null);
							ad.loadImage(imgLogo, iconImage.getUrl());
						}
						if (mainImage != null) {
							imgMain.setImageBitmap(null);
							ad.loadImage(imgMain, mainImage.getUrl());
						}

						txtTitle.setText(ad.getTitle());
						txtDescription.setText(ad.getText());
						if (ad.getRating() > 0f) {
							ratingBar.setRating(ad.getRating());
							ratingBar.setVisibility(View.VISIBLE);
						} else {
							ratingBar.setRating(ad.getRating());
							ratingBar.setVisibility(View.GONE);
						}

					}
				});
			}

		}

		@Override
		public void onReceivedThirdPartyRequest(MASTNativeAd ad,
				Map<String, String> properties, Map<String, String> parameters) {
			appendOutput("Third Party Ad Received. \n Properties : \n "
					+ properties + " Parameters : \n " + parameters);
		}

		@Override
		public void onNativeAdClicked(MASTNativeAd ad) {
			appendOutput("Ad is clicked.");
		}
	}
}
