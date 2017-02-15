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

package com.pubmatic.sdk.nativead;

import static com.pubmatic.sdk.common.CommonConstants.DEFAULTED_EXCREATIVES;
import static com.pubmatic.sdk.common.CommonConstants.DEFAULTED_PUBMATIC_EXFEEDS;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_IMAGE_H;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_IMAGE_W;
import static com.pubmatic.sdk.common.CommonConstants.REQUESTPARAM_ANDROID_ID_SHA1;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_DIRECT_STRING;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_MEDIATION;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_URL;
import static com.pubmatic.sdk.common.CommonConstants.TELEPHONY_MCC;
import static com.pubmatic.sdk.common.CommonConstants.TELEPHONY_MNC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.AdResponse.Renderable;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CHANNEL;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.PMLogger.LogLevel;
import com.pubmatic.sdk.common.CommonConstants.MediationNetwork;
import com.pubmatic.sdk.common.LocationDetector;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.AdTracking;
import com.pubmatic.sdk.common.network.HttpHandler;
import com.pubmatic.sdk.common.network.HttpHandler.HttpRequestListener;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.ui.BrowserDialog;
import com.pubmatic.sdk.nativead.bean.PMAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMDataAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMImageAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetRequest;

/**
 * Main class used for requesting native ads. <br> Refer Sample application for example of
 * implementation.
 */
public final class PMNativeAd {

    public interface NativeRequestListener {

        /**
         * Callback when the native ad is received.
         *
         * @param ad
         */
        public void onNativeAdReceived(PMNativeAd ad);

        /**
         * Callback when MASTNativeAd fails to get an ad. This may be due to invalid zone, network
         * connection not available, timeout, no Ad to fill at this point.
         *
         * @param ad - MASTNativeAd object which has requested for the Ad.
         * @param ex - Exception occurred.
         */
        public void onNativeAdFailed(PMNativeAd ad, Exception ex);

        /**
         * Third party ad received. The application should be expecting this and ready to render the
         * ad with the supplied configuration.
         *
         * @param ad
         * @param properties Properties of the ad request (ad network information).
         * @param parameters Parameters for the third party network (expected to be passed to that
         * network).
         */
        public void onReceivedThirdPartyRequest(PMNativeAd ad,
                Map<String, String> properties,
                Map<String, String> parameters);

        /**
         * Callback when the user clicks on the native ad. Implement your logic on view click in
         * this method.
         * <p/>
         * <B> Do not implement your own clickListener in your activity. </b>
         *
         * @param ad
         */
        public void onNativeAdClicked(PMNativeAd ad);
    }

    private Context mContext = null;

    private Map<MediationNetwork, String> mMapMediationNetworkTestDeviceIds = null;
    // Used to store the the current native response
    private NativeAdDescriptor mNativeAdDescriptor = null;
    private boolean mImpressionTrackerSent = false;
    private boolean mThirdPartyImpTrackerSent = false;
    private boolean mThirdPartyClickTrackerSent = false;
    private boolean mClickTrackerSent = false;
    private NativeRequestListener mListener = null;
    private String mUserAgent = null;
    private boolean test = false;
    private final HashMap<String, String> mAdRequestParameters;
    private BrowserDialog mBrowserDialog = null;
    // Use external system native browser by default
    private boolean mUseInternalBrowser = false;
    private MediationData mMediationData = null;
    // Android Device ID androidaid
    private boolean isAndroidaidEnabled;

    //Controller related objects
	protected AdRequest 	mAdRequest 		= null;
	protected RRFormatter 		mRRFormatter 	= null;
	
    /**
     * If false, denotes that SDK should handle the third party request with the help of adapter. If
     * true, SDK should give the control back to publisher to handle third party request.
     */
    private boolean mOverrideAdapter = true;

    // Location support
    private Location location;
    private boolean mRetrieveLocationInfo = true;

    //private NativeAdController mAdController;
    private CHANNEL mChannel;

    protected void setAdrequest(AdRequest adRequest) {
        if (adRequest == null) {
            throw new IllegalArgumentException("AdRequest object is null");
        }

        // Since banneradRequest is class is abstract now, we will always have
        // correct channel value here.
        // and hence the controller. No need for null check.
        // mAdRequest = adRequest;
        setChannel(adRequest.getChannel());
        setAdRequest(adRequest);

        //Start the location update if Publisher has enabled location detection
        if(mRetrieveLocationInfo) {
            location = LocationDetector.getInstance(mContext).getLocation();
        }

    }

