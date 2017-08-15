package com.pubmatic.sdk.banner;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.PMAdRendered;
import com.pubmatic.sdk.common.ResponseGenerator;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PMInterstitialAd implements PMAdRendered {


    @Override
    public void renderPrefetchedAd(String impressionId, ResponseGenerator responseGenerator) {
        interstitialAdView.renderPrefetchedAd(impressionId, responseGenerator);
    }

    public interface InterstitialAdListener {

        public interface RequestListener
        {
            /**
             * Failed to receive ad content (network or other related error).
             *
             * @param ad
             * @param errorCode, if any, encountered while attempting to reqest an ad.
             * @param errorMessage, error message if any
             */
            public void onFailedToReceiveAd(PMInterstitialAd ad, int errorCode, String errorMessage);

            /**
             * Ad received and rendered.
             *
             * @param ad
             */
            public void onReceivedAd(PMInterstitialAd ad);
        }

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
             * @param ad The PMInterstitialAd instance invoking the method.
             * @param url The URL to open.
             * @return Boolean true if caller has completely handled the click event and wants
             * to skip the default SDK click processing; return false if the caller has only
             * implemented a "side-effect" such as logging, and wants the default SDK logic
             * to continue.
             */
            public boolean onOpenUrl(PMInterstitialAd ad, String url);

            /**
             * Invoked when the ad will start a new system activity.
             *
             * @param adView
             */
            public void onLeavingApplication(PMInterstitialAd adView);

            /**
             * Invoked when the ad receives a close button press that should be handled by
             * the application.
             *
             * This only occurs for the close button enabled with showCloseButton() or in
             * the case of a interstitial rich media ad that closes itself.  It will not be
             * sent for rich media close buttons that collapse expanded or resized ads.
             *
             * @param adView The PMInterstitialAd instance invoking the method.
             * @return Boolean true if the caller has completely handled the click event and
             * wants to skip the default SDK click processing; return false if the caller has
             * only implemented a "side-effect" such as logging and wants the default SDK logic
             * to continue.  For BaseAdView instances that are interstitial implementations MUST
             * call closeInterstitial() if returning true from this method.
             */
            public boolean onCloseButtonClick(PMInterstitialAd adView);
        }

        public interface InternalBrowserListener
        {
            /**
             * Invoked when the internal browser has been presented to the user.
             *
             * @param ad
             */
            public void onInternalBrowserPresented(PMInterstitialAd ad);

            /**
             * Invoked when the internal browser has been closed by the user or the SDK.
             * @param ad
             */
            public void onInternalBrowserDismissed(PMInterstitialAd ad);
        }

        public interface RichMediaListener
        {
            /**
             * Invoked when a rich media ad expands to the full screen size.
             *
             * @param adView
             */
            public void onExpanded(PMInterstitialAd adView);

            /**
             * Invoked when a rich media ad is resized larger than it's default/configured size.
             *
             * @param adView
             * @param area Area of the screen used to render the resized ad.
             */
            public void onResized(PMInterstitialAd adView, Rect area);

            /**
             * Invoked when a rich media ad collapses from an expanded or resized state.
             *
             * @param adView
             */
            public void onCollapsed(PMInterstitialAd adView);

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
            public boolean onPlayVideo(PMInterstitialAd adView, String url);

            /**
             * Invoked after a rich media (MRAID) event has occurred.  Since the event has already been handled
             * applications need not implement any behavior.  However, applications can use this to listen and act
             * on handled rich media events with other behavior.
             *
             * @param adView
             * @param request
             */
            public void onEventProcessed(PMInterstitialAd adView, String request);
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
            public Boolean shouldSupportSMS(PMInterstitialAd adView);

            /**
             * Should placing phone calls be reported as a supported feature?
             * @return Boolean true if this feature should be reported as a supported feature,
             * Boolean false if it should not, or NULL if the normal SDK hardware/permission check
             * should be performed.
             */
            public Boolean shouldSupportPhone(PMInterstitialAd adView);

            /**
             * Should creating calendar entries by reported as a supported feature?
             * @return Boolean true if this feature should be reported as a supported feature,
             * Boolean false if it should not, or NULL if the normal SDK hardware/permission check
             * should be performed.
             */
            public Boolean shouldSupportCalendar(PMInterstitialAd adView);

            /**
             * Should storing pictures to the camera roll be reported as a supported feature?
             * @return Boolean true if this feature should be reported as a supported feature,
             * Boolean false if it should not, or NULL if the normal SDK hardware/permission check
             * should be performed.
             */
            public Boolean shouldSupportStorePicture(PMInterstitialAd adView);

            /**
             * Invoked when an ad intends to store a picture to the device camera role. Return boolean
             * true indicating the user has approved storing the picture, or false otherwise.
             * NOTE: the application developer is responsible for displaying user dialog, and associated
             * details such as running UI code on a UI thread if needed.
             * @param sender The originating ad view where the event was triggered.
             * @param url String URL of image that will be downloaded and stored, if approved.
             * @return True to allow picture storage, false otherwise.
             */
            public boolean shouldStorePicture(PMInterstitialAd sender, String url);

            /**
             * Invoked when an ad intends to create an event in the users' calendar. Return boolean
             * true indicating the user has approved creating the event, or false otherwise.
             * NOTE: the application developer is responsible for displaying user dialog, and associated
             * details such as running UI code on a UI thread if needed.
             * @param sender The originating ad view where the event was triggered.
             * @param calendarProperties Complex string describing specifics of the calendar event.
             * @return True to allow picture storage, false otherwise.
             */
            public boolean shouldAddCalendarEntry(PMInterstitialAd sender, String calendarProperties);
        }
    }


    PMBannerAdView interstitialAdView;

    // Delegates
    private InterstitialAdListener.ActivityListener activityListener;
    private InterstitialAdListener.FeatureSupportHandler featureSupportHandler;
    private InterstitialAdListener.InternalBrowserListener internalBrowserListener;
    private InterstitialAdListener.RequestListener requestListener;
    private InterstitialAdListener.RichMediaListener richMediaListener;


    public InterstitialAdListener.ActivityListener getActivityListener() {
        return activityListener;
    }

    public void setActivityListener(final InterstitialAdListener.ActivityListener listener) {
        activityListener = listener;
        interstitialAdView.setActivityListener(new PMBannerAdView.BannerAdViewDelegate.ActivityListener() {
            @Override
            public boolean onOpenUrl(PMBannerAdView adView, String url) {
                return activityListener.onOpenUrl(PMInterstitialAd.this, url);
            }

            @Override
            public void onLeavingApplication(PMBannerAdView adView) {
                activityListener.onLeavingApplication(PMInterstitialAd.this);
            }

            @Override
            public boolean onCloseButtonClick(PMBannerAdView adView) {
                return activityListener.onCloseButtonClick(PMInterstitialAd.this);
            }
        });

    }

    public InterstitialAdListener.FeatureSupportHandler getFeatureSupportHandler() {
        return featureSupportHandler;
    }

    public void setFeatureSupportHandler(InterstitialAdListener.FeatureSupportHandler handler) {
        this.featureSupportHandler = handler;
        interstitialAdView.setFeatureSupportHandler(new PMBannerAdView.BannerAdViewDelegate.FeatureSupportHandler() {
            @Override
            public Boolean shouldSupportSMS(PMBannerAdView adView) {
                return featureSupportHandler.shouldSupportSMS(PMInterstitialAd.this);
            }

            @Override
            public Boolean shouldSupportPhone(PMBannerAdView adView) {
                return featureSupportHandler.shouldSupportPhone(PMInterstitialAd.this);
            }

            @Override
            public Boolean shouldSupportCalendar(PMBannerAdView adView) {
                return featureSupportHandler.shouldSupportCalendar(PMInterstitialAd.this);
            }

            @Override
            public Boolean shouldSupportStorePicture(PMBannerAdView adView) {
                return featureSupportHandler.shouldSupportStorePicture(PMInterstitialAd.this);
            }

            @Override
            public boolean shouldStorePicture(PMBannerAdView sender, String url) {
                return featureSupportHandler.shouldStorePicture(PMInterstitialAd.this, url);
            }

            @Override
            public boolean shouldAddCalendarEntry(PMBannerAdView sender, String calendarProperties) {
                return featureSupportHandler.shouldStorePicture(PMInterstitialAd.this, calendarProperties);
            }
        });
    }

    public InterstitialAdListener.InternalBrowserListener getInternalBrowserListener() {
        return internalBrowserListener;
    }

    public void setInternalBrowserListener(InterstitialAdListener.InternalBrowserListener listener) {
        this.internalBrowserListener = listener;
        interstitialAdView.setInternalBrowserListener(new PMBannerAdView.BannerAdViewDelegate.InternalBrowserListener() {
            @Override
            public void onInternalBrowserPresented(PMBannerAdView adView) {
                internalBrowserListener.onInternalBrowserPresented(PMInterstitialAd.this);
            }

            @Override
            public void onInternalBrowserDismissed(PMBannerAdView adView) {
                internalBrowserListener.onInternalBrowserDismissed(PMInterstitialAd.this);
            }
        });
    }

    public InterstitialAdListener.RequestListener getRequestListener() {
        return requestListener;
    }

    public void setRequestListener(InterstitialAdListener.RequestListener listener) {
        this.requestListener = listener;
        interstitialAdView.setRequestListener(new PMBannerAdView.BannerAdViewDelegate.RequestListener() {
            @Override
            public void onFailedToReceiveAd(PMBannerAdView adView, int errorCode, String errorMessage) {
                requestListener.onFailedToReceiveAd(PMInterstitialAd.this, errorCode, errorMessage);
            }

            @Override
            public void onReceivedAd(PMBannerAdView adView) {
                requestListener.onReceivedAd(PMInterstitialAd.this);
            }

            @Override
            public void onReceivedThirdPartyRequest(PMBannerAdView adView, Map<String, String> properties, Map<String, String> parameters) {

            }
        });
    }

    public InterstitialAdListener.RichMediaListener getRichMediaListener() {
        return richMediaListener;
    }

    public void setRichMediaListener(InterstitialAdListener.RichMediaListener listener) {
        this.richMediaListener = listener;
        interstitialAdView.setRichMediaListener(new PMBannerAdView.BannerAdViewDelegate.RichMediaListener() {
            @Override
            public void onExpanded(PMBannerAdView adView) {
                richMediaListener.onExpanded(PMInterstitialAd.this);
            }

            @Override
            public void onResized(PMBannerAdView adView, Rect area) {
                richMediaListener.onResized(PMInterstitialAd.this, area);
            }

            @Override
            public void onCollapsed(PMBannerAdView adView) {
                richMediaListener.onCollapsed(PMInterstitialAd.this);
            }

            @Override
            public boolean onPlayVideo(PMBannerAdView adView, String url) {
                return richMediaListener.onPlayVideo(PMInterstitialAd.this, url);
            }

            @Override
            public void onEventProcessed(PMBannerAdView adView, String request) {
                richMediaListener.onEventProcessed(PMInterstitialAd.this, request);
            }
        });
    }

    /**
     * Constructor
     * @param context
     */
    public PMInterstitialAd(Context context) {

        interstitialAdView = new PMBannerAdView(context);
        interstitialAdView.init(true);
    }


    // main/background thread
    public void closeInterstitial() {
        if (interstitialAdView != null) {
            interstitialAdView.closeInterstitial();
        }
    }

    public void execute(AdRequest adrequest) throws IllegalArgumentException {
        interstitialAdView.execute(adrequest);
    }

    public void showInterstitial() {
        interstitialAdView.showInterstitial();
    }

    public void showInterstitialForDuration(int durationSeconds) {
        interstitialAdView.showInterstitialWithDuration(durationSeconds);
    }

    /**
     * Resets instance state to it's default (doesn't destroy configured parameters). Stops update
     * interval timer, closes internal browser if open, disables location detection.
     * <p/>
     * Invoke this method to stop any ad processing.
     */
    public void destroy() {
        interstitialAdView.destroy();
    }

    /**
     * Sets the delay time between showing an interstitial with showInterstitial() and showing the
     * close button. A value of 0 indicates the button should be shown immediately.
     *
     * @param closeButtonDelay Time interval in seconds to delay showing a close button after
     * showing interstitial ad.
     */
    public void showCloseButtonAfterDelay(int closeButtonDelay) {
        interstitialAdView.setCloseButtonDelay(closeButtonDelay);
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
        interstitialAdView.setUseInternalBrowser(useInternalBrowser);
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
        interstitialAdView.setCloseButtonCustomDrawable(closeButtonCustomDrawable);
    }

    public View getView() {
        return interstitialAdView;
    }
}
