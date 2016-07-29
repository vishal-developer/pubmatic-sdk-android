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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moceanmobile.mast.AdvertisingIdClient.AdInfo;
import com.moceanmobile.mast.AdvertisingIdClient.AdInfo.HASHING_TECHNIQUE;
import com.moceanmobile.mast.MASTAdViewDelegate.ActivityListener;
import com.moceanmobile.mast.MASTAdViewDelegate.FeatureSupportHandler;
import com.moceanmobile.mast.MASTAdViewDelegate.InternalBrowserListener;
import com.moceanmobile.mast.MASTAdViewDelegate.LogListener;
import com.moceanmobile.mast.MASTAdViewDelegate.RequestListener;
import com.moceanmobile.mast.MASTAdViewDelegate.RichMediaListener;
import com.moceanmobile.mast.mraid.Bridge;
import com.moceanmobile.mast.mraid.Consts;
import com.moceanmobile.mast.mraid.Consts.Feature;
import com.moceanmobile.mast.mraid.Consts.ForceOrientation;
import com.moceanmobile.mast.mraid.Consts.PlacementType;
import com.moceanmobile.mast.mraid.Consts.State;
import com.moceanmobile.mast.mraid.ExpandProperties;
import com.moceanmobile.mast.mraid.OrientationProperties;
import com.moceanmobile.mast.mraid.ResizeProperties;
import com.moceanmobile.mast.mraid.WebView;

/**
 * Main class used for rendering ad content.
 * <p>
 * Can be placed in XML layouts or created and placed in code.
 * <p>
 * To obtain ad content simply place an ad view of appropriate size in the view
 * tree, set the zone and call update.
 */
@SuppressLint("NewApi")
public class MASTAdView extends ViewGroup {
	public enum LogLevel {
		None, Error, Debug,
	}

	private static final String TAG = MASTAdView.class.getSimpleName();

	final private String sdkVersion = Defaults.SDK_VERSION;
	final private int CloseAreaSizeDp = 50;
	final private int OrientationReset = Short.MIN_VALUE;

	// User agent used for all requests
	private String userAgent = null;

	// Configuration
	private int zone = 0;
	private boolean test = false;
	private String creativeCode = null;
	private int updateInterval = 0;
	private String adNetworkURL = Defaults.AD_NETWORK_URL;
	private Map<String, String> adRequestDefaultParameters = new HashMap<String, String>();
	private Map<String, List<String>> adRequestCustomParameters = new HashMap<String, List<String>>();

	private boolean useInternalBrowser = false;
	private LogLevel logLevel = LogLevel.Error;
	private PlacementType placementType = PlacementType.Inline;

	// Ad containers (render ad content)
	private WebView webView = null;
	private TextView textView = null;
	private ImageView imageView = null;

	// Close button
	private boolean showCloseButton = false;
	private int closeButtonDelay = 0;
	private Drawable closeButtonCustomDrawable = null;
	private ScheduledFuture<?> closeButtonFuture = null;

	// Interstitial configuration
	private ExpandDialog interstitialDialog = null;
	private ScheduledFuture<?> interstitialDelayFuture = null;

	// MRAID support
	private Bridge mraidBridge = null;
	private boolean mraidBridgeInit = false;
	private Bridge.Handler mraidBridgeHandler = new MRAIDHandler();
	private ExpandDialog mraidExpandDialog = null;
	private RelativeLayout mraidResizeLayout = null;
	private View mraidResizeCloseArea = null;
	private boolean mraidTwoPartExpand = false;
	private Bridge mraidTwoPartBridge = null;
	private boolean mraidTwoPartBridgeInit = false;
	private WebView mraidTwoPartWebView = null;
	private int mraidOriginalOrientation = OrientationReset;

	// Handles WebView client callbacks for MRAID or other WebView based ads.
	private WebView.Handler webViewHandler = new WebViewHandler();

	// Updating
	private boolean updateOnLayout = false;
	private boolean deferredUpdate = false;
	private AdRequest mAdRequest = null;
	private AdDescriptor mAdDescriptor = null;
	private MediationData mMediationData;
	private AdRequestHandler adRequestHandler = new AdRequestHandler();
	private ScheduledFuture<?> adUpdateIntervalFuture = null;

	// Tracking
	private boolean invokeTracking = false;
	private boolean mImpressionTrackerSent = false;
	private boolean mClickTrackerSent = false;

	// Internal browser
	private BrowserDialog browserDialog = null;

	// Location support
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;

	// Delegates
	private ActivityListener activityListener;
	private FeatureSupportHandler featureSupportHandler;
	private InternalBrowserListener internalBrowserListener;
	private LogListener logListener;
	private RequestListener requestListener;
	private RichMediaListener richMediaListener;

	// androidid
	private boolean isAndroidIdEnabled;

	// androidAid
	private boolean isAndroidAidEnabled = true;
	private HASHING_TECHNIQUE hashing = HASHING_TECHNIQUE.NONE;

	// Receiver
	private BroadcastReceiver mReceiver;
	private IntentFilter filter;

	/**
	 * Used to create instances for placement in code. Only produces inline
	 * instances.
	 * 
	 * @param context
	 */
	public MASTAdView(Context context) {
		super(context);
		init(false);
	}

	/**
	 * Used to create instances for placement in code. Produces inline or
	 * interstitial instances.
	 * 
	 * @param context
	 * @param interstitial
	 *            set to true to produce interstitial instances. Interstitial
	 *            instances should never be added to any view group parent.
	 */
	public MASTAdView(Context context, boolean interstitial) {
		super(context);
		init(interstitial);
	}