    /**
     * Sets the ad network channel.
     * <p/>
     * REQUIRED - If not set updates will fail.
     *
     * @param channel Ad network channel.
     */
    private void setChannel(CHANNEL channel) {
        // If channel is changed, controller needs to be re-initialized.
        initController(channel);

        mChannel = channel;
    }

    protected void initController(CHANNEL channel) {
    	mChannel = channel;
		createDefaultAdRequest();
    }

    private boolean checkForMandatoryParams() {
    	return (mAdRequest!=null) ?
         mAdRequest.checkMandatoryParams() : false;
    }

    /**
     * @param context
     */
    public PMNativeAd(Context context) {
        mContext = context;
        mAdRequestParameters = new HashMap<String, String>();
    }

    /**
     * Sets the instance test mode. If set to test mode the instance will request test ads for the
     * configured zone.
     * <p/>
     * Warning: This should never be enabled for application releases.
     *
     * @param test true to set test mode, false to disable test mode.
     */
    public void setTest(boolean test) {
        this.test = test;
    }

    /**
     * Access for test mode state of the instance.
     *
     * @return true if the instance is set to test mode, false if test mode is disabled.
     */
    public boolean isTest() {
        return test;
    }

    public void setRequestListener(NativeRequestListener requestListener) {
        if (requestListener == null || !(requestListener instanceof NativeRequestListener)) {
            throw new IllegalArgumentException(
                    "Kindly pass the object of type NativeRequestListener in case you want to use NativeAdView." + " Implement NativeRequestListener in your activity instead of RequestListener. ");
        } else {
            mListener = requestListener;
        }
    }

    /**
     * Collection of ad request parameters. Allows setting extra network parameters.
     * <p/>
     * The SDK will set various parameters based on configuration and other options. For more
     * information see http://developer.moceanmobile.com/Mocean_Ad_Request_API.
     *
     * @return Map containing optional request parameters.
     */
    public Map<String, String> getAdRequestParameters() {
        return mAdRequestParameters;
    }

    /**
     * Allows setting of extra custom parameters to ad request. Add custom parameter (key-value).
     *
     * @see - For details of custom parameters refer http://developer.moceanmobile.com/Mocean_Ad_Request_API
     */
    public void addCustomParameter(String customParamName, String value) {
        if (mAdRequestParameters != null && customParamName != null) {
            mAdRequestParameters.put(customParamName, value);
        }
    }

