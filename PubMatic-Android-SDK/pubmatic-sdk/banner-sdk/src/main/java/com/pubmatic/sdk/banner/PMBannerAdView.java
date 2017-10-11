/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2017
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
package com.pubmatic.sdk.banner;

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
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.widget.RelativeLayout;

import com.pubmatic.sdk.banner.mraid.Bridge;
import com.pubmatic.sdk.banner.mraid.Consts;
import com.pubmatic.sdk.banner.mraid.Consts.Feature;
import com.pubmatic.sdk.banner.mraid.Consts.ForceOrientation;
import com.pubmatic.sdk.banner.mraid.Consts.PlacementType;
import com.pubmatic.sdk.banner.mraid.Consts.State;
import com.pubmatic.sdk.banner.mraid.ExpandProperties;
import com.pubmatic.sdk.banner.mraid.OrientationProperties;
import com.pubmatic.sdk.banner.mraid.ResizeProperties;
import com.pubmatic.sdk.banner.mraid.WebView;
import com.pubmatic.sdk.banner.ui.ImageView;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CHANNEL;
import com.pubmatic.sdk.common.LocationDetector;
import com.pubmatic.sdk.common.PMAdRendered;
import com.pubmatic.sdk.common.PMAdSize;
import com.pubmatic.sdk.common.PMError;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.PMLogger.PMLogLevel;
import com.pubmatic.sdk.common.PMUtils;
import com.pubmatic.sdk.common.PubMaticSDK;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.ResponseGenerator;
import com.pubmatic.sdk.common.network.AdTracking;
import com.pubmatic.sdk.common.network.HttpHandler;
import com.pubmatic.sdk.common.network.HttpHandler.HttpRequestListener;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.ui.BrowserDialog;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.pubmatic.sdk.banner.BannerUtils.downloadUrl;

/**
 * View class to renders simple Banner & Mraid ads.
 */
public class PMBannerAdView extends ViewGroup implements PMAdRendered {

    /**
     * Interface for interaction with the PMBannerAdView.
     * The entire interface is optional. Some methods override default behavior and some are required to get full support for MRAID 2 ad content (saving calendar entries or pictures).
     * All messages are guaranteed to occur on the main thread. If any long running tasks are needed in reponse to any of the sent messages then they should be executed in a background thread to prevent and UI delays for the user.
     */
    public interface BannerAdViewDelegate {

        /**
         * An interface to notify the ad request callbacks.
         */
        public interface RequestListener
        {
            /**
             * Failed to receive ad content (network or other related error).
             *
             * @param adView
             * @param error, if any, encountered while attempting to reqest an ad.
             */
            public void onFailedToReceiveAd(PMBannerAdView adView, PMError error);

            /**
             * Ad received and rendered.
             *
             * @param adView
             */
            public void onReceivedAd(PMBannerAdView adView);
        }

        /**
         * An interface to notify when user is navigated from current activity
         */
        public interface ActivityListener
        {
            /**
             * Invoked when the ad will navigate to a clicked link (or rich media open).
             *
             * Applications can use this method to filter URLs being opened by the SDK.  If
             * the application will handle the URL directly it should return false from this
             * method so the SDK doesn't also act on the URL.
             *
             * Note that for rich media ads the SDK may have already resized or expanded the ad
             * and this method may be invoked when the ad invokes MRAID's open method.
             *
             * @param adView The BaseAdView instance invoking the method.
             * @param url The URL to open.
             * @return Boolean true if caller has completely handled the click event and wants
             * to skip the default SDK click processing; return false if the caller has only
             * implemented a "side-effect" such as logging, and wants the default SDK logic
             * to continue.
             */
            public boolean onOpenUrl(PMBannerAdView adView, String url);

            /**
             * Invoked when the ad will start a new system activity.
             *
             * @param adView
             */
            public void onLeavingApplication(PMBannerAdView adView);

            /**
             * Invoked when the ad receives a close button press that should be handled by
             * the application.
             *
             * This only occurs for the close button enabled with showCloseButton() or in
             * the case of a interstitial rich media ad that closes itself.  It will not be
             * sent for rich media close buttons that collapse expanded or resized ads.
             *
             * @param adView The BaseAdView instance invoking the method.
             * @return Boolean true if the caller has completely handled the click event and
             * wants to skip the default SDK click processing; return false if the caller has
             * only implemented a "side-effect" such as logging and wants the default SDK logic
             * to continue.  For BaseAdView instances that are interstitial implementations MUST
             * call close() if returning true from this method.
             */
            public boolean onCloseButtonClick(PMBannerAdView adView);
        }

        /**
         * An interface to notify the activity of internal browser.
         */
        public interface InternalBrowserListener
        {
            /**
             * Invoked when the internal browser has been presented to the user.
             *
             * @param adView
             */
            public void onInternalBrowserPresented(PMBannerAdView adView);

            /**
             * Invoked when the internal browser has been closed by the user or the SDK.
             * @param adView
             */
            public void onInternalBrowserDismissed(PMBannerAdView adView);
        }

        /**
         * An interface to notify the events of MRAID ads
         */
        public interface RichMediaListener
        {
            /**
             * Invoked when a rich media ad expands to the full screen size.
             *
             * @param adView
             */
            public void onExpanded(PMBannerAdView adView);

            /**
             * Invoked when a rich media ad is resized larger than it's default/configured size.
             *
             * @param adView
             * @param area Area of the screen used to render the resized ad.
             */
            public void onResized(PMBannerAdView adView, Rect area);

            /**
             * Invoked when a rich media ad collapses from an expanded or resized state.
             *
             * @param adView
             */
            public void onCollapsed(PMBannerAdView adView);

            /**
             * Invoked when a rich media ad requests a video to be played.
             * <p>
             * If false is returned the URL is handled like any other URL action and ActivityListener.onOpenUrl()
             * will be invoked for further processing.
             *
             * @param adView
             * @param url
             * @return true to indicate the video playing has been handled by the application, false to
             * allow the SDK to handle the URL.
             */
            public boolean onPlayVideo(PMBannerAdView adView, String url);

            /**
             * Invoked after a rich media (MRAID) event has occurred.  Since the event has already been handled
             * applications need not implement any behavior.  However, applications can use this to listen and act
             * on handled rich media events with other behavior.
             *
             * @param adView
             * @param request
             */
            public void onEventProcessed(PMBannerAdView adView, String request);
        }

        /**
         * Interface allowing application developers to control which device features are exposed to rich media
         * ads. By default the SDK considers hardware availability and OS level permissions to determine which
         * features should be reported as available to rich media ads. Using this interface, the application
         * can override this process and force features to be reported as available, or not.
         */
        public interface FeatureSupportHandler
        {
            /**
             * Should sending SMS messages be reported as a supported feature?
             * @return Boolean true if this feature should be reported as a supported feature,
             * Boolean false if it should not, or NULL if the normal SDK hardware/permission check
             * should be performed.
             */
            public Boolean shouldSupportSMS(PMBannerAdView adView);

            /**
             * Should placing phone calls be reported as a supported feature?
             * @return Boolean true if this feature should be reported as a supported feature,
             * Boolean false if it should not, or NULL if the normal SDK hardware/permission check
             * should be performed.
             */
            public Boolean shouldSupportPhone(PMBannerAdView adView);

            /**
             * Should creating calendar entries by reported as a supported feature?
             * @return Boolean true if this feature should be reported as a supported feature,
             * Boolean false if it should not, or NULL if the normal SDK hardware/permission check
             * should be performed.
             */
            public Boolean shouldSupportCalendar(PMBannerAdView adView);

            /**
             * Should storing pictures to the camera roll be reported as a supported feature?
             * @return Boolean true if this feature should be reported as a supported feature,
             * Boolean false if it should not, or NULL if the normal SDK hardware/permission check
             * should be performed.
             */
            public Boolean shouldSupportStorePicture(PMBannerAdView adView);

