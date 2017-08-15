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

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.nativead.PMNativeAd;
import com.pubmatic.sdk.nativead.bean.PMAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMDataAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMDataAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMDataAssetTypes;
import com.pubmatic.sdk.nativead.bean.PMImageAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMImageAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMImageAssetTypes;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetResponse;
import com.pubmatic.sdk.nativead.pubmatic.PubMaticNativeAdRequest;

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

		setPrefetchIds("31400", "52368", "383372");
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

		// Enable device id detection
		ad.setAndroidaidEnabled(true);

		// ad.setTest(true); // Uncomment to serve ads in test mode

		PubMaticNativeAdRequest adRequest = PubMaticNativeAdRequest
				.createPubMaticNativeAdRequest(this, pubId, siteId, adId, getAssetRequests());

		// Request for ads
		ad.execute(adRequest);
	}
	private List<PMAssetRequest> getAssetRequests() {
		List<PMAssetRequest> assets = new ArrayList<PMAssetRequest>();

		PMTitleAssetRequest titleAsset = new PMTitleAssetRequest(3);// Unique assetId is mandatory for each asset
		titleAsset.setLength(50);
		titleAsset.setRequired(true); // Optional (Default: false)
		assets.add(titleAsset);

		PMImageAssetRequest imageAssetIcon = new PMImageAssetRequest(1);
		imageAssetIcon.setImageType(PMImageAssetTypes.icon);
		assets.add(imageAssetIcon);

		PMImageAssetRequest imageAssetMainImage = new PMImageAssetRequest(5);
		imageAssetMainImage.setImageType(PMImageAssetTypes.main);
		assets.add(imageAssetMainImage);

		PMDataAssetRequest dataAssetDesc = new PMDataAssetRequest(2);
		dataAssetDesc.setDataAssetType(PMDataAssetTypes.desc);
		dataAssetDesc.setLength(25);
		assets.add(dataAssetDesc);

		PMDataAssetRequest dataAssetRating = new PMDataAssetRequest(6);
		dataAssetRating.setDataAssetType(PMDataAssetTypes.rating);
		assets.add(dataAssetRating);

		PMDataAssetRequest dataAssetCta = new PMDataAssetRequest(7);
		dataAssetCta.setDataAssetType(PMDataAssetTypes.ctatext);
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
		public void onNativeAdFailed(PMNativeAd ad, Exception ex) {
			if(ex!=null) {
				ex.printStackTrace();
				appendOutput("Error Message/Code : " + ex!=null ? ex.getMessage() : "No msg.");
			}
		}

		@Override
		public void onNativeAdReceived(final PMNativeAd ad) {

			if (ad != null) {

				appendOutput("Native Ad Received. Response is : \n "
						+ ad.getAdResponse());

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						List<PMAssetResponse> nativeAssets = ad.getNativeAssets();
						for (PMAssetResponse asset : nativeAssets) {
							try {
								/*
								 * As per openRTB standard, assetId in response
								 * must match that of in request.
								 */
								switch (asset.getAssetId()) {
								case 3:
									txtTitle.setText(((PMTitleAssetResponse) asset)
											.getTitleText());
									break;
								case 1:
									PMNativeAd.Image iconImage = ((PMImageAssetResponse) asset)
											.getImage();
									if (iconImage != null) {
										imgLogo.setImageBitmap(null);
										ad.loadImage(imgLogo,
												iconImage.getUrl());
									}
									break;
								case 5:
									PMNativeAd.Image mainImage = ((PMImageAssetResponse) asset)
											.getImage();
									if (mainImage != null) {
										imgMain.setImageBitmap(null);
										ad.loadImage(imgMain,
												mainImage.getUrl());
									}
									break;
								case 2:
									txtDescription
											.setText(((PMDataAssetResponse) asset)
													.getValue());
									break;
								case 7:
									ctaText
											.setText(((PMDataAssetResponse) asset).getValue());
									break;
								case 6:
									String ratingStr = ((PMDataAssetResponse) asset)
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
					}
				});

				if (ad.getJsTracker() != null) {
					appendOutput(ad.getJsTracker());
					/*
					 * Note: Publisher should execute the javascript tracker
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
		public void onNativeAdClicked(PMNativeAd ad) {
			appendOutput("Ad is clicked.");
		}
	}

	private class LogEventListner implements PMLogger.LogListener {


		@Override
		public void onLogEvent(String event, PMLogger.LogLevel logLevel) {
			Log.i(LOG_TAG, event);
		}
	}
}