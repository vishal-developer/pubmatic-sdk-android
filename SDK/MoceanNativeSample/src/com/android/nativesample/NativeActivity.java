/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2014
 * PubMatic, All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained herein are
 * proprietary to PubMatic and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from PubMatic.
 * Access to the source code contained herein is hereby forbidden to anyone
 * except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 * 
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes information
 * that is confidential and/or proprietary, and is a trade secret, of PubMatic.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC
 * DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN
 * CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO
 * REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.android.nativesample;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moceanmobile.mast.MASTAdView.LogLevel;
import com.moceanmobile.mast.MASTNativeAd;
import com.moceanmobile.mast.MASTNativeAd.Image;
import com.moceanmobile.mast.MASTNativeAd.LogListener;
import com.moceanmobile.mast.MASTNativeAd.NativeRequestListener;
import com.moceanmobile.mast.MediationData;
import com.moceanmobile.mast.bean.AssetRequest;
import com.moceanmobile.mast.bean.AssetResponse;
import com.moceanmobile.mast.bean.DataAssetRequest;
import com.moceanmobile.mast.bean.DataAssetResponse;
import com.moceanmobile.mast.bean.DataAssetTypes;
import com.moceanmobile.mast.bean.ImageAssetRequest;
import com.moceanmobile.mast.bean.ImageAssetResponse;
import com.moceanmobile.mast.bean.ImageAssetTypes;
import com.moceanmobile.mast.bean.TitleAssetRequest;
import com.moceanmobile.mast.bean.TitleAssetResponse;

public class NativeActivity extends Activity {

	// TODO : Add your Mocean zone id
	private static final int ZONE_ID = 179492;

