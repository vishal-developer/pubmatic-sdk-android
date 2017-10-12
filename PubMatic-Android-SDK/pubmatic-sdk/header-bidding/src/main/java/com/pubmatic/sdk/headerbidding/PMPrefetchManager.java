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

package com.pubmatic.sdk.headerbidding;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.webkit.WebView;

import com.pubmatic.sdk.banner.Background;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMInterstitialAd;
import com.pubmatic.sdk.banner.pubmatic.PMBannerAdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.LocationDetector;
import com.pubmatic.sdk.common.PMAdRendered;
import com.pubmatic.sdk.common.PMError;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.PMUtils;
import com.pubmatic.sdk.common.PubMaticSDK;
import com.pubmatic.sdk.common.ResponseGenerator;
import com.pubmatic.sdk.common.network.HttpHandler;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is the responsible for fetching the bids from PubMatic ad server.
 * It gives callback to Publisher application via PMPrefetchListener. It also
 * manages the bids for future rendering.
 *
 * Provides the auto refresh feature using which it continous notify the Publisher
 * with new bid details after specified interval. Only for first refresh the notification
 * occurs at <refreshInterval - networkTimeout> in seconds. And all successor notification
 * will occur after <refreshInterval> in seconds.
 */
public class PMPrefetchManager implements ResponseGenerator {

    private Context mContext;
    private String userAgent;

    /**
     * Listener to channel result events of a header bidding request to the publisher app.
     */
    public interface PMPrefetchListener {
        /**
         * Success callback for Header Bidding request on PubMatic Server.
         *
         * @param publisherHBResponse mapping of adslot to corresponding prefetched creative.
         */
        void onBidsFetched(Map<String, PMBid> publisherHBResponse);

        /**
         * Failure callback for header bidding. Publisher must go ahead with normal DFP calls.
         *
         * @param error Failure message.
         */
        void onBidsFailed(PMError error);
    }

    private class PrefetchAdDescripor {
        public Map<String, PMBid> publisherHBResponse = new HashMap<>();
        public PMError error;

    }

    private PrefetchAdDescripor adDescripor;

    private List<WeakReference<PMAdRendered>> pubmaticAdViews;
    private List<WeakReference<PMAdRendered>> pubmaticInterstitialAdViews;

    private PMPrefetchListener pmPreFetchListener;

    private Location location;

    public PMPrefetchManager(Context context, PMPrefetchListener pmPrefetchListener) {
        mContext = context;
        this.pmPreFetchListener = pmPrefetchListener;

        WebView webView = new WebView(context);
        userAgent = webView.getSettings().getUserAgentString();
        webView = null;
    }

    public PMPrefetchListener getPrefetchListener() {
        return pmPreFetchListener;
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
        return PubMaticSDK.isLocationDetectionEnabled();
    }


    public void prefetchCreatives(PMPrefetchRequest adRequest) {

        if(adRequest!=null && adRequest.getImpressions()!=null && adRequest.getImpressions().size() > 0) {

            if(validateHeaderBiddingRequest(adRequest))
            {
                // If User has provided the location set the source as user
                Location userProvidedLocation = adRequest.getLocation();
                if(userProvidedLocation != null) {
                    userProvidedLocation.setProvider("user");
                    adRequest.setLocation(userProvidedLocation);
                }

                // Insert the location parameter in ad request,
                // if publisher has enabled location detection
                if(PubMaticSDK.isLocationDetectionEnabled()) {
                    location = LocationDetector.getInstance(mContext).getLocation();
                    if(location != null)
                        adRequest.setLocation(location);
                }

                adRequest.createRequest(mContext);

                HttpRequest httpRequest = formatHeaderBiddingRequest(adRequest);
                PMLogger.logEvent("Request Url : " + httpRequest.getRequestUrl() + "\n body : " + httpRequest.getPostData(),
                        PMLogger.PMLogLevel.Debug);

                if(PMUtils.isNetworkConnected(mContext)) {
                    HttpHandler requestProcessor = new HttpHandler(networkListener, httpRequest);

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(requestProcessor);
                } else
                    fireCallback(PREFETCH_AD_FAILED, new PMError(PMError.NETWORK_ERROR, "Not able to get network connection"));

            }
        } else {
            PMLogger.logEvent("Missing valid impressions found for Header Bidding Request.", PMLogger.PMLogLevel.Error);
            fireCallback(PREFETCH_AD_FAILED, new PMError(PMError.INVALID_REQUEST,"Missing valid impressions found for Header Bidding Request."));
        }
    }