            /**
             * Invoked when an ad intends to store a picture to the device camera role. Return boolean
             * true indicating the user has approved storing the picture, or false otherwise.
             * NOTE: the application developer is responsible for displaying user dialog, and associated
             * details such as running UI code on a UI thread if needed.
             * @param sender The originating ad view where the event was triggered.
             * @param url String URL of image that will be downloaded and stored, if approved.
             * @return True to allow picture storage, false otherwise.
             */
            public boolean shouldStorePicture(PMBannerAdView sender, String url);

            /**
             * Invoked when an ad intends to create an event in the users' calendar. Return boolean
             * true indicating the user has approved creating the event, or false otherwise.
             * NOTE: the application developer is responsible for displaying user dialog, and associated
             * details such as running UI code on a UI thread if needed.
             * @param sender The originating ad view where the event was triggered.
             * @param calendarProperties Complex string describing specifics of the calendar event.
             * @return True to allow picture storage, false otherwise.
             */
            public boolean shouldAddCalendarEntry(PMBannerAdView sender, String calendarProperties);
        }
    }

    private static final String TAG = PMBannerAdView.class.getSimpleName();

    final private int CloseAreaSizeDp = 50;
    final private int OrientationReset = Short.MIN_VALUE;

    // User agent used for all requests
    private String userAgent = null;
    //No need to have location here. Can get directly from singleton.
    private Location location;

    // Configuration
    private int updateInterval = 0;
    private int viewVisibility = View.INVISIBLE;

    private boolean useInternalBrowser = false;
    private PlacementType placementType = PlacementType.Inline;

    // Ad containers (render ad content)
    private WebView webView = null;

    // Close button
    private boolean showCloseButton = false;
    private int closeButtonDelay = 0;
    private Drawable closeButtonCustomDrawable = null;
    private ScheduledFuture<?> closeButtonFuture = null;

    // Interstitial configuration
    private boolean isInterstitialReady = false;
    private boolean isInterstitialShown = false;
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
//    private boolean updateOnLayout = false;
    private boolean deferredUpdate = false;
    private BannerAdDescriptor mAdDescriptor = null;
    private ScheduledFuture<?> adUpdateIntervalFuture = null;
    private long remainingDelay = 0;

    // Tracking
    private boolean invokeTracking = false;
    private boolean mImpressionTrackerSent = false;
    private boolean mClickTrackerSent = false;

    // Internal browser
    private BrowserDialog browserDialog = null;

    // Delegates
    private BannerAdViewDelegate.ActivityListener activityListener;
    private BannerAdViewDelegate.FeatureSupportHandler featureSupportHandler;
    private BannerAdViewDelegate.InternalBrowserListener internalBrowserListener;
    private BannerAdViewDelegate.RequestListener requestListener;
    private BannerAdViewDelegate.RichMediaListener richMediaListener;

    // Receiver
    private BroadcastReceiver mReceiver;
    private IntentFilter filter;

    private AdRequest 		mAdRequest 		= null;
    private RRFormatter 	mRRFormatter 	= null;

    private CHANNEL mChannel;

    private void setAdRequest(AdRequest adRequest) throws IllegalArgumentException {
        if (adRequest == null) {
            throw new IllegalArgumentException("AdRequest object is null");
        }

        // Since banneradRequest is class is abstract now, we will always have
        // correct channel value here.
        // and hence the controller. No need for null check.
        mAdRequest      = null;
        mRRFormatter    = null;
        mAdRequest      = adRequest;
        mAdRequest.setContext(getContext());
        mChannel        = adRequest.getChannel();

        createRRFormatter();

        //Start the location update if Publisher has enabled location detection
        if(PubMaticSDK.isLocationDetectionEnabled()) {
            location = LocationDetector.getInstance(getContext()).getLocation();
            if(LocationDetector.getInstance(getContext()).hasObserver(locationObserver) == false) {
                LocationDetector.getInstance(getContext()).addObserver(locationObserver);
            }
        }
    }

    private void createRRFormatter() {
        if(mAdRequest != null && mRRFormatter==null)
        {
            //Get RRFormater from AdRequest
            mRRFormatter = mAdRequest.getFormatter();
        }
    }

    public AdRequest getAdRequest() {
        return mAdRequest;
    }

    /**
     * Used to create instances for placement in code. Only produces inline instances.
     *
     * @param context
     */
    public PMBannerAdView(Context context) {
        super(context);
        init(false);
    }

    /**
     * Used to create instances for placement in code. Produces inline or interstitial instances.
     *
     * @param context
     * @param interstitial set to true to produce interstitial instances. Interstitial instances
     * should never be added to any view group parent.
     */
    public PMBannerAdView(Context context, boolean interstitial) {
        super(context);
        init(interstitial);
    }