	private static final String LOG_TAG = NativeActivity.class.getSimpleName();
	private MASTNativeAd ad = null;
	private ImageView imgLogo = null;
	private ImageView imgMain = null;
	private TextView txtTitle = null;
	private Button ctaButton = null;
	private TextView txtDescription = null;
	private RatingBar ratingBar = null;
	private TextView txtLogView = null;
	private RelativeLayout mLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_native);

		mLayout = (RelativeLayout) findViewById(R.id.layout);
		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		imgMain = (ImageView) findViewById(R.id.imgMain);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		ctaButton = (Button) findViewById(R.id.buttonCta);
		txtDescription = (TextView) findViewById(R.id.txtDescription);
		ratingBar = (RatingBar) findViewById(R.id.ratingbar);
		txtLogView = (TextView) findViewById(R.id.textView);

		// Initialize the adview
		ad = new MASTNativeAd(this);
		ad.setLogLevel(LogLevel.Debug); // Logging should be disabled in
										// production
		ad.setLogListener(new LogEventListner()); // Set LogListener
		ad.setRequestListener(new AdRequestListener());
		ad.setZone(ZONE_ID);

		// Set custom Base URL for mocean adserver if required
		// ad.setAdNetworkURL("http://ads.mocean.mobi/ad");

		// Request for native assets
		ad.addNativeAssetRequestList(getAssetRequests());
		/*
		 * Uncomment following line to use internal browser instead system
		 * default browser, to open ads when clicked
		 */
		ad.setUseInternalBrowser(true);

		// ad.setTest(true); // Uncomment to serve ads in test mode

		// Request for ads
		ad.update();
	}

	private List<AssetRequest> getAssetRequests() {
		List<AssetRequest> assets = new ArrayList<AssetRequest>();

		TitleAssetRequest titleAsset = new TitleAssetRequest();
		titleAsset.setAssetId(1001); // Unique assetId is mandatory for each
										// asset
		titleAsset.setLength(50);
		titleAsset.setRequired(true); // Optional (Default: false)
		assets.add(titleAsset);

		ImageAssetRequest imageAssetIcon = new ImageAssetRequest();
		imageAssetIcon.setAssetId(1003);
		imageAssetIcon.setImageType(ImageAssetTypes.icon);
		imageAssetIcon.setWidth(60); // Optional
		imageAssetIcon.setHeight(60); // Optional
		assets.add(imageAssetIcon);

		ImageAssetRequest imageAssetMainImage = new ImageAssetRequest();
		imageAssetMainImage.setAssetId(1004);
		imageAssetMainImage.setImageType(ImageAssetTypes.main);
		assets.add(imageAssetMainImage);

		DataAssetRequest dataAssetDesc = new DataAssetRequest();
		dataAssetDesc.setAssetId(1002);
		dataAssetDesc.setDataAssetType(DataAssetTypes.desc);
		dataAssetDesc.setLength(25);
		assets.add(dataAssetDesc);

		DataAssetRequest dataAssetRating = new DataAssetRequest();
		dataAssetRating.setAssetId(1005);
		dataAssetRating.setDataAssetType(DataAssetTypes.rating);
		assets.add(dataAssetRating);

		DataAssetRequest dataAssetCta = new DataAssetRequest();
		dataAssetCta.setAssetId(1006);
		dataAssetCta.setDataAssetType(DataAssetTypes.ctatext);
		assets.add(dataAssetCta);

		return assets;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		resetViews();
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
		ctaButton.setText("<CTA>");
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

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						List<AssetResponse> nativeAssets = ad.getNativeAssets();
						for (AssetResponse asset : nativeAssets) {
							try {
								/*
								 * As per openRTB standard, assetId in response
								 * must match that of in request.
								 */
								switch (asset.getAssetId()) {
								case 1001:
									txtTitle.setText(((TitleAssetResponse) asset)
											.getTitleText());
									break;
								case 1003:
									Image iconImage = ((ImageAssetResponse) asset)
											.getImage();
									if (iconImage != null) {
										imgLogo.setImageBitmap(null);
										ad.loadImage(imgLogo,
												iconImage.getUrl());
									}
									break;
								case 1004:
									Image mainImage = ((ImageAssetResponse) asset)
											.getImage();
									if (mainImage != null) {
										imgMain.setImageBitmap(null);
										ad.loadImage(imgMain,
												mainImage.getUrl());
									}
									break;
								case 1002:
									txtDescription
											.setText(((DataAssetResponse) asset)
													.getValue());
									break;
								case 1006:
									ctaButton
											.setText(((DataAssetResponse) asset)
													.getValue());
									break;
								case 1005:
									String ratingStr = ((DataAssetResponse) asset)
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
		public void onReceivedThirdPartyRequest(MASTNativeAd mastNativeAd,
				Map<String, String> properties, Map<String, String> parameters) {

			appendOutput("Third Party Ad Received. \n Properties : \n "
					+ properties + " Parameters : \n " + parameters);
			MediationData mediationData = mastNativeAd.getMediationData();
			if (mediationData != null) {
				appendOutput("Name: " + mediationData.getMediationNetworkName());
				appendOutput("NetworkId: "
						+ mediationData.getMediationNetworkId());
				appendOutput("Source: " + mediationData.getMediationSource());
				appendOutput("AdId: " + mediationData.getMediationAdId());
			}

			// ---------------------------------------------------------
			// Write Code to initialize third party SDK and request ads.
			// ---------------------------------------------------------

			// Test sending impression tracker and click trackers.

			// Note: This method should be called only when ad from third party
			// SDK is rendered.
			// mastNativeAd.sendImpression(); // Method added here only for
			// testing
			// purpose

			// Note: This method should be called only when ad clicked callback
			// is received from third party SDK.
			// mastNativeAd.sendClickTracker(); // Method added here only for
			// testing purpose

		}

		@Override
		public void onNativeAdClicked(MASTNativeAd ad) {
			appendOutput("Ad is clicked.");
		}
	}

	private class LogEventListner implements LogListener {

		@Override
		public boolean onLogEvent(MASTNativeAd nativeAd, String eventMessage,
				LogLevel logLevel) {
			Log.i(LOG_TAG, eventMessage);
			return false;
		}

	}
}
