package com.mopub.mobileads;

import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.moceanmobile.mast.MASTAdView;
import com.moceanmobile.mast.MASTAdView.LogLevel;
import com.moceanmobile.mast.MASTAdViewDelegate;

/**
 * Mocean Banner Adapter for MoPub SDK. This adapter extends
 * {@link CustomEventBanner} class. You can use this adapter class to integrate
 * Mocean SDK into MoPub SDK using client side mediation.
 * <p>
 * <b>Mandatory server extra parameters required for Mocean SDK:</b><br>
 * In order to initialize and use Mocean SDK, following server extra parameters
 * needs to be configured on MoPub portal.
 * <ol>
 * <li>zoneId : Mocean ZoneID
 * <li>adWidth : Width of Mocean Banner Ad view in DIP (depth independent
 * pixels). Note: This value will be converted into pixels before initializing
 * Mocean SDK. Set this value to -1 if view width should
 * MATCH_PARENT/FILL_PARENT.
 * <li>adHeight : Height of Mocean Banner Ad view in DIP. Note: This value will
 * be converted into pixels before initializing Mocean SDK.
 * </ol>
 * </p>
 * On the MoPub web interface, create a network with the "Custom Native Network"
 * type. Place the fully-qualified class name of your custom event class
 * (<b>com.mopub.mobileads.MoceanBannerAdapter</b>) in the "Custom Event Class"
 * column.
 * <p>
 * <b>Note:</b> To configure above server extra parameters for MoPub Custom
 * Native Network, enter following JSON in "Custom Event Class Data" column.<br>
 * Example JSON:
 * 
 * <pre>
 * {"zoneId": "123456", "adWidth": "320", "adHeight":"50"}
 * </pre>
 * 
 * Once you've completed these steps, the MoPub SDK will be able to cause your
 * CustomEventBanner subclass to be instantiated at the proper time while your
 * application is running. You do not need to instantiate any of these
 * subclasses in your application code. <br>
 * Note: the MoPub SDK will cause a new CustomEventBanner object to be
 * instantiated on every ad call, so you can safely make changes to the custom
 * event object's internal state between calls. <br>
 * <p>
 * <b>Optional : </b><br>
 * Optionally you can also set following local extra parameters in MoPub SDK.
 * <ol>
 * <li>mocean_sdk_log_level : Set the log level of Mocean SDK. Permissible
 * values are from {@link LogLevel} enum.
 * <li>mocean_sdk_location_detection_flag : Add this extra param with value as
 * {@link Boolean}.TRUE to enable location detection in Mocean SDK.
 * <li>mocean_sdk_test_mode_flag : Add this extra param with value as
 * {@link Boolean}.TRUE to serve ads in test mode.
 * </ol>
 * 
 * @see <a
 *      href="https://github.com/mopub/mopub-android-sdk/wiki/Custom-Events">MoPub
 *      Custom Events Wiki</a>
 * 
 */