	/**
	 * Used to create instances when placed in XML layouts. The view should be
	 * positioned like any other view.
	 * 
	 * @param context
	 * @param attrs
	 */
	public MASTAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		applyAttributeSet(attrs);
		init(false);
	}

	/**
	 * Used to create instances when placed in XML layouts. The view should be
	 * positioned like any other view.
	 * 
	 * @param context
	 * @param attrs
	 */
	public MASTAdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		applyAttributeSet(attrs);
		init(false);
	}

	protected void init(boolean interstitial) {
		
		placementType = PlacementType.Inline;

		if (interstitial) {
			placementType = PlacementType.Interstitial;
			interstitialDialog = new ExpandDialog(getContext());
		}

		if (zone != 0) {
			updateOnLayout = true;
		}

		setOnClickListener(new OnClickHandler());

		initUserAgent();
	}

	protected void applyAttributeSet(AttributeSet attrs) {
		setZone(attrs.getAttributeIntValue(null,
				MASTAdViewConstants.xml_layout_attribute_zone, zone));

		String strValue = attrs.getAttributeValue(null,
				MASTAdViewConstants.xml_layout_attribute_logLevel);
		if (TextUtils.isEmpty(strValue) == false) {
			setLogLevel(LogLevel.valueOf(strValue));
		}
	}

	private void updateOnLayout() {
		if (updateOnLayout) {
			updateOnLayout = false;

			update();
		}
	}

	private void initUserAgent() {
		if (TextUtils.isEmpty(userAgent)) {
			userAgent = getWebView().getSettings().getUserAgentString();

			if (TextUtils.isEmpty(userAgent)) {
				userAgent = Defaults.USER_AGENT;
			}
		}
	}

	/**
	 * Accessor to the User-Agent header value the SDK will send to the ad
	 * network.
	 * 
	 * @return
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * Determines if the instance is configured as inline.
	 * 
	 * @return true if instance represents inline, false if it represents
	 *         interstitial. Interstitial instances should not be added to view
	 *         layouts.
	 */
	public boolean isInline() {
		if (placementType == PlacementType.Inline)
			return true;

		return false;
	}

	/**
	 * Determines if the instance is configured as interstitial.
	 * 
	 * @return true if instance represents interstitial, false if it represents
	 *         inline. Interstitial instances should not be added to view
	 *         layouts.
	 */
	public boolean isInterstitial() {
		if (placementType == PlacementType.Interstitial)
			return true;

		return false;
	}

	/**
	 * Sets the activity listener. This listener provides information for user
	 * ad interaction events. Set to null when no longer interested in events.
	 * 
	 * @param activityListener
	 *            MASTAdViewDelegate.ActivityListener implementation
	 */
	public void setActivityListener(
			MASTAdViewDelegate.ActivityListener activityListener) {
		this.activityListener = activityListener;
	}

	/**
	 * Returns the currently configured activity listener.
	 * 
	 * @return MASTAdViewDelegate.ActivityListener set with
	 *         setActivityListener().
	 */
	public MASTAdViewDelegate.ActivityListener getActivityListener() {
		return activityListener;
	}

	/**
	 * Sets the feature support handler. This handler is used to control
	 * features of the SDK. Set to override default behavior.
	 * 
	 * @param featureSupportHandler
	 *            MASTAdViewDelegate.FeatureSupportHandler implementation
	 */
	public void setFeatureSupportHandler(
			MASTAdViewDelegate.FeatureSupportHandler featureSupportHandler) {
		this.featureSupportHandler = featureSupportHandler;
	}

	/**
	 * Returns the currently configured handler.
	 * 
	 * @return MASTAdViewDelegate.FeatureSupportHandler set with
	 *         setFeatureSupportHandler().
	 */
	public MASTAdViewDelegate.FeatureSupportHandler getFeatureSupportHandler() {
		return featureSupportHandler;
	}

	/**
	 * Sets the internal browser listener. This listener provides information on
	 * internal browser related events.
	 * 
	 * @param internalBrowserListener
	 *            MASTAdViewDelegate.InternalBrowserListener implementation
	 */
	public void setInternalBrowserListener(
			MASTAdViewDelegate.InternalBrowserListener internalBrowserListener) {
		this.internalBrowserListener = internalBrowserListener;
	}

	/**
	 * Returns the currently configured listener.
	 * 
	 * @return MASTAdViewDelegate.InternalBrowserListener set with
	 *         setInternalBrowserListener().
	 */
	public MASTAdViewDelegate.InternalBrowserListener getInternalBrowserListener() {
		return internalBrowserListener;
	}

	/**
	 * Sets the log listener. This listener provides the ability to override
	 * default logging behavior.
	 * 
	 * @param internalBrowserListener
	 *            MASTAdViewDelegate.LogListener implementation
	 */
	public void setLogListener(MASTAdViewDelegate.LogListener logListener) {
		this.logListener = logListener;
	}

	/**
	 * Returns the currently configured listener.
	 * 
	 * @return MASTAdViewDelegate.LogListener set with setLogListener().
	 */
	public MASTAdViewDelegate.LogListener getLogListener() {
		return logListener;
	}

	/**
	 * Sets the request listener. This listener provides information on ad
	 * update events.
	 * 
	 * @param internalBrowserListener
	 *            MASTAdViewDelegate.RequestListener implementation
	 */
	public void setRequestListener(
			MASTAdViewDelegate.RequestListener requestListener) {
		this.requestListener = requestListener;
	}

	/**
	 * Returns the currently configured listener.
	 * 
	 * @return MASTAdViewDelegate.RequestListener set with setRequestListener().
	 */
	public MASTAdViewDelegate.RequestListener getRequestListener() {
		return requestListener;
	}

	/**
	 * Sets the rich media listener. This listener provides information on rich
	 * media events.
	 * 
	 * @param internalBrowserListener
	 *            MASTAdViewDelegate.RichMediaListener implementation
	 */
	public void setRichMediaListener(
			MASTAdViewDelegate.RichMediaListener richMediaListener) {
		this.richMediaListener = richMediaListener;
	}

	/**
	 * Returns the currently configured listener.
	 * 
	 * @return MASTAdViewDelegate.RichMediaListener set with
	 *         setRichMediaListener().
	 */
	public MASTAdViewDelegate.RichMediaListener getRichMediaListener() {
		return richMediaListener;
	}

	/**
	 * Specifies the URL of the ad network. This defaults to Mocean's ad
	 * network.
	 * 
	 * @param adNetworkURL
	 *            URL of the ad server (ex: http://ads.moceanads.com/ad);
	 */
	public void setAdNetworkURL(String adNetworkURL) {
		this.adNetworkURL = adNetworkURL;
	}

	/**
	 * Returns the currently configured ad network.
	 * 
	 * @return Currently configured ad network URL.
	 */
	public String getAdNetworkURL() {
		return adNetworkURL;
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
		return adRequestDefaultParameters;
	}

	public Map<String, List<String>> getAdRequestCustomParameters() {
		return adRequestCustomParameters;
	}

	/**
	 * Add custom parameters in the Ad request. <br>
	 * Mocean SDK allows its customers to add their own parameters and pass them
	 * in ad requests. <br>
	 * You can add multiple values with the same key name, by calling this
	 * method multiple times for same key name. <br>
	 * E.g.:<br>
	 * 
	 * <pre>
	 * mastAdView.addAdRequestCustomParameter(&quot;age&quot;, &quot;20&quot;);
	 * mastAdView.addAdRequestCustomParameter(&quot;age&quot;, &quot;25&quot;);
	 * </pre>
	 * 
	 * @param key
	 *            Key name for custom parameter to be passed. E.g.: "age"
	 * @param value
	 *            Value of the custom parameter to be passed. E.g.: "25"
	 */
	public void addAdRequestCustomParameter(String key, String value) {
		if ((!TextUtils.isEmpty(key) || !TextUtils.isEmpty(value))
				&& adRequestCustomParameters != null) {
			List<String> valueList = adRequestCustomParameters.get(key);
			if (valueList == null) {
				valueList = new ArrayList<String>();
			}
			valueList.add(value);
			adRequestCustomParameters.put(key, valueList);
		}
	}

	/**
	 * Sets the interval between updates.
	 * <p>
	 * Invoke update() after setting for changes to apply immediately.
	 * 
	 * @param updateInterval
	 *            Time interval in seconds between ad requests.
	 */
	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	/**
	 * Returns the currently configured update interval.
	 * 
	 * @return Time interval in seconds between ad requests.
	 */
	public int getUpdateInterval() {
		return updateInterval;
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
		this.zone = zone;
	}

	/**
	 * Returns the currently configured ad network zone.
	 * 
	 * @return Ad network zone.
	 */
	public int getZone() {
		return zone;
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

	/** Get the test creativeCode you have set */
	public String getCreativeCode() {
		return creativeCode;
	}

	/**
	 * Set the creativeCode script to request this test creative in SDK. <br>
	 * <b>Note:</b> Use only while testing. Do not use this in production. <br>
	 * Server side configuration might be required to use this feature.
	 * 
	 * @param creativeCode
	 *            The javascript creative code to set.
	 */
	public void setCreativeCode(String creativeCode) {
		this.creativeCode = creativeCode;
	}

	/**
	 * Used with interstitial to show a close button. If not set, users will not
	 * see a close button on interstitial ads. Does nothing if used with inline
	 * instances.
	 * 
	 * @param showCloseButton
	 *            true to show a close button, false to not show a close button.
	 */
	public void setShowCloseButton(boolean showCloseButton) {
		this.showCloseButton = showCloseButton;

		// TODO: Make this apply immediately after the fact, vs on
		// showInterstitial.
	}

	/**
	 * Returns state of showing the close button for interstitial ads.
	 * 
	 * @return true if showing close button, false if close button will not be
	 *         shown.
	 */
	public boolean getShowCloseButton() {
		return showCloseButton;
	}

	/**
	 * Sets the delay time between showing an interstitial with
	 * showInterstitial() and showing the close button. A value of 0 indicates
	 * the button should be shown immediately.
	 * 
	 * @param closeButtonDelay
	 *            Time interval in seconds to delay showing a close button after
	 *            showing interstitial ad.
	 */
	public void setCloseButtonDelay(int closeButtonDelay) {
		this.closeButtonDelay = closeButtonDelay;
	}

	/**
	 * Returns the currently configured close button delay.
	 * 
	 * @return Time interval in seconds to delay showing a close button after
	 *         showing interstitial.
	 */
	public int getCloseButtonDelay() {
		return closeButtonDelay;
	}

	/**
	 * Allows custom close buttons to override SDK default. If set the provided
	 * drawable will be used for the close button for interstitial and rich
	 * media ads (if ad uses SDK provided close button).
	 * 
	 * @param closeButtonCustomDrawable
	 *            Drawable used to override the default close button image or
	 *            null to use the default.
	 */
	public void setCloseButtonCustomDrawable(Drawable closeButtonCustomDrawable) {
		this.closeButtonCustomDrawable = closeButtonCustomDrawable;
	}

	/**
	 * Returns the currently configured close button custom drawable.
	 * 
	 * @return Returns the custom close button drawable set with
	 *         setCloseButtonCustomDrawable() or null if one is not set.
	 */
	public Drawable getCloseButtonCustomDrawable() {
		return closeButtonCustomDrawable;
	}

	/**
	 * Controls enablement of the internal browser. If used, a dialog will be
	 * used to show a browser in the application for ads that are clicked on
	 * (that open URLs). If not used an intent is started to invoke the system
	 * browser (or whatever is configured to handle the intent).
	 * 
	 * @param useInternalBrowser
	 *            true to use the internal browser, false to not use the
	 *            internal browser.
	 */
	public void setUseInternalBrowser(boolean useInternalBrowser) {
		this.useInternalBrowser = useInternalBrowser;
	}

	/**
	 * Returns the currently configured internal browser setting.
	 * 
	 * @return true if using the internal browser, false if not using the
	 *         internal browser.
	 */
	public boolean getUseInternalBrowser() {
		return useInternalBrowser;
	}

	/**
	 * Determines if the internal browser is open.
	 * 
	 * @return true if the internal browser is open, false if not.
	 */
	public boolean isInternalBrowserOpen() {
		if ((browserDialog != null) && browserDialog.isShowing()) {
			return true;
		}

		return false;
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
			if (locationManager != null && locationListener != null) {
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
			throw new IllegalArgumentException("criteria or provider required");

		locationManager = (LocationManager) MASTAdView.this.getContext()
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

	/**
	 * Sets the log level of the instance. Logging is done through console
	 * logging.
	 * 
	 * @param logLevel
	 *            LogLevel
	 */
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	/**
	 * Returns the currently configured log level.
	 * 
	 * @return currently configured LogLevel
	 */
	public LogLevel getLogLevel() {
		return logLevel;
	}

	/**
	 * Updates ad.
	 * 
	 * Invokes update(false).
	 */
	public void update() {
		update(false);
	}

	/**
	 * add androidid as request param.
	 * 
	 * @param isAndroidIdEnabled
	 */
	public void setAndroidIdEnabled(boolean isAndroidIdEnabled) {
		this.isAndroidIdEnabled = isAndroidIdEnabled;
	}

	public boolean isAndoridIdEnabled() {
		return isAndroidIdEnabled;
	}

	/**
	 * Add androidaid as request param & send to ad request.
	 * By default, its values is true.
	 * 
	 * @param isAndroidaidEnabled Set false if do not want to send AID
	 */
	public void setAndroidAidEnabled(boolean isAndroidAidEnabled) {
		this.isAndroidAidEnabled = isAndroidAidEnabled;
	}

	public boolean isAndoridAidEnabled() {
		return isAndroidAidEnabled;
	}

	/**
	 * Invokes an update which requests and if received, renders ad content
	 * replacing any previous ad content. If the force parameter is set to false
	 * the update will be deferred if the user is interacting with the current
	 * ad (rich media resize/expand or internal browser open). If the force
	 * parameter is set to true will close any interaction with the current ad
	 * before updating.
	 * 
	 * @param force
	 *            true to force an update regardless of ad state, false to defer
	 *            update if needed
	 */
	public void update(boolean force) {
		if (zone == 0)
			throw new IllegalStateException("zone not set");

		if (adUpdateIntervalFuture != null) {
			adUpdateIntervalFuture.cancel(true);
			adUpdateIntervalFuture = null;
		}

		if (updateInterval > 0) {
			adUpdateIntervalFuture = Background.getExecutor()
					.scheduleAtFixedRate(new Runnable() {
						@Override
						public void run() {
							internalUpdate(false);
						}

					}, updateInterval, updateInterval, TimeUnit.SECONDS);
		}

		if (force) {
			closeInternalBrowser();

			if (placementType == PlacementType.Inline) {
				if ((mraidBridge != null) && (mraidBridgeHandler != null)) {
					switch (mraidBridge.getState()) {
					case Loading:
					case Default:
					case Hidden:
						break;

					case Expanded:
					case Resized:
						mraidBridgeHandler.mraidClose(mraidBridge);
						break;
					}
				}
			}
		}

		internalUpdate(false);
	}

	/**
	 * Resets instance state to it's default (doesn't reset configured
	 * parameters). Stops update interval timer, closes internal browser if
	 * open, disables location detection.
	 * <p>
	 * Invoke this method to stop any ad processing. This should be done for ads
	 * that have a update time interval set with setUpdateInterval() before the
	 * owning context/activity is destroyed.
	 */
	public void reset() {
		deferredUpdate = false;
		mImpressionTrackerSent = false;
		mClickTrackerSent = false;

		removeContent();

		if (adUpdateIntervalFuture != null) {
			adUpdateIntervalFuture.cancel(true);
			adUpdateIntervalFuture = null;
		}

		if (interstitialDelayFuture != null) {
			interstitialDelayFuture.cancel(true);
			interstitialDelayFuture = null;
		}

		closeInternalBrowser();
		browserDialog = null;

		setLocationDetectionEnabled(false);
		unregisterReceiver();
	}

	/**
	 * Removes any displayed ad content.
	 */
	public void removeContent() {
		deferredUpdate = false;

		resetRichMediaAd();
		resetImageAd();
		resetTextAd();

		switch (placementType) {
		case Inline:
			removeAllViews();
			break;

		case Interstitial:
			interstitialDialog.removeAllViews();
		}

		mAdDescriptor = null;
		mMediationData = null;
	}

	/**
	 * Call this method if the third party partner defaults or gives the error.
	 * SDK will initiate a new request eliminating the call to the defaulted
	 * partner.
	 */
	public void thirdpartyPartnerDefaulted() {
		internalUpdate(true);
	}

	private void internalUpdate(boolean isLastRequestDafaulted) {

		deferredUpdate = false;

		if (isInternalBrowserOpen()) {
			deferredUpdate = true;
			return;
		}

		if ((mraidBridge != null) && (mraidBridgeHandler != null)) {
			switch (mraidBridge.getState()) {
			case Loading:
			case Default:
			case Hidden:
				break;

			case Expanded:
			case Resized:
				deferredUpdate = true;
				return;
			}
		}

		if ((mraidBridge != null) && (mraidBridgeHandler != null)) {
			switch (mraidBridge.getState()) {
			case Loading:
			case Default:
			case Hidden:
				break;

			case Expanded:
			case Resized:
				mraidBridgeHandler.mraidClose(mraidBridge);
				break;
			}
		}

		Map<String, List<String>> args = new HashMap<String, List<String>>();

		// Default size_x/y used.
		int size_x = getWidth();
		int size_y = getHeight();
		if (isInterstitial()) {
			DisplayMetrics displayMetrics = Resources.getSystem()
					.getDisplayMetrics();
			size_x = displayMetrics.widthPixels;
			size_y = displayMetrics.heightPixels;
		}
		boolean isX = false, isY = false;
		if (adRequestDefaultParameters.size() > 0) {
			for (Map.Entry<String, String> entry : adRequestDefaultParameters
					.entrySet()) {
				if (entry.getKey().equals("size_x")) {
					isX = true;
				}
				if (entry.getKey().equals("size_y")) {
					isY = true;
				}
			}

			adRequestDefaultParameters.put("size_required", "1");
			if (isAndoridIdEnabled())
				adRequestDefaultParameters.put("androidid_sha1",
						getUdidFromContext(getContext()));

			if (!isX)
				adRequestDefaultParameters
						.put("size_x", String.valueOf(size_x));
			if (!isY)
				adRequestDefaultParameters
						.put("size_y", String.valueOf(size_y));
		} else {
			adRequestDefaultParameters.put("size_x", String.valueOf(size_x));
			adRequestDefaultParameters.put("size_y", String.valueOf(size_y));
		}

		AdInfo adInfo = AdvertisingIdClient.refreshAdvertisingInfo(getContext());
		
		if(adInfo!=null) {

			if (isAndoridAidEnabled() && !TextUtils.isEmpty(adInfo.getId())) {

				adRequestDefaultParameters.put("androidaid", adInfo.getId());
				switch (hashing) {
					case ALL:
						adRequestDefaultParameters.put("androidaid_sha1",
								sha1(adInfo.getId()));
						adRequestDefaultParameters.put("androidaid_md5",
								md5(adInfo.getId()));
						break;
					case SHA1:
						adRequestDefaultParameters.put("androidaid_sha1",
								sha1(adInfo.getId()));
						break;
					case MD5:
						adRequestDefaultParameters.put("androidaid_md5",
								md5(adInfo.getId()));
						break;
				default:
					break;
				}
			}

			/*
			 * Pass dnt=1 if user have enabled Opt-Out of interest based ads in
			 * Google settings in Android device
			 */
			adRequestDefaultParameters.put("dnt", String.valueOf(adInfo.isLimitAdTrackingEnabled() == true ? 1 : 0));
		}
		
		System.out.println("Ad Params = "+adRequestDefaultParameters.toString());
		try {
			TelephonyManager tm = (TelephonyManager) getContext()
					.getSystemService(Context.TELEPHONY_SERVICE);
			String networkOperator = tm.getNetworkOperator();
			if ((networkOperator != null) && (networkOperator.length() > 3)) {
				String mcc = networkOperator.substring(0, 3);
				String mnc = networkOperator.substring(3);
				boolean isMCC = false, isMNC = false;
				for (Map.Entry<String, String> entry : adRequestDefaultParameters
						.entrySet()) {
					if (entry.getKey().equals("mcc")) {
						isMCC = true;
					}
					if (entry.getKey().equals("mnc")) {
						isMNC = true;
					}
				}
				if (!isMCC)
					adRequestDefaultParameters.put("mcc", String.valueOf(mcc));
				if (!isMNC)
					adRequestDefaultParameters.put("mnc", String.valueOf(mnc));
			}
		} catch (Exception ex) {
			logEvent("Unable to obtain mcc and mnc. Exception:" + ex,
					LogLevel.Debug);
		}

		// Don't allow these to be overridden.
		adRequestDefaultParameters.put("ua", getUserAgent());
		adRequestDefaultParameters.put("version", sdkVersion);
		adRequestDefaultParameters.put("count", "1");
		adRequestDefaultParameters.put("key", "3");
		adRequestDefaultParameters.put("zone", String.valueOf(zone));

		if (this.test) {
			adRequestDefaultParameters.put("test", "1");
		}

		if (creativeCode != null && !creativeCode.trim().equals("")) {
			adRequestDefaultParameters.put("creativecode", creativeCode);
		}

		/*
		 * If the mediation partner has defaulted, add this parameter to denote
		 * that this network has defaulted and to remove this network from
		 * auction next time. In case the source is direct send the creativeId
		 * instead of network id.
		 */
		// Clear old params if present.
		adRequestDefaultParameters
				.remove(MASTAdViewConstants.DEFAULTED_EXCREATIVES);
		adRequestDefaultParameters
				.remove(MASTAdViewConstants.DEFAULTED_PUBMATIC_EXFEEDS);
		if (mAdDescriptor != null && isLastRequestDafaulted
				&& "thirdparty".equalsIgnoreCase(mAdDescriptor.getType())) {
			if ("mediation".equals(mAdDescriptor.getMediationData()
					.getMediationSource())) {
				// For thirdparty mediation, send pubmatic_exfeed
				adRequestDefaultParameters.put(
						MASTAdViewConstants.DEFAULTED_PUBMATIC_EXFEEDS,
						mAdDescriptor.getMediationData()
								.getMediationNetworkId());
			} else {
				// For thirdparty direct, send excreatives
				adRequestDefaultParameters.put(
						MASTAdViewConstants.DEFAULTED_EXCREATIVES,
						mAdDescriptor.getAdCreativeId());
			}
		}

		addCustomParams(args);
		List<String> argValueList;
		for (Map.Entry<String, String> entry : adRequestDefaultParameters
				.entrySet()) {

			Log.d(TAG,
					"Default params : " + entry.getKey() + " : "
							+ entry.getValue());

			argValueList = args.get(entry.getKey());

			if (argValueList == null) {
				argValueList = new ArrayList<String>();
			}
			argValueList.add(entry.getValue());

			args.put(entry.getKey(), argValueList);
		}

		mMediationData = null; // Reset old mediation data
		try {
			if (mAdRequest != null)
				mAdRequest.cancel();
			mAdRequest = AdRequest.create(Defaults.NETWORK_TIMEOUT_SECONDS,
					adNetworkURL, userAgent, args, adRequestHandler);
			String requestUrl = mAdRequest.getRequestUrl();
			logEvent("Ad request:" + requestUrl, LogLevel.Debug);
		} catch (UnsupportedEncodingException e) {
			logEvent("Exception encountered while generating ad request URL:"
					+ e, LogLevel.Error);
			if (requestListener != null) {
				requestListener.onFailedToReceiveAd(this, e);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void addCustomParams(Map<String, List<String>> args) {
		args.putAll(adRequestCustomParameters);
		Set entrySet = adRequestCustomParameters.entrySet();
		Iterator it = entrySet.iterator();
		List list;
		while (it.hasNext()) {
			Map.Entry mapEntry = (Map.Entry) it.next();
			list = (List) adRequestCustomParameters.get(mapEntry.getKey());
			for (int j = 0; j < list.size(); j++) {
				if (mapEntry.getKey().equals("size_x")) {
					args.remove("size_x");
				}
				if (mapEntry.getKey().equals("size_y")) {
					args.remove("size_y");
				}
				if (mapEntry.getKey().equals("mcc")) {
					args.remove("mcc");
				}
				if (mapEntry.getKey().equals("mnc")) {
					args.remove("mnc");
				}
				if (mapEntry.getKey().equals("ua")) {
					args.remove("ua");
				}
				if (mapEntry.getKey().equals("version")) {
					args.remove("version");
				}
				if (mapEntry.getKey().equals("count")) {
					args.remove("count");
				}
				if (mapEntry.getKey().equals("key")) {
					args.remove("key");
				}
				if (mapEntry.getKey().equals("zone")) {
					args.remove("zone");
				}
			}
		}
	}

	public void showInterstitial() {
		showInterstitialWithDuration(0);
	}

	public void showInterstitialWithDuration(int durationSeconds) {
		if (isInterstitial() == false)
			throw new IllegalStateException(
					"showInterstitial requires interstitial instance");

		if (interstitialDelayFuture != null) {
			interstitialDelayFuture.cancel(true);
			interstitialDelayFuture = null;
		}

		interstitialDialog.show();

		prepareCloseButton();
		performAdTracking();

		if (durationSeconds > 0) {
			interstitialDelayFuture = Background.getExecutor().schedule(
					new Runnable() {
						@Override
						public void run() {
							closeInterstitial();
						}

					}, durationSeconds, TimeUnit.SECONDS);
		}
	}

	// main/background thread
	public void closeInterstitial() {
		if (interstitialDelayFuture != null) {
			interstitialDelayFuture.cancel(true);
			interstitialDelayFuture = null;
		}

		if (interstitialDialog != null) {
			interstitialDialog.dismiss();
		}
	}

	public ImageView getImageView() {
		if (imageView == null) {
			imageView = new ImageView(getContext());
		}

		return imageView;
	}

	public TextView getTextView() {
		if (textView == null) {
			textView = new TextView(getContext());
			textView.setGravity(Gravity.CENTER);
		}

		return textView;
	}

	public android.webkit.WebView getWebView() {
		if (webView == null) {
			webView = new WebView(getContext());
			webView.setHandler(webViewHandler);
		}

		return webView;
	}

	private void addContentView(View view, LayoutParams layoutParams) {
		switch (placementType) {
		case Inline:
			if (view.getParent() != this) {
				if (view.getParent() != null) {
					ViewGroup viewGroup = (ViewGroup) view.getParent();
					viewGroup.removeView(view);
				}

				addView(view, layoutParams);
			}
			break;

		case Interstitial:
			interstitialDialog.addView(view);
			break;
		}
	}

	// background/main thread
	private void renderAdDescriptor(final AdDescriptor adDescriptor) {
		if (adDescriptor == null)
			throw new IllegalArgumentException("adDescriptor null");

		invokeTracking = true;
		mImpressionTrackerSent = false;
		mClickTrackerSent = false;

		String adType = adDescriptor.getType();
		if (adType.startsWith("image")) {
			String img = adDescriptor.getImage();
			fetchImage(adDescriptor, img);
			return;
		}

		if (adType.startsWith("text")) {
			final String txt = adDescriptor.getText();

			runOnUiThread(new Runnable() {
				public void run() {
					renderText(adDescriptor, txt);
				}
			});

			return;
		}

		String content = adDescriptor.getContent();

		if (adType.startsWith("thirdparty")) {
			String url = adDescriptor.getURL();
			if (TextUtils.isEmpty(url) == false && url.trim().length() > 0) {
				String img = adDescriptor.getImage();
				if (TextUtils.isEmpty(img) == false && img.trim().length() > 0) {
					if (verifyThirdPartyRendering(content, url, img)) {
						fetchImage(adDescriptor, img);
						return;
					}
				}

				final String txt = adDescriptor.getText();
				if (TextUtils.isEmpty(txt) == false && txt.trim().length() > 0) {
					if (verifyThirdPartyRendering(content, url, txt)) {
						runOnUiThread(new Runnable() {
							public void run() {
								renderText(adDescriptor, txt);
							}
						});
						return;
					}
				}
			} else if (TextUtils.isEmpty(content) == false) {
				if (content.contains("client_side_external_campaign") == true) {
					mMediationData = null;
					try {
						if (requestListener != null) {
							ThirdPartyDescriptor thirdPartyDescriptor = ThirdPartyDescriptor
									.parseDescriptor(content);
							// Set MediationData as member object
							mMediationData = adDescriptor.getMediationData();
							this.mAdDescriptor = adDescriptor;
							requestListener.onReceivedThirdPartyRequest(this,
									thirdPartyDescriptor.getProperties(),
									thirdPartyDescriptor.getParams());
						}
					} catch (Exception ex) {
						logEvent(
								"Error parsing third party content descriptor.  Exception:"
										+ ex, LogLevel.Error);
					}

					return;
				}
			}
		}

		if (TextUtils.isEmpty(content)) {
			logEvent("Ad descriptor missing ad content", LogLevel.Error);

			if (requestListener != null) {
				requestListener.onFailedToReceiveAd(this, null);
			}

			return;
		}

		runOnUiThread(new Runnable() {
			public void run() {
				renderRichMedia(adDescriptor);
			}
		});
	}

	/**
	 * Call this method to get the third-party mediation data if available. <br>
	 * In case if third party mediation response is received from Mocean server
	 * (when source=mediation), then user should call this method after
	 * onReceivedThirdPartyRequest() callback, to get mediation data. This
	 * mediation data can then be used to Initialize third party SDK. <br>
	 * Please note that MediationData will be null when mediation source is
	 * direct.
	 * 
	 * @return {@link MediationData} if available, else returns null.
	 */
	public MediationData getMediationData() {
		return mMediationData;
	}

	private boolean verifyThirdPartyRendering(String content, String url,
			String imgOrText) {
		// May as well attempt to render image or text if there's no content to
		// render.
		if (TextUtils.isEmpty(content)) {
			return true;
		}

		// If there is any script content then the ad must be rendered in the
		// web view.
		if (content.contains("<script")) {
			return false;
		}

		// The content must contain both the url and the image url or text
		// content and
		// after removing the length of those pieces should be a length
		// representative
		// of simple <a and <img wrapping fluff to be validated.
		if (content.contains(url) && content.contains(imgOrText)) {
			int length = content.length();
			length -= url.length();
			length -= imgOrText.length();

			if (length < Defaults.DESCRIPTOR_THIRD_PARTY_VALIDATOR_LENGTH) {
				return true;
			}
		}

		return false;
	}

	// main thread
	private void performAdTracking() {
		if ((isInterstitial() == false) && (isShown() == false)) {
			return;
		}
		if (invokeTracking && (mAdDescriptor != null)) {
			invokeTracking = false;

			// String url = adDescriptor.getTrack();
			if (mAdDescriptor.getImpressionTrackers().size() > 0) {
				for (String urls : mAdDescriptor.getImpressionTrackers()) {
					AdTracking.invokeTrackingUrl(
							Defaults.NETWORK_TIMEOUT_SECONDS, urls, userAgent);
				}
			}
		}
	}

	public int getImageWidth() {
		int width = 0;
		if (mAdDescriptor != null && mAdDescriptor.getWidth() != null) {
			return Integer.parseInt(mAdDescriptor.getWidth());
		}
		return width;
	}

	public int getImageHeight() {
		int height = 0;
		if (mAdDescriptor != null && mAdDescriptor.getHeight() != null) {
			return Integer.parseInt(mAdDescriptor.getHeight());
		}
		return height;
	}

	/**
	 * Call this method whenever a client side thirdparty response is received
	 * and user have rendered ad using third-party SDK. User should call this
	 * method when successful ad load complete callback is received from third
	 * party SDK.
	 */
	public void sendImpression() {
		if (!mImpressionTrackerSent && mAdDescriptor != null
				&& "thirdparty".equals(mAdDescriptor.getType())) {
			if (mAdDescriptor.getImpressionTrackers() != null
					&& mAdDescriptor.getImpressionTrackers().size() > 0) {
				for (String url : mAdDescriptor.getImpressionTrackers()) {
					AdTracking.invokeTrackingUrl(
							Defaults.NETWORK_TIMEOUT_SECONDS, url, userAgent);
				}
			}
			mImpressionTrackerSent = true;
		}
	}

	/**
	 * Call this method whenever ad received from client side thirdparty SDK is
	 * clicked. User should call this method when ad clicked callback is
	 * received from third party SDK.
	 */
	public void sendClickTracker() {
		if (!mClickTrackerSent && mAdDescriptor != null
				&& "thirdparty".equals(mAdDescriptor.getType())) {
			if (mAdDescriptor.getClickTrackers() != null
					&& mAdDescriptor.getClickTrackers().size() > 0) {
				for (String url : mAdDescriptor.getClickTrackers()) {
					AdTracking.invokeTrackingUrl(
							Defaults.NETWORK_TIMEOUT_SECONDS, url, userAgent);
				}
			}
			mClickTrackerSent = true;
		}
	}

	// main/background thread
	private void fetchImage(final AdDescriptor adDescriptor, final String url) {
		ImageRequest.create(Defaults.NETWORK_TIMEOUT_SECONDS, url,
				getUserAgent(), true, new ImageRequest.Handler() {
					@Override
					public void imageFailed(ImageRequest request, Exception ex) {
						logEvent("Image download failure.  Exception:" + ex,
								LogLevel.Error);

						if (requestListener != null) {
							requestListener.onFailedToReceiveAd(
									MASTAdView.this, ex);
						}
					}

					@Override
					public void imageReceived(ImageRequest request,
							Object imageObject) {
						final Object finalImaegObject = imageObject;

						runOnUiThread(new Runnable() {
							public void run() {
								renderImage(adDescriptor, finalImaegObject);
							}
						});
					}
				});
	}

	// main thread
	private void renderImage(AdDescriptor adDescriptor, Object imageObject) {
		resetTextAd();
		resetRichMediaAd();

		getImageView();

		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		addContentView(imageView, layoutParams);

		if (imageObject instanceof Bitmap) {
			imageView.setImageBitmap((Bitmap) imageObject);
		} else if (imageObject instanceof GifDecoder) {
			imageView.setImageGifDecoder((GifDecoder) imageObject);
		}

		this.mAdDescriptor = adDescriptor;

		prepareCloseButton();
		performAdTracking();

		if (requestListener != null) {
			requestListener.onReceivedAd(MASTAdView.this);
		}
	}

	private void resetImageAd() {
		if (imageView != null) {
			imageView.setImageBitmap(null);
			imageView = null;
		}

		mAdDescriptor = null;
	}

	// main thread
	private void renderText(AdDescriptor adDescriptor, String text) {
		resetImageAd();
		resetRichMediaAd();

		getTextView();

		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		addContentView(textView, layoutParams);

		textView.setText(text);

		this.mAdDescriptor = adDescriptor;

		prepareCloseButton();
		performAdTracking();

		if (requestListener != null) {
			requestListener.onReceivedAd(MASTAdView.this);
		}
	}

	private void resetTextAd() {
		if (textView != null) {
			textView.setText("");
		}

		mAdDescriptor = null;
	}

	// main thread
	private void renderRichMedia(AdDescriptor adDescriptor) {
		invokeTracking = false;

		resetImageAd();
		resetTextAd();

		getWebView().stopLoading();

		LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		addContentView(webView, layoutParams);

		mraidBridgeInit = false;
		mraidBridge = new Bridge(webView, mraidBridgeHandler);

		String fragment = adDescriptor.getContent();
		webView.loadFragment(fragment, mraidBridge, this.getAdNetworkURL());

		this.mAdDescriptor = adDescriptor;

		if (requestListener != null) {
			requestListener.onReceivedAd(MASTAdView.this);
		}
	}

	@SuppressWarnings("deprecation")
	private void resetRichMediaAd() {
		if (mraidBridge != null) {
			mraidBridgeHandler.mraidClose(mraidBridge);

			if (mraidExpandDialog != null) {
				mraidExpandDialog.dismiss();
				mraidExpandDialog = null;
			}

			if (mraidResizeLayout != null) {
				ViewGroup parent = (ViewGroup) mraidResizeLayout.getParent();
				if (parent != null) {
					parent.removeView(mraidResizeLayout);
				}

				mraidResizeLayout = null;
				mraidResizeCloseArea = null;
			}

			mraidBridge = null;
		}

		if (webView != null) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
				webView.loadUrl("about:blank");
			} else {
				webView.clearView();
			}
			webView.clearHistory();
		}

		mAdDescriptor = null;
	}

	// main thread
	private void renderTwoPartExpand(String url) {
		mraidTwoPartExpand = true;

		mraidTwoPartWebView = new WebView(getContext());
		mraidTwoPartWebView.setHandler(webViewHandler);
		mraidTwoPartBridgeInit = false;
		mraidTwoPartBridge = new Bridge(mraidTwoPartWebView, mraidBridgeHandler);
		mraidTwoPartBridge.setExpandProperties(mraidBridge
				.getExpandProperties());

		mraidTwoPartWebView.loadUrl(url, mraidTwoPartBridge);

		mraidExpandDialog = new ExpandDialog(getContext());
		mraidExpandDialog.addView(mraidTwoPartWebView);
		mraidExpandDialog.show();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		performAdTracking();
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		if (visibility == View.VISIBLE) {
			performAdTracking();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		Activity activity = getActivity();
		if (activity == null)
			return;

		if (activity.isFinishing())
			return;

		if (mraidBridge != null) {
			switch (mraidBridge.getState()) {
			case Loading:
			case Hidden:
			case Default:
				break;

			case Resized:
			case Expanded:
				mraidBridgeHandler.mraidClose(mraidBridge);
				break;
			}
		}

		logEvent("Unregistering recevier", LogLevel.Debug);
		unregisterReceiver();

		super.onDetachedFromWindow();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (webView != null) {
			if (webView.getParent() == this)
				webView.layout(0, 0, getWidth(), getHeight());

			if (mraidBridge != null) {
				if ((changed == false) && webView.hasFocus())
					return;

				updateMRAIDLayoutForState(mraidBridge, mraidBridge.getState());
			}
		}

		if (imageView != null) {
			imageView.layout(0, 0, getWidth(), getHeight());
		}

		if (textView != null) {
			textView.layout(0, 0, getWidth(), getHeight());
		}

		updateOnLayout();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		super.measureChildren(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable parcelable = super.onSaveInstanceState();

		if (mAdDescriptor == null)
			return parcelable;

		return parcelable;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable parcelable) {
		super.onRestoreInstanceState(parcelable);
	}

	// background/main thread
	private void openUrl(final String url, final boolean bypassInternalBrowser) {
		if (activityListener != null) {
			if (activityListener.onOpenUrl(this, url) == true) {
				return;
			}
		}
		try {
			final String decodedURL = URLDecoder.decode(url, "UTF-8");

			runOnUiThread(new Runnable() {
				public void run() {
					if ((bypassInternalBrowser == false) && useInternalBrowser) {
						try {
							URI uri = new URI(decodedURL);
							String scheme = uri.getScheme();
							if (scheme.startsWith("http")) {
								openInternalBrowser(decodedURL);
								return;
							}
						} catch (URISyntaxException e) {
							// If it can't be parsed and validated as http/s don't
							// bother with the internal browser.
						}
					}
	
					if (activityListener != null) {
						activityListener.onLeavingApplication(MASTAdView.this);
					}
	
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(decodedURL));
					if (intentAvailable(intent)) {
						getContext().startActivity(intent);
					} else {
						logEvent(
								"Unable to start activity for browsing URL:" + decodedURL,
								LogLevel.Error);
					}
				}
			});

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	// main thread
	private void openInternalBrowser(String url) {
		if (browserDialog == null) {
			browserDialog = new BrowserDialog(getContext(), url,
					new BrowserDialog.Handler() {
						@Override
						public void browserDialogDismissed(
								BrowserDialog browserDialog) {
							if (internalBrowserListener != null) {
								internalBrowserListener
										.onInternalBrowserDismissed(MASTAdView.this);
							}
						}

						@Override
						public void browserDialogOpenUrl(
								BrowserDialog browserDialog, String url,
								boolean dismiss) {
							openUrl(url, true);

							if (dismiss) {
								browserDialog.dismiss();
							}
						}
					});
		} else {
			browserDialog.loadUrl(url);
		}

		if (browserDialog.isShowing() == false) {
			browserDialog.show();
		}

		if (internalBrowserListener != null) {
			internalBrowserListener.onInternalBrowserPresented(this);
		}
	}

	// main thread
	private void closeInternalBrowser() {
		if (browserDialog != null) {
			if (browserDialog.isShowing()) {
				browserDialog.dismiss();
			}
		}
	}

	private void initMRAIDBridge(Bridge bridge) {
		if (bridge == null)
			return;

		synchronized (bridge) {
			if ((bridge == mraidBridge) && (mraidBridgeInit == false)) {
				return;
			} else if ((bridge == mraidTwoPartBridge)
					&& (mraidTwoPartBridgeInit == false)) {
				return;
			}

			if (bridge.webView.isLoaded() == false)
				return;

			if (bridge.getState() != Consts.State.Loading)
				return;

			// Initialize the bridge.
			bridge.setPlacementType(placementType);

			setMRAIDSupportedFeatures(bridge);

			if (bridge == mraidBridge) {
				switch (placementType) {
				case Inline:
					updateMRAIDLayoutForState(bridge, State.Default);
					break;

				case Interstitial:
					updateMRAIDLayoutForState(bridge, State.Expanded);
					break;
				}

				bridge.setState(State.Default);
			} else {
				// Copy expand properties from first part since the MRAID
				// specification requires that expand properties be set
				// BEFORE invoking expand.
				bridge.setExpandProperties(mraidBridge.getExpandProperties());

				updateMRAIDLayoutForState(bridge, State.Expanded);
				bridge.setState(State.Expanded);
			}

			bridge.sendReady();
			registerReceiver();
		}
	}

	private void setMRAIDSupportedFeatures(Bridge bridge) {
		if (bridge == null)
			return;

		Boolean smsSupported = null;
		Boolean phoneSupported = null;
		Boolean calendarSupported = null;
		Boolean pictureSupported = null;

		if (featureSupportHandler != null) {
			smsSupported = featureSupportHandler.shouldSupportSMS(this);
			phoneSupported = featureSupportHandler.shouldSupportPhone(this);
			calendarSupported = featureSupportHandler
					.shouldSupportCalendar(this);
			pictureSupported = featureSupportHandler
					.shouldSupportStorePicture(this);
		}

		if (smsSupported == null) {
			smsSupported = getContext().checkCallingOrSelfPermission(
					android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
		}
		if (phoneSupported == null) {
			phoneSupported = getContext().checkCallingOrSelfPermission(
					android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
		}
		if (calendarSupported == null) {
			calendarSupported = ((getContext().checkCallingOrSelfPermission(
					android.Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) && (getContext()
					.checkCallingOrSelfPermission(
							android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED));
		}
		if (pictureSupported == null) {
			pictureSupported = getContext().checkCallingOrSelfPermission(
					android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
		}

		bridge.setSupportedFeature(Feature.SMS, smsSupported);
		bridge.setSupportedFeature(Feature.Tel, phoneSupported);
		bridge.setSupportedFeature(Feature.Calendar, calendarSupported);
		bridge.setSupportedFeature(Feature.StorePicture, pictureSupported);
		bridge.setSupportedFeature(Feature.InlineVideo, false);
	}

	private void updateMRAIDLayoutForState(Bridge bridge, State state) {
		WebView webView = this.webView;
		if (bridge == mraidTwoPartBridge)
			webView = mraidTwoPartWebView;

		boolean viewable = webView.isShown();

		DisplayMetrics displayMetrics = Resources.getSystem()
				.getDisplayMetrics();
		View rootView = getRootView();

		float defaultWidthPx = getWidth();
		float defaultHeightPx = getHeight();
		int defaultWidthDp = pxToDp(defaultWidthPx);
		int defaultHeightDp = pxToDp(defaultHeightPx);

		float currentWidthPx = webView.getWidth();
		float currentHeightPx = webView.getHeight();
		int currentWidthDp = pxToDp(currentWidthPx);
		int currentHeightDp = pxToDp(currentHeightPx);

		int[] containerScreenLocation = new int[2];
		getLocationOnScreen(containerScreenLocation);
		int containerScreenX = pxToDp(containerScreenLocation[0]);
		int containerScreenY = pxToDp(containerScreenLocation[1]);

		int[] webViewScreenLocation = new int[2];
		if ((state == State.Resized) && (mraidResizeLayout != null)) {
			RelativeLayout.LayoutParams webViewLayoutParams = (RelativeLayout.LayoutParams) webView
					.getLayoutParams();
			webViewScreenLocation[0] = webViewLayoutParams.leftMargin;
			webViewScreenLocation[1] = webViewLayoutParams.topMargin;
		} else {
			webView.getLocationOnScreen(webViewScreenLocation);
		}
		int webViewScreenX = pxToDp(webViewScreenLocation[0]);
		int webViewScreenY = pxToDp(webViewScreenLocation[1]);

		int screenWidthDp = pxToDp(displayMetrics.widthPixels);
		int screenHeightDp = pxToDp(displayMetrics.heightPixels);
		int maxWidthDp = pxToDp(rootView.getWidth());
		int maxHeightDp = pxToDp(rootView.getHeight());

		// Android fails at notifying on post-presentation so we'll use
		// the crystal ball and foresee what it should do.
		switch (state) {
		case Loading:
			break;

		case Default:
			webViewScreenX = containerScreenX;
			webViewScreenY = containerScreenY;
			currentWidthDp = defaultWidthDp;
			currentHeightDp = defaultHeightDp;
			break;

		case Hidden:
		case Resized:
			break;

		case Expanded:
			webViewScreenX = 0;
			webViewScreenY = 0;
			currentWidthDp = screenWidthDp;
			currentHeightDp = screenHeightDp;
		}

		if (placementType == PlacementType.Interstitial) {
			containerScreenX = 0;
			containerScreenY = 0;
			maxWidthDp = screenWidthDp;
			maxHeightDp = screenHeightDp;
			defaultWidthDp = screenWidthDp;
			defaultHeightDp = screenHeightDp;
			currentWidthDp = screenWidthDp;
			currentHeightDp = screenHeightDp;
		}

		bridge.setScreenSize(screenWidthDp, screenHeightDp);
		bridge.setMaxSize(maxWidthDp, maxHeightDp);
		bridge.setDefaultPosition(containerScreenX, containerScreenY,
				defaultWidthDp, defaultHeightDp);
		bridge.setCurrentPosition(webViewScreenX, webViewScreenY,
				currentWidthDp, currentHeightDp);
		bridge.setViewable(viewable);
		registerReceiver();
	}

	// main thread
	private void setMRAIDOrientation() {
		Activity activity = getActivity();
		if (activity == null)
			return;

		if (mraidOriginalOrientation == OrientationReset) {
			mraidOriginalOrientation = activity.getRequestedOrientation();
		}

		OrientationProperties orientationProperties = mraidBridge
				.getOrientationProperties();

		ForceOrientation forceOrientation = orientationProperties
				.getForceOrientation();
		switch (forceOrientation) {
		case Portrait:
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Landscape:
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		case None:
			break;
		}

		if (orientationProperties.getAllowOrientationChange() == true) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else {
			if (forceOrientation == ForceOrientation.None) {
				int currentOrientation = activity.getResources()
						.getConfiguration().orientation;

				switch (currentOrientation) {
				case Configuration.ORIENTATION_PORTRAIT:
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					break;
				case Configuration.ORIENTATION_LANDSCAPE:
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					break;
				default:
					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
					break;
				}
			}
		}
	}

	// main thread
	private void resetMRAIDOrientation() {
		Activity activity = getActivity();
		if (activity == null)
			return;

		if (mraidOriginalOrientation != OrientationReset) {
			activity.setRequestedOrientation(mraidOriginalOrientation);
		}
	}

	// main thread
	private void prepareCloseButton() {
		if (mraidExpandDialog != null) {
			mraidExpandDialog.setCloseImage(null);
		}

		if (closeButtonFuture != null) {
			closeButtonFuture.cancel(true);
			closeButtonFuture = null;
		}

		if (mraidBridge != null) {
			switch (mraidBridge.getState()) {
			case Default:
				if (placementType == PlacementType.Interstitial) {
					if (mraidBridge.getExpandProperties().useCustomClose() == false) {
						showCloseButton();
					}
					return;
				}
				break;

			case Expanded:
				// When expanded use the built in button or the custom one,
				// else
				// nothing else.
				ExpandProperties expandProperties = mraidBridge
						.getExpandProperties();
				if (mraidTwoPartExpand && mraidTwoPartBridgeInit
						&& (mraidTwoPartBridge != null)) {
					expandProperties = mraidTwoPartBridge.getExpandProperties();
				}

				if (expandProperties.useCustomClose() == false) {
					showCloseButton();
				}

				return;

			case Resized:
				// The ad creative MUST supply it's own close button.
				return;

			default:
				break;
			}
		}

		if (closeButtonDelay < 0)
			return;

		if (closeButtonDelay == 0) {
			showCloseButton();
			return;
		}

		closeButtonFuture = Background.getExecutor().schedule(new Runnable() {
			@Override
			public void run() {
				showCloseButton();
			}

		}, closeButtonDelay, TimeUnit.SECONDS);
	}

	// main thread
	@SuppressWarnings("deprecation")
	private void showCloseButton() {
		Drawable closeButtonDrawable = closeButtonCustomDrawable;

		if (closeButtonDrawable == null) {
			try {
				InputStream is = WebView.class
						.getResourceAsStream("/close_button.png");
				closeButtonDrawable = new BitmapDrawable(getResources(), is);
				// ((BitmapDrawable)
				// closeButtonDrawable).setGravity(Gravity.CENTER);
			} catch (Exception ex) {
				logEvent("Error loading built in close button.  Exception:"
						+ ex, LogLevel.Error);
			}
		}

		if (closeButtonDrawable == null)
			return;

		if (mraidBridge != null) {
			switch (mraidBridge.getState()) {
			case Loading:
			case Default:
				if (placementType == PlacementType.Interstitial) {
					interstitialDialog.setCloseImage(closeButtonDrawable);
					return;
				}
			case Hidden:
				// Like text or image ads just put the close button at the
				// top
				// of the stack
				// on the ad view and not on the webview.
				break;

			case Expanded:
				mraidExpandDialog.setCloseImage(closeButtonDrawable);
				return;

			case Resized:
				// Supporting API8 and higher.
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
					mraidResizeCloseArea.setBackground(closeButtonDrawable);
				} else {
					mraidResizeCloseArea
							.setBackgroundDrawable(closeButtonDrawable);
				}
				return;
			}
		}

		switch (placementType) {
		case Inline: {
			// TODO: Support inline close button?
			break;
		}

		case Interstitial: {
			interstitialDialog.setCloseImage(closeButtonDrawable);
			break;
		}
		}
	}

	private class OnClickHandler implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			if (((imageView != null) && (imageView.getParent() == view))
					|| ((textView != null) && (textView.getParent() == view))) {
				if ((mAdDescriptor != null)
						&& (TextUtils.isEmpty(mAdDescriptor.getURL()) == false)) {
					openUrl(mAdDescriptor.getURL(), false);
				}
			}
		}
	}

	private class WebViewHandler implements WebView.Handler {
		@Override
		public void webViewPageStarted(WebView webView) {

		}

		@Override
		public void webViewPageFinished(WebView webView) {
			if ((mraidBridge != null) && (mraidBridge.webView == webView)) {
				initMRAIDBridge(mraidBridge);
			} else if ((mraidTwoPartBridge != null)
					&& (mraidTwoPartBridge.webView == webView)) {
				initMRAIDBridge(mraidTwoPartBridge);
			}
		}

		@Override
		public void webViewReceivedError(WebView webView, int errorCode,
				String description, String failingUrl) {
			resetRichMediaAd();

			logEvent(
					"Error loading rich media ad content.  Error code:"
							+ String.valueOf(errorCode) + " Description:"
							+ description, LogLevel.Error);

			if (requestListener != null) {
				requestListener.onFailedToReceiveAd(MASTAdView.this, null);
			}

			removeContent();
		}

		@Override
		public boolean webViewshouldOverrideUrlLoading(WebView view, String url) {
			openUrl(url, false);
			return true;
		}
	}

	private class MRAIDHandler implements Bridge.Handler {
		@Override
		public void mraidInit(final Bridge bridge) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			if (bridge == mraidBridge) {
				mraidBridgeInit = true;
			} else if (bridge == mraidTwoPartBridge) {
				mraidTwoPartBridgeInit = true;
			}

			initMRAIDBridge(bridge);
		}

		@Override
		public void mraidClose(final Bridge bridge) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			if (placementType == PlacementType.Interstitial) {
				if (activityListener != null) {
					activityListener.onCloseButtonClick(MASTAdView.this);
				}

				return;
			}

			switch (bridge.getState()) {
			case Loading:
			case Hidden:
				break;

			case Default:
				// MRAID specification is weak in this case so ignoring.
				break;

			case Resized:
				runOnUiThread(new Runnable() {
					public void run() {
						if (mraidResizeLayout == null)
							return;

						ViewGroup parent = (ViewGroup) webView.getParent();
						if (parent != null) {
							parent.removeView(webView);
						}
						parent = (ViewGroup) mraidResizeLayout.getParent();
						if (parent != null) {
							parent.removeView(mraidResizeLayout);
						}
						mraidResizeLayout = null;
						mraidResizeCloseArea = null;

						LayoutParams layoutParams = new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
						addView(webView, layoutParams);

						updateMRAIDLayoutForState(bridge, State.Default);
						bridge.setState(State.Default);

						if (richMediaListener != null) {
							richMediaListener.onCollapsed(MASTAdView.this);
						}

						if (bridge == mraidBridge) {
							if (deferredUpdate)
								update();
						}
					}
				});
				break;

			case Expanded:
				if (mraidExpandDialog == null)
					return;

				mraidExpandDialog.dismiss();
				runOnUiThread(new Runnable() {
					public void run() {
						// TODO: Race condition... if the creative calls
						// close
						// this will invoke dismiss but the dismiss handler
						// will
						// also turn around and call close. One of them is
						// triggering
						// an NPE.
						if (mraidTwoPartExpand == false) {
							mraidExpandDialog.removeView(webView);
							addView(webView);
						} else {
							mraidExpandDialog.removeView(mraidTwoPartWebView);

							mraidTwoPartWebView = null;
							mraidTwoPartBridge = null;
							mraidTwoPartExpand = false;
						}

						mraidExpandDialog = null;

						// For normal or two part expand the original bridge
						// gets reset back to the default state.
						updateMRAIDLayoutForState(mraidBridge, State.Default);
						mraidBridge.setState(State.Default);

						resetMRAIDOrientation();

						if (richMediaListener != null) {
							richMediaListener.onCollapsed(MASTAdView.this);
						}

						if (bridge == mraidBridge) {
							if (deferredUpdate)
								update();
						}
					}
				});
				break;
			}
		}

		@Override
		public void mraidOpen(final Bridge bridge, String url) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			openUrl(url, false);
		}

		@Override
		public void mraidUpdateCurrentPosition(final Bridge bridge) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			updateMRAIDLayoutForState(bridge, bridge.getState());
		}

		@Override
		public void mraidUpdatedExpandProperties(final Bridge bridge) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					prepareCloseButton();
				}
			});
		}

		@Override
		public void mraidExpand(final Bridge bridge, final String url) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			if (placementType == PlacementType.Interstitial) {
				bridge.sendErrorMessage(
						"Can not expand with placementType interstitial.",
						Consts.CommandExpand);
				return;
			}

			boolean hasUrl = false;
			if (TextUtils.isEmpty(url) == false) {
				hasUrl = true;
			}

			switch (bridge.getState()) {
			case Loading:
				if (mraidTwoPartExpand && (hasUrl == false)) {
					// This is the SDK setting the expand state when
					// initializing
					// the two part expand operation.
					break;
				}

				bridge.sendErrorMessage(
						"Can not expand while state is loading.",
						Consts.CommandExpand);
				return;

			case Hidden:
				// Expand from this state is a no-op.
				return;

			case Expanded:
				bridge.sendErrorMessage(
						"Can not expand while state is expanded.",
						Consts.CommandExpand);
				return;

			case Default:
			case Resized:
				// Expand permitted.
				break;
			}

			if (hasUrl == false) {
				// Normal expand.
				runOnUiThread(new Runnable() {
					public void run() {
						removeView(webView);

						mraidExpandDialog = new ExpandDialog(getContext());
						mraidExpandDialog.addView(webView);
						mraidExpandDialog.show();
					}
				});
			} else {
				// Two part expand.
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						renderTwoPartExpand(url);
					}
				});
			}
		}

		@Override
		public void mraidUpdatedOrientationProperties(final Bridge bridge) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			// TODO: This should only be allowed when in the expanded state.
			// Also, on the very first change of orientation the current state
			// needs to be captured and restored when the ad is collapsed.

			runOnUiThread(new Runnable() {
				public void run() {
					setMRAIDOrientation();
				}
			});
		}

		@Override
		public void mraidUpdatedResizeProperties(final Bridge bridge) {
			// Nothing to act on here (bridge has properties updated).
		}

		@Override
		public void mraidResize(final Bridge bridge) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			if (placementType == PlacementType.Interstitial) {
				bridge.sendErrorMessage(
						"Can not resize with placementType interstitial.",
						Consts.CommandResize);
				return;
			}

			switch (bridge.getState()) {
			case Loading:
			case Hidden:
			case Expanded:
				bridge.sendErrorMessage(
						"Can not resize loading, hidden or expanded.",
						Consts.CommandResize);
				return;

			case Default:
			case Resized:
				// Resize permitted.
				break;
			}

			DisplayMetrics displayMetrics = Resources.getSystem()
					.getDisplayMetrics();
			int screenWidth = pxToDp(displayMetrics.widthPixels);
			int screenHeight = pxToDp(displayMetrics.heightPixels);

			int[] currentScreenLocation = new int[2];
			getLocationOnScreen(currentScreenLocation);
			int currentX = pxToDp(currentScreenLocation[0]);
			int currentY = pxToDp(currentScreenLocation[1]);

			ResizeProperties resizeProperties = bridge.getResizeProperties();
			boolean allowOffscreen = resizeProperties.getAllowOffscreen();
			int x = currentX + resizeProperties.getOffsetX();
			int y = currentY + resizeProperties.getOffsetY();
			int width = resizeProperties.getWidth();
			int height = resizeProperties.getHeight();
			Consts.CustomClosePosition customClosePosition = resizeProperties
					.getCustomClosePosition();

			if ((width >= screenWidth) && (height >= screenHeight)) {
				bridge.sendErrorMessage(
						"Size must be smaller than the max size.",
						Consts.CommandResize);
				return;
			} else if ((width < CloseAreaSizeDp) || (height < CloseAreaSizeDp)) {
				bridge.sendErrorMessage(
						"Size must be at least the minimum close area size.",
						Consts.CommandResize);
				return;
			}

			int minX = 0;
			int minY = statusBarHeightDp();

			if (allowOffscreen == false) {
				int desiredScreenX = x;
				int desiredScreenY = y;
				int resultingScreenX = desiredScreenX;
				int resultingScreenY = desiredScreenY;

				if (width > screenWidth)
					width = screenWidth;

				if (height > screenHeight)
					height = screenHeight;

				if (desiredScreenX < minX) {
					resultingScreenX = minX;
				} else if ((desiredScreenX + width) > screenWidth) {
					double diff = desiredScreenX + width - screenWidth;
					resultingScreenX -= diff;
				}

				if (desiredScreenY < minY) {
					resultingScreenY = minY;
				} else if ((desiredScreenY + height) > screenHeight) {
					double diff = desiredScreenY + height - screenHeight;
					resultingScreenY -= diff;
				}

				double adjustedX = desiredScreenX - resultingScreenX;
				double adjustedY = desiredScreenY - resultingScreenY;
				x -= adjustedX;
				y -= adjustedY;
			}

			// Determine where the close control area will be. This MUST be on
			// screen.
			// By default it is in the top right but the ad can specify where it
			// should be.
			// The ad MUST provide the graphic for it or some other means to
			// close the resize.
			// These coordinates are relative to the container (the resized
			// view).
			int closeControlX = width - CloseAreaSizeDp;
			int closeControlY = 0;

			switch (customClosePosition) {
			case TopRight:
				// Already configured above.
				break;

			case TopCenter:
				closeControlX = width / 2 - CloseAreaSizeDp / 2;
				closeControlY = 0;
				break;

			case TopLeft:
				closeControlX = 0;
				closeControlY = 0;
				break;

			case BottomLeft:
				closeControlX = 0;
				closeControlY = height - CloseAreaSizeDp;
				break;

			case BottomCenter:
				closeControlX = width / 2 - CloseAreaSizeDp / 2;
				closeControlY = height - CloseAreaSizeDp;
				break;

			case BottomRight:
				closeControlX = width - CloseAreaSizeDp;
				closeControlY = height - CloseAreaSizeDp;
				break;

			case Center:
				closeControlX = width / 2 - CloseAreaSizeDp / 2;
				closeControlY = height / 2 - CloseAreaSizeDp / 2;
				break;
			}

			int resultingCloseControlX = x + closeControlX;
			int resultingCloseControlY = y + closeControlY;
			int resultingCloseControlR = resultingCloseControlX
					+ CloseAreaSizeDp;
			int resultingCloseControlB = resultingCloseControlY
					+ CloseAreaSizeDp;

			if ((resultingCloseControlX < minX)
					|| (resultingCloseControlY < minY)
					|| (resultingCloseControlR > screenWidth)
					|| (resultingCloseControlB > screenHeight)) {
				bridge.sendErrorMessage(
						"Resize close control must remain on screen.",
						Consts.CommandResize);
				return;
			}

			// Convert to pixel values.
			final int xPx = dpToPx(x);
			final int yPx = dpToPx(y);
			final int widthPx = dpToPx(width);
			final int heightPx = dpToPx(height);
			final int closeXPx = dpToPx(resultingCloseControlX);
			final int closeYPx = dpToPx(resultingCloseControlY);

			runOnUiThread(new Runnable() {
				public void run() {
					Activity activity = getActivity();
					ViewGroup windowDecorView = (ViewGroup) activity
							.getWindow().getDecorView();

					RelativeLayout.LayoutParams webViewLayoutParams = new RelativeLayout.LayoutParams(
							widthPx, heightPx);
					webViewLayoutParams.setMargins(xPx, yPx, Integer.MIN_VALUE,
							Integer.MIN_VALUE);

					RelativeLayout.LayoutParams closeControlLayoutParams = new RelativeLayout.LayoutParams(
							dpToPx(CloseAreaSizeDp), dpToPx(CloseAreaSizeDp));
					closeControlLayoutParams.setMargins(closeXPx, closeYPx,
							Integer.MIN_VALUE, Integer.MIN_VALUE);

					if (mraidResizeLayout == null) {
						ViewGroup webViewParent = (ViewGroup) webView
								.getParent();
						if (webViewParent != null) {
							webViewParent.removeView(webView);
						}

						mraidResizeCloseArea = new View(getContext());
						mraidResizeCloseArea.setBackgroundColor(0x00000000);
						mraidResizeCloseArea
								.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										if (v != mraidResizeCloseArea)
											return;

										mraidBridgeHandler.mraidClose(bridge);
									}
								});

						mraidResizeLayout = new RelativeLayout(getContext());
						mraidResizeLayout.addView(webView, webViewLayoutParams);
						mraidResizeLayout.addView(mraidResizeCloseArea,
								closeControlLayoutParams);

						LayoutParams resizeLayoutParams = new LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);
						windowDecorView.addView(mraidResizeLayout, 0,
								resizeLayoutParams);
						windowDecorView.bringChildToFront(mraidResizeLayout);
					} else {
						mraidResizeLayout.updateViewLayout(webView,
								webViewLayoutParams);
						mraidResizeLayout.updateViewLayout(
								mraidResizeCloseArea, closeControlLayoutParams);
					}

					updateMRAIDLayoutForState(bridge, State.Resized);
					bridge.setState(State.Resized);

					// TODO:PrepareCloseButton();

					if (richMediaListener != null) {
						richMediaListener.onResized(MASTAdView.this, new Rect(
								xPx, yPx, widthPx, heightPx));
					}
				}
			});
		}

		@Override
		public void mraidPlayVideo(final Bridge bridge, String url) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			if (richMediaListener != null) {
				if (richMediaListener.onPlayVideo(MASTAdView.this, url)) {
					return;
				}
			}

			openUrl(url, true);
		}

		@SuppressLint("SimpleDateFormat")
		@Override
		public void mraidCreateCalendarEvent(final Bridge bridge,
				String calendarEvent) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			if (featureSupportHandler != null) {
				if (featureSupportHandler.shouldAddCalendarEntry(
						MASTAdView.this, calendarEvent) == false) {
					bridge.sendErrorMessage("Access denied.",
							Consts.CommandCreateCalendarEvent);
					return;
				}
			}

			try {
				DateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mmZ");

				JSONObject jsonEvent = new JSONObject(calendarEvent);

				final Intent intent = new Intent(Intent.ACTION_EDIT);
				intent.setType("vnd.android.cursor.item/event");

				if (jsonEvent.has("start")) {
					String value = jsonEvent.getString("start");
					long time = dateFormat.parse(value).getTime();
					intent.putExtra("beginTime", time);
				}

				if (jsonEvent.has("end")) {
					String value = jsonEvent.getString("end");
					long time = dateFormat.parse(value).getTime();
					intent.putExtra("endTime", time);
				}

				if (jsonEvent.has("description")) {
					String value = jsonEvent.getString("description");
					intent.putExtra("title", value);
				}

				if (jsonEvent.has("summary")) {
					String value = jsonEvent.getString("summary");
					intent.putExtra("description", value);
				}

				if (jsonEvent.has("location")) {
					String value = jsonEvent.getString("location");
					intent.putExtra("eventLocation", value);
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (intentAvailable(intent) == true) {
							getContext().startActivity(intent);

							if (activityListener != null) {
								activityListener
										.onLeavingApplication(MASTAdView.this);
							}
						} else {
							logEvent(
									"Unable to start activity for calendary edit.",
									LogLevel.Error);
						}
					}
				});
			} catch (Exception ex) {
				bridge.sendErrorMessage("Error parsing event data.",
						Consts.CommandCreateCalendarEvent);
			}
		}

		@Override
		public void mraidStorePicture(final Bridge bridge, String url) {
			if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge))
				return;

			if (TextUtils.isEmpty(url)) {
				bridge.sendErrorMessage("Missing picture url.",
						Consts.CommandStorePicture);
				return;
			}

			if (featureSupportHandler != null) {
				if (featureSupportHandler.shouldStorePicture(MASTAdView.this,
						url) == false) {
					bridge.sendErrorMessage("Access denied.",
							Consts.CommandStorePicture);
					return;
				}
			}

			ImageRequest.create(Defaults.NETWORK_TIMEOUT_SECONDS, url,
					getUserAgent(), false, new ImageRequest.Handler() {
						@Override
						public void imageFailed(ImageRequest request,
								Exception ex) {
							bridge.sendErrorMessage(
									"Network error connecting to url.",
									Consts.CommandStorePicture);

							logEvent(
									"Error obtaining photo request to save to camera roll.  Exception:"
											+ ex, LogLevel.Error);
						}

						@Override
						public void imageReceived(ImageRequest request,
								Object imageObject) {
							// TODO: android.permission.WRITE_EXTERNAL_STORAGE
							final Bitmap bitmap = (Bitmap) imageObject;

							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									String errorMessage = "Error saving picture to device.";

									try {
										String insertedUrl = MediaStore.Images.Media
												.insertImage(getContext()
														.getContentResolver(),
														bitmap, "AdImage",
														"Image created by rich media ad.");
										if (TextUtils.isEmpty(insertedUrl)) {
											bridge.sendErrorMessage(
													errorMessage,
													Consts.CommandStorePicture);

											logEvent(errorMessage,
													LogLevel.Error);
											return;
										}

										MediaScannerConnection.scanFile(
												getContext(),
												new String[] { insertedUrl },
												null, null);
									} catch (Exception ex) {
										bridge.sendErrorMessage(errorMessage,
												Consts.CommandStorePicture);

										logEvent(errorMessage + " Exception:"
												+ ex, LogLevel.Error);
									}
								}
							});
						}
					});
		}
	}

	private class AdRequestHandler implements AdRequest.Handler {
		/*
		 * Deprecating these methods as these methods are public and may be
		 * accidentally called by the user causing unexpected behavior.
		 * 
		 * The access modifier will be changed to protected or default in the
		 * future so that the user will not be able to see these methods and
		 * will not be able to invoke these.
		 */
		@Override
		@Deprecated
		/**
		 * 
		 * @param request
		 * @param exception
		 * 
		 * @deprecated - Do not use this method. It is being used internally.
		 */
		public void adRequestFailed(AdRequest request, Exception exception) {
			if (request != mAdRequest)
				return;

			logEvent("Ad request failed: " + exception, LogLevel.Error);

			if (requestListener != null) {
				requestListener.onFailedToReceiveAd(MASTAdView.this, exception);
			}

			mAdRequest = null;
		}

		@Override
		@Deprecated
		/**
		 * 
		 * @param request
		 * @param errorCode
		 * @param errorMessage
		 * 
		 * @deprecated - Do not use this method. It is being used internally.
		 */
		public void adRequestError(AdRequest request, String errorCode,
				String errorMessage) {
			if (request != mAdRequest)
				return;

			if (requestListener != null) {
				requestListener.onFailedToReceiveAd(MASTAdView.this, null);
			}

			LogLevel logLevel = LogLevel.Error;
			if (String.valueOf(404).equals(errorCode)) {
				logLevel = LogLevel.Debug;
			}

			logEvent("Error response from server.  Error code: " + errorCode
					+ ". Error message: " + errorMessage, logLevel);

			mAdRequest = null;
		}

		@Override
		@Deprecated
		/**
		 * 
		 * @param request
		 * @param adDescriptor
		 * 
		 * @deprecated - Do not use this method. It is being used internally.
		 */
		public void adRequestCompleted(AdRequest request,
				AdDescriptor adDescriptor) {
			if (request != mAdRequest)
				return;

			renderAdDescriptor(adDescriptor);

			mAdRequest = null;
		}
	}

	private class ExpandDialog extends Dialog {
		private ViewGroup container = null;
		private ViewGroup closeArea = null;

		public ExpandDialog(Context context) {
			super(context, android.R.style.Theme_NoTitleBar_Fullscreen);

			LayoutParams layoutParams = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			container = new RelativeLayout(getContext());
			container.setBackgroundColor(0xff000000);
			setContentView(container, layoutParams);

			container.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (((imageView != null) && (imageView.getParent() == container))
							|| ((textView != null) && (textView.getParent() == container))) {
						if ((mAdDescriptor != null)
								&& (TextUtils.isEmpty(mAdDescriptor.getURL()) == false)) {
							openUrl(mAdDescriptor.getURL(), false);
						}
					}
				}
			});

			RelativeLayout.LayoutParams closeAreaLayoutParams = new RelativeLayout.LayoutParams(
					dpToPx(CloseAreaSizeDp), dpToPx(CloseAreaSizeDp));
			closeAreaLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			closeAreaLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			closeArea = new RelativeLayout(getContext());
			closeArea.setBackgroundColor(0x00000000);
			container.addView(closeArea, closeAreaLayoutParams);
			closeArea.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (activityListener != null) {
						if (activityListener
								.onCloseButtonClick(MASTAdView.this) == true) {
							return;
						}
					}

					dismiss();
				}
			});

			setOnDismissListener(new OnDismissListener() {
				// TODO: Resolve double close when ad invokes close (thus
				// causing a dismiss and another close).
				// Possibly synchronize set/get state on the bridge.

				@Override
				public void onDismiss(DialogInterface dialog) {
					if (mraidBridge != null) {
						switch (placementType) {
						case Inline:
							// TODO: What about two part?
							if (mraidBridge.getState() == State.Expanded) {
								mraidBridgeHandler.mraidClose(mraidBridge);
							}
							break;

						case Interstitial:
							mraidBridge.setState(State.Hidden);
							break;
						}
					}

					resetMRAIDOrientation();
				}
			});
		}

		protected void onStart() {
			super.onStart();

			switch (placementType) {
			case Inline:
				if (mraidTwoPartExpand == false) {
					updateMRAIDLayoutForState(mraidBridge, State.Expanded);
				}
				mraidBridge.setState(State.Expanded);
				break;

			case Interstitial:
				// if (mraidBridge != null)
				// {
				// updateMRAIDLayoutForState(mraidBridge, State.Expanded);
				// mraidBridge.setState(State.Default);
				// }
				break;
			}

			closeArea.bringToFront();

			if (mraidBridge != null) {
				if (richMediaListener != null) {
					richMediaListener.onExpanded(MASTAdView.this);
				}
			}

			prepareCloseButton();
		}

		public void onBackPressed() {
			if (this == interstitialDialog) {
				if (closeArea.getBackground() == null) {
					// Don't allow close until the close button is available.
					return;
				}

				if (activityListener != null) {
					if (activityListener.onCloseButtonClick(MASTAdView.this) == true) {
						return;
					}
				}
			}

			super.onBackPressed();
		}

		public void addView(View view) {
			if (view.getParent() != container) {
				if (view.getParent() != null) {
					ViewGroup viewGroup = (ViewGroup) view.getParent();
					viewGroup.removeView(view);
				}

				LayoutParams layoutParams = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				container.addView(view, layoutParams);
			}

			closeArea.bringToFront();
		}

		public void removeView(View view) {
			container.removeView(view);
		}

		public void removeAllViews() {
			int childCount = container.getChildCount();
			for (int i = 0; i < childCount; ++i) {
				View child = container.getChildAt(i);
				if (child != closeArea) {
					container.removeView(child);
				}
			}
		}

		public void setCloseImage(final Drawable image) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					closeArea.removeAllViews();

					if (image != null) {
						RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
								LayoutParams.MATCH_PARENT,
								LayoutParams.MATCH_PARENT);

						int marginPx = pxToDp(15);
						layoutParams.setMargins(marginPx, marginPx, marginPx,
								marginPx);

						ImageView imageView = new ImageView(getContext());
						imageView.setBackgroundColor(0x00000000);
						imageView.setImageDrawable(image);

						((RelativeLayout) closeArea).addView(imageView,
								layoutParams);
					}
				}
			});

		}

		@Override
		public void show() {
			super.show();
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

			adRequestDefaultParameters.put("lat", lat);
			adRequestDefaultParameters.put("long", lng);
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

			adRequestDefaultParameters.remove("lat");
			adRequestDefaultParameters.remove("long");

		}
	}

	private void logEvent(String event, LogLevel eventLevel) {
		if (eventLevel.ordinal() > logLevel.ordinal())
			return;

		if (logListener != null) {
			if (logListener.onLogEvent(this, event, eventLevel)) {
				return;
			}
		}

		switch (eventLevel) {
		case Debug:
			Log.d(TAG, event);
			break;
		case Error:
			Log.e(TAG, event);
			break;
		default: // No Log
			break;
		}
	}

	private final Activity getActivity() {
		Activity activity = null;

		Context context = getContext();
		if (context instanceof Activity) {
			activity = (Activity) context;
		}

		return activity;
	}

	private final boolean intentAvailable(Intent intent) {
		PackageManager packageManager = getContext().getPackageManager();
		List<ResolveInfo> resolveInfoList = packageManager
				.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
		if ((resolveInfoList != null) && (resolveInfoList.isEmpty() == false)) {
			return true;
		}

		return false;
	}

	protected final void runOnUiThread(final Runnable runnable) {
		if (runnable == null)
			return;

		Runnable uiRunnable = new Runnable() {
			public void run() {
				try {
					runnable.run();
				} catch (Exception ex) {
					logEvent("Exception during runOnUiThread:" + ex,
							LogLevel.Error);
				}
			}
		};

		Context ctx = getContext();
		if (ctx instanceof Activity) {
			Activity activity = (Activity) ctx;
			activity.runOnUiThread(uiRunnable);
		} else {
			logEvent(
					"Context not instance of Activity, unable to run on UI thread.",
					LogLevel.Error);
		}
	}

	public int statusBarHeightDp() {
		View rootView = getRootView();

		int statusBarHeightDp = 25;
		if (rootView != null) {
			int resourceId = rootView.getResources().getIdentifier(
					"status_bar_height", "dimen", "android");
			if (resourceId > 0)
				statusBarHeightDp = pxToDp(rootView.getResources()
						.getDimensionPixelSize(resourceId));
		}

		return statusBarHeightDp;
	}

	public static int pxToDp(float px) {
		DisplayMetrics displayMetrics = Resources.getSystem()
				.getDisplayMetrics();
		int dp = (int) (px / displayMetrics.density + .5f);
		return dp;
	}

	public static int dpToPx(int dp) {
		DisplayMetrics displayMetrics = Resources.getSystem()
				.getDisplayMetrics();
		int px = (int) (dp * displayMetrics.density + .5f);
		return px;
	}

	private static String getUdidFromContext(Context context) {
		String deviceId = Settings.Secure.getString(
				context.getContentResolver(), Settings.Secure.ANDROID_ID);
		deviceId = (deviceId == null) ? "" : sha1(deviceId);
		return deviceId;

	}

	@SuppressLint("DefaultLocale")
	public static String sha1(String string) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = string.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);
			bytes = digest.digest();

			for (final byte b : bytes) {
				stringBuilder.append(String.format("%02X", b));
			}

			return stringBuilder.toString().toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}
	
	public static String md5(String string) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] bytes = string.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);
			bytes = digest.digest();

			for (final byte b : bytes) {
				stringBuilder.append(String.format("%02X", b));
			}

			return stringBuilder.toString().toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}

	public static boolean isScreenOn = true;

	public class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				isScreenOn = false;
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				isScreenOn = true;
			}
			setMraidViewable(isScreenOn);
		}
	}

	public void registerReceiver() {
		if (filter == null) {
			filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
		}
		if (mReceiver == null) {
			mReceiver = new ScreenReceiver();
			getContext().registerReceiver(mReceiver, filter);
		}
	}

	public void unregisterReceiver() {
		if (mReceiver != null) {
			try {
				getContext().unregisterReceiver(mReceiver);
			} catch (Exception e) {
				logEvent(
						"Error during unregistering receiver. Receiver not registered",
						LogLevel.Error);
			}
		}
	}

	public void setMraidViewable(boolean isViewable) {
		if (mraidBridge != null) {
			Log.d("Test", "On set maraid " + isViewable);
			mraidBridge.setViewable(isViewable);
		}
	}

	public HASHING_TECHNIQUE getAidHashing() {
		return hashing;
	}

	/**
	 * Based on the given hashing type, AID value hashed & sent to the ad server. 
	 * Calling setAndroidAidEnabled(false) will not send the aid & it's hashed values.
	 * @param hashing Type of hashing to be used.
	 */
	public void setAidHashing(HASHING_TECHNIQUE hashing) {
		this.hashing = hashing;
	}
}
