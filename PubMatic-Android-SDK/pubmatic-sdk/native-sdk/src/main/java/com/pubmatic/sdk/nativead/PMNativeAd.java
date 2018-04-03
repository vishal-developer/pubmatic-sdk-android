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

import static com.pubmatic.sdk.common.CommonConstants.NATIVE_IMAGE_H;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_IMAGE_W;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_URL;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.AdResponse.Renderable;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMError;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.PMLogger.PMLogLevel;
import com.pubmatic.sdk.common.LocationDetector;
import com.pubmatic.sdk.common.PMUtils;
import com.pubmatic.sdk.common.PubMaticSDK;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.AdTracking;
import com.pubmatic.sdk.common.network.HttpHandler;
import com.pubmatic.sdk.common.network.HttpHandler.HttpRequestListener;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.pubmatic.PUBDeviceInformation;
import com.pubmatic.sdk.common.ui.BrowserDialog;
import com.pubmatic.sdk.nativead.bean.PMNativeAssetResponse;

/**
 * Main class used for requesting native ads. <br> Refer Sample application for example of
 * implementation.
 */
public final class PMNativeAd {

    /**
     * An interface to notify the native ad request callbacks
     */
    public interface NativeRequestListener {

        /**
         * Callback when the native ad is received.
         *
         * @param ad
         */
        public void onNativeAdReceived(PMNativeAd ad);

        /**
         * Callback when PMNativeAd fails to get an ad. This may be due to invalid zone, network
         * connection not available, timeout, no Ad to fill at this point.
         *
         * @param ad - PMNativeAd object which has requested for the Ad.
         * @param error - Error occurred.
         */
        public void onNativeAdFailed(PMNativeAd ad, PMError error);

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

    // Used to store the the current native response
    private NativeAdDescriptor mNativeAdDescriptor = null;
    private boolean mImpressionTrackerSent = false;
    private boolean mClickTrackerSent = false;
    private NativeRequestListener mListener = null;
    private boolean test = false;
    private final HashMap<String, String> mAdRequestParameters;
    private BrowserDialog mBrowserDialog = null;
    // Use external system native browser by default
    private boolean mUseInternalBrowser = false;

    //Controller related objects
	protected AdRequest 	    mAdRequest 		= null;
	protected RRFormatter 		mRRFormatter 	= null;

    // Location support
    private LocationDetector mLocationDetector;