    /**
     * Used to create instances when placed in XML layouts. The view should be positioned like any
     * other view.
     *
     * @param context
     * @param attrs
     */
    public PMBannerAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(false);
    }

    /**
     * Used to create instances when placed in XML layouts. The view should be positioned like any
     * other view.
     *
     * @param context
     * @param attrs
     */
    public PMBannerAdView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(false);
    }

    protected void init(boolean interstitial) {
        placementType = PlacementType.Inline;

        if (interstitial) {
            placementType = PlacementType.Interstitial;
            interstitialDialog = new ExpandDialog(getContext());
        }
    }

    private boolean checkForMandatoryParams() {
        boolean result = (mAdRequest!=null) ?
                mAdRequest.checkMandatoryParams() : false;

        if(! (getContext() instanceof Activity )) {
            result = false;
        }

        return result;
    }

    private void updateOnLayout() {
//        if (updateOnLayout) {
//            updateOnLayout = false;
//
//            update();
//        }
    }

    private void initUserAgent() {
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = getWebView().getSettings().getUserAgentString();

            if (TextUtils.isEmpty(userAgent)) {
                userAgent = CommonConstants.DEFAULT_USER_AGENT;
            }
        }
    }

    /**
     * Determines if the instance is configured as interstitial.
     *
     * @return true if instance represents interstitial, false if it represents inline. Interstitial
     * instances should not be added to view layouts.
     */
    private boolean isInterstitial() {
        if (placementType == PlacementType.Interstitial) {
            return true;
        }

        return false;
    }

    boolean isInterstitialReady() {
        return isInterstitialReady;
    }

    /**
     * Sets the activity listener. This listener provides information for user ad interaction
     * events. Set to null when no longer interested in events.
     *
     * @param activityListener AdViewDelegate.ActivityListener implementation
     */
    public void setActivityListener(BannerAdViewDelegate.ActivityListener activityListener) {
        this.activityListener = activityListener;
    }

    /**
     * Returns the currently configured activity listener.
     *
     * @return AdViewDelegate.ActivityListener set with setActivityListener().
     */
    public BannerAdViewDelegate.ActivityListener getActivityListener() {
        return activityListener;
    }

    /**
     * Sets the feature support handler. This handler is used to control features of the SDK. Set to
     * override default behavior.
     *
     * @param featureSupportHandler AdViewDelegate.FeatureSupportHandler implementation
     */
    public void setFeatureSupportHandler(BannerAdViewDelegate.FeatureSupportHandler featureSupportHandler) {
        this.featureSupportHandler = featureSupportHandler;
    }

    /**
     * Returns the currently configured handler.
     *
     * @return AdViewDelegate.FeatureSupportHandler set with setFeatureSupportHandler().
     */
    public BannerAdViewDelegate.FeatureSupportHandler getFeatureSupportHandler() {
        return featureSupportHandler;
    }

    /**
     * Sets the internal browser listener. This listener provides information on internal browser
     * related events.
     *
     * @param internalBrowserListener AdViewDelegate.InternalBrowserListener implementation
     */
    public void setInternalBrowserListener(BannerAdViewDelegate.InternalBrowserListener internalBrowserListener) {
        this.internalBrowserListener = internalBrowserListener;
    }

    /**
     * Returns the currently configured listener.
     *
     * @return AdViewDelegate.InternalBrowserListener set with setInternalBrowserListener().
     */
    public BannerAdViewDelegate.InternalBrowserListener getInternalBrowserListener() {
        return internalBrowserListener;
    }

    /**
     * Sets the request listener. This listener provides information on ad update events.
     *
     * @param requestListener AdViewDelegate.RequestListener implementation
     */
    public void setRequestListener(BannerAdViewDelegate.RequestListener requestListener) {
        this.requestListener = requestListener;
    }

    /**
     * Returns the currently configured listener.
     *
     * @return AdViewDelegate.RequestListener set with setRequestListener().
     */
    public BannerAdViewDelegate.RequestListener getRequestListener() {
        return requestListener;
    }

    /**
     * Sets the rich media listener. This listener provides information on rich media events.
     *
     * @param richMediaListener AdViewDelegate.RichMediaListener implementation
     */
    public void setRichMediaListener(BannerAdViewDelegate.RichMediaListener richMediaListener) {
        this.richMediaListener = richMediaListener;
    }

    /**
     * Returns the currently configured listener.
     *
     * @return AdViewDelegate.RichMediaListener set with setRichMediaListener().
     */
    public BannerAdViewDelegate.RichMediaListener getRichMediaListener() {
        return richMediaListener;
    }

    /**
     * Sets the interval between updates. Set update interval to auto load ads after specified
     * update interval. <p/> Valid values for update interval are between 12 to 120 seconds.
     * <p/>
     * Note: Make sure to set update interval before calling loadRequest() method.
     *
     * @param updateInterval Time interval in seconds between ad requests. Valid values are between
     * 12 to 120 (both inclusive).
     */
    public void setUpdateInterval(int updateInterval) {
        if (updateInterval > 0 && updateInterval <= 12) {
            this.updateInterval = 12;
            PMLogger.logEvent(
                    "Valid update interval time is between 12 to 120 sec. Setting update interval to minimum 12 seconds",
                    PMLogger.PMLogLevel.Debug);
        } else if (updateInterval > 12 && updateInterval <= 120) {
            this.updateInterval = updateInterval;
            PMLogger.logEvent("Ad Update interval set to " + updateInterval + " seconds.",
                    PMLogger.PMLogLevel.Debug);
        } else if (updateInterval > 120) {
            this.updateInterval = 120;
            PMLogger.logEvent(
                    "Valid update interval time is between 12 to 120 sec. Setting update interval to maximum 120 seconds",
                    PMLogLevel.Debug);
        }
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
     * Sets the delay time between showing an interstitial with show() and showing the
     * close button. A value of 0 indicates the button should be shown immediately.
     *
     * @param closeButtonDelay Time interval in seconds to delay showing a close button after
     * showing interstitial ad.
     */
    void setCloseButtonDelay(int closeButtonDelay) {
        this.closeButtonDelay = closeButtonDelay;
    }

    /**
     * Returns the currently configured close button delay.
     *
     * @return Time interval in seconds to delay showing a close button after showing interstitial.
     */
    int getCloseButtonDelay() {
        return closeButtonDelay;
    }

    /**
     * Allows custom close buttons to override SDK default. If set the provided drawable will be
     * used for the close button for interstitial and rich media ads (if ad uses SDK provided close
     * button).
     *
     * @param closeButtonCustomDrawable Drawable used to override the default close button image or
     * null to use the default.
     */
    public void setCloseButtonCustomDrawable(Drawable closeButtonCustomDrawable) {
        this.closeButtonCustomDrawable = closeButtonCustomDrawable;
    }

    /**
     * Returns the currently configured close button custom drawable.
     *
     * @return Returns the custom close button drawable set with setCloseButtonCustomDrawable() or
     * null if one is not set.
     */
    public Drawable getCloseButtonCustomDrawable() {
        return closeButtonCustomDrawable;
    }

    /**
     * Controls enablement of the internal browser. If used, a dialog will be used to show a browser
     * in the application for ads that are clicked on (that open URLs). If not used an intent is
     * started to invoke the system browser (or whatever is configured to handle the intent).
     *
     * @param useInternalBrowser true to use the internal browser, false to not use the internal
     * browser.
     */
    public void setUseInternalBrowser(boolean useInternalBrowser) {
        this.useInternalBrowser = useInternalBrowser;
    }

    /**
     * Returns the currently configured internal browser setting.
     *
     * @return true if using the internal browser, false if not using the internal browser.
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

    public Location getLocation() {
        return location;
    }

    /**
     * Resets instance state to it's default (doesn't destroy configured parameters). Stops update
     * interval timer, closes internal browser if open, disables location detection.
     * <p/>
     * Invoke this method to stop any ad processing. This should be done for ads that have a update
     * time interval set with setUpdateInterval() before the owning context/activity is destroyed.
     */
    public void destroy() {
        deferredUpdate = false;
        mImpressionTrackerSent = false;
        mClickTrackerSent = false;

        removeContent();

        if (adUpdateIntervalFuture != null) {
            adUpdateIntervalFuture.cancel(true);
            adUpdateIntervalFuture = null;
        }

        closeInterstitial();

        closeInternalBrowser();
        browserDialog = null;
        unregisterReceiver();

    }

    /**
     * @param adrequest
     */
    public void loadRequest(AdRequest adrequest) {
        try {
            setAdRequest(adrequest);

            if (checkForMandatoryParams()) {
                update();
            } else {
                fireCallback(BANNER_AD_FAILED, new PMError(PMError.INVALID_REQUEST, "Missing Mandatory parameters"));
            }
        } catch(Exception e) {
            fireCallback(BANNER_AD_FAILED, new PMError(PMError.INTERNAL_ERROR, "Something went wrong. Please verify error : "+e.toString()));
        }
    }

    private void fireCallback(final int callbackType,
                              final PMError error) {

        // Check if listener is set.
        if (requestListener != null) {

            try {
                ((Activity) getContext()).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        switch (callbackType) {
                            case BANNER_AD_RECEIVED:
                                requestListener.onReceivedAd(PMBannerAdView.this);
                                break;
                            case BANNER_AD_FAILED:
                                if(error!=null)
                                    PMLogger.logEvent("Error response : " + error.toString(), PMLogLevel.Error);
                                requestListener.onFailedToReceiveAd(PMBannerAdView.this, error);
                                break;
                        }
                    }
                });

            } catch (ClassCastException e) {
                requestListener.onFailedToReceiveAd(PMBannerAdView.this, new PMError(PMError.INVALID_REQUEST, "Activity context is required and passed is application context."));
            }
        }
    }

    // constants and listeners
    private static final int  BANNER_AD_RECEIVED = 10001, BANNER_AD_FAILED = 10002;

    /**
     * Updates ad.
     *
     * Invokes update(false).
     */
    private void update() {
        update(false);
    }

    /**
     * Invokes an update which requests and if received, renders ad content replacing any previous
     * ad content. If the force parameter is set to false the update will be deferred if the user is
     * interacting with the current ad (rich media resize/expand or internal browser open). If the
     * force parameter is set to true will close any interaction with the current ad before
     * updating.
     *
     * @param force true to force an update regardless of ad state, false to defer update if needed
     */
    private void update(boolean force) {

        // This will be the case if Publisher added the view in xml & sets the
        // values from java file
        // If channel is set => adcontroller is initialized correctly
        if (mChannel == null || mAdRequest==null || mAdRequest.checkMandatoryParams()==false) {

            fireCallback(BANNER_AD_FAILED, new PMError(PMError.INVALID_REQUEST, "Missing Mandatory parameters"));
            return;
        }

        if (adUpdateIntervalFuture != null) {
            adUpdateIntervalFuture.cancel(true);
            adUpdateIntervalFuture = null;
        }

        if (updateInterval > 0) {
            startUpdateTimer(updateInterval);
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

        internalUpdate();
    }

    private synchronized void startUpdateTimer(long delay) {

        if (adUpdateIntervalFuture == null && !isInterstitial()) {
            adUpdateIntervalFuture = Background.getExecutor().scheduleAtFixedRate(new Runnable() {
                                                                                      @Override
                                                                                      public void run() {
                                                                                          internalUpdate();
                                                                                      }

                                                                                  },
                    delay,
                    updateInterval,
                    TimeUnit.SECONDS);
        }
    }

    private Observer locationObserver = new Observer() {
        @Override
        public void update(Observable observable, Object data) {

            if(data instanceof Location) {
                location = (Location) data;
            }
        }
    };

    /**
     * Removes any displayed ad content.
     */
    private void removeContent() {
        deferredUpdate = false;

        resetRichMediaAd();

        switch (placementType) {
            case Inline:
                removeAllViews();
                break;

            case Interstitial:
                closeInterstitial();
        }

        mAdRequest = null;
    }

    private void internalUpdate() {
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

        //Fetch user-agent, if publisher has not explicitly provided
        if(TextUtils.isEmpty(mAdRequest.getUserAgent())) {
            initUserAgent();
            mAdRequest.setUserAgent(userAgent);
        } else {
            //Set the publisher provided user-agent.
            // It also requied for other http calls.
            userAgent = mAdRequest.getUserAgent();
        }

        // Set common width/height param to all platforms.
        // Respective AdRequest implementation will send them as per their key name.
        if (isInterstitial()) {
            DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
            mAdRequest.setAdSize(new PMAdSize(displayMetrics.widthPixels, displayMetrics.heightPixels));
        }

        // If User has provided the location set the source as user
        // This logic will not work in refresh Ad
//        Location userProvidedLocation = mAdRequest.getLocation();
//        if(userProvidedLocation != null) {
//            userProvidedLocation.setProvider("user");
//            mAdRequest.setLocation(userProvidedLocation);
//        }

        // Insert the location parameter in ad request,
        // if publisher has enabled location detection
        if(PubMaticSDK.isLocationDetectionEnabled() && location != null) {
            mAdRequest.setLocation(location);
        }

        HttpRequest httpRequest = mRRFormatter.formatRequest(mAdRequest);

        PMLogger.logEvent("Ad request URL :" + httpRequest.getRequestUrl(), PMLogLevel.Custom);
        PMLogger.logEvent("Ad request body:" + httpRequest.getPostData(), PMLogLevel.Custom);

        if(PMUtils.isNetworkConnected(getContext())) {
            HttpHandler requestProcessor = new HttpHandler(networkListener, httpRequest);
            Background.getExecutor().execute(requestProcessor);
        } else
            fireCallback(BANNER_AD_FAILED, new PMError(PMError.NETWORK_ERROR, "Not able to get network connection"));

    }

    private HttpRequestListener networkListener = new HttpRequestListener() {

        @Override
        public void onRequestComplete(HttpResponse response, String requestURL) {

            if (response != null && mRRFormatter!=null) {

                AdResponse adData = mRRFormatter.formatResponse(response);

                if (isAdResponseValid(adData)) {
                    renderAdDescriptor(adData.getRenderable());
                } else {
                    PMError error = null;
                    if(adData!=null)
                        error = adData.getError();

                    if(error==null)
                        error = new PMError(PMError.INVALID_RESPONSE, "Invalid ad response for given ad tag. Please check ad tag parameters.");
                    fireCallback(BANNER_AD_FAILED, error);
                }
            }
        }

        @Override
        public void onErrorOccured(PMError error, String requestURL) {
            if (requestListener != null) {
                fireCallback(BANNER_AD_FAILED, error);
            }
        }

        @Override
        public boolean overrideRedirection() {
            return false;
        }

    };

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

    void showInterstitial() {
        showInterstitialWithDuration(0);
    }

    void showInterstitialWithDuration(int durationSeconds) {

        if (isInterstitial() == false) {
            PMLogger.logEvent("show requires interstitial instance", PMLogLevel.Error);
            return;
        }

        //Show the interstitial ad if it is not already shown and if it is ready to show
        if (isInterstitialReady) {
            if (!isInterstitialShown) {
                isInterstitialShown = true;

                if (interstitialDelayFuture != null) {
                    interstitialDelayFuture.cancel(true);
                    interstitialDelayFuture = null;
                }

                interstitialDialog.show();

                prepareCloseButton();
                performAdTracking();

                if (durationSeconds > 0) {
                    interstitialDelayFuture = Background.getExecutor().schedule(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        closeInterstitial();
                                                                                    }

                                                                                },
                            durationSeconds,
                            TimeUnit.SECONDS);
                }

            }else{
                PMLogger.logEvent("Ad is already shown once, please request new ad", PMLogLevel.Error);
                fireCallback(BANNER_AD_FAILED, new PMError(PMError.INTERSTITIAL_ALREADY_USED, "Ad is already shown once, please request new ad"));
            }
        }else{
            PMLogger.logEvent("Cannot present interstitial. It is not ready", PMLogLevel.Debug);
        }
    }

    // main/background thread
    void closeInterstitial() {
        if (interstitialDelayFuture != null) {
            interstitialDelayFuture.cancel(true);
            interstitialDelayFuture = null;
        }

        if (interstitialDialog != null) {
            interstitialDialog.dismiss();
        }
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
    private void renderAdDescriptor(final AdResponse.Renderable renderable) {
        if (renderable == null) {
            throw new IllegalArgumentException("renderable is null");
        }

        final BannerAdDescriptor adDescriptor = (BannerAdDescriptor) renderable;

        invokeTracking = true;
        mImpressionTrackerSent = false;
        mClickTrackerSent = false;

        runOnUiThread(new Runnable() {
            public void run() {
                renderAd(adDescriptor);
            }
        });
    }

    // main thread
    private void performAdTracking() {
        if ((isInterstitial() == false) && (isShown() == false)) {
            return;
        }

        if (invokeTracking && (mAdDescriptor != null)) {
            invokeTracking = false;
            mImpressionTrackerSent = true;

            if (mAdDescriptor.getImpressionTrackers().size() > 0) {
                for (String url : mAdDescriptor.getImpressionTrackers()) {
                    try {
                        AdTracking.invokeTrackingUrl(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                                URLDecoder.decode(url, "UTF-8"),
                                userAgent);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public int getAdWidth() {
        int width = 0;
        if (mAdDescriptor != null && mAdDescriptor.getWidth() != null) {
            return Integer.parseInt(mAdDescriptor.getWidth());
        }
        return width;
    }

    public int getAdHeight() {
        int height = 0;
        if (mAdDescriptor != null && mAdDescriptor.getHeight() != null) {
            return Integer.parseInt(mAdDescriptor.getHeight());
        }
        return height;
    }

    /**
     * Call this method whenever a client side thirdparty response is received and user have
     * rendered ad using third-party SDK. User should call this method when successful ad load
     * complete callback is received from third party SDK.
     */
    private void sendImpression() {
        if (!mImpressionTrackerSent && mAdDescriptor != null && "thirdparty".equals(mAdDescriptor.getType())) {
            if (mAdDescriptor.getImpressionTrackers() != null && mAdDescriptor.getImpressionTrackers()
                    .size() > 0) {
                for (String url : mAdDescriptor.getImpressionTrackers()) {
                    AdTracking.invokeTrackingUrl(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                            url,
                            userAgent);
                }
            }
            mImpressionTrackerSent = true;
        }
    }

    /**
     * Call this method whenever ad received from client side thirdparty SDK is clicked. User should
     * call this method when ad clicked callback is received from third party SDK.
     */
    private void sendClickTracker() {
        if (!mClickTrackerSent && mAdDescriptor != null && "thirdparty".equals(mAdDescriptor.getType())) {
            if (mAdDescriptor.getClickTrackers() != null && mAdDescriptor.getClickTrackers()
                    .size() > 0) {
                for (String url : mAdDescriptor.getClickTrackers()) {
                    AdTracking.invokeTrackingUrl(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                            url,
                            userAgent);
                }
            }
            mClickTrackerSent = true;
        }
    }

    private void performClickTracking(){
        if (!mClickTrackerSent && mAdDescriptor != null) {
            if (mAdDescriptor.getClickTrackers() != null && mAdDescriptor.getClickTrackers()
                    .size() > 0) {
                for (String url : mAdDescriptor.getClickTrackers()) {
                    AdTracking.invokeTrackingUrl(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                            url,
                            userAgent);
                }
            }
            mClickTrackerSent = true;
        }
    }

    // main thread
    private void renderAd(BannerAdDescriptor adDescriptor) {

        getWebView().stopLoading();

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        addContentView(webView, layoutParams);

        mraidBridgeInit = false;
        mraidBridge = new Bridge(webView, mraidBridgeHandler);

        String creative = adDescriptor.getContent();
        String url = "";
        if (getAdRequest() != null)
            url = getAdRequest().getRequestUrl();

        webView.loadFragment(creative, mraidBridge, url);

        this.mAdDescriptor = adDescriptor;

        notifyAdReadyEvent();
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
    private void renderTwoPartExpand(final String url) {

        try {

            AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {

                @Override
                protected String doInBackground(String... params) {
                    String resultString = null;
                    try {
                        URL urlObj = new URL(params[0]);
                        resultString = downloadUrl(urlObj);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return resultString;
                }

                @Override
                protected void onPostExecute(String resultString) {
                    super.onPostExecute(resultString);

                    mraidTwoPartExpand = true;

                    mraidTwoPartWebView = new WebView(getContext());
                    mraidTwoPartWebView.setHandler(webViewHandler);
                    mraidTwoPartBridgeInit = false;

                    mraidTwoPartBridge = new Bridge(mraidTwoPartWebView, mraidBridgeHandler);
                    mraidTwoPartBridge.setExpandProperties(mraidBridge.getExpandProperties());

                    mraidExpandDialog = new ExpandDialog(getContext());
                    mraidExpandDialog.addView(mraidTwoPartWebView);
                    mraidExpandDialog.show();

                    //If content is empty then load directly URL else inject MRAID
                    if(TextUtils.isEmpty(resultString))
                        mraidTwoPartWebView.loadUrl(url, mraidTwoPartBridge);
                    else
                        mraidTwoPartWebView.loadFragment(resultString, mraidTwoPartBridge, url);

                }
            };
            task.execute(url);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        performAdTracking();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        try {
            if (hasWindowFocus) {
                PMLogger.logEvent("Window focus gain ad is VISIBLE", PMLogger.PMLogLevel.Debug);
                setViewVisibility(View.VISIBLE);
            } else {
                PMLogger.logEvent("Window focus lost ad is INVISIBLE", PMLogger.PMLogLevel.Debug);
                setViewVisibility(View.INVISIBLE);
            }
        }catch (Exception e) {
            PMLogger.logEvent("Exception in PMBannerAdView:onWindowFocusChanged - "+e.getMessage(), PMLogLevel.Error);
        }

    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == View.VISIBLE) {
            PMLogger.logEvent("Ad view is VISIBLE", PMLogLevel.Debug);
            performAdTracking();
        } else
            PMLogger.logEvent("Ad view is INVISIBLE", PMLogLevel.Debug);

        //If Publisher enabled the update interval timer feature
        setViewVisibility(visibility);
    }

    private void setViewVisibility(int visibility) {

        if(viewVisibility==visibility)
            return;

        viewVisibility = visibility;

        //If Publisher enabled the update interval timer feature
        if(updateInterval>0) {

            if(visibility==View.VISIBLE) {
                startUpdateTimer(remainingDelay);
            }
            else if(adUpdateIntervalFuture!=null) {
                remainingDelay = adUpdateIntervalFuture.getDelay(TimeUnit.SECONDS);
                adUpdateIntervalFuture.cancel(true);
                adUpdateIntervalFuture = null;
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (activity.isFinishing()) {
            return;
        }

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

        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (webView != null) {
            if (webView.getParent() == this) {
                webView.layout(0, 0, getWidth(), getHeight());
            }

            if (mraidBridge != null) {
                if ((changed == false) && webView.hasFocus()) {
                    return;
                }

                updateMRAIDLayoutForState(mraidBridge, mraidBridge.getState());
            }
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

        if (mAdDescriptor == null) {
            return parcelable;
        }

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

        runOnUiThread(new Runnable() {
            public void run() {
                if ((bypassInternalBrowser == false) && useInternalBrowser) {
                    try {
                        URI uri = new URI(url);
                        String scheme = uri.getScheme();
                        if (scheme.startsWith("http")) {
                            openInternalBrowser(url);
                            return;
                        }
                    } catch (URISyntaxException e) {
                        // If it can't be parsed and validated as http/s don't
                        // bother with the internal browser.
                    }
                }

                if (activityListener != null) {
                    activityListener.onLeavingApplication(PMBannerAdView.this);
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (intentAvailable(intent)) {
                    getContext().startActivity(intent);
                } else {
                    PMLogger.logEvent("Unable to start activity for browsing URL:" + url, PMLogger.PMLogLevel.Error);
                }
            }
        });
        performClickTracking();
    }

    // main thread
    private void openInternalBrowser(String url) {
        if (browserDialog != null)
        {
            browserDialog.dismiss();
            browserDialog = null;
        }
        browserDialog = new BrowserDialog(getContext(), url, new BrowserDialog.Handler() {
            @Override
            public void browserDialogDismissed(BrowserDialog browserDialog) {
                if (internalBrowserListener != null) {
                    internalBrowserListener.onInternalBrowserDismissed(PMBannerAdView.this);
                }
            }

            @Override
            public void browserDialogOpenUrl(BrowserDialog browserDialog,
                                             String url,
                                             boolean dismiss) {
                openUrl(url, true);

                if (dismiss) {
                    browserDialog.dismiss();
                }
            }
        });

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
        if (bridge == null) {
            return;
        }

        synchronized (bridge) {
            if ((bridge == mraidBridge) && (mraidBridgeInit == false)) {
                return;
            } else if ((bridge == mraidTwoPartBridge) && (mraidTwoPartBridgeInit == false)) {
                return;
            }

            if (bridge.webView.isLoaded() == false) {
                return;
            }

            if (bridge.getState() != Consts.State.Loading) {
                return;
            }

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
        }
    }

    private void setMRAIDSupportedFeatures(Bridge bridge) {
        if (bridge == null) {
            return;
        }

        Boolean smsSupported = null;
        Boolean phoneSupported = null;
        Boolean calendarSupported = null;
        Boolean pictureSupported = null;

        if (featureSupportHandler != null) {
            smsSupported = featureSupportHandler.shouldSupportSMS(this);
            phoneSupported = featureSupportHandler.shouldSupportPhone(this);
            calendarSupported = featureSupportHandler.shouldSupportCalendar(this);
            pictureSupported = featureSupportHandler.shouldSupportStorePicture(this);
        }

        //Commenting it as SMS, CALL, CALENDAR permissions are not required for MRAID Ads
        /*if (smsSupported == null) {
            smsSupported = getContext().checkCallingOrSelfPermission(android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
        }
        if (phoneSupported == null) {
            phoneSupported = getContext().checkCallingOrSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
        }
        if (calendarSupported == null) {
            calendarSupported = ((getContext().checkCallingOrSelfPermission(android.Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) && (getContext()
                    .checkCallingOrSelfPermission(android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED));
        }*/
        if (pictureSupported == null) {
            pictureSupported = getContext().checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        if(smsSupported!=null)
            bridge.setSupportedFeature(Feature.SMS, smsSupported);
        if(phoneSupported!=null)
            bridge.setSupportedFeature(Feature.Tel, phoneSupported);
        if(calendarSupported!=null)
            bridge.setSupportedFeature(Feature.Calendar, calendarSupported);
        if(pictureSupported!=null)
            bridge.setSupportedFeature(Feature.StorePicture, pictureSupported);
        if(smsSupported!=null)
            bridge.setSupportedFeature(Feature.InlineVideo, false);
    }

    private void updateMRAIDLayoutForState(Bridge bridge, State state) {
        WebView webView = this.webView;
        if (bridge == mraidTwoPartBridge) {
            webView = mraidTwoPartWebView;
        }

        boolean viewable = webView.isShown();

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        View rootView = getRootView();

        float defaultWidthPx = getWidth();
        float defaultHeightPx = getHeight();
        int defaultWidthDp = BannerUtils.pxToDp(defaultWidthPx);
        int defaultHeightDp = BannerUtils.pxToDp(defaultHeightPx);

        float currentWidthPx = webView.getWidth();
        float currentHeightPx = webView.getHeight();
        int currentWidthDp = BannerUtils.pxToDp(currentWidthPx);
        int currentHeightDp = BannerUtils.pxToDp(currentHeightPx);

        int[] containerScreenLocation = new int[2];
        getLocationOnScreen(containerScreenLocation);
        int containerScreenX = BannerUtils.pxToDp(containerScreenLocation[0]);
        int containerScreenY = BannerUtils.pxToDp(containerScreenLocation[1]);

        int[] webViewScreenLocation = new int[2];
        if ((state == State.Resized) && (mraidResizeLayout != null)) {
            RelativeLayout.LayoutParams webViewLayoutParams = (RelativeLayout.LayoutParams) webView.getLayoutParams();
            webViewScreenLocation[0] = webViewLayoutParams.leftMargin;
            webViewScreenLocation[1] = webViewLayoutParams.topMargin;
        } else {
            webView.getLocationOnScreen(webViewScreenLocation);
        }
        int webViewScreenX = BannerUtils.pxToDp(webViewScreenLocation[0]);
        int webViewScreenY = BannerUtils.pxToDp(webViewScreenLocation[1]);

        int screenWidthDp = BannerUtils.pxToDp(displayMetrics.widthPixels);
        int screenHeightDp = BannerUtils.pxToDp(displayMetrics.heightPixels);
        int maxWidthDp = BannerUtils.pxToDp(rootView.getWidth());
        int maxHeightDp = BannerUtils.pxToDp(rootView.getHeight());

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
        bridge.setDefaultPosition(containerScreenX,
                containerScreenY,
                defaultWidthDp,
                defaultHeightDp);
        bridge.setCurrentPosition(webViewScreenX, webViewScreenY, currentWidthDp, currentHeightDp);
        bridge.setViewable(viewable);
    }

    // main thread
    private void setMRAIDOrientation() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if (mraidOriginalOrientation == OrientationReset) {
            mraidOriginalOrientation = activity.getRequestedOrientation();
        }

        OrientationProperties orientationProperties = mraidBridge.getOrientationProperties();

        ForceOrientation forceOrientation = orientationProperties.getForceOrientation();
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
                int currentOrientation = activity.getResources().getConfiguration().orientation;

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
    @SuppressWarnings({"WrongConstant", "ResourceType"})
    private void resetMRAIDOrientation() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

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
                        if (mraidBridge.getExpandProperties().useCustomClose() == false && closeButtonDelay <= 0) {
                            showCloseButton();
                            return;
                        }
                    }
                    break;

                case Expanded:
                    // When expanded use the built in button or the custom one, else
                    // nothing else.
                    ExpandProperties expandProperties = mraidBridge.getExpandProperties();
                    if (mraidTwoPartExpand && mraidTwoPartBridgeInit && (mraidTwoPartBridge != null)) {
                        expandProperties = mraidTwoPartBridge.getExpandProperties();
                    }

                    if (expandProperties.useCustomClose() == false) {
                        showCloseButton();
                    }

                    return;

                case Resized:
                    // The ad creative MUST supply it's own close button.
                    showCloseButton();
                    return;

                default:
                    break;
            }
        }

//        if (closeButtonDelay < 0) {
//            return;
//        }

        //If close delay <= 0 then show close button immediately
        if (closeButtonDelay <= 0) {
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
                InputStream is = WebView.class.getResourceAsStream("/close_button.png");
                closeButtonDrawable = new BitmapDrawable(getResources(), is);
                // ((BitmapDrawable)
                // closeButtonDrawable).setGravity(Gravity.CENTER);
            } catch (Exception ex) {
                PMLogger.logEvent("Error loading built in close button.  Exception:" + ex, PMLogLevel.Error);
            }
        }

        if (closeButtonDrawable == null) {
            return;
        }

        final Drawable closeCustomButtonDrawable = closeButtonDrawable;

        if (mraidBridge != null) {
            switch (mraidBridge.getState()) {
                case Loading:
                case Default:
                    if (placementType == PlacementType.Interstitial) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                interstitialDialog.setCloseImage(closeCustomButtonDrawable);
                            }
                        });
                        return;
                    }
                case Hidden:
                    // Like text or image ads just put the close button at the top
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
                        mraidResizeCloseArea.setBackgroundDrawable(closeButtonDrawable);
                    }
                    return;
            }
        }

        switch (placementType) {
            case Inline: {
                // No close button on inline
                break;
            }
            case Interstitial: {
                interstitialDialog.setCloseImage(closeButtonDrawable);
                break;
            }
        }
    }

    private void notifyAdReadyEvent() {

        if(isInterstitial())
            isInterstitialReady = true;
        else
            performAdTracking();

        if (requestListener != null) {
            fireCallback(BANNER_AD_RECEIVED, null);
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
            } else if ((mraidTwoPartBridge != null) && (mraidTwoPartBridge.webView == webView)) {
                initMRAIDBridge(mraidTwoPartBridge);
            }
        }

        @Override
        public void webViewReceivedError(WebView webView,
                                         int errorCode,
                                         String description,
                                         String failingUrl) {
            PMLogger.logEvent("Error loading rich media ad content.  Error code:" + String.valueOf(errorCode) + " Description:" + description,
                    PMLogger.PMLogLevel.Error);

            if (requestListener != null) {
                fireCallback(BANNER_AD_FAILED, new PMError(PMError.RENDER_ERROR, description+" (" + errorCode+")"));
            }

            //removeContent();
        }

        @Override
        public void webViewReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && error!=null) {

                webViewReceivedError(view, error.getErrorCode(),
                        error.getDescription()!=null?error.getDescription().toString():null,
                        request!=null?request.getUrl().toString():null);
            }
        }

        @Override
        public boolean webViewShouldOverrideUrlLoading(WebView view, String url) {
            openUrl(url, false);
            return true;
        }

        @Override
        public boolean webViewShouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                openUrl(request.getUrl().toString(), false);
                return true;
            }
            return false;
        }
    }

    private class MRAIDHandler implements Bridge.Handler {
        @Override
        public void mraidInit(final Bridge bridge) {
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            if (bridge == mraidBridge) {
                mraidBridgeInit = true;
            } else if (bridge == mraidTwoPartBridge) {
                mraidTwoPartBridgeInit = true;
            }

            initMRAIDBridge(bridge);
        }

        @Override
        public void mraidClose(final Bridge bridge) {
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            if (placementType == PlacementType.Interstitial) {
                if (activityListener != null) {
                    activityListener.onCloseButtonClick(PMBannerAdView.this);
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
                            if (mraidResizeLayout == null) {
                                return;
                            }

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

                            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                                    LayoutParams.MATCH_PARENT);
                            addView(webView, layoutParams);

                            updateMRAIDLayoutForState(bridge, State.Default);
                            bridge.setState(State.Default);

                            if (richMediaListener != null) {
                                richMediaListener.onCollapsed(PMBannerAdView.this);
                            }

                            if (bridge == mraidBridge) {
                                if (deferredUpdate) {
                                    update();
                                }
                            }
                        }
                    });
                    break;

                case Expanded:
                    if (mraidExpandDialog == null) {
                        return;
                    }

                    mraidExpandDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        public void run() {
                             /* TODO: Race condition...
                             if the creative calls close this will invoke dismiss
                             but the dismiss handler will also turn around and call close.
                             One of them is triggering an NPE. */
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
                            // gets destroy back to the default state.
                            updateMRAIDLayoutForState(mraidBridge, State.Default);
                            mraidBridge.setState(State.Default);

                            resetMRAIDOrientation();

                            if (richMediaListener != null) {
                                richMediaListener.onCollapsed(PMBannerAdView.this);
                            }

                            if (bridge == mraidBridge) {
                                if (deferredUpdate) {
                                    update();
                                }
                            }
                        }
                    });
                    break;
            }
        }

        @Override
        public void mraidOpen(final Bridge bridge, String url) {
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            openUrl(url, false);
        }

        @Override
        public void mraidUpdateCurrentPosition(final Bridge bridge) {
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            updateMRAIDLayoutForState(bridge, bridge.getState());
        }

        @Override
        public void mraidUpdatedExpandProperties(final Bridge bridge) {
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    prepareCloseButton();
                }
            });
        }

        @Override
        public void mraidExpand(final Bridge bridge, final String url) {
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            if (placementType == PlacementType.Interstitial) {
                bridge.sendErrorMessage("Can not expand with placementType interstitial.",
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

                    bridge.sendErrorMessage("Can not expand while state is loading.",
                            Consts.CommandExpand);
                    return;

                case Hidden:
                    // Expand from this state is a no-op.
                    return;

                case Expanded:
                    bridge.sendErrorMessage("Can not expand while state is expanded.",
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
                renderTwoPartExpand(url);
            }
        }

        @Override
        public void mraidUpdatedOrientationProperties(final Bridge bridge) {
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

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
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            if (placementType == PlacementType.Interstitial) {
                bridge.sendErrorMessage("Can not resize with placementType interstitial.",
                        Consts.CommandResize);
                return;
            }

            switch (bridge.getState()) {
                case Loading:
                case Hidden:
                case Expanded:
                    bridge.sendErrorMessage("Can not resize loading, hidden or expanded.",
                            Consts.CommandResize);
                    return;

                case Default:
                case Resized:
                    // Resize permitted.
                    break;
            }

            DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
            int screenWidth = BannerUtils.pxToDp(displayMetrics.widthPixels);
            int screenHeight = BannerUtils.pxToDp(displayMetrics.heightPixels);

            int[] currentScreenLocation = new int[2];
            getLocationOnScreen(currentScreenLocation);
            int currentX = BannerUtils.pxToDp(currentScreenLocation[0]);
            int currentY = BannerUtils.pxToDp(currentScreenLocation[1]);

            ResizeProperties resizeProperties = bridge.getResizeProperties();
            boolean allowOffscreen = resizeProperties.getAllowOffscreen();
            int x = currentX + resizeProperties.getOffsetX();
            int y = currentY + resizeProperties.getOffsetY();
            int width = resizeProperties.getWidth();
            int height = resizeProperties.getHeight();
            Consts.CustomClosePosition customClosePosition = resizeProperties.getCustomClosePosition();

            if ((width >= screenWidth) && (height >= screenHeight)) {
                bridge.sendErrorMessage("Size must be smaller than the max size.",
                        Consts.CommandResize);
                return;
            } else if ((width < CloseAreaSizeDp) || (height < CloseAreaSizeDp)) {
                bridge.sendErrorMessage("Size must be at least the minimum close area size.",
                        Consts.CommandResize);
                return;
            }

            int minX = 0;
            int minY = statusBarHeightDp();

            if (!allowOffscreen) {
                int desiredScreenX = x;
                int desiredScreenY = y;
                int resultingScreenX = desiredScreenX;
                int resultingScreenY = desiredScreenY;

                if (width > screenWidth) {
                    width = screenWidth;
                }

                if (height > screenHeight) {
                    height = screenHeight;
                }

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
            int resultingCloseControlR = resultingCloseControlX + CloseAreaSizeDp;
            int resultingCloseControlB = resultingCloseControlY + CloseAreaSizeDp;

            if ((resultingCloseControlX < minX) || (resultingCloseControlY < minY) || (resultingCloseControlR > screenWidth) || (resultingCloseControlB > screenHeight)) {
                bridge.sendErrorMessage("Resize close control must remain on screen.",
                        Consts.CommandResize);
                return;
            }

            // Convert to pixel values.
            final int xPx = BannerUtils.dpToPx(x);
            final int yPx = BannerUtils.dpToPx(y);
            final int widthPx = BannerUtils.dpToPx(width);
            final int heightPx = BannerUtils.dpToPx(height);
            final int closeXPx = BannerUtils.dpToPx(resultingCloseControlX);
            final int closeYPx = BannerUtils.dpToPx(resultingCloseControlY);

            runOnUiThread(new Runnable() {
                public void run() {
                    Activity activity = getActivity();
                    ViewGroup windowDecorView = (ViewGroup) activity.getWindow().getDecorView();

                    RelativeLayout.LayoutParams webViewLayoutParams = new RelativeLayout.LayoutParams(
                            widthPx,
                            heightPx);
                    webViewLayoutParams.setMargins(xPx, yPx, Integer.MIN_VALUE, Integer.MIN_VALUE);

                    RelativeLayout.LayoutParams closeControlLayoutParams = new RelativeLayout.LayoutParams(
                            BannerUtils.dpToPx(CloseAreaSizeDp),
                            BannerUtils.dpToPx(CloseAreaSizeDp));
                    closeControlLayoutParams.setMargins(closeXPx,
                            closeYPx,
                            Integer.MIN_VALUE,
                            Integer.MIN_VALUE);

                    if (mraidResizeLayout == null) {
                        ViewGroup webViewParent = (ViewGroup) webView.getParent();
                        if (webViewParent != null) {
                            webViewParent.removeView(webView);
                        }

                        mraidResizeCloseArea = new View(getContext());
                        mraidResizeCloseArea.setBackgroundColor(0x00000000);
                        mraidResizeCloseArea.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (v != mraidResizeCloseArea) {
                                    return;
                                }

                                mraidBridgeHandler.mraidClose(bridge);
                            }
                        });

                        mraidResizeLayout = new RelativeLayout(getContext());
                        mraidResizeLayout.addView(webView, webViewLayoutParams);
                        mraidResizeLayout.addView(mraidResizeCloseArea, closeControlLayoutParams);

                        LayoutParams resizeLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                        windowDecorView.addView(mraidResizeLayout, 0, resizeLayoutParams);
                        windowDecorView.bringChildToFront(mraidResizeLayout);
                    } else {
                        mraidResizeLayout.updateViewLayout(webView, webViewLayoutParams);
                        mraidResizeLayout.updateViewLayout(mraidResizeCloseArea,
                                closeControlLayoutParams);
                    }

                    updateMRAIDLayoutForState(bridge, State.Resized);
                    bridge.setState(State.Resized);

                    prepareCloseButton();

                    if (richMediaListener != null) {
                        richMediaListener.onResized(PMBannerAdView.this, new Rect(xPx,
                                yPx,
                                widthPx,
                                heightPx));
                    }
                }
            });
        }

        @Override
        public void mraidPlayVideo(final Bridge bridge, String url) {
            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            if (richMediaListener != null) {
                if (richMediaListener.onPlayVideo(PMBannerAdView.this, url)) {
                    return;
                }
            }

            openUrl(url, true);
        }

        @Override
        public void mraidCreateCalendarEvent(final Bridge bridge, String calendarEvent) {
            //Send click tracker
            performClickTracking();

            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            if (featureSupportHandler != null) {
                if (featureSupportHandler.shouldAddCalendarEntry(PMBannerAdView.this,
                        calendarEvent) == false) {
                    bridge.sendErrorMessage("Access denied.", Consts.CommandCreateCalendarEvent);
                    return;
                }
            }

            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

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
                                activityListener.onLeavingApplication(PMBannerAdView.this);
                            }
                        } else {
                            PMLogger.logEvent("Unable to start activity for calendary edit.",
                                    PMLogger.PMLogLevel.Error);
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
            //Send click tracker
            performClickTracking();

            if ((bridge != mraidBridge) && (bridge != mraidTwoPartBridge)) {
                return;
            }

            if (TextUtils.isEmpty(url)) {
                bridge.sendErrorMessage("Missing picture url.", Consts.CommandStorePicture);
                return;
            }

            if (featureSupportHandler != null) {
                if (featureSupportHandler.shouldStorePicture(PMBannerAdView.this, url) == false) {
                    bridge.sendErrorMessage("Access denied.", Consts.CommandStorePicture);
                    return;
                }
            }

            ImageRequest.create(CommonConstants.NETWORK_TIMEOUT_SECONDS,
                    url,
                    userAgent,
                    false,
                    new ImageRequest.Handler() {
                        @Override
                        public void imageFailed(ImageRequest request, Exception ex) {
                            bridge.sendErrorMessage("Network error connecting to url.",
                                    Consts.CommandStorePicture);

                            PMLogger.logEvent(
                                    "Error obtaining photo request to save to camera roll.  Exception:" + ex,
                                    PMLogger.PMLogLevel.Error);
                        }

                        @Override
                        public void imageReceived(ImageRequest request,
                                                  Object imageObject) {
                            final Bitmap bitmap = (Bitmap) imageObject;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String errorMessage = "Error saving picture to device through MRAID ad.";

                                    try {
                                        boolean pictureSupported = getContext().checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                                        if (pictureSupported == true) {
                                            // It requires android.permission.WRITE_EXTERNAL_STORAGE
                                            String insertedUrl = MediaStore.Images.Media.insertImage(
                                                    getContext().getContentResolver(),
                                                    bitmap,
                                                    "AdImage",
                                                    "Image created by rich media ad.");
                                            if (TextUtils.isEmpty(insertedUrl)) {
                                                bridge.sendErrorMessage(errorMessage,
                                                        Consts.CommandStorePicture);

                                                PMLogger.logEvent(errorMessage, PMLogger.PMLogLevel.Error);
                                                return;
                                            }

                                            MediaScannerConnection.scanFile(getContext(),
                                                    new String[]{
                                                            insertedUrl},
                                                    null,
                                                    null);


                                        } else {
                                            PMLogger.logEvent(errorMessage + " WRITE_EXTERNAL_STORAGE permission is not granted. " +
                                                            "Please grant this permission to save pictures in storage. ",
                                                    PMLogLevel.Error);
                                        }
                                    } catch (Exception ex) {
                                        bridge.sendErrorMessage(errorMessage,
                                                Consts.CommandStorePicture);

                                        PMLogger.logEvent(errorMessage + " Exception:" + ex,
                                                PMLogger.PMLogLevel.Error);
                                    }
                                }
                            });
                        }
                    });
        }
    }

    private class ExpandDialog extends Dialog {
        private ViewGroup container = null;
        private ViewGroup closeArea = null;

        public ExpandDialog(Context context) {
            super(context, android.R.style.Theme_NoTitleBar_Fullscreen);

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            container = new RelativeLayout(getContext());
            container.setBackgroundColor(0xff000000);
            setContentView(container, layoutParams);

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

            if(closeArea!=null)
                closeArea.bringToFront();

            if (mraidBridge != null) {
                if (richMediaListener != null) {
                    richMediaListener.onExpanded(PMBannerAdView.this);
                }
            }

            prepareCloseButton();
        }

        public void onBackPressed() {
            if (this == interstitialDialog) {
                if (closeArea!=null && closeArea.getBackground() == null) {
                    // Don't allow close until the close button is available.
                    return;
                }

                if (activityListener != null) {
                    if (activityListener.onCloseButtonClick(PMBannerAdView.this) == true) {
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

                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);
                container.addView(view, layoutParams);
            }

            if(closeArea!=null)
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

        private void addCloseButton() {

            if(closeArea==null) {

                RelativeLayout.LayoutParams closeAreaLayoutParams = new RelativeLayout.LayoutParams(
                        BannerUtils.dpToPx(CloseAreaSizeDp),
                        BannerUtils.dpToPx(CloseAreaSizeDp));
                closeAreaLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                closeAreaLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                closeArea = new RelativeLayout(getContext());
                closeArea.setBackgroundColor(0x00000000);
                container.addView(closeArea, closeAreaLayoutParams);
                closeArea.bringToFront();
                closeArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activityListener != null) {
                            if (activityListener.onCloseButtonClick(PMBannerAdView.this) == true) {
                                return;
                            }
                        }

                        dismiss();
                    }
                });
            }
        }

        public void setCloseImage(Drawable image) {

            //Set the provided close button image
            if (image != null) {

                //First instantiate the close button area, if not already done
                addCloseButton();

                //Remove already set close button image
                if(closeArea!=null && closeArea.getChildCount()>0)
                    closeArea.removeAllViews();

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT);

                int marginPx = BannerUtils.pxToDp(15);
                layoutParams.setMargins(marginPx, marginPx, marginPx, marginPx);

                ImageView imageView = new ImageView(getContext());
                imageView.setBackgroundColor(0x00000000);
                imageView.setImageDrawable(image);

                ((RelativeLayout) closeArea).addView(imageView, layoutParams);
            }
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
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if ((resolveInfoList != null) && (resolveInfoList.isEmpty() == false)) {
            return true;
        }

        return false;
    }

    protected final void runOnUiThread(final Runnable runnable) {
        if (runnable == null) {
            return;
        }

        Runnable uiRunnable = new Runnable() {
            public void run() {
                try {
                    runnable.run();
                } catch (Exception ex) {
                    PMLogger.logEvent("Exception during runOnUiThread:" + ex, PMLogger.PMLogLevel.Error);
                }
            }
        };

        Context ctx = getContext();
        if (ctx instanceof Activity) {
            Activity activity = (Activity) ctx;
            activity.runOnUiThread(uiRunnable);
        } else {
            PMLogger.logEvent("Context not instance of Activity, unable to run on UI thread.",
                    PMLogger.PMLogLevel.Error);
        }
    }

    private int statusBarHeightDp() {
        View rootView = getRootView();

        int statusBarHeightDp = 25;
        if (rootView != null) {
            int resourceId = rootView.getResources().getIdentifier("status_bar_height",
                    "dimen",
                    "android");
            if (resourceId > 0) {
                statusBarHeightDp = BannerUtils.pxToDp(rootView.getResources()
                        .getDimensionPixelSize(resourceId));
            }
        }

        return statusBarHeightDp;
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
            getContext().unregisterReceiver(mReceiver);
        }
    }

    public void setMraidViewable(boolean isViewable) {
        if (mraidBridge != null) {
            mraidBridge.setViewable(isViewable);
        }
    }

    @Override
    public void renderPrefetchedAd(String impressionId, ResponseGenerator responseGenerator) {

        if(responseGenerator!=null && !TextUtils.isEmpty(impressionId)) {

            AdResponse pubResponse = new AdResponse();

            Map<String, String> adInfo = new HashMap<String, String>();
            ArrayList<String> impressionTrackers = new ArrayList<String>();
            ArrayList<String> clickTrackers = new ArrayList<String>();
            adInfo.put("type", "thirdparty");

            try {

                String creative = responseGenerator.getCreative(impressionId);
                if (!TextUtils.isEmpty(creative)) {

                    adInfo.put("content", responseGenerator.getCreative(impressionId));

                    // Setting ecpm if not null
                    if (responseGenerator.getPrice(impressionId) != 0) {
                        adInfo.put("ecpm", String.valueOf(responseGenerator.getPrice(impressionId)));
                    }

                    // Setting tracking url if not null
                    if (responseGenerator.getTrackingUrl(impressionId) != null && !responseGenerator.getTrackingUrl(impressionId).equals("")) {
                        impressionTrackers.add( URLDecoder.decode(responseGenerator.getTrackingUrl(impressionId), CommonConstants.ENCODING_UTF_8));
                    }

                    // Setting click tracking url if not null
                    if (responseGenerator.getClickTrackingUrl(impressionId) != null && !responseGenerator.getClickTrackingUrl(impressionId).equals("")) {
                        clickTrackers.add( URLDecoder.decode(responseGenerator.getClickTrackingUrl(impressionId), CommonConstants.ENCODING_UTF_8));
                    }
                } else {
                    PMLogger.logEvent("Creative not found for impression Id = "+impressionId, PMLogger.PMLogLevel.Error);
                    return;
                }

                BannerAdDescriptor adDescriptor = new BannerAdDescriptor(adInfo);
                adDescriptor.setImpressionTrackers(impressionTrackers);
                adDescriptor.setClickTrackers(clickTrackers);

                pubResponse.setRenderable(adDescriptor);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } finally {
                //response = null;
            }

            if (isAdResponseValid(pubResponse))
                renderAdDescriptor(pubResponse.getRenderable());

        }
    }
}