    /**
     * Convenience method to add all custom parameters in one Map. Allows setting of extra custom
     * parameters to ad request.
     *
     * @param customParamMap Map containing custom parameters
     *
     * @see - For details of custom parameters refer http://developer.moceanmobile.com/Mocean_Ad_Request_API
     */
    public void addCustomParametersMap(HashMap<String, String> customParamMap) {
        if (mAdRequestParameters != null && customParamMap != null) {
            mAdRequestParameters.putAll(customParamMap);
        }
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
	 * add androidaid as request param.
	 * 
	 * @param isAndroidaidEnabled
	 */
	public void setAndroidaidEnabled(boolean isAndroidaidEnabled) {
		this.isAndroidaidEnabled = isAndroidaidEnabled;
	}

	public boolean isAndoridaidEnabled() {
		return isAndroidaidEnabled;
	}

	/**
	 * Use this method to remove test devices ids for network.
	 * 
	 * e.g. if you have set device ids using
	 * {@link PMNativeAd#addTestDeviceIdForNetwork(MediationNetwork, String)}
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
    /**
     * @param adrequest
     */
    public void execute(AdRequest adrequest) {
        setAdrequest(adrequest);

        if (checkForMandatoryParams()) {
            update();
        }
        else
        {
            fireCallback(NATIVEAD_FAILED,
                    new Exception("Mandatory parameters validation error"),
                    null);
        }

    }

    /**
     *
     */
    public void update() {
        try {
            internalUpdate(false); // Proceed to request for ad
        } catch (IllegalArgumentException ex) {
            /*
			 * Catch the validation exception thrown from
			 * validateAssetRequest().
			 */
            ex.printStackTrace();
            PMLogger.logEvent("ERROR: Native asset validation failed. Ad requested interrupted.",
                     LogLevel.Error);
        }

    }

    /**
     * This is an internal method used to update and make a new request to the server. If defaulted
     * is true then the id of the defaulted ad network is added to the request so that this network
     * will be removed from the auction at PubMatic side.
     *
     * @param defaulted denotes whether third party ad network defaulted
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
                args.put(DEFAULTED_EXCREATIVES, mNativeAdDescriptor.getCreativeId());
                args.remove(DEFAULTED_PUBMATIC_EXFEEDS);
            } else if (RESPONSE_MEDIATION.equals(mNativeAdDescriptor.getSource())) {
                args.put(DEFAULTED_PUBMATIC_EXFEEDS, mNativeAdDescriptor.getMediationId());
                args.remove(DEFAULTED_EXCREATIVES);
            }
        }

        reset();
        initUserAgent();

        try {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator = tm.getNetworkOperator();
            if ((networkOperator != null) && (networkOperator.length() > 3)) {
                String mcc = networkOperator.substring(0, 3);
                String mnc = networkOperator.substring(3);

                args.put(TELEPHONY_MCC, String.valueOf(mcc));
                args.put(TELEPHONY_MNC, String.valueOf(mnc));
            }
        } catch (Exception ex) {
            PMLogger.logEvent("Unable to obtain mcc and mnc. Exception:" + ex, LogLevel.Debug);
        }
        // Add Device ID
        if (isAndoridaidEnabled()) {
            args.put(REQUESTPARAM_ANDROID_ID_SHA1, getUdidFromContext(mContext));
        }

        // Insert the location parameter in ad request,
        // if publisher has enabled location detection
        // and does not provid location
        if(mRetrieveLocationInfo && location != null)
            mAdRequest.setLocation(location);

        // Make a fresh adRequest
        mAdRequest.setUserAgent(getUserAgent());

        mAdRequest.createRequest(mContext);

        HttpRequest httpRequest = mRRFormatter.formatRequest(mAdRequest);

        PMLogger.logEvent("Ad request:" + httpRequest.getRequestUrl(), LogLevel.Debug);

        HttpHandler requestProcessor = new HttpHandler(networkListener, httpRequest);
        Background.getExecutor().execute(requestProcessor);

    }

    private void renderAdDescriptor(final Renderable renderable) {

        if (renderable == null) {
            throw new IllegalArgumentException("renderable is null");
        }

        final AdDescriptor adDescriptor = (AdDescriptor) renderable;

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
                        ThirdPartyDescriptor thirdPartyDescriptor = ThirdPartyDescriptor.getDescriptor(
                                mNativeAdDescriptor);

                        if (("mediation").equalsIgnoreCase(mNativeAdDescriptor.getSource())) {
                            mMediationData = new MediationData();
                            mMediationData.setMediationNetworkId(mNativeAdDescriptor.getMediationId());
                            mMediationData.setMediationNetworkName(mNativeAdDescriptor.getMediation());
                            mMediationData.setMediationSource(mNativeAdDescriptor.getSource());
                            mMediationData.setMediationAdId(mNativeAdDescriptor.getAdUnitId());
                        }

                        fireCallback(THIRDPARTY_RECEIVED, null, thirdPartyDescriptor);
                    } else {
                        PMLogger.logEvent(
                                "Error parsing third party content descriptor. No Inbuilt adapter available.",
                                LogLevel.Error);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    PMLogger.logEvent("Error parsing third party content descriptor.  Exception:" + ex,
                             LogLevel.Error);
                }
            } else {
                // If it is the native response, then pass the native response
                // Set all the properties for native ad
                fireCallback(NATIVEAD_RECEIVED, null, null);
            } // if (mNativeAdDescriptor.isTypeMediation()) ends
        } else {
            fireCallback(NATIVEAD_FAILED,
                         new Exception("Incorrect response received"),
                         null);
        }
    }

    private HttpRequestListener networkListener = new HttpRequestListener() {

        @Override
        public void onRequestComplete(HttpResponse response, String requestURL) {

            if (response != null) {

                AdResponse adData = mRRFormatter.formatResponse(response);
                if (adData.getRequest() != mAdRequest) {
                    return;
                }

                // ErrorHandling section
                String errorCode = adData.getErrorCode();
                if (errorCode != null) {

                    Exception exception = adData.getException();
                    String errorMessage = adData.getErrorMessage();

                    if (exception != null) {
                        PMLogger.logEvent("Ad request failed: " + exception, LogLevel.Error);

                    } else {
                        if (String.valueOf(404).equals(errorCode)) {

                            PMLogger.logEvent("Error response from server.  Error code: " + errorCode + ". Error message: " + errorMessage,
                                    LogLevel.Error);
                        }
                    }

                    if (mListener != null) {
                        mListener.onNativeAdFailed(PMNativeAd.this, exception);
                    }

                    return;
                }

                renderAdDescriptor(adData.getRenderable());
            }
        }

        @Override
        public void onErrorOccured(int errorType, int errorCode, String requestURL) {

            if (mAdRequest != null && requestURL != null) {
                return;
            }

            fireCallback(NATIVEAD_FAILED,
                         new Exception(errorType + ", " + errorCode),
                         null);

            LogLevel logLevel = LogLevel.Error;
            if (String.valueOf(404).equals(errorCode)) {
                logLevel = LogLevel.Debug;
            }

            PMLogger.logEvent("Error response from server.  Error code: " + errorCode + ". Error type: " + errorType,
                     logLevel);
        }

        @Override
        public boolean overrideRedirection() {
            return false;
        }

    };

    private boolean mDoRemoveListeners = false;

    /**
     * Publisher
     *
     * @param state
     */
    public void resetClickListener(final boolean state) {
        mDoRemoveListeners = state;
    }

    /**
     * Call this method whenever your view is ready to display. Calling this method will fire the Ad
     * display tracker and count impression.
     * <p/>
     * You must call this whenever the view is visible.
     *
     * @param view top level view which displays the native ads i.e. container for the native ad
     */
    public void trackViewForInteractions(View view) {
        if (view != null) {
            if (mDoRemoveListeners) {
                removeListenerFromChildViews(view);
            }
            view.setClickable(true); // Set only parent view clickable
			/*
			 * Since publisher's view is ready to display. Send our impression
			 * tracker.
			 */
            if (!mImpressionTrackerSent) {
                sendImpressions(IMPRESSION_TRACKER);
                mImpressionTrackerSent = true;
            }

            // If the adapter has served the ad, inform to track this view for
            // interactions.
			/*
			 * if (mBaseAdapter != null) {
			 * mBaseAdapter.trackViewForInteractions(view); } else
			 */
            {
				/*
				 * Set listener on this view, so that we will receive all the
				 * interactions
				 */
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        adClicked();
                        openClickUrlInBrowser();

                        fireCallback(NATIVEAD_CLICKED, null, null);
                    }
                });
            }
        }
    }

    /**
     * Traverse view and remove click listeners for all children views.
     * <p/>
     * In some 3rd party mediation SDK's, click listeners are set on all sub views. This causes
     * problem with click listener in mocean and other native ad views. To fix this, we remove click
     * listeners on all sub views and set all sub views as non-clickable.
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
     * The SDK handles the adapter loading by default in case of third party SDK wins and invokes
     * the third party SDK through adapters. The default value is false.
     * <p/>
     * Set override = true if you want to handle the invoking of the third party SDK. The SDK will
     * call {@link NativeRequestListener#onReceivedThirdPartyRequest(PMNativeAd, Map, Map)} in case
     * third party SDK wins. If the third party SDK defaults you may call {@link
     * PMNativeAd#thirdpartyPartnerDefaulted()}.
     * <p/>
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
     * Call this method if the third party partner defaults or gives the error. SDK will initiate a
     * new request eliminating the call to the defaulted partner.
     */
    public void thirdpartyPartnerDefaulted() {
        internalUpdate(true);
    }

    /**
     * Determines if location detection is enabled. If enabled, the SDK will use the location
     * services of the device to determine the device's location ad add ad request parameters
     * (lat/long) to the ad request. Location detection can be enabled with
     * setLocationDetectionEnabled() or enableLocationDetection().
     *
     * @return true if location detection is enabled, false if not
     */
    public boolean isLocationDetectionEnabled() {
        return mRetrieveLocationInfo;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Enables or disable SDK location detection. If enabled with this method the most battery
     * optimized settings are used. This method is used to disable location detection for either
     * method of enabling location detection.
     * <p/>
     * Permissions for coarse or fine location detection may be required.
     *
     * @param locationDetectionEnabled
     */
    public void setLocationDetectionEnabled(boolean locationDetectionEnabled) {
        mRetrieveLocationInfo = locationDetectionEnabled;
    }

    private Observer locationObserver = new Observer() {
        @Override
        public void update(Observable observable, Object data) {

            if(data instanceof Location) {
                location = (Location) data;
            }
        }
    };

    public void reset() {

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
     * @return List of {@link PMAssetResponse} if response contains any assets else returns null
     */
    public List<PMAssetResponse> getNativeAssets() {
        if (mNativeAdDescriptor != null && mNativeAdDescriptor.getNativeAssetList() != null && mNativeAdDescriptor
                .getNativeAssetList()
                .size() > 0) {
            return mNativeAdDescriptor.getNativeAssetList();
        }
        return null;
    }

    /**
     * Get the javascript tracker received in native response. <br> The javascript tracker is
     * represented by 'jstracker' object in OpenRTB Native ad specification.
     * <p/>
     * <b>Note:</b> If jstracker is present, publisher should execute this javascript at impression
     * time (after onNativeAdReceived callback) whenever possible.
     *
     * @return JsTracker as string if present, else returns null.
     */
    public String getJsTracker() {
        if (mNativeAdDescriptor != null && mNativeAdDescriptor.getJsTracker() != null) {
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
     * In case if third party mediation response is received from Mocean server (when
     * source=mediation), then user should call this method in onReceivedThirdPartyRequest()
     * callback, to get mediation data. This mediation data can then be used to Initialize third
     * party SDK. <br> Please note that MediationData will be null when mediation source is direct.
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
                mUserAgent = CommonConstants.DEFAULT_USER_AGENT;
            }

            webView = null;
        }
    }

    /**
     * Accessor to the User-Agent header value the SDK will send to the ad network.
     *
     * @return
     */
    public String getUserAgent() {
        return mUserAgent;
    }


    /**
     * Call this method whenever a client side thirdparty response is received and user have
     * rendered ad using third-party SDK. User should call this method when successful ad load
     * complete callback is received from third party SDK.
     */
    public void sendImpression() {
        if (!mThirdPartyImpTrackerSent && mNativeAdDescriptor != null && mNativeAdDescriptor.isTypeMediation()) {
            sendImpressions(IMPRESSION_TRACKER);
            mThirdPartyImpTrackerSent = true;
        }
    }

    /**
     * Call this method whenever ad received from client side thirdparty SDK is clicked. User should
     * call this method when ad clicked callback is received from third party SDK.
     */
    public void sendClickTracker() {
        if (!mThirdPartyClickTrackerSent && mNativeAdDescriptor != null && mNativeAdDescriptor.isTypeMediation()) {
            sendImpressions(CLICK_TRACKER);
            mThirdPartyClickTrackerSent = true;
        }
    }

    private void sendImpressions(final int trackerType) {
        if (mNativeAdDescriptor != null) {
            switch (trackerType) {
                case IMPRESSION_TRACKER:
                     AdTracking.invokeTrackingUrl(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                                                  mNativeAdDescriptor.getNativeAdImpressionTrackers(),
                                                  mUserAgent);
                    break;
                case CLICK_TRACKER:
                    AdTracking.invokeTrackingUrl(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                                                 mNativeAdDescriptor.getNativeAdClickTrackers(),
                                                 mUserAgent);
                    break;
            }
        }
    }

    private void adClicked() {
        if (!mClickTrackerSent) {
            sendImpressions(CLICK_TRACKER);

            mClickTrackerSent = true;
        }
    }

    /**
     * Controls use of the internal browser. If used, a dialog will be used to show a browser in the
     * application for ads that are clicked on (that open URLs). If not used an intent is started to
     * invoke the system browser (or whatever is configured to handle the intent).
     *
     * @param useInternalBrowser true to use the internal browser, false to not use the internal
     * browser.
     */
    public void setUseInternalBrowser(boolean useInternalBrowser) {
        this.mUseInternalBrowser = useInternalBrowser;
    }

    /**
     * Returns the currently configured internal browser setting.
     *
     * @return true if using the internal browser, false if not using the internal browser.
     */
    public boolean getUseInternalBrowser() {
        return mUseInternalBrowser;
    }

    private void openClickUrlInBrowser() {
        String url = null;
        // Open the url in the default/native browser
        if (mNativeAdDescriptor != null && (url = mNativeAdDescriptor.getClick()) != null) {
            if (mUseInternalBrowser) {

                if (mBrowserDialog == null) {
                    mBrowserDialog = new BrowserDialog(mContext, url, new BrowserDialog.Handler() {
                        @Override
                        public void browserDialogDismissed(BrowserDialog browserDialog) {
                        }

                        @Override
                        public void browserDialogOpenUrl(BrowserDialog browserDialog,
                                String url,
                                boolean dismiss) {
									/*
									 * Since internal browser is unable to
									 * handle the url, open the url in native
									 * browser.
									 */
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
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

    private void fireCallback(int callbackType,
            Exception ex,
            ThirdPartyDescriptor thirdPartyDescriptor) {
        // Check if listener is set.
        if (mListener != null) {

            switch (callbackType) {
                case NATIVEAD_RECEIVED:
                    mListener.onNativeAdReceived(this);
                    break;
                case NATIVEAD_FAILED:
                    mListener.onNativeAdFailed(this, ex);
                    break;
                case THIRDPARTY_RECEIVED:
                    if (thirdPartyDescriptor != null) {
                        mListener.onReceivedThirdPartyRequest(this,
                                                              thirdPartyDescriptor.getProperties(),
                                                              thirdPartyDescriptor.getParams());
                    } else {
                        fireCallback(NATIVEAD_FAILED, new Exception(
                                "Third party response is invalid"), thirdPartyDescriptor);
                    }
                    break;
                case NATIVEAD_CLICKED:
                    mListener.onNativeAdClicked(this);
                    break;
            }
        }
    }

//    private enum CallbackType {
//        NativeReceived, NativeFailed, ThirdPartyReceived, NativeAdClicked;
//    }
//  private enum TrackerType {
//  //IMPRESSION_TRACKER, CLICK_TRACKER;
//	}
 // constants and listeners
    private final int  NATIVEAD_RECEIVED = 10001, NATIVEAD_FAILED = 10002, THIRDPARTY_RECEIVED = 10003, NATIVEAD_CLICKED = 10004;
    
    private final int  IMPRESSION_TRACKER = 20001, CLICK_TRACKER = 2002;
    
    public static class Image {

        public Image(String url) {
            this.url = url;
        }

        Image(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        public String url = null;
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
         * Parses the JSON and returns the object of {@link Image}, null otherwise.
         *
         * @param jsonImage
         *
         * @return object of {@link Image}, else null if the parsing fails
         */
        public static Image getImage(JSONObject jsonImage) {
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


    private String getUdidFromContext(Context context) {
        String deviceId = Settings.Secure.getString(context.getContentResolver(),
                                                    Settings.Secure.ANDROID_ID);
        deviceId = (deviceId == null) ? "" : sha1(deviceId);
        return deviceId;

    }

    @SuppressLint("DefaultLocale")
    private String sha1(String string) {
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
    
    //------------ Controller related code --------------

	//TODO :: Need to verify this method
	private void createDefaultAdRequest() {

		String adRequestName = null;
		Class<?>  className     = null;
		Method m			 = null;
		try {
			

			switch (mChannel) {
				case MOCEAN:
					adRequestName = "com.pubmatic.sdk.nativead.mocean.MoceanNativeAdRequest";
					className = Class.forName(adRequestName);
					m = className.getMethod("createMoceanNativeAdRequest", Context.class, String.class);
					mAdRequest = (AdRequest)m.invoke(null, mContext, null);
					break;
				case PUBMATIC:
					adRequestName = "com.pubmatic.sdk.nativead.pubmatic.PubMaticNativeAdRequest";
					className = Class.forName(adRequestName);
					m = className.getMethod("createPubMaticNativeAdRequest", 
							Context.class, String.class, String.class, String.class);
					mAdRequest = (AdRequest)m.invoke(null, mContext, null, null, null);
					break;
		
				default:
					break;
			}
			
			createRRFormatter();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassCastException ex) {
			
		}
	}
	
	public void setAdRequest(AdRequest adRequest) {
		
		if (adRequest == null)
			throw new IllegalArgumentException("AdRequest object is null");

        adRequest.copyRequestParams(mAdRequest);
        mAdRequest = adRequest;

        //Need to call copy parameters
        //if(mAdRequest!=null)
        //   mAdRequest.copyRequestParams();

        /*
		if (adRequest instanceof NativeAdRequest) {
			((NativeAdRequest)adRequest).copyRequestParams(mAdRequest);
			mAdRequest = (NativeAdRequest)adRequest;
		} else
			throw new IllegalStateException(
					"AdRequest and channel type do not match.");
		*/
		//Create RRFormater
		createRRFormatter();
	}

	private void createRRFormatter() {
		if(mAdRequest != null)
		{
			//Create RRFormater
			String rrFormaterName = mAdRequest.getFormatter();
			
			try {
				Class<?> className = Class.forName(rrFormaterName);
				mRRFormatter = (RRFormatter) className.newInstance();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (ClassCastException ex) {
				
			}
		}
	}
    //---------------------------------------------------
}