    protected void setAdrequest(AdRequest adRequest) {
        if (adRequest == null) {
            throw new IllegalArgumentException("AdRequest object is null");
        }

        setAdRequest(adRequest);

        //Start the location update if Publisher has enabled location detection
        if(PubMaticSDK.isLocationDetectionEnabled() && mLocationDetector==null) {
            mLocationDetector = LocationDetector.getInstance(mContext.getApplicationContext());
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLocationDetector.registerForLocationUpdates();
                }
            });
        }

    }

    private boolean checkForMandatoryParams() throws ClassCastException {
    	boolean result = (mAdRequest!=null) ?
         mAdRequest.checkMandatoryParams() : false;

        if(! (mContext instanceof Activity )) {
            result = false;
        }

        return result;
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
     * The SDK will set various parameters based on configuration and other options.
     *
     * @return Map containing optional request parameters.
     */
    public Map<String, String> getAdRequestParameters() {
        return mAdRequestParameters;
    }

    /**
     * Allows setting of extra custom parameters to ad request. Add custom parameter (key-value).
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
     */
    public void addCustomParametersMap(HashMap<String, String> customParamMap) {
        if (mAdRequestParameters != null && customParamMap != null) {
            mAdRequestParameters.putAll(customParamMap);
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
    public void loadRequest(AdRequest adrequest) {
        try {
            setAdrequest(adrequest);

            if (checkForMandatoryParams()) {
                update();
            } else {
                fireCallback(NATIVEAD_FAILED, new PMError(PMError.INVALID_REQUEST, "Mandatory parameters validation error"));
            }
        } catch(Exception e) {
            fireCallback(NATIVEAD_FAILED, new PMError(PMError.INTERNAL_ERROR, "Something went wrong. Please verify error : "+e.toString()));
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
                     PMLogger.PMLogLevel.Error);
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

        reset();

        // If User has provided the location set the source as user
        Location userProvidedLocation = mAdRequest.getLocation();
        if(userProvidedLocation != null) {
            userProvidedLocation.setProvider("user");
            mAdRequest.setLocation(userProvidedLocation);
        }

        // Insert the location parameter in ad request,
        // if publisher has enabled location detection
        // and does not provid location
        if(PubMaticSDK.isLocationDetectionEnabled() && mLocationDetector!=null) {
            mAdRequest.setLocation(mLocationDetector.getLocation());
        }

        // Make a fresh adRequest
        mAdRequest.setUserAgent(PUBDeviceInformation.getUserAgent(mContext));

        HttpRequest httpRequest = mRRFormatter.formatRequest(mAdRequest);

        PMLogger.logEvent("Ad request URL :" + httpRequest.getRequestUrl(), PMLogLevel.Custom);
        PMLogger.logEvent("Ad request body:" + httpRequest.getPostData(), PMLogLevel.Custom);

        if(PMUtils.isNetworkConnected(mContext)) {
            HttpHandler requestProcessor = new HttpHandler(networkListener, httpRequest);
            Background.getExecutor().execute(requestProcessor);
        } else
            fireCallback(NATIVEAD_FAILED, new PMError(PMError.NETWORK_ERROR, "Not able to get network connection"));

    }

    private void renderAdDescriptor(final Renderable renderable) {

        if (renderable == null) {
            throw new IllegalArgumentException("renderable is null");
        }

        final AdDescriptor adDescriptor = (AdDescriptor) renderable;

        if (adDescriptor != null && adDescriptor instanceof NativeAdDescriptor) {
            mNativeAdDescriptor = (NativeAdDescriptor) adDescriptor;
            // If it is the native response, then pass the native response
            // Set all the properties for native ad
            fireCallback(NATIVEAD_RECEIVED, null);
        } else {
            fireCallback(NATIVEAD_FAILED, new PMError(PMError.INTERNAL_ERROR, "Ad Descriptor is null"));
        }
    }

    /**
     * Checks whether Adresponse resulted in null or error code.
     *
     * @param adData AdResponse instance
     * @return true if valid response.
     */
    private boolean isAdResponseValid(AdResponse adData) {

        // ErrorHandling section
        if (adData == null) {
            PMLogger.logEvent("Ad Response passed is Null.", PMLogger.PMLogLevel.Error);
            return false;
        }

        PMError error = adData.getError();

        if(error != null)
            return false;
        else
            return true;
    }

    private HttpRequestListener networkListener = new HttpRequestListener() {

        @Override
        public void onRequestComplete(HttpResponse response, String requestURL) {

            if (response != null && mRRFormatter!=null) {

                AdResponse adData = mRRFormatter.formatResponse(response);
                if (adData.getRequest() != mAdRequest) {
                    fireCallback(NATIVEAD_FAILED, new PMError(PMError.INTERNAL_ERROR, "Native request mismatch error"));
                    return;
                }

                if (isAdResponseValid(adData)) {
                    renderAdDescriptor(adData.getRenderable());
                }
                else {
                    PMError error = null;
                    if(adData!=null)
                        error = adData.getError();

                    if(error==null)
                        error = new PMError(PMError.INVALID_RESPONSE, "Invalid ad response for given ad tag. Please check ad tag parameters.");
                    fireCallback(NATIVEAD_FAILED, error);
                }
            }
        }

        @Override
        public void onErrorOccured(PMError error, String requestURL) {

            fireCallback(NATIVEAD_FAILED, error);
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

                        fireCallback(NATIVEAD_CLICKED, null);
                    }
                });
            }
        }
    }

    /**
     * Traverse view and remove click listeners for all children views.
     * <p/>
     * In some 3rd party mediation SDK's, click listeners are set on all sub views. This causes
     * problem with click listener in PubMatic and other native ad views. To fix this, we remove click
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

    public void reset() {

        mNativeAdDescriptor = null;

        mImpressionTrackerSent = false;
        mClickTrackerSent = false;
    }

    public void destroy() {
        reset();

        if (mBrowserDialog != null)
        {
            mBrowserDialog.dismiss();
            mBrowserDialog = null;
        }

        if(mLocationDetector!=null) {
            mLocationDetector.unregisterForLocationUpdates();
            mLocationDetector = null;
        }

        mListener = null;
    }

    /**
     * Get the list of native assets.
     *
     * @return List of {@link PMNativeAssetResponse} if response contains any assets else returns null
     */
    public List<PMNativeAssetResponse> getNativeAssets() {
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

    // Private methods start
    private void sendImpressions(final int trackerType) {
        if (mNativeAdDescriptor != null) {
            switch (trackerType) {
                case IMPRESSION_TRACKER:
                     AdTracking.invokeTrackingUrl(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                                                  mNativeAdDescriptor.getNativeAdImpressionTrackers(),
                             PUBDeviceInformation.getUserAgent(mContext));
                    break;
                case CLICK_TRACKER:
                    AdTracking.invokeTrackingUrl(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                                                 mNativeAdDescriptor.getNativeAdClickTrackers(),
                            PUBDeviceInformation.getUserAgent(mContext));
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

        if(mNativeAdDescriptor != null)
        {
            String url = mNativeAdDescriptor.getClick();

            if(url != null)
            {
                if (mUseInternalBrowser) {

                    if (mBrowserDialog != null)
                    {
                        mBrowserDialog.dismiss();
                        mBrowserDialog = null;
                    }

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
    }

    private void fireCallback(final int callbackType,
            final PMError error) {

        // Check if listener is set.
        if (mListener != null) {

            try {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        switch (callbackType) {
                            case NATIVEAD_RECEIVED:
                                mListener.onNativeAdReceived(PMNativeAd.this);
                                break;
                            case NATIVEAD_FAILED:
                                if(error!=null)
                                    PMLogger.logEvent("Error response : " + error.toString(), PMLogLevel.Error);
                                mListener.onNativeAdFailed(PMNativeAd.this, error);
                                break;
                            case THIRDPARTY_RECEIVED:

                                break;
                            case NATIVEAD_CLICKED:
                                mListener.onNativeAdClicked(PMNativeAd.this);
                                break;
                        }
                    }
                });

            } catch (ClassCastException e) {
                mListener.onNativeAdFailed(PMNativeAd.this, new PMError(PMError.INVALID_REQUEST, "Activity context is required and passed is application context."));
            }
        }
    }

 // constants and listeners
    private static final int  NATIVEAD_RECEIVED = 10001, NATIVEAD_FAILED = 10002, THIRDPARTY_RECEIVED = 10003, NATIVEAD_CLICKED = 10004;
    
    private static final int  IMPRESSION_TRACKER = 20001, CLICK_TRACKER = 2002;
    
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

	public void setAdRequest(AdRequest adRequest) {
		
		if (adRequest == null)
			throw new IllegalArgumentException("AdRequest object is null");

        mAdRequest = adRequest;
        mAdRequest.setContext(mContext);

		//Create RRFormater
		createRRFormatter();
	}

	private void createRRFormatter() {
		if(mAdRequest != null)
		{
			//Get RRFormatter from AdRequest
            mRRFormatter = mAdRequest.getFormatter();
		}
	}
}
