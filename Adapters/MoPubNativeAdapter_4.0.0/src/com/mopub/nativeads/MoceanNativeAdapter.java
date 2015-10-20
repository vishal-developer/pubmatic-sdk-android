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

package com.mopub.nativeads;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.moceanmobile.mast.MASTAdView.LogLevel;
import com.moceanmobile.mast.MASTNativeAd;
import com.moceanmobile.mast.MASTNativeAd.NativeRequestListener;
import com.moceanmobile.mast.bean.AssetResponse;
import com.moceanmobile.mast.bean.DataAssetRequest;
import com.moceanmobile.mast.bean.DataAssetResponse;
import com.moceanmobile.mast.bean.DataAssetTypes;
import com.moceanmobile.mast.bean.ImageAssetRequest;
import com.moceanmobile.mast.bean.ImageAssetResponse;
import com.moceanmobile.mast.bean.ImageAssetTypes;
import com.moceanmobile.mast.bean.TitleAssetRequest;
import com.moceanmobile.mast.bean.TitleAssetResponse;

import static com.mopub.nativeads.NativeImageHelper.preCacheImages;

/**
 * PubMatic Native Adapter for MoPub SDK. This adapter extends
 * {@link CustomEventNative} class. You can use this adapter class to integrate
 * PubMatic (Mocean) Native Ad SDK into MoPub SDK using client side mediation.
 * <p>
 * <b>Mandatory server extra parameters required for Mocean SDK:</b><br>
 * In order to initialize and use Mocean Native SDK, following server extra
 * parameters needs to be configured on MoPub portal.
 * <ol>
 * <li>zoneId : Mocean ZoneID
 * </ol>
 * </p>
 * On the MoPub web interface, create a network with the "Custom Native Network"
 * type. Place the fully-qualified class name of your custom event class
 * (<b>com.mopub.mobileads.MoceanNativeAdapter</b>) in the "Custom Event Class"
 * column.
 * <p>
 * <b>Note:</b> To configure above server extra parameters for MoPub Custom
 * Native Network, enter following JSON in "Custom Event Class Data" column.<br>
 * Example JSON:
 * 
 * <pre>
 * {"zoneId":"123456"}
 * </pre>
 * 
 * <b>Optional : </b><br>
 * Optionally you can also set following extra parameters:
 * <ol>
 * <li>adServerUrl : Use this flag to set custom ad server URL. Permissible
 * value could be a URL of custom ad server.
 * </ol>
 * Example JSON: (Note that adServerUrl is optional)
 * 
 * <pre>
 * {"zoneId":"123456", "adServerUrl":"http://ads.test.mocean.mobi/ad?"}
 * </pre>
 * 
 * Once you've completed these steps, the MoPub SDK will be able to cause your
 * CustomEventNative subclass to be instantiated at the proper time while your
 * application is running. You do not need to instantiate any of these
 * subclasses in your application code. <br>
 * <p>
 * <b>Optional Local extra parameters: </b><br>
 * Optionally you can also set following local extra parameters in MoPub SDK.
 * <ol>
 * <li>mocean_sdk_log_level : Set the log level of Mocean SDK. Permissible
 * values are from {@link LogLevel} enum.
 * <li>mocean_sdk_location_detection_flag : Add this extra param with value as
 * {@link Boolean}.TRUE to enable location detection in Mocean SDK. (Default is
 * true, i.e. location detection is enabled).
 * <li>mocean_sdk_test_mode_flag : Add this extra param with value as
 * {@link Boolean}.TRUE to serve ads in test mode. (Default is false).
 * <li>mocean_sdk_use_inapp_browser_flag : Add this extra param with value as
 * {@link Boolean}.TRUE to use in-app browser for opening landing page. Else set
 * to FALSE to use native device browser. (Default is true).
 * </ol>
 * 
 * @see <a
 *      href="https://github.com/mopub/mopub-android-sdk/wiki/Custom-Events">MoPub
 *      Custom Events Wiki</a>
 * 
 */
public class MoceanNativeAdapter extends CustomEventNative {

	// Server Extra Parameter keys
	public static final String KEY_ZONE_ID = "zoneId";
	// Optional : adServerUrl
	public static final String KEY_AD_SERVER_URL = "adServerUrl";

	// Local Extra Parameter keys
	/**
	 * Set Log Level for Mocean Ad View using setLocalExtras method of MoPub
	 * SDK. Refer {@link LogLevel} for valid values of LogLevel.
	 * 
	 * @see LogLevel
	 */
	public static final String KEY_MOCEAN_LOG_LEVEL = "mocean_sdk_log_level";

