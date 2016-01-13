package com.pubmatic.sampleapp.nativead;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

import com.pubmatic.sampleapp.R;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.nativead.MediationData;
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

public class PubMaticNativeActivity extends Activity {

	private static final String ZONE_ID = "277298"; // TODO : Add your Mocean zone id

	private static final String LOG_TAG = MoceanNativeActivity.class.getSimpleName();
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

		mLayout = (RelativeLayout) findViewById(R.id.layout);
		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		imgMain = (ImageView) findViewById(R.id.imgMain);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		ctaText = (TextView) findViewById(R.id.ctaText);
		txtDescription = (TextView) findViewById(R.id.txtDescription);
		ratingBar = (RatingBar) findViewById(R.id.ratingbar);
		txtLogView = (TextView) findViewById(R.id.textView);

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
				.createPubMaticNativeAdRequest(this, "31400", "52368", "383372", getAssetRequests());
		// Request for ads
		ad.execute(adRequest);
	}

	private List<PMAssetRequest> getAssetRequests() {
		List<PMAssetRequest> assets = new ArrayList<PMAssetRequest>();

		PMTitleAssetRequest titleAsset = new PMTitleAssetRequest();
		titleAsset.setAssetId(1001); // Unique assetId is mandatory for each
										// asset
		titleAsset.setLength(50);
		titleAsset.setRequired(true); // Optional (Default: false)
		assets.add(titleAsset);

		PMImageAssetRequest imageAssetIcon = new PMImageAssetRequest();
		imageAssetIcon.setAssetId(1003);
		imageAssetIcon.setImageType(PMImageAssetTypes.icon);
		assets.add(imageAssetIcon);

		PMImageAssetRequest imageAssetMainImage = new PMImageAssetRequest();
		imageAssetMainImage.setAssetId(1004);
		imageAssetMainImage.setImageType(PMImageAssetTypes.main);
		assets.add(imageAssetMainImage);

		PMDataAssetRequest dataAssetDesc = new PMDataAssetRequest();
		dataAssetDesc.setAssetId(1002);
		dataAssetDesc.setDataAssetType(PMDataAssetTypes.desc);
		dataAssetDesc.setLength(25);
		assets.add(dataAssetDesc);

		PMDataAssetRequest dataAssetRating = new PMDataAssetRequest();
		dataAssetRating.setAssetId(1005);
		dataAssetRating.setDataAssetType(PMDataAssetTypes.rating);
		assets.add(dataAssetRating);

		PMDataAssetRequest dataAssetCta = new PMDataAssetRequest();
		dataAssetCta.setAssetId(1006);
		dataAssetCta.setDataAssetType(PMDataAssetTypes.ctatext);
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
								case 1001:
									txtTitle.setText(((PMTitleAssetResponse) asset)
											.getTitleText());
									break;
								case 1003:
									PMNativeAd.Image iconImage = ((PMImageAssetResponse) asset)
											.getImage();
									if (iconImage != null) {
										imgLogo.setImageBitmap(null);
										ad.loadImage(imgLogo,
												iconImage.getUrl());
									}
									break;
								case 1004:
									PMNativeAd.Image mainImage = ((PMImageAssetResponse) asset)
											.getImage();
									if (mainImage != null) {
										imgMain.setImageBitmap(null);
										ad.loadImage(imgMain,
												mainImage.getUrl());
									}
									break;
								case 1002:
									txtDescription
											.setText(((PMDataAssetResponse) asset)
													.getValue());
									break;
								case 1006:
									ctaText
											.setText(((PMDataAssetResponse) asset).getValue());
									break;
								case 1005:
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
		public void onReceivedThirdPartyRequest(PMNativeAd mastNativeAd,
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
		public void onNativeAdClicked(PMNativeAd ad) {
			appendOutput("Ad is clicked.");
		}
	}

	private class LogEventListner implements PMLogger.LogListener {

		@Override
		public boolean onLogEvent(View nativeAd, String eventMessage,
				PMLogger.LogLevel logLevel) {
			Log.i(LOG_TAG, eventMessage);
			return false;
		}
	}
}