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

package com.moceanmobile.mast;

import static com.moceanmobile.mast.MASTNativeAdConstants.DEFAULTED_EXCREATIVES;
import static com.moceanmobile.mast.MASTNativeAdConstants.DEFAULTED_PUBMATIC_EXFEEDS;
import static com.moceanmobile.mast.MASTNativeAdConstants.NATIVE_IMAGE_H;
import static com.moceanmobile.mast.MASTNativeAdConstants.NATIVE_IMAGE_W;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_COUNT;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_KEY;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_LATITUDE;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_LONGITUDE;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_SDK_VERSION;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_TEST;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_TYPE;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_UA;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUESTPARAM_ZONE;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_DIRECT_STRING;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_MEDIATION;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_URL;
import static com.moceanmobile.mast.MASTNativeAdConstants.TELEPHONY_MCC;
import static com.moceanmobile.mast.MASTNativeAdConstants.TELEPHONY_MNC;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.moceanmobile.mast.Defaults.MediationNetwork;
import com.moceanmobile.mast.bean.AssetRequest;
import com.moceanmobile.mast.bean.AssetResponse;
import com.moceanmobile.mast.bean.DataAssetRequest;
import com.moceanmobile.mast.bean.ImageAssetRequest;
import com.moceanmobile.mast.bean.TitleAssetRequest;

/**
 * Main class used for requesting native ads. <br>
 * Refer Sample application for example of implementation.
 */
public final class MASTNativeAd implements AdRequest.Handler {
	public enum LogLevel {
		None, Error, Debug,
	}

	/**
	 * Interface allowing application developers to control logging.
	 */
	public interface LogListener {
		/**
		 * Invoked when the SDK logs events. If applications override logging
		 * they can return true to indicate the log event has been consumed and
		 * the SDK processing is not needed.
		 * <p>
		 * Will not be invoked if the MASTNativeAd instance's logLevel is set
		 * lower than the event.
		 * 
		 * @param nativeAd
		 * @param event
		 *            String representing the event to be logged.
		 * @param eventLevel
		 *            LogLevel of the event.
		 * @return
		 */
		public boolean onLogEvent(MASTNativeAd nativeAd, String event,
				LogLevel eventLevel);
	}

	public interface NativeRequestListener {

		/**
		 * Callback when the native ad is received.
		 * 
		 * @param ad
		 */
		public void onNativeAdReceived(MASTNativeAd ad);

		/**
		 * Callback when MASTNativeAd fails to get an ad. This may be due to
		 * invalid zone, network connection not available, timeout, no Ad to
		 * fill at this point.
		 * 
		 * @param ad
		 *            - MASTNativeAd object which has requested for the Ad.
		 * @param ex
		 *            - Exception occurred.
		 */
		public void onNativeAdFailed(MASTNativeAd ad, Exception ex);

		/**
		 * Third party ad received. The application should be expecting this and
		 * ready to render the ad with the supplied configuration.
		 * 
		 * @param ad
		 * @param properties
		 *            Properties of the ad request (ad network information).
		 * @param parameters
		 *            Parameters for the third party network (expected to be
		 *            passed to that network).
		 */
		public void onReceivedThirdPartyRequest(MASTNativeAd ad,
				Map<String, String> properties, Map<String, String> parameters);

		/**
		 * Callback when the user clicks on the native ad. Implement your logic
		 * on view click in this method.
		 * 
		 * <B> Do not implement your own clickListener in your activity. </b>
		 * 
		 * @param ad
		 */
		public void onNativeAdClicked(MASTNativeAd ad);
	}

	private Context mContext = null;

	private int mZone = 0;
	private Map<MediationNetwork, String> mMapMediationNetworkTestDeviceIds = null;
	// Used to store the the current native response
	private NativeAdDescriptor mNativeAdDescriptor = null;
	private boolean mImpressionTrackerSent = false;
	private boolean mThirdPartyImpTrackerSent = false;
	private boolean mThirdPartyClickTrackerSent = false;
	private boolean mClickTrackerSent = false;
	private NativeRequestListener mListener = null;
	private String mUserAgent = null;
	private LogListener mLogListener = null;
	private LogLevel mLogLevel = LogLevel.None;
	private boolean test = false;
	private final HashMap<String, String> mAdRequestParameters;
	private List<AssetRequest> mRequestedNativeAssets;
	private BrowserDialog mBrowserDialog = null;
	// Use external system native browser by default
	private boolean mUseInternalBrowser = false;
	private MediationData mMediationData = null;

	/**
	 * If false, denotes that SDK should handle the third party request with the
	 * help of adapter. If true, SDK should give the control back to publisher
	 * to handle third party request.
	 */
	private boolean mOverrideAdapter = true;
	private String mNetworkUrl = Defaults.AD_NETWORK_URL;

	// Location support
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;

	private AdRequest mAdRequest = null;

	/**
	 * @param context
	 */
	public MASTNativeAd(Context context) {
		mContext = context;
		mAdRequestParameters = new HashMap<String, String>();
		mRequestedNativeAssets = new ArrayList<AssetRequest>();
	}

	/**
	 * Sets the zone on the ad network to obtain ad content.
	 * <p>
	 * REQUIRED - If not set updates will fail.
	 * 
	 * @param zone
	 *            Ad network zone.
	 */
	public void setZone(int zone) {
		this.mZone = zone;
	}

	/**
	 * Returns the currently configured ad network zone.
	 * 
	 * @return Ad network zone.
	 */
	public int getZone() {
		return mZone;
	}

	/**
	 * Sets the instance test mode. If set to test mode the instance will
	 * request test ads for the configured zone.
	 * <p>
	 * Warning: This should never be enabled for application releases.
	 * 
	 * @param test
	 *            true to set test mode, false to disable test mode.
	 */
	public void setTest(boolean test) {
		this.test = test;
	}

	/**
	 * Access for test mode state of the instance.
	 * 
	 * @return true if the instance is set to test mode, false if test mode is
	 *         disabled.
	 */
	public boolean isTest() {
		return test;
	}

	public void setRequestListener(NativeRequestListener requestListener) {
		if (requestListener == null
				|| !(requestListener instanceof NativeRequestListener)) {
			throw new IllegalArgumentException(
					"Kindly pass the object of type NativeRequestListener in case you want to use NativeAdView."
							+ " Implement NativeRequestListener in your activity instead of RequestListener. ");
		} else {
			mListener = requestListener;
		}
	}

	/**
	 * Sets the log listener. This listener provides the ability to override
	 * default logging behavior.
	 * 
	 * @param logListener
	 *            MASTNativeAd.LogListener implementation
	 */
	public void setLogListener(LogListener logListener) {
		this.mLogListener = logListener;
	}

	/**
	 * Returns the currently configured listener.
	 * 
	 * @return MASTNativeAd.LogListener set with setLogListener().
	 */
	public LogListener getLogListener() {
		return mLogListener;
	}

	/**
	 * Sets the log level of the instance. Logging is done through console
	 * logging.
	 * 
	 * @param logLevel
	 *            LogLevel
	 */
	public void setLogLevel(LogLevel logLevel) {
		this.mLogLevel = logLevel;
	}

	/**
	 * Returns the currently configured log level.
	 * 
	 * @return currently configured LogLevel
	 */
	public LogLevel getLogLevel() {
		return mLogLevel;
	}

	/**
	 * List of requested native assets.
	 * <p>
	 * Returns subclasses of {@link AssetRequest} class viz:
	 * {@link TitleAssetRequest}, {@link ImageAssetRequest} and
	 * {@link DataAssetRequest}.
	 * <p>
	 * Sub-type of asset can be identified by 'type' variable where ever
	 * available.
	 * 
	 * @return
	 */
	public List<AssetRequest> getRequestedNativeAssetsList() {
		return mRequestedNativeAssets;
	}

	/**
	 * Add native asset request in ad request.
	 * <p>
	 * <b>NOTE:</b> Use unique assetId for each asset in asset request. The same
	 * assetId will be returned in native ad response. You can use this assetId
	 * to map requested native assets with response.
	 * 
	 * @see addNativeAssetRequestList(List<AssetRequest> assetList) :
	 *      Convenience method to add all assets at once.
	 * @param asset
	 *            Native {@link AssetRequest} to add in the ad request
	 */
	public void addNativeAssetRequest(AssetRequest asset) {
		if (mRequestedNativeAssets != null && asset != null) {
			mRequestedNativeAssets.add(asset);
		}
	}

	/**
	 * Convenience method to add all native asset requests at once. *
	 * <p>
	 * <b>NOTE:</b> Use unique assetId for each asset in asset request. The same
	 * assetId will be returned in native ad response. You can use this assetId
	 * to map requested native assets with response.
	 * 
	 * @see addNativeAssetRequest(AssetRequest asset)
	 * @param assetList
	 *            List of Native {@link AssetRequest} to add in the ad request
	 */
	public void addNativeAssetRequestList(List<AssetRequest> assetList) {
		if (mRequestedNativeAssets != null && assetList != null) {
			mRequestedNativeAssets.addAll(assetList);
		}
	}

	/**
	 * Collection of ad request parameters. Allows setting extra network
	 * parameters.
	 * <p>
	 * The SDK will set various parameters based on configuration and other
	 * options. For more information see
	 * http://developer.moceanmobile.com/Mocean_Ad_Request_API.
	 * 
	 * @return Map containing optional request parameters.
	 */
	public Map<String, String> getAdRequestParameters() {
		return mAdRequestParameters;
	}

	/**
	 * Allows setting of extra custom parameters to ad request. Add custom
	 * parameter (key-value).
	 * 
	 * @see - For details of custom parameters refer
	 *      http://developer.moceanmobile.com/Mocean_Ad_Request_API
	 */
	public void addCustomParameter(String customParamName, String value) {
		if (mAdRequestParameters != null && customParamName != null) {
			mAdRequestParameters.put(customParamName, value);
		}
	}

	/**
	 * Convenience method to add all custom parameters in one Map. Allows
	 * setting of extra custom parameters to ad request.
	 * 
	 * @param customParamMap
	 *            Map containing custom parameters
	 * @see - For details of custom parameters refer
	 *      http://developer.moceanmobile.com/Mocean_Ad_Request_API
	 */
	public void addCustomParametersMap(HashMap<String, String> customParamMap) {
		if (mAdRequestParameters != null && customParamMap != null) {
			mAdRequestParameters.putAll(customParamMap);
		}
	}

	/**
	 * Specifies the URL of the ad network. This defaults to Mocean's ad
	 * network.
	 * 
	 * @param adNetworkURL
	 *            URL of the ad server (ex: http://ads.moceanads.com/ad);
	 */
	public void setAdNetworkURL(String adNetworkURL) {
		mNetworkUrl = adNetworkURL;
	}

	/**
	 * Returns the currently configured ad network.
	 * 
	 * @return Currently configured ad network URL.
	 */
	public String getAdNetworkURL() {
		return mNetworkUrl;
	}

	// @formatter:off
	@SuppressWarnings("unused")
	/**
	 * Use this method if you are testing third party mediation SDKs using
	 * adapter and set the deviceId with respect to particular network. <br />
	 * e.g. If you are testing Facebook Audience Network with Mocean SDK using
	 * <b> Mocean Adapter support </b>(<i> in which case Mocean SDK will
	 * initialize the Facebook SDK internally </i>). Use
	 * {@link MASTNativeAd#addTestDeviceIdForNetwork(MediationNetwork, String)};
	 * 
	 * <br />
	 * You will know your device id when you run the application for the first
	 * time and Facebook SDK will print your device id in console. Replace
	 * "deviceid" with the generated id.
	 * 
	 * NativeAdObj.addTestDeviceIdForNetwork(MediationNetwork.
	 * FACEBOOK_AUDIENCE_NETWORK, "deviceId");
	 * 
	 * <br />
	 * Follow same instructions for other supported networks as well.
	 * 
	 * @param network
	 * @param deviceId
	 */
	@Deprecated
	// This method is deprecated and will be removed in next release
	private void addTestDeviceIdForNetwork(MediationNetwork network,
			String deviceId) {
		if (mMapMediationNetworkTestDeviceIds == null) {
			mMapMediationNetworkTestDeviceIds = new HashMap<MediationNetwork, String>();
		}

		mMapMediationNetworkTestDeviceIds.put(network, deviceId);
	}

	/**
	 * Use this method to remove test devices ids for network.
	 * 
	 * e.g. if you have set device ids using
	 * {@link MASTNativeAd#addTestDeviceIdForNetwork(MediationNetwork, String)}
	 * remove it using nativeAd.removeTestDeviceIdForNetwork(MediationNetwork.
	 * FACEBOOK_AUDIENCE_NETWORK);
	 * 
	 * @param network
	 */
	// @formatter:on
	public void removeTestDeviceIdForNetwork(MediationNetwork network) {
		if (mMapMediationNetworkTestDeviceIds != null) {
			mMapMediationNetworkTestDeviceIds.remove(network);
		}
	}

	/**
	 * Use this method to load your resource images.
	 * 
	 * @param imageView
	 * @param url
	 */
	public void loadImage(android.widget.ImageView imageView, String url) {
		ImageDownloader.loadImage(imageView, url);
	}

	/**
	 * Request for Native ad.
	 * <p>
	 * <b>NOTE: </b>Please make sure to set zoneId, requestListener and add
	 * native asset requests before calling this method.
	 */
	public void update() {
		try {
			validateAssetRequest(); // validate request
			internalUpdate(false); // Proceed to request for ad
		} catch (IllegalArgumentException ex) {
			/*
			 * Catch the validation exception thrown from
			 * validateAssetRequest().
			 */
			ex.printStackTrace();
			logEvent(
					"ERROR: Native asset validation failed. Ad requested interrupted.",
					LogLevel.Error);
		}

	}

	/**
	 * This method validates Native {@link AssetRequest} list which is set using
	 * addNativeAssetRequest() or addNativeAssetRequestList() methods.
	 */
	private void validateAssetRequest() {

		Set<Integer> assetIdSet = new HashSet<Integer>();
		for (AssetRequest assetRequest : mRequestedNativeAssets) {
			if (assetRequest.assetId < 1) {
				throw new IllegalArgumentException(
						"ERROR: Missing/Invalid assetId.\nNote: Asset id is mandatory for each requested asset."
								+ " Each assetId should be unique and should be > 0");
			}
			if (!assetIdSet.add(assetRequest.assetId)) {
				throw new IllegalArgumentException(
						"ERROR: Duplicate assetId.\nNote: "
								+ "Unique assetId is mandatory for each requested asset. Each assetId should be > 0");
			}
		}

	}

	/**
	 * This is an internal method used to update and make a new request to the
	 * server. If defaulted is true then the id of the defaulted ad network is
	 * added to the request so that this network will be removed from the
	 * auction at PubMatic side.
	 * 
	 * @param defaulted
	 *            denotes whether third party ad network defaulted
	 */
	private void internalUpdate(boolean defaulted) {
		Map<String, String> args = new HashMap<String, String>();

		/*
		 * If the mediation partner has defaulted, add this parameter to denote
		 * that this network has defaulted and remove this network from auction
		 * next time. In case the source is direct send the creativeId instead
		 * of network id.
		 */
		if (defaulted && mNativeAdDescriptor != null) {
			if (RESPONSE_DIRECT_STRING.equals(mNativeAdDescriptor.getSource())) {
				args.put(DEFAULTED_EXCREATIVES,
						mNativeAdDescriptor.getCreativeId());
				args.remove(DEFAULTED_PUBMATIC_EXFEEDS);
			} else if (RESPONSE_MEDIATION.equals(mNativeAdDescriptor
					.getSource())) {
				args.put(DEFAULTED_PUBMATIC_EXFEEDS,
						mNativeAdDescriptor.getMediationId());
				args.remove(DEFAULTED_EXCREATIVES);
			}
		}

		reset();
		initUserAgent();

		try {
			TelephonyManager tm = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			String networkOperator = tm.getNetworkOperator();
			if ((networkOperator != null) && (networkOperator.length() > 3)) {
				String mcc = networkOperator.substring(0, 3);
				String mnc = networkOperator.substring(3);

				args.put(TELEPHONY_MCC, String.valueOf(mcc));
				args.put(TELEPHONY_MNC, String.valueOf(mnc));
			}
		} catch (Exception ex) {
			logEvent("Unable to obtain mcc and mnc. Exception:" + ex,
					LogLevel.Debug);
		}

		// Put all the user sent parameters in the request
		args.putAll(mAdRequestParameters);

		// Don't allow these to be overridden.
		args.put(REQUESTPARAM_UA, mUserAgent);
		args.put(REQUESTPARAM_SDK_VERSION, Defaults.SDK_VERSION);
		args.put(REQUESTPARAM_COUNT, Defaults.NATIVE_REQUEST_COUNT);
		// Response type for Generic Json
		args.put(REQUESTPARAM_KEY, Defaults.NATIVE_REQUEST_KEY);
		// Ad type for native.
		args.put(REQUESTPARAM_TYPE, Defaults.NATIVE_REQUEST_AD_TYPE);
		args.put(REQUESTPARAM_ZONE, String.valueOf(mZone));

		/* Putting optional parameters related to Native Ad */

		if (this.test) {
			args.put(REQUESTPARAM_TEST, Defaults.NATIVE_REQUEST_TEST_TRUE);
		}

		try {
			if (mAdRequest != null)
				mAdRequest.cancel();

			mAdRequest = AdRequest.create(Defaults.NETWORK_TIMEOUT_SECONDS,
					mNetworkUrl, mUserAgent, args, mRequestedNativeAssets,
					this, true);

			String requestUrl = mAdRequest.getRequestUrl();
			logEvent("Ad request:" + requestUrl, LogLevel.Debug);
		} catch (UnsupportedEncodingException e) {
			logEvent("Exception encountered while generating ad request URL:"
					+ e, LogLevel.Error);

			fireCallback(CallbackType.NativeFailed, e, null);
		}
	}

	/**
	 * Call this method whenever your view is ready to display. Calling this
	 * method will fire the Ad display tracker and count impression.
	 * 
	 * You must call this whenever the view is visible.
	 * 
	 * @param view
	 *            top level view which displays the native ads i.e. container
	 *            for the native ad
	 */
	public void trackViewForInteractions(View view) {
		if (view != null) {
			removeListenerFromChildViews(view);
			view.setClickable(true); // Set only parent view clickable
			/*
			 * Since publisher's view is ready to display. Send our impression
			 * tracker.
			 */
			if (!mImpressionTrackerSent) {
				sendImpressions(TrackerType.IMPRESSION_TRACKER);
				mImpressionTrackerSent = true;
			}

			// If the adapter has served the ad, inform to track this view for
			// interactions.
			/*if (mBaseAdapter != null) {
				mBaseAdapter.trackViewForInteractions(view);
			} else */{
				/*
				 * Set listener on this view, so that we will receive all the
				 * interactions
				 */
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						adClicked();
						openClickUrlInBrowser();

						fireCallback(CallbackType.NativeAdClicked, null, null);
					}
				});
			}
		}
	}

	/**
	 * Traverse view and remove click listeners for all children views.
	 * <p>
	 * In some 3rd party mediation SDK's, click listeners are set on all sub
	 * views. This causes problem with click listener in mocean and other native
	 * ad views. To fix this, we remove click listeners on all sub views and set
	 * all sub views as non-clickable.
	 */
	private void removeListenerFromChildViews(View view) {
		if (view != null) {
			view.setOnClickListener(null);

			if (view instanceof ViewGroup) {
				ViewGroup group = (ViewGroup) view;
				for (int index = 0; index < group.getChildCount(); index++) {
					removeListenerFromChildViews(group.getChildAt(index));
				}
			} else {
				// Set all child views non-clickable.
				view.setClickable(false);
			}
		}
	}

	/**
	 * The SDK handles the adapter loading by default in case of third party SDK
	 * wins and invokes the third party SDK through adapters. The default value
	 * is false.
	 * 
	 * Set override = true if you want to handle the invoking of the third party
	 * SDK. The SDK will call
	 * {@link NativeRequestListener#onReceivedThirdPartyRequest(MASTNativeAd, Map, Map)}
	 * in case third party SDK wins. If the third party SDK defaults you may
	 * call {@link MASTNativeAd#thirdpartyPartnerDefaulted()}.
	 * 
	 * If the adapter is invoked the defaulting is handled by the SDK itself.
	 * 
	 * @param override
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void overrideAdapterLoading(boolean override) {
		// This method will be removed in next release
		mOverrideAdapter = override;
	}

	/**
	 * Call this method if the third party partner defaults or gives the error.
	 * SDK will initiate a new request eliminating the call to the defaulted
	 * partner.
	 */
	public void thirdpartyPartnerDefaulted() {
		internalUpdate(true);
	}

	/**
	 * Determines if location detection is enabled. If enabled, the SDK will use
	 * the location services of the device to determine the device's location ad
	 * add ad request parameters (lat/long) to the ad request. Location
	 * detection can be enabled with setLocationDetectionEnabled() or
	 * enableLocationDetection().
	 * 
	 * @return true if location detection is enabled, false if not
	 */
	public boolean isLocationDetectionEnabled() {
		if (locationManager != null) {
			return true;
		}

		return false;
	}

	/**
	 * Enables or disable SDK location detection. If enabled with this method
	 * the most battery optimized settings are used. For more fine tuned control
	 * over location detection settings use enableLocationDetection(). This
	 * method is used to disable location detection for either method of
	 * enabling location detection.
	 * <p>
	 * Permissions for coarse or fine location detection may be required.
	 * 
	 * @param locationDetectionEnabled
	 */
	public void setLocationDetectionEnabled(boolean locationDetectionEnabled) {
		if (locationDetectionEnabled == false) {
			if (locationManager != null) {
				locationManager.removeUpdates(locationListener);
				locationManager = null;
				locationListener = null;
			}

			return;
		}

		Criteria criteria = new Criteria();
		criteria.setCostAllowed(false);

		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		criteria.setBearingRequired(false);
		criteria.setSpeedRequired(false);
		criteria.setAltitudeRequired(false);
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);

		enableLocationDetection(Defaults.LOCATION_DETECTION_MINTIME,
				Defaults.LOCATION_DETECTION_MINDISTANCE, criteria, null);
	}

	/**
	 * Enables location detection with specified criteria. To disable location
	 * detection use setLocationDetectionEnabled(false).
	 * 
	 * @see LocationManager.requestLocationUpdates
	 * @param minTime
	 *            LocationManager.requestLocationUpdates minTime
	 * @param minDistance
	 *            LocationManager.requestLocationUpdates minDistance
	 * @param criteria
	 *            Criteria used to find an available provider. Ignored if
	 *            provider is non-null.
	 * @param provider
	 *            Named provider used by the LocationManager to obtain location
	 *            updates.
	 */
	public void enableLocationDetection(long minTime, float minDistance,
			Criteria criteria, String provider) {
		if ((provider == null) && (criteria == null))
			throw new IllegalArgumentException("Criteria or Provider required");

		locationManager = (LocationManager) mContext
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			try {
				if (provider == null) {
					List<String> providers = locationManager.getProviders(
							criteria, true);
					if ((providers != null) && (providers.size() > 0)) {
						provider = providers.get(0);
					}
				}

				if (provider != null) {
					locationListener = new LocationListener();
					locationManager.requestLocationUpdates(provider, minTime,
							minDistance, locationListener);
				}
			} catch (Exception ex) {
				logEvent("Error requesting location updates.  Exception:" + ex,
						LogLevel.Error);

				locationManager.removeUpdates(locationListener);
				locationManager = null;
				locationListener = null;
			}
		}
	}

	public void reset() {

		if (mAdRequest != null) {
			mAdRequest.cancel();
		}

		mNativeAdDescriptor = null;
		mMediationData = null;

		mImpressionTrackerSent = false;
		mThirdPartyImpTrackerSent = false;
		mThirdPartyClickTrackerSent = false;
		mClickTrackerSent = false;
	}

	public void destroy() {
		reset();

		mOverrideAdapter = true;
		mListener = null;
	}

	// SDK methods for native ads

	/**
	 * Get the list of native assets.
	 * 
	 * @return List of {@link AssetResponse} if response contains any assets
	 *         else returns null
	 */
	public List<AssetResponse> getNativeAssets() {
		if (mNativeAdDescriptor != null
				&& mNativeAdDescriptor.getNativeAssetList() != null
				&& mNativeAdDescriptor.getNativeAssetList().size() > 0) {
			return mNativeAdDescriptor.getNativeAssetList();
		}
		return null;
	}

	/**
	 * Get the javascript tracker received in native response. <br>
	 * The javascript tracker is represented by 'jstracker' object in OpenRTB
	 * Native ad specification.
	 * <p>
	 * <b>Note:</b> If jstracker is present, publisher should execute this
	 * javascript at impression time (after onNativeAdReceived callback)
	 * whenever possible.
	 * 
	 * @return JsTracker as string if present, else returns null.
	 */
	public String getJsTracker() {
		if (mNativeAdDescriptor != null
				&& mNativeAdDescriptor.getJsTracker() != null) {
			return mNativeAdDescriptor.getJsTracker();
		}
		return null;
	}

	/**
	 * Get the landing url whenever user clicks on the Native Ad
	 * 
	 * @return landing page url
	 */
	public String getClick() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getClick();
		}

		return null;
	}

	/**
	 * Returns the native ad response in json format.
	 * 
	 * @return
	 */
	public String getAdResponse() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getNativeAdJSON();
		}

		return null;
	}

	/**
	 * Returns true if mediation response
	 * 
	 * @return true for mediation response
	 */
	public boolean isMediationResponse() {
		return mNativeAdDescriptor.isTypeMediation();
	}

	/**
	 * In case if third party mediation response is received from Mocean server
	 * (when source=mediation), then user should call this method in
	 * onReceivedThirdPartyRequest() callback, to get mediation data. This
	 * mediation data can then be used to Initialize third party SDK. <br>
	 * Please note that MediationData will be null when mediation source is
	 * direct.
	 * 
	 * @return {@link MediationData} instance.
	 */
	public MediationData getMediationData() {
		return mMediationData;
	}

	// Private methods start
	private void initUserAgent() {
		if (TextUtils.isEmpty(mUserAgent)) {
			WebView webView = new WebView(mContext);
			mUserAgent = webView.getSettings().getUserAgentString();

			if (TextUtils.isEmpty(mUserAgent)) {
				mUserAgent = Defaults.USER_AGENT;
			}

			webView = null;
		}
	}

	private void logEvent(String event, LogLevel eventLevel) {
		if (eventLevel.ordinal() > mLogLevel.ordinal())
			return;

		if (mLogListener != null) {
			if (mLogListener.onLogEvent(this, event, eventLevel)) {
				return;
			}
		}
	}

	/**
	 * Call this method whenever a client side thirdparty response is received
	 * and user have rendered ad using third-party SDK. User should call this
	 * method when successful ad load complete callback is received from third
	 * party SDK.
	 */
	public void sendImpression() {
		if (!mThirdPartyImpTrackerSent && mNativeAdDescriptor != null
				&& mNativeAdDescriptor.isTypeMediation()) {
			sendImpressions(TrackerType.IMPRESSION_TRACKER);
			mThirdPartyImpTrackerSent = true;
		}
	}

	/**
	 * Call this method whenever ad received from client side thirdparty SDK is
	 * clicked. User should call this method when ad clicked callback is
	 * received from third party SDK.
	 */
	public void sendClickTracker() {
		if (!mThirdPartyClickTrackerSent && mNativeAdDescriptor != null
				&& mNativeAdDescriptor.isTypeMediation()) {
			sendImpressions(TrackerType.CLICK_TRACKER);
			mThirdPartyClickTrackerSent = true;
		}
	}

	private void sendImpressions(TrackerType trackerType) {
		if (mNativeAdDescriptor != null) {
			switch (trackerType) {
			case IMPRESSION_TRACKER:
				AdTracking.invokeTrackingUrl(Defaults.NETWORK_TIMEOUT_SECONDS,
						mNativeAdDescriptor.getNativeAdImpressionTrackers(),
						mUserAgent);
				break;
			case CLICK_TRACKER:
				AdTracking.invokeTrackingUrl(Defaults.NETWORK_TIMEOUT_SECONDS,
						mNativeAdDescriptor.getNativeAdClickTrackers(),
						mUserAgent);
				break;
			}
		}
	}

	private void adClicked() {
		if (!mClickTrackerSent) {
			sendImpressions(TrackerType.CLICK_TRACKER);

			mClickTrackerSent = true;
		}
	}

	/**
	 * Controls use of the internal browser. If used, a dialog will be used to
	 * show a browser in the application for ads that are clicked on (that open
	 * URLs). If not used an intent is started to invoke the system browser (or
	 * whatever is configured to handle the intent).
	 * 
	 * @param useInternalBrowser
	 *            true to use the internal browser, false to not use the
	 *            internal browser.
	 */
	public void setUseInternalBrowser(boolean useInternalBrowser) {
		this.mUseInternalBrowser = useInternalBrowser;
	}

	/**
	 * Returns the currently configured internal browser setting.
	 * 
	 * @return true if using the internal browser, false if not using the
	 *         internal browser.
	 */
	public boolean getUseInternalBrowser() {
		return mUseInternalBrowser;
	}

	private void openClickUrlInBrowser() {
		String url = null;
		// Open the url in the default/native browser
		if (mNativeAdDescriptor != null
				&& (url = mNativeAdDescriptor.getClick()) != null) {
			if (mUseInternalBrowser) {

				if (mBrowserDialog == null) {
					mBrowserDialog = new BrowserDialog(mContext, url,
							new BrowserDialog.Handler() {
								@Override
								public void browserDialogDismissed(
										BrowserDialog browserDialog) {
								}

								@Override
								public void browserDialogOpenUrl(
										BrowserDialog browserDialog,
										String url, boolean dismiss) {
									/*
									 * Since internal browser is unable to
									 * handle the url, open the url in native
									 * browser.
									 */
									Intent intent = new Intent(
											Intent.ACTION_VIEW, Uri.parse(url));
									mContext.startActivity(intent);

									if (dismiss) {
										browserDialog.dismiss();
									}
								}
							});
				} else {
					mBrowserDialog.loadUrl(url);
				}

				if (mBrowserDialog.isShowing() == false) {
					mBrowserDialog.show();
				}

			} else {
				/*
				 * Open the url in native browser.
				 */
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				mContext.startActivity(intent);
			}

		}
	}

	// Private methods end

	// Listeners for AdRequest.Listener start
	/*
	 * Deprecating these methods as these methods are public and may be
	 * accidentally called by the user causing unexpected behavior.
	 * 
	 * The access modifier will be changed to protected or default in the future
	 * so that the user will not be able to see these methods and will not be
	 * able to invoke these.
	 */
	@Override
	@Deprecated
	public void adRequestFailed(AdRequest request, Exception exception) {
		if (request != mAdRequest)
			return;

		logEvent("Ad request failed: " + exception, LogLevel.Error);

		fireCallback(CallbackType.NativeFailed, exception, null);
	}

	@Override
	@Deprecated
	// @formatter:off
	public void adRequestError(AdRequest request, String errorCode,
			String errorMessage) {
		if (request != mAdRequest)
			return;

		fireCallback(CallbackType.NativeFailed, new Exception(errorMessage),
				null);

		LogLevel logLevel = LogLevel.Error;
		if (String.valueOf(404).equals(errorCode)) {
			logLevel = LogLevel.Debug;
		}

		logEvent("Error response from server.  Error code: " + errorCode
				+ ". Error message: " + errorMessage, logLevel);
	}

	// @formatter:on

	@Override
	@Deprecated
	public void adRequestCompleted(AdRequest request, AdDescriptor adDescriptor) {
		if (request != mAdRequest)
			return;

		if (adDescriptor != null && adDescriptor instanceof NativeAdDescriptor) {
			mNativeAdDescriptor = (NativeAdDescriptor) adDescriptor;
			mMediationData = null;

			/*
			 * Check if the third party network has win. Then parse the response
			 * and inform the publisher.
			 */
			if (mNativeAdDescriptor.isTypeMediation()) {
				try {
					if (mOverrideAdapter) {
						/*
						 * Since user has decided to handle the third party SDK
						 * initialization using overrideAdapter = true, parse
						 * the response to get third party descriptor and inform
						 * the user.
						 */
						ThirdPartyDescriptor thirdPartyDescriptor = ThirdPartyDescriptor
								.getDescriptor(mNativeAdDescriptor);

						if (("mediation").equalsIgnoreCase(mNativeAdDescriptor
								.getSource())) {
							mMediationData = new MediationData();
							mMediationData
									.setMediationNetworkId(mNativeAdDescriptor
											.getMediationId());
							mMediationData
									.setMediationNetworkName(mNativeAdDescriptor
											.getMediation());
							mMediationData
									.setMediationSource(mNativeAdDescriptor
											.getSource());
							mMediationData.setMediationAdId(mNativeAdDescriptor
									.getAdUnitId());
						}

						fireCallback(CallbackType.ThirdPartyReceived, null,
								thirdPartyDescriptor);
					} else {
						logEvent(
								"Error parsing third party content descriptor. No Inbuilt adapter available.",
								LogLevel.Error);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					logEvent(
							"Error parsing third party content descriptor.  Exception:"
									+ ex, LogLevel.Error);
				}
			} else {
				// If it is the native response, then pass the native response
				// Set all the properties for native ad
				fireCallback(CallbackType.NativeReceived, null, null);
			} // if (mNativeAdDescriptor.isTypeMediation()) ends
		} else {
			fireCallback(CallbackType.NativeFailed, new Exception(
					"Incorrect response received"), null);
		}
	}

	private void fireCallback(CallbackType callbackType, Exception ex,
			ThirdPartyDescriptor thirdPartyDescriptor) {
		// Check if listener is set.
		if (mListener != null) {

			switch (callbackType) {
			case NativeReceived:
				mListener.onNativeAdReceived(this);
				break;
			case NativeFailed:
				mListener.onNativeAdFailed(this, ex);
				break;
			case ThirdPartyReceived:
				if (thirdPartyDescriptor != null) {
					mListener.onReceivedThirdPartyRequest(this,
							thirdPartyDescriptor.getProperties(),
							thirdPartyDescriptor.getParams());
				} else {
					fireCallback(CallbackType.NativeFailed, new Exception(
							"Third party response is invalid"),
							thirdPartyDescriptor);
				}
				break;
			case NativeAdClicked:
				mListener.onNativeAdClicked(this);
				break;
			}
		}
	}

	private enum CallbackType {
		NativeReceived, NativeFailed, ThirdPartyReceived, NativeAdClicked;
	}

	// constants and listeners
	private enum TrackerType {
		IMPRESSION_TRACKER, CLICK_TRACKER;
	}

	public static class Image {

		public Image(String url) {
			this.url = url;
		}

		Image(String url, int width, int height) {
			this.url = url;
			this.width = width;
			this.height = height;
		}

		String url = null;
		int width = 0;
		int height = 0;

		public String getUrl() {
			return url;
		}

		int getWidth() {
			return width;
		}

		int getHeight() {
			return height;
		}

		/**
		 * Parses the JSON and returns the object of {@link Image}, null
		 * otherwise.
		 * 
		 * @param json
		 * @return object of {@link Image}, else null if the parsing fails
		 */
		static Image getImage(JSONObject jsonImage) {
			Image image = null;

			if (jsonImage != null && jsonImage.optString(RESPONSE_URL) != null) {
				String url = jsonImage.optString(RESPONSE_URL);
				int width = jsonImage.optInt(NATIVE_IMAGE_W, 0);
				int height = jsonImage.optInt(NATIVE_IMAGE_H, 0);
				image = new Image(url, width, height);
			}

			return image;
		}
	}

	private class LocationListener implements android.location.LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			logEvent(
					"LocationListener.onLocationChanged location:"
							+ location.toString(), LogLevel.Debug);

			String lat = String.valueOf(location.getLatitude());
			String lng = String.valueOf(location.getLongitude());

			mAdRequestParameters.put(REQUESTPARAM_LATITUDE, lat);
			mAdRequestParameters.put(REQUESTPARAM_LONGITUDE, lng);
		}

		@Override
		public void onProviderDisabled(String provider) {
			logEvent(
					"LocationListener.onProviderDisabled provider:" + provider,
					LogLevel.Debug);
		}

		@Override
		public void onProviderEnabled(String provider) {
			logEvent("LocationListener.onProviderEnabled provider:" + provider,
					LogLevel.Debug);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			logEvent("LocationListener.onStatusChanged provider:" + provider
					+ " status:" + String.valueOf(status), LogLevel.Debug);

			if (status == LocationProvider.AVAILABLE)
				return;

			mAdRequestParameters.remove(REQUESTPARAM_LATITUDE);
			mAdRequestParameters.remove(REQUESTPARAM_LONGITUDE);
		}
	}
}