	/**
	 * Set test mode for Mocean AdView using setLocalExtras method of MoPub SDK.<br>
	 * Valid values are Boolean : true/false.<br>
	 * Set true to enable ad serving in test mode
	 */
	public static final String KEY_MOCEAN_TEST_MODE = "mocean_sdk_test_mode_flag";

	/**
	 * Set to use In-App browser using setLocalExtras method of MoPub SDK.<br>
	 * Valid values are Boolean : true/false.<br>
	 * Set true to use In-App browser. Else set false to use Native device
	 * browser for opening landing page when ad is clicked. <br>
	 * Default : true
	 */
	public static final String KEY_MOCEAN_INAPP_BROWSER = "mocean_sdk_use_inapp_browser_flag";

	/**
	 * Enable or disable location detection for Mocean SDK. <br>
	 * To enable, set this flag to true (Boolean), else set to false (default).<br>
	 * Note: For location detection to work, application should add necessary
	 * access location permissions in the manifest file.
	 */
	public static final String KEY_MOCEAN_LOCATION_DETECTION_FLAG = "mocean_sdk_location_detection_flag";

	private static final String TAG = MoceanNativeAdapter.class.getSimpleName();
	private static final Handler MAIN_UI_THREAD_HANDLER = new Handler(
			Looper.getMainLooper());

	// AssetId's
	public static final int ASSET_ID_TITLE = 1001;
	public static final int ASSET_ID_TEXT_DESCRIPTION = 1002;
	public static final int ASSET_ID_ICON_IMAGE = 1003;
	public static final int ASSET_ID_MAIN_IMAGE = 1004;
	public static final int ASSET_ID_STAR_RATING = 1005;
	public static final int ASSET_ID_CALL_TO_ACTION = 1006;

	@Override
	protected void loadNativeAd(Context context,
			CustomEventNativeListener customEventNativeListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras) {
		final int zoneId;
		zoneId = parseInteger(serverExtras.get(KEY_ZONE_ID));
		if (zoneId < 1) {
			customEventNativeListener
					.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
			return;
		}

		MASTNativeAd mastNativeAd = new MASTNativeAd(context);
		mastNativeAd.setZone(zoneId);

		// Set custom ad network URL if set
		if (serverExtras.containsKey(KEY_AD_SERVER_URL)) {
			try {
				String adNetworkURL = (serverExtras.get(KEY_AD_SERVER_URL))
						.toString();
				if (!TextUtils.isEmpty(adNetworkURL)) {
					mastNativeAd.setAdNetworkURL(adNetworkURL);
					Log.i(TAG, "Using custom ad network URL: " + adNetworkURL);
				}
			} catch (Exception ex) {
				Log.w(TAG, "Invalid value for Ad network URL");
			}
		}

		// Set test mode if passed
		if (localExtras.containsKey(KEY_MOCEAN_TEST_MODE)) {
			try {
				boolean testMode = false;
				testMode = (Boolean) localExtras.get(KEY_MOCEAN_TEST_MODE);
				mastNativeAd.setTest(testMode);
			} catch (Exception ex) {
				Log.w(TAG,
						"Invalid value for test mode flag. Valid values true/false");
			}
		}

		// Set Log level if passed
		if (localExtras.containsKey(KEY_MOCEAN_LOG_LEVEL)) {
			setLogLevel(localExtras.get(KEY_MOCEAN_LOG_LEVEL), mastNativeAd);
		}

		// Set to use in-app browser
		if (localExtras.containsKey(KEY_MOCEAN_INAPP_BROWSER)) {
			try {
				boolean useInAppBrowser = true;
				useInAppBrowser = (Boolean) localExtras
						.get(KEY_MOCEAN_INAPP_BROWSER);
				mastNativeAd.setUseInternalBrowser(useInAppBrowser);
			} catch (Exception ex) {
				Log.w(TAG,
						"Invalid value for In-App browser flag. Valid values Boolean true/false. "
								+ "Using In-App browser as default.");
				// Using internal browser by default
				mastNativeAd.setUseInternalBrowser(true);
			}
		} else {
			mastNativeAd.setUseInternalBrowser(true);
		}

		// Disable location detection
		mastNativeAd.setLocationDetectionEnabled(true); // Default
		if (localExtras.containsKey(KEY_MOCEAN_LOCATION_DETECTION_FLAG)) {
			try {
				boolean isEnabled = true; // Default
				isEnabled = (Boolean) localExtras
						.get(KEY_MOCEAN_LOCATION_DETECTION_FLAG);
				mastNativeAd.setLocationDetectionEnabled(isEnabled);
			} catch (Exception ex) {
				Log.w(TAG,
						"invalid value for location detection flag. Valid values true/false");
			}
		}

		final MoceanStaticNativeAd moceanForwardingNativeAd = new MoceanStaticNativeAd(
				context, mastNativeAd, customEventNativeListener);
		// Load Ad
		moceanForwardingNativeAd.loadAd();
	}

