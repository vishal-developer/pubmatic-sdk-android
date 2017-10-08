/*
 * PubMatic Inc. (�PubMatic�) CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2017 PubMatic, All Rights Reserved.
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
package com.pubmatic.sampleapp.nativead;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.common.PMError;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.nativead.PMNativeAd;
import com.pubmatic.sdk.nativead.bean.PMNativeAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMNativeAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMNativeDataAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMNativeDataAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMNativeDataAssetTypes;
import com.pubmatic.sdk.nativead.bean.PMNativeImageAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMNativeImageAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMNativeImageAssetTypes;
import com.pubmatic.sdk.nativead.bean.PMNativeTitleAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMNativeTitleAssetResponse;
import com.pubmatic.sdk.nativead.pubmatic.PMNativeAdRequest;

public class NativeDemoActivity extends Activity {

	private static final String LOG_TAG = NativeDemoActivity.class.getSimpleName();
	private PMNativeAd ad = null;
	private ImageView imgLogo = null;
	private ImageView imgMain = null;
	private TextView txtTitle = null;
	private TextView ctaText = null;
	private TextView txtDescription = null;
	private RatingBar ratingBar = null;
	private TextView txtLogView = null;
	private RelativeLayout mLayout = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nativead_activity);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		mLayout = (RelativeLayout) findViewById(R.id.layout);
		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		imgMain = (ImageView) findViewById(R.id.imgMain);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		ctaText = (TextView) findViewById(R.id.ctaText);
		txtDescription = (TextView) findViewById(R.id.txtDescription);
		ratingBar = (RatingBar) findViewById(R.id.ratingbar);
		txtLogView = (TextView) findViewById(R.id.textView);

		setPrefetchIds("156453", "219778", "1178273");
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
		loadAdBtn.setText("Load Native Ad");
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

				loadAd(pubId, siteId, adId, v);
			}
		});
	}

	private void loadAd(String pubId, String siteId, String adId, View view) {

		// Initialize the adview
		ad = new PMNativeAd(this);
		ad.setRequestListener(new AdRequestListener());

		/*
		 * Uncomment following line to use internal browser instead system
		 * default browser, to open ads when clicked
		 */
		ad.setUseInternalBrowser(true);

		// ad.setTest(true); // Uncomment to serve ads in test mode

		PMNativeAdRequest adRequest = PMNativeAdRequest
				.createPMNativeAdRequest(pubId, siteId, adId, getAssetRequests());

		// Request for ads
		ad.loadRequest(adRequest);
	}

	private List<PMNativeAssetRequest> getAssetRequests() {
		// First create some assets to add in the request
		List<PMNativeAssetRequest> assets = new ArrayList<PMNativeAssetRequest>();

		// Unique assetId is mandatory for each asset
		PMNativeTitleAssetRequest titleAsset = new PMNativeTitleAssetRequest(1);
		titleAsset.setLength(50);
		titleAsset.setRequired(true); // Optional (Default: false)
		assets.add(titleAsset);

		PMNativeImageAssetRequest imageAssetIcon = new PMNativeImageAssetRequest(2);
		imageAssetIcon.setImageType(PMNativeImageAssetTypes.icon);
		assets.add(imageAssetIcon);

		PMNativeImageAssetRequest imageAssetMainImage = new PMNativeImageAssetRequest(3);
		imageAssetMainImage.setImageType(PMNativeImageAssetTypes.main);
		assets.add(imageAssetMainImage);

		PMNativeDataAssetRequest dataAssetDesc = new PMNativeDataAssetRequest(5);
		dataAssetDesc.setDataAssetType(PMNativeDataAssetTypes.desc);
		dataAssetDesc.setLength(25);
		assets.add(dataAssetDesc);

		PMNativeDataAssetRequest dataAssetRating = new PMNativeDataAssetRequest(6);
		dataAssetRating.setDataAssetType(PMNativeDataAssetTypes.rating);
		assets.add(dataAssetRating);

		PMNativeDataAssetRequest dataAssetCta = new PMNativeDataAssetRequest(4);
		dataAssetCta.setDataAssetType(PMNativeDataAssetTypes.ctatext);
		assets.add(dataAssetCta);

		return assets;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		resetViews();
		if(ad!=null)
			ad.destroy();
	}

	public void onReloadAdClicked(View v) {
		if (ad != null) {

			resetViews();
			ad.update();
		}
	}

	private void resetViews() {
		imgLogo.setImageBitmap(null);
		imgMain.setImageBitmap(null);
		ctaText.setText("<CTA>");
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
					Log.d(LOG_TAG, message);
				}
			}
		});
	}

	private class AdRequestListener implements PMNativeAd.NativeRequestListener {

		@Override
		public void onNativeAdReceived(final PMNativeAd ad) {

			if (ad != null) {

				appendOutput("Native Ad Received. Response is : \n "
						+ ad.getAdResponse());

				List<PMNativeAssetResponse> nativeAssets = ad.getNativeAssets();
				for (PMNativeAssetResponse asset : nativeAssets) {
					try {
						/*
						 * As per openRTB standard, assetId in response
						 * must match that of in request.
						 */
						switch (asset.getAssetId()) {
						case 1:
							txtTitle.setText(((PMNativeTitleAssetResponse) asset)
									.getTitleText());
							break;
						case 2:
							PMNativeAd.Image iconImage = ((PMNativeImageAssetResponse) asset)
									.getImage();
							if (iconImage != null) {
								imgLogo.setImageBitmap(null);
								ad.loadImage(imgLogo,
										iconImage.getUrl());
							}
							break;
						case 3:
							PMNativeAd.Image mainImage = ((PMNativeImageAssetResponse) asset)
									.getImage();
							if (mainImage != null) {
								imgMain.setImageBitmap(null);
								ad.loadImage(imgMain,
										mainImage.getUrl());
							}
							break;
						case 5:
							txtDescription
									.setText(((PMNativeDataAssetResponse) asset)
											.getValue());
							break;
						case 4:
							ctaText
									.setText(((PMNativeDataAssetResponse) asset).getValue());
							break;
						case 6:
							String ratingStr = ((PMNativeDataAssetResponse) asset)
									.getValue();
							try {
								float rating = Float
										.parseFloat(ratingStr);
								if (rating > 0f) {
									ratingBar.setRating(rating);
									ratingBar
											.setVisibility(View.VISIBLE);
								} else {
									ratingBar.setRating(rating);
									ratingBar.setVisibility(View.GONE);
								}
							} catch (Exception e) {
								// Invalid rating string
								Log.e("NativeActivity",
										"Error parsing 'rating'");
							}
							break;

						default: // NOOP
							break;
						}
					} catch (Exception ex) {
						appendOutput("ERROR in rendering asset. Skipping asset.");
						ex.printStackTrace();
					}
				}

				if (ad.getJsTracker() != null) {
					appendOutput(ad.getJsTracker());
					/*
					 * Note: Publisher should loadRequest the javascript tracker
					 * whenever possible.
					 */
				}

				/*
				 * IMPORTANT : Must call this method when response rendering is
				 * complete. This method sets click listener on the ad container
				 * layout. This is required for firing click tracker when ad is
				 * clicked by the user.
				 */
				ad.trackViewForInteractions(mLayout);
			}

		}

		@Override
		public void onNativeAdFailed(PMNativeAd ad, PMError error) {
			Toast.makeText(NativeDemoActivity.this, error.toString(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onNativeAdClicked(PMNativeAd ad) {
			appendOutput("Ad is clicked.");
		}
	}

	private class LogEventListner implements PMLogger.LogListener {


		@Override
		public void onLogEvent(String event, PMLogger.PMLogLevel logLevel) {
			Log.i(LOG_TAG, event);
		}
	}
}