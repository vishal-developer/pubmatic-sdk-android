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

package com.moceanmobile.mast;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.webkit.WebView;

import com.moceanmobile.mast.MASTAdView.LogLevel;
import com.moceanmobile.mast.MASTBaseAdapter.MediationNetwork;

public final class MASTNativeAd implements AdRequest.Handler {

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

	private String mNativeContent = null; // types of images required
	private NativeAdSize mIconImageSize = NativeAdSize.ICON_IMAGE_75X75;
	private NativeAdSize mLogoImageSize = NativeAdSize.LOGO_IMAGE_75X75;
	private NativeAdSize mMainImageSize = NativeAdSize.MAIN_IMAGE_1200X627;
	private int mTitleLength = -1;
	private int mDescriptionLength = -1;
	private int mCtaLength = -1;
	private int mZone = 0;
	private Map<MediationNetwork, String> mMapMediationNetworkTestDeviceIds = null;
	// Used to store the the current native response
	private NativeAdDescriptor mNativeAdDescriptor = null;
	private boolean mImpressionTrackerSent = false;
	private boolean mClickTrackerSent = false;
	private NativeRequestListener mListener = null;
	private String mUserAgent = null;
	private LogListener mLogListener = null;
	private LogLevel mLogLevel = LogLevel.None;
	private boolean test = false;
	//private List<Integer> mListDefaultedNetworkIds = null;
	private final HashMap<String, String> mAdRequestParameters;
	private MASTBaseAdapter mBaseAdapter = null;
	private BrowserDialog mBrowserDialog = null;

	/**
	 * If false, denotes that SDK should handle the third party request with the
	 * help of adapter. If true, SDK should give the control back to publisher
	 * to handle third party request.
	 */
	private boolean mOverrideAdapter = false;
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

	/**
	 * Sets the size of the Main image for native ad.
	 * 
	 * @param mainImageSize
	 *            Size of the Main image for native ad.
	 * 
	 */
	public void setMainImageSize(NativeAdSize mainImageSize) {
		this.mMainImageSize = mainImageSize;
	}

	/**
	 * Sets the size of the Icon image for native ad.
	 * 
	 * @param iconImageSize
	 *            Size of the Icon image for native ad.
	 */
	public void setIconImageSize(NativeAdSize iconImageSize) {
		this.mIconImageSize = iconImageSize;
	}

	/**
	 * Sets the size of the Logo image for native ad.
	 * 
	 * @param logoImageSize
	 */
	public void setLogoImageSize(NativeAdSize logoImageSize) {
		this.mLogoImageSize = logoImageSize;
	}

	// @formatter:off
	/**
	 * List of supported asset elements of native ad. <br />
	 * Possible values: <br />
	 * 0 - all fields; <br />
	 * 1 - icon image; <br />
	 * 2 - main image; <br />
	 * 3 - title; <br />
	 * 4 - description; <br />
	 * 5 - call to action; <br />
	 * 6 - rating. <br />
	 * <br />
	 * You can set different combinations with these values. <br />
	 * For example, native_content=1,3,4 (icon + title + description).
	 * 
	 * @param nativeContent
	 *            Any of the values between 0 to 6. Or combintation of values
	 *            comma separated e.g. 1,4,5.
	 */
	public void setNativeContent(String nativeContent) {
		this.mNativeContent = nativeContent;
	}

	// @formatter:on

	/**
	 * Set the length of the title for native ads. The valid value should be
	 * greater than zero.
	 * 
	 * @param titleLength
	 *            Length of title for native ads.
	 */
	public void setTitleLength(int titleLength) {
		if (titleLength <= 0) {
			throw new IllegalArgumentException(
					"The title length should be greater than zero");
		}
		this.mTitleLength = titleLength;
	}

	/**
	 * Set the length of the description for the native ads. The valid value
	 * should be greater than zero.
	 * 
	 * @param descriptionLength
	 *            Length of description for native ads.
	 */
	public void setDescriptionLength(int descriptionLength) {
		if (descriptionLength <= 0) {
			throw new IllegalArgumentException(
					"The description length should be greater than zero");
		}
		this.mDescriptionLength = descriptionLength;
	}

	/**
	 * Set the length of Call-to-Action text for native ads. The valid value
	 * should be greater than zero.
	 * 
	 * @param ctaLength
	 *            Length of Call-to-Action text for native ads.
	 */
	public void setCtaLength(int ctaLength) {
		if (ctaLength <= 0) {
			throw new IllegalArgumentException(
					"The CTA length should be greater than zero");
		}

		this.mCtaLength = ctaLength;
	}

	// @formatter:off
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
	public void addTestDeviceIdForNetwork(MediationNetwork network,
			String deviceId) {
		if (mMapMediationNetworkTestDeviceIds == null) {
			mMapMediationNetworkTestDeviceIds = new HashMap<MASTBaseAdapter.MediationNetwork, String>();
		}

		mMapMediationNetworkTestDeviceIds.put(network, deviceId);
	}
	
	
	/**
	 * Use this method to remove test devices ids for network.
	 * 
	 * e.g. if you have set device ids using
	 * {@link MASTNativeAd#addTestDeviceIdForNetwork(MediationNetwork, String)}
	 * remove it using 
	 * nativeAd.removeTestDeviceIdForNetwork(MediationNetwork.FACEBOOK_AUDIENCE_NETWORK);
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
	 * Updates ad.
	 */
	public void update() {
		internalUpdate(false);
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
			if ("direct".equals(mNativeAdDescriptor.getSource())) {
				args.put("excreatives", mNativeAdDescriptor.getCreativeId());
				args.remove("pubmatic_exfeeds");
			} else if ("mediation".equals(mNativeAdDescriptor.getSource())) {
				/*
				// Initialize defaulted id list. Send comma separated ids for
				// multiple defaulting. Valid for PubMatic case only.
				if (mListDefaultedNetworkIds == null) {
					mListDefaultedNetworkIds = new ArrayList<Integer>();
				}
				int defaultedId = Integer.parseInt(mNativeAdDescriptor
						.getMediationId());
				mListDefaultedNetworkIds.add(defaultedId);

				
				// Convert the list of defaulted ids to comma separated values.
				String defaultedNetworks = null;
				if (mListDefaultedNetworkIds.size() == 1) {
					defaultedNetworks = String.valueOf(mListDefaultedNetworkIds
							.get(0));
				} else {
					defaultedNetworks = TextUtils.join(",",
							mListDefaultedNetworkIds);
				}
				args.put("pubmatic_exfeeds", defaultedNetworks);
				*/
				args.put("pubmatic_exfeeds", mNativeAdDescriptor.getMediationId());

				args.remove("excreatives");
			}
		} else {
			// Clear the defaulted network list as this is a fresh request.
			// mListDefaultedNetworkIds = null;
		}

		reset();

//		if (mZone == 0) {
//			// @formatter:off
//			throw new IllegalArgumentException(
//					"Zone not set. Please set zone to receive ads!");
//			// @formatter:on
//		}

		initUserAgent();

		try {
			TelephonyManager tm = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			String networkOperator = tm.getNetworkOperator();
			if ((networkOperator != null) && (networkOperator.length() > 3)) {
				String mcc = networkOperator.substring(0, 3);
				String mnc = networkOperator.substring(3);

				args.put("mcc", String.valueOf(mcc));
				args.put("mnc", String.valueOf(mnc));
			}
		} catch (Exception ex) {
			logEvent("Unable to obtain mcc and mnc. Exception:" + ex,
					LogLevel.Debug);
		}

		// Put all the user sent parameters in the request
		args.putAll(mAdRequestParameters);

		// Don't allow these to be overridden.
		args.put("ua", mUserAgent);
		args.put("version", Defaults.SDK_VERSION);
		args.put("count", "1");
		args.put("key", "8"); // Response type for Generic Json
		args.put("type", "8"); // Ad type for native.
		args.put("zone", String.valueOf(mZone));

		/* Putting optional parameters related to Native Ad */

		// Main Image size
		// img_size_x - Maximal width of main image.
		// img_size_y - Maximal height of main image.
		if (mMainImageSize != null) {
			args.put("img_size_x", String.valueOf(mMainImageSize.getWidth()));
			args.put("img_size_y", String.valueOf(mMainImageSize.getHeight()));

			if (mMainImageSize.getAspectRatio() != null) {
				args.put("img_ratio", mMainImageSize.getAspectRatio());
			}
		}

		// Icon size.
		// icon_size_x - Maximal width of the icon for native ads.
		// icon_size_y - Maximal height of the icon for native ads.
		if (mIconImageSize != null) {
			args.put("icon_size_x", String.valueOf(mIconImageSize.getWidth()));
			args.put("icon_size_y", String.valueOf(mIconImageSize.getHeight()));
		}

		// Logo size.
		// logo_size_x - Maximal width of the logo for native ads.
		// logo_size_y - Maximal height of the logo for native ads.
		if (mLogoImageSize != null) {
			args.put("logo_size_x", String.valueOf(mLogoImageSize.getWidth()));
			args.put("logo_size_y", String.valueOf(mLogoImageSize.getHeight()));
		}

		// List of supported asset elements of native ad.
		if (!TextUtils.isEmpty(mNativeContent)) {
			args.put("native_content", String.valueOf(mNativeContent));
		}

		if (mTitleLength != -1) {
			args.put("title_length", String.valueOf(mTitleLength));
		}

		if (mDescriptionLength != -1) {
			args.put("description_length", String.valueOf(mDescriptionLength));
		}

		if (mCtaLength != -1) {
			args.put("cta_length", String.valueOf(mCtaLength));
		}

		// args.put("seq","");

		if (this.test) {
			args.put("test", "1");
		}

		try {
			if (mAdRequest != null)
				mAdRequest.cancel();

			mAdRequest = AdRequest.create(Defaults.NETWORK_TIMEOUT_SECONDS,
					mNetworkUrl, mUserAgent, args, this);

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
			if (mBaseAdapter != null) {
				mBaseAdapter.trackViewForInteractions(view);
			} else {
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
	public void overrideAdapterLoading(boolean override) {
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

		mImpressionTrackerSent = false;
		mClickTrackerSent = false;

		if (mBaseAdapter != null) {
			mBaseAdapter.destroy();
			mBaseAdapter = null;
		}
	}

	public void destroy() {
		reset();

		mOverrideAdapter = false;
		mListener = null;
	}

	// SDK methods for native ads
	/**
	 * Get the title of the Native Ad
	 * 
	 * @return title of the Native Ad
	 */
	public String getTitle() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getNativeAdTitle();
		}

		return null;
	}

	/**
	 * Get the description text of the Native Ad
	 * 
	 * @return description text of the Native Ad
	 */
	public String getText() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getNativeAdText();
		}

		return null;
	}

	/**
	 * Get the icon image for the Native Ad
	 * 
	 * @return an Object of {@link Image}
	 */
	public Image getIconImage() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getIconImage();
		}

		return null;
	}

	/**
	 * Get the main image for the Native Ad
	 * 
	 * @return an Object of {@link Image}
	 */
	public Image getMainImage() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getMainImage();
		}

		return null;
	}

	/**
	 * Get the Call to Action Text for the Native Ad
	 * 
	 * @return CTA Text of the Native Ad
	 */
	public String getCallToAction() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getNativeAdCallToAction();
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
	 * Get the star rating for the Native Ad
	 * 
	 * @return star rating for the Native Ad
	 */
	public float getRating() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getNativeAdRating();
		}

		return 0.0f;
	}

	/**
	 * Get number of downloads of the application
	 * 
	 * @return number of downloads
	 */
	public long getDownloads() {
		if (mNativeAdDescriptor != null) {
			return mNativeAdDescriptor.getDownloads();
		}

		return 0;
	}

	/**
	 * Get the VASTTag in case of video
	 * 
	 * @return
	 */
	public String getVastTag() {
		if (mNativeAdDescriptor != null) {
			mNativeAdDescriptor.getVastTag();
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

	private void openClickUrlInBrowser() {
		String url = null;
		// Open the url in the default/native browser
		if (mNativeAdDescriptor != null
				&& (url = mNativeAdDescriptor.getClick()) != null) {
			if (mBrowserDialog == null) {
				mBrowserDialog = new BrowserDialog(mContext, url,
						new BrowserDialog.Handler() {
							@Override
							public void browserDialogDismissed(
									BrowserDialog browserDialog) {
							}

							@Override
							public void browserDialogOpenUrl(
									BrowserDialog browserDialog, String url,
									boolean dismiss) {
								/*
								 * Since internal browser is unable to handle
								 * the url, open the url in native browser.
								 */
								Intent intent = new Intent(Intent.ACTION_VIEW,
										Uri.parse(url));
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

		fireCallback(CallbackType.NativeFailed, new Exception(errorMessage), null);

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

			/*
			 * Check if the third party network has win. Then parse the response
			 * and inform the publisher.
			 */
			if (mNativeAdDescriptor.isTypeMediation()) {
				try {
					if (!mOverrideAdapter) {
						// @formatter:off
						// String create a class name in case of FAN or MoPub
						String className = null;
						if (TextUtils.equals("FAN", mNativeAdDescriptor.getMediation())) {
							className = "com.moceanmobile.mast.mediation.MASTFacebookAdapter";
						} else if (TextUtils.equals("MoPub", mNativeAdDescriptor.getMediation())) {
							className = "com.moceanmobile.mast.mediation.MASTMoPubAdapter";
						} else {
							className = mNativeAdDescriptor.getMediation();
						}

						// Launch the adapter with the class name received.
						mBaseAdapter = MASTBaseAdapter.getAdapterForClass(className, mContext);

						if (mBaseAdapter != null) {
							mBaseAdapter.init(mContext, mNativeAdDescriptor, adapterListener);
							mBaseAdapter.mKeywords = mAdRequestParameters;
							mBaseAdapter.mNativeContent = mNativeContent;
							mBaseAdapter.mMapMediationNetworkTestDeviceIds = mMapMediationNetworkTestDeviceIds;
							mBaseAdapter.loadAd();
						} else {
							// Make new request notifying that third party partner defaulted
							thirdpartyPartnerDefaulted();
							logEvent("Adapter not found for class " + className, LogLevel.Error);
						}
						// @formatter:on
					} else {
						/*
						 * Since user has decided to handle the third party SDK
						 * initialization using overrideAdapter = true, parse
						 * the response to get third party descriptor and inform
						 * the user.
						 */
						ThirdPartyDescriptor thirdPartyDescriptor = ThirdPartyDescriptor
								.getDescriptor(mNativeAdDescriptor);
						fireCallback(CallbackType.ThirdPartyReceived, null,
								thirdPartyDescriptor);
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

			if (jsonImage != null && jsonImage.optString("url") != null) {
				String url = jsonImage.optString("url");
				int width = jsonImage.optInt("w", 0);
				int height = jsonImage.optInt("h", 0);
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

			mAdRequestParameters.put("lat", lat);
			mAdRequestParameters.put("long", lng);
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

			mAdRequestParameters.remove("lat");
			mAdRequestParameters.remove("long");
		}
	}

	// Listener methods for MASTBaseAdapter
	private MASTBaseAdapterListener adapterListener = new MASTBaseAdapterListener() {

		@Override
		public void onReceiveError(MASTBaseAdapter adapter, Exception exception) {

			/*
			 * Initiate a new request informing that third party SDK has
			 * defaulted.
			 */
			thirdpartyPartnerDefaulted();
		}

		@Override
		public void onReceiveAd(MASTBaseAdapter adapter) {
			if (mListener != null) {
				mListener.onNativeAdReceived(MASTNativeAd.this);
			}
		}

		@Override
		public void onAdClicked(MASTBaseAdapter adapter) {
			adClicked();

			fireCallback(CallbackType.NativeAdClicked, null, null);
		}
	};
}