    private boolean validateHeaderBiddingRequest(PMPrefetchRequest adRequest)
    {
        String pubId = adRequest.getPubId();
        if(TextUtils.isEmpty(pubId)) {
            PMLogger.logEvent("Missing Publisher ID for this request.", PMLogger.PMLogLevel.Error);
            fireCallback(PREFETCH_AD_FAILED, new PMError(PMError.INVALID_REQUEST,"Missing Publisher ID for this request."));
            return false;
        }
        return true;
    }

    private void fireCallback(final int callbackType,
                              final PMError error) {

        // Check if listener is set.
        if (getPrefetchListener() != null) {

            try {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        switch (callbackType) {
                            case PREFETCH_AD_RECEIVED:
                                getPrefetchListener().onBidsFetched(adDescripor.publisherHBResponse);
                                break;
                            case PREFETCH_AD_FAILED:
                                if(error!=null)
                                    PMLogger.logEvent("Error response : " + error.toString(), PMLogger.PMLogLevel.Error);
                                getPrefetchListener().onBidsFailed(error);
                                break;
                        }
                    }
                });

            } catch (ClassCastException e) {
                getPrefetchListener().onBidsFailed(new PMError(PMError.INVALID_REQUEST, "Activity context is required and passed is application context."));
            }
        }
    }

    // constants and listeners
    private static final int  PREFETCH_AD_RECEIVED = 10001, PREFETCH_AD_FAILED = 10002;

    /**
     * Provide the rendered adView from PubMatic cached creative.
     * This creative is the header bidding winner for the provided impressionId.
     *
     * @param impressionId  the winning impressionId
     */
    public void loadBannerAd(String impressionId, PMAdRendered pmAdRendered) {

        if(pmAdRendered!=null)
            pmAdRendered.renderPrefetchedAd(impressionId, this);

        if(adDescripor!=null && adDescripor.publisherHBResponse!=null)
            adDescripor.publisherHBResponse.remove(impressionId);

        // Save a weak reference to this view. To be used in destroy method later.
        if (pubmaticAdViews == null)
            pubmaticAdViews = new ArrayList<>();
        WeakReference<PMAdRendered> weakRefAdView = new WeakReference<>(pmAdRendered);
        pubmaticAdViews.add(weakRefAdView);
    }

    /**
     * Provide the rendered adView from PubMatic cached creative.
     * This creative is the header bidding winner for the provided adSlotId.
     *
     */
    public void loadInterstitialAd(String impressionId, PMAdRendered pmAdRendered) {

        if(pmAdRendered!=null)
            pmAdRendered.renderPrefetchedAd(impressionId, this);

        if(adDescripor!=null && adDescripor.publisherHBResponse!=null)
            adDescripor.publisherHBResponse.remove(impressionId);

        // Save a weak reference to this view. To be used in destroy method later.
        if (pubmaticInterstitialAdViews == null)
            pubmaticInterstitialAdViews = new ArrayList<>();
        WeakReference<PMAdRendered> weakRefAdView = new WeakReference<>(pmAdRendered);
        pubmaticInterstitialAdViews.add(weakRefAdView);

    }

    private HttpRequest formatHeaderBiddingRequest(PMBannerAdRequest adRequest) {
        HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);
        httpRequest.setUserAgent(adRequest.getUserAgent());
        httpRequest.setConnection("close");
        httpRequest.setRequestMethod(CommonConstants.HTTPMETHODPOST);
        httpRequest.setRequestType(CommonConstants.AD_REQUEST_TYPE.PUB_BANNER);
        httpRequest.setRequestUrl(adRequest.getRequestUrl());
        httpRequest.setPostData(adRequest.getPostData());
        httpRequest.setUserAgent(userAgent);
        return httpRequest;
    }

    private PrefetchAdDescripor formatHBResponse(HttpResponse response) {

        PrefetchAdDescripor adDescripor = new PrefetchAdDescripor();

        JSONObject headerBiddingJsonObject;
        JSONArray seatBidJsonArray;
        JSONObject bidParentJsonObject;
        JSONArray bidJsonArray;
        JSONObject bidJsonObject;
        JSONObject extJsonObject;
        JSONObject extensionJsonObject;
        JSONArray summaryJsonArray;
        JSONObject summaryJsonObject;

        try
        {

            if(response==null || TextUtils.isEmpty(response.getResponseData())) {
                adDescripor.error = new PMError(PMError.INVALID_RESPONSE, "Invalid server response for prefetch ad request.");
                return adDescripor;
            }
            headerBiddingJsonObject = new JSONObject(response.getResponseData());

            seatBidJsonArray = headerBiddingJsonObject.optJSONArray("seatbid");

            if(seatBidJsonArray != null && seatBidJsonArray.length() > 0)
            {
                bidParentJsonObject = seatBidJsonArray.optJSONObject(0);

                bidJsonArray = bidParentJsonObject.optJSONArray("bid");

                if(bidJsonArray != null && bidJsonArray.length() > 0)
                {
                    PMBid bid;

                    for(int i = 0 ; i < bidJsonArray.length(); i++) {
                        bidJsonObject = bidJsonArray.getJSONObject(i);
                        bid = new PMBid();

                        try {
                            bid.setImpressionId(bidJsonObject.optString("impid"));
                            bid.setPrice(bidJsonObject.optDouble("price"));
                            bid.setCreative(bidJsonObject.optString("adm"));
                            bid.setDealId(bidJsonObject.optString("dealid"));
                            bid.setWidth(bidJsonObject.optInt("w"));
                            bid.setHeight(bidJsonObject.optInt("h"));

                            extJsonObject = bidJsonObject.optJSONObject("ext");

                            if(extJsonObject != null)
                            {
                                extensionJsonObject = extJsonObject.optJSONObject("extension");

                                if(extensionJsonObject != null)
                                {
                                    bid.setTrackingUrl(extensionJsonObject.optString("trackingUrl"));
                                    bid.setClickTrackingUrl(extensionJsonObject.optString("clicktrackingurl"));
                                    bid.setSlotName(extensionJsonObject.getString("slotname"));
                                    bid.setSlotIndex(extensionJsonObject.optInt("slotindex"));

                                    // This will be deleted after the summary bug is resolved
                                    bid.setErrorCode(extensionJsonObject.optInt("errorCode"));

                                    summaryJsonArray = extensionJsonObject.optJSONArray("summary");

                                    if(summaryJsonArray != null && summaryJsonArray.length() > 0)
                                    {
                                        summaryJsonObject = summaryJsonArray.optJSONObject(0);

                                        if(summaryJsonObject != null)
                                        {
                                            bid.setErrorCode(summaryJsonObject.optInt("errorCode"));
                                            bid.setErrorMessage(summaryJsonObject.optString("errorMessage"));
                                        }
                                    }
                                }
                            }

                            if(adDescripor.publisherHBResponse==null)
                                adDescripor.publisherHBResponse = new HashMap<>();
                            adDescripor.publisherHBResponse.put(bid.getImpressionId(), bid);

                        } catch (JSONException e) {
                            PMLogger.logEvent("Error parsing bid " + e.getMessage(), PMLogger.PMLogLevel.Debug);
                        }
                    }
                }
                else
                {
                    PMLogger.logEvent("Parsing error : No bids found", PMLogger.PMLogLevel.Debug);
                    adDescripor.error = new PMError(PMError.NO_ADS_AVAILABLE, "No bids available");
                    return adDescripor;
                }
            }
            else
            {
                PMLogger.logEvent("Parsing error : No seatbid found", PMLogger.PMLogLevel.Debug);
                adDescripor.error = new PMError(PMError.NO_ADS_AVAILABLE, "No bids available");
                return adDescripor;
            }
        } catch (JSONException e) {
            PMLogger.logEvent("Invalid Json response received for Header Bidding. " + e.getMessage(), PMLogger.PMLogLevel.Debug);
            adDescripor.error = new PMError(PMError.INVALID_RESPONSE, e.getMessage());
            return adDescripor;
        }

        return adDescripor;
    }

    /**
     * Checks whether Adresponse resulted in null or error code.
     *
     * @param adData AdResponse instance
     * @return true if valid response.
     */
    private boolean isAdResponseValid(PrefetchAdDescripor adData) {

        // ErrorHandling section
        if (adData == null || adData.error != null) {
            return false;
        }

        return true;
    }

    private HttpHandler.HttpRequestListener networkListener = new HttpHandler.HttpRequestListener() {

        @Override
        public void onRequestComplete(HttpResponse response, String requestURL) {
            // parse request to populate the hbCreatives map.
            adDescripor = formatHBResponse(response);

            if (isAdResponseValid(adDescripor)) {
                fireCallback(PREFETCH_AD_RECEIVED, null);
            } else {
                if(adDescripor.error==null)
                    adDescripor.error = new PMError(PMError.INVALID_RESPONSE, "Invalid ad response for given Prefetch request. Please check request parameters.");
                fireCallback(PREFETCH_AD_FAILED, adDescripor.error);
            }
        }

        @Override
        public void onErrorOccured(PMError error, String requestURL) {
            String message = "Error Occurred while sending HB request.  " + " Error : " + error + " requestURL " + requestURL;
            PMLogger.logEvent(message, PMLogger.PMLogLevel.Debug);
            if (pmPreFetchListener != null)
                fireCallback(PREFETCH_AD_FAILED, error);
        }

        @Override
        public boolean overrideRedirection() {
            return false;
        }
    };

    /**
     * Release resources, clear maps and destroy the adViews used.
     */
    public void destroy() {
        if(adDescripor!=null) {
            if(adDescripor.publisherHBResponse!=null)
                adDescripor.publisherHBResponse.clear();
            adDescripor = null;
        }

        // Reset all PMBannerAdViews.
        if (pubmaticAdViews != null && pubmaticAdViews.size() != 0) {
            for (WeakReference adView : pubmaticAdViews) {
                if (adView!=null && adView.get() != null)
                    ((PMBannerAdView) adView.get()).destroy();
            }
            pubmaticAdViews.clear();
        }

        // Reset all PMInterstitialAdView.
        if (pubmaticInterstitialAdViews != null && pubmaticInterstitialAdViews.size() != 0) {
            for (WeakReference adView : pubmaticInterstitialAdViews) {
                if (adView!=null && adView.get() != null)
                    ((PMInterstitialAd) adView.get()).destroy();
            }
            pubmaticInterstitialAdViews.clear();
        }
        pmPreFetchListener = null;
    }

    @Override
    public String getTrackingUrl(String impressionId) {

        if(adDescripor!=null && adDescripor.publisherHBResponse != null)
        {
            PMBid pmBid = adDescripor.publisherHBResponse.get(impressionId);
            return pmBid.getTrackingUrl();
        }

        return null;
    }

    @Override
    public String getClickTrackingUrl(String impressionId) {

        if(adDescripor!=null && adDescripor.publisherHBResponse != null)
        {
            PMBid pmBid = adDescripor.publisherHBResponse.get(impressionId);
            return pmBid.getClickTrackingUrl();
        }

        return null;
    }

    @Override
    public String getCreative(String impressionId) {

        if(adDescripor!=null && adDescripor.publisherHBResponse != null)
        {
            PMBid pmBid = adDescripor.publisherHBResponse.get(impressionId);
            if(pmBid!=null)
                return pmBid.getCreative();
        }

        return null;

    }

    @Override
    public Double getPrice(String impressionId) {

        if(adDescripor!=null && adDescripor.publisherHBResponse != null)
        {
            PMBid pmBid = adDescripor.publisherHBResponse.get(impressionId);
            return pmBid.getPrice();
        }

        return null;

    }
}