	private void setLogLevel(Object logLevel, MASTNativeAd nativeAd) {
		try {
			LogLevel level = (LogLevel) logLevel;
			nativeAd.setLogLevel(level);
		} catch (Exception ex) {
			Log.w(TAG,
					"Invalid log level set. Valid values can be from MASTAdView.LogLevel enum");
		}
	}

	private int parseInteger(String value) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return -1;
		}
	}

	static class MoceanStaticNativeAd extends StaticNativeAd implements
			NativeRequestListener {
		protected Context mContext;
		private MASTNativeAd mNativeAd;
		private CustomEventNativeListener mCustomEventNativeListener;

		public MoceanStaticNativeAd(Context context, MASTNativeAd nativeAd,
				CustomEventNativeListener customEventNativeListener) {
			mContext = context;
			mNativeAd = nativeAd;
			mCustomEventNativeListener = customEventNativeListener;
		}

		void loadAd() {
			mNativeAd.setRequestListener(this);
			addRequestAssets(mNativeAd);
			mNativeAd.update();
		}

		// Override BaseForwardingNativeAd methods
		@Override
		public void prepare(final View view) {
			notifyAdImpressed();
			MAIN_UI_THREAD_HANDLER.post(new Runnable() {

				@Override
				public void run() {
					if (view != null) {
						mNativeAd.trackViewForInteractions(view);
					}
				}
			});
		}

		@Override
		public void destroy() {
			// Destroy Native Ad
			mNativeAd.destroy();
		}

		@Override
		public void clear(View view) {
			super.clear(view);

		}

		// Mocean Native Ad Event listeners
		@Override
		public void onNativeAdClicked(MASTNativeAd nativeAd) {
			notifyAdClicked();
		}

		@Override
		public void onNativeAdFailed(MASTNativeAd nativeAd, Exception ex) {

			if (ex == null) {
				MAIN_UI_THREAD_HANDLER.post(new Runnable() {

					@Override
					public void run() {
						mCustomEventNativeListener
								.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
					}
				});

			} else {
				MAIN_UI_THREAD_HANDLER.post(new Runnable() {

					@Override
					public void run() {
						mCustomEventNativeListener
								.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
					}
				});

			}
		}

		@Override
		public void onNativeAdReceived(MASTNativeAd nativeAd) {

			if (nativeAd == null || nativeAd.getNativeAssets() == null) {
				MAIN_UI_THREAD_HANDLER.post(new Runnable() {

					@Override
					public void run() {
						mCustomEventNativeListener
								.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
					}
				});
				return;
			}
			NativeAdResponse nativeAssetResponse = getNativeAdAssets(nativeAd);
			if (nativeAssetResponse == null) {
				MAIN_UI_THREAD_HANDLER.post(new Runnable() {

					@Override
					public void run() {
						mCustomEventNativeListener
								.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
					}
				});
				return;
			}
			setImpressionMinTimeViewed(0); // No minimum impression view time

			setTitle(nativeAssetResponse.title);
			setText(nativeAssetResponse.descriptionText);
			setCallToAction(nativeAssetResponse.callToAction);
			setIconImageUrl(nativeAssetResponse.iconImageUrl);
			setMainImageUrl(nativeAssetResponse.mainImageUrl);
			setStarRating(nativeAssetResponse.starRating);

			final List<String> imageUrlList = new ArrayList<String>();
			if (nativeAssetResponse.iconImageUrl != null) {
				imageUrlList.add(nativeAssetResponse.iconImageUrl);
			}
			if (nativeAssetResponse.mainImageUrl != null) {
				imageUrlList.add(nativeAssetResponse.mainImageUrl);
			}

			MAIN_UI_THREAD_HANDLER.post(new Runnable() {

				@Override
				public void run() {
					preCacheImages(mContext, imageUrlList,
							new NativeImageHelper.ImageListener() {

								@Override
								public void onImagesFailedToCache(
										NativeErrorCode errorCode) {
									mCustomEventNativeListener
											.onNativeAdFailed(errorCode);

								}

								@Override
								public void onImagesCached() {
									mCustomEventNativeListener
											.onNativeAdLoaded(MoceanStaticNativeAd.this);

								}
							});
				}
			});

		}

		@Override
		public void onReceivedThirdPartyRequest(MASTNativeAd nativeAd,
				Map<String, String> properties, Map<String, String> parameters) {
			Log.w(MoceanNativeAdapter.class.getSimpleName(),
					"Third party response can not be rendered in case of mediation.");

			MAIN_UI_THREAD_HANDLER.post(new Runnable() {

				@Override
				public void run() {
					mCustomEventNativeListener
							.onNativeAdFailed(NativeErrorCode.NETWORK_INVALID_STATE);
				}
			});
		}

		private void addRequestAssets(MASTNativeAd nativeAd) {

			TitleAssetRequest titleAsset = new TitleAssetRequest();
			titleAsset.setAssetId(ASSET_ID_TITLE);
			titleAsset.length = 50;
			titleAsset.setRequired(true);
			nativeAd.addNativeAssetRequest(titleAsset);

			DataAssetRequest dataAssetDesc = new DataAssetRequest();
			dataAssetDesc.setAssetId(ASSET_ID_TEXT_DESCRIPTION);
			dataAssetDesc.setDataAssetType(DataAssetTypes.desc);
			dataAssetDesc.setRequired(true);
			nativeAd.addNativeAssetRequest(dataAssetDesc);

			ImageAssetRequest imageAssetIcon = new ImageAssetRequest();
			imageAssetIcon.setAssetId(ASSET_ID_ICON_IMAGE);
			imageAssetIcon.setImageType(ImageAssetTypes.icon);
			titleAsset.setRequired(true);
			nativeAd.addNativeAssetRequest(imageAssetIcon);

			ImageAssetRequest imageAssetMainImage = new ImageAssetRequest();
			imageAssetMainImage.setAssetId(ASSET_ID_MAIN_IMAGE);
			imageAssetMainImage.setImageType(ImageAssetTypes.main);
			imageAssetIcon.setRequired(true);
			nativeAd.addNativeAssetRequest(imageAssetMainImage);

			DataAssetRequest dataAssetRating = new DataAssetRequest();
			dataAssetRating.setAssetId(ASSET_ID_STAR_RATING);
			dataAssetRating.setDataAssetType(DataAssetTypes.rating);
			nativeAd.addNativeAssetRequest(dataAssetRating);

			DataAssetRequest dataAssetCta = new DataAssetRequest();
			dataAssetCta.setAssetId(ASSET_ID_CALL_TO_ACTION);
			dataAssetCta.setDataAssetType(DataAssetTypes.ctatext);
			dataAssetCta.setRequired(true);
			nativeAd.addNativeAssetRequest(dataAssetCta);
		}

		private NativeAdResponse getNativeAdAssets(MASTNativeAd nativeAd) {
			List<AssetResponse> nativeAssets = nativeAd.getNativeAssets();
			NativeAdResponse adResponse = null;
			if (nativeAssets != null) {
				adResponse = new NativeAdResponse();
				for (AssetResponse assetResponse : nativeAssets) {
					try {
						switch (assetResponse.assetId) {
						case ASSET_ID_TITLE:
							adResponse.title = ((TitleAssetResponse) assetResponse)
									.getTitleText();
							break;
						case ASSET_ID_TEXT_DESCRIPTION:
							adResponse.descriptionText = ((DataAssetResponse) assetResponse)
									.getValue();
							break;
						case ASSET_ID_CALL_TO_ACTION:
							adResponse.callToAction = ((DataAssetResponse) assetResponse)
									.getValue();
							break;
						case ASSET_ID_ICON_IMAGE:
							adResponse.iconImageUrl = ((ImageAssetResponse) assetResponse)
									.getImage().getUrl();
							break;
						case ASSET_ID_MAIN_IMAGE:
							adResponse.mainImageUrl = ((ImageAssetResponse) assetResponse)
									.getImage().getUrl();
							break;
						case ASSET_ID_STAR_RATING:
							String ratingStr = ((DataAssetResponse) assetResponse)
									.getValue();
							adResponse.starRating = parseRatingString(ratingStr);
							break;

						default:
							break;
						} // Switch end
					} catch (Exception ex) {
						Log.e(MoceanNativeAdapter.class.getSimpleName(),
								"Error in fetching native asset. Skipping an asset. "
										+ ex.getMessage());
					} // Try-Catch end
				} // For end
			} // If end
			return adResponse;
		}

		private Double parseRatingString(String ratingString) {
			Double rating = null;
			try {
				rating = Double.parseDouble(ratingString);
				// Respect rating value only if it is between 0 to 5 range
				if (rating < 0 || rating > 5) {
					rating = null;
				}
			} catch (Exception ex) {
				Log.w(MoceanNativeAdapter.class.getSimpleName(),
						"Error while parsing Rating asset value. "
								+ ex.getMessage());
			}
			return rating;
		}

		/** Container for native asset response */
		class NativeAdResponse {
			String mainImageUrl;
			String iconImageUrl;
			String callToAction;
			String title;
			String descriptionText;
			Double starRating;
		}
	}
}