public class MoceanBannerAdapter extends CustomEventBanner implements
		MASTAdViewDelegate.RequestListener, MASTAdViewDelegate.ActivityListener {

	// Server Extra Param keys
	public static final String KEY_ZONE_ID = "zoneId";
	public static final String KEY_AD_WIDTH = "adWidth";
	public static final String KEY_AD_HEIGHT = "adHeight";
	public static final String KEY_AD_SERVER_URL = "adServerUrl";

	// Local extra param keys
	/**
	 * Set Log Level for Mocean Ad View using setLocalExtras method of MoPub
	 * SDK. Refer {@link LogLevel} for valid values of LogLevel.
	 * 
	 * @see LogLevel
	 */
	public static final String KEY_MOCEAN_LOG_LEVEL = "mocean_sdk_log_level";

	/**
	 * Set test mode for Mocean AdView using setLocalExtras mthod of MoPub SDK.<br>
	 * Valid values are Boolean : true/false.<br>
	 * Set true to enable ad serving in test mode
	 */
	public static final String KEY_MOCEAN_TEST_MODE = "mocean_sdk_test_mode_flag";

	/**
	 * Enable or disable location detection for Mocean SDK. <br>
	 * To enable, set this flag to true (Boolean), else set to false (default).<br>
	 * Note: For location detection to work, application should add necessary
	 * access location permissions in the manifest file.
	 */
	public static final String KEY_MOCEAN_LOCATION_DETECTION_FLAG = "mocean_sdk_location_detection_flag";

	// For Internal Adapter use only
	private static final String MOCEAN_CUSTOM_PARAM_FOR_AD_WIDHT = "size_x";
	private static final String MOCEAN_CUSTOM_PARAM_FOR_AD_HEIGHT = "size_y";

	private static final String TAG = MoceanBannerAdapter.class.getSimpleName();
	private MASTAdView mMastAdView;
	private CustomEventBannerListener mBannerListener;

	@Override
	protected void loadBanner(Context context,
			CustomEventBannerListener bannerListener,
			Map<String, Object> localExtras, Map<String, String> serverExtras) {
		mBannerListener = bannerListener;

		final int zoneId;
		final int adWidth;
		final int adHeight;
		if (validateServerExtraParams(serverExtras)) {
			zoneId = Integer.parseInt(serverExtras.get(KEY_ZONE_ID));
			adWidth = Integer.parseInt(serverExtras.get(KEY_AD_WIDTH));
			adHeight = Integer.parseInt(serverExtras.get(KEY_AD_HEIGHT));
		} else {
			mBannerListener
					.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
			return;
		}
		// Initialize Mocean AdView
		mMastAdView = new MASTAdView(context);

		mMastAdView.setZone(zoneId);
		mMastAdView.setRequestListener(this);

		// Get Custom Param Map
		Map<String, String> customParamMap = mMastAdView
				.getAdRequestParameters();
		customParamMap.put(MOCEAN_CUSTOM_PARAM_FOR_AD_WIDHT, "" + adWidth);
		customParamMap.put(MOCEAN_CUSTOM_PARAM_FOR_AD_HEIGHT, "" + adHeight);

		// Set custom network url if passed
		if (!TextUtils.isEmpty(serverExtras.get(KEY_AD_SERVER_URL))
				&& URLUtil.isValidUrl(serverExtras.get(KEY_AD_SERVER_URL))) {
			mMastAdView.setAdNetworkURL(serverExtras.get(KEY_AD_SERVER_URL));
		}
		// Set Log level if passed
		if (localExtras.containsKey(KEY_MOCEAN_LOG_LEVEL)) {
			setLogLovel(localExtras.get(KEY_MOCEAN_LOG_LEVEL));
		}
		// Set test mode if passed
		if (localExtras.containsKey(KEY_MOCEAN_TEST_MODE)) {
			try {
				boolean testMode = false;
				testMode = (Boolean) localExtras.get(KEY_MOCEAN_TEST_MODE);
				mMastAdView.setTest(testMode);
			} catch (Exception ex) {
				Log.w(TAG,
						"invalid value for test mode flag. Valid values true/false");
			}
		}
		// Enable/Disable location detection
		if (localExtras.containsKey(KEY_MOCEAN_LOCATION_DETECTION_FLAG)) {
			try {
				boolean isEnabled = false;
				isEnabled = (Boolean) localExtras
						.get(KEY_MOCEAN_LOCATION_DETECTION_FLAG);
				mMastAdView.setLocationDetectionEnabled(isEnabled);
			} catch (Exception ex) {
				Log.w(TAG,
						"invalid value for location detection flag. Valid values true/false");
			}
		}
		// Using internal browser by default
		mMastAdView.setUseInternalBrowser(true);
		// Load Ad
		mMastAdView.update();

	}

	@Override
	protected void onInvalidate() {
		if (mMastAdView != null) {
			mMastAdView.setRequestListener(null); // Remove listener
			mMastAdView.reset();
			mMastAdView = null;
		}
	}

	private boolean validateServerExtraParams(Map<String, String> serverExtras) {
		boolean isValid = true;
		try {
			Integer.parseInt(serverExtras.get(KEY_ZONE_ID));
			Integer.parseInt(serverExtras.get(KEY_AD_WIDTH));
			Integer.parseInt(serverExtras.get(KEY_AD_HEIGHT));
			isValid = true;
		} catch (Exception ex) {
			isValid = false;
			Log.w(TAG, "Server extra params passed are invalid.");
		}
		return isValid;
	}

	private void setLogLovel(Object logLevel) {
		try {
			LogLevel level = (LogLevel) logLevel;
			mMastAdView.setLogLevel(level);
		} catch (Exception ex) {
			Log.w(TAG,
					"Invalid log level set. Valid values can be from MASTAdView.LogLevel enum");
		}
	}

	// Mocean SDK Banner View Callbacks - Start
	@Override
	public void onFailedToReceiveAd(MASTAdView mastAdView, Exception ex) {
		Log.d(TAG, "Mocean banner ad failed to load. Error: " + ex.getMessage());
		if (mBannerListener != null) {
			mBannerListener.onBannerFailed(MoPubErrorCode.NETWORK_NO_FILL);
		}
	}

	@Override
	public void onReceivedAd(MASTAdView mastAdView) {
		Log.d(TAG, "Mocean banner ad loaded successfully.");
		if (mBannerListener != null && mastAdView != null) {
			mBannerListener.onBannerLoaded(mastAdView);
		}
	}

	@Override
	public void onReceivedThirdPartyRequest(MASTAdView mastAdView,
			Map<String, String> properties, Map<String, String> parameters) {
		Log.d(TAG, "Mocean third-party banner ad loaded.");
		if (mBannerListener != null && mastAdView != null) {
			// Calling MoPub onBannerAdLoaded callback even if Mocean third
			// party ad is received.
			mBannerListener.onBannerLoaded(mastAdView);
		}
	}

	@Override
	public boolean onCloseButtonClick(MASTAdView view) {
		// NOOP
		return false;
	}

	@Override
	public void onLeavingApplication(MASTAdView view) {
		if (mBannerListener != null) {
			mBannerListener.onLeaveApplication();
		}

	}

	@Override
	public boolean onOpenUrl(MASTAdView view, String url) {
		// NOOP
		return false;
	}
	// Mocean SDK Banner View Callbacks - End

}
