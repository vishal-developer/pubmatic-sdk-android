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

import android.content.Context;
import android.location.Location;
import android.webkit.WebView;

import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMInterstitialAd;
import com.pubmatic.sdk.banner.pubmatic.PMBannerAdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.LocationDetector;
import com.pubmatic.sdk.common.PMAdRendered;
import com.pubmatic.sdk.common.PMLogger;
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
         * @param errorMessage Failure message.
         */
        void onBidsFailed(String errorMessage);
    }

    private Map<String, PMBid> publisherHBResponse = new HashMap<>();

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

        if(adRequest!=null) {

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

                HttpHandler requestProcessor = new HttpHandler(networkListener, httpRequest);

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(requestProcessor);
            }
        }
    }

    private boolean validateHeaderBiddingRequest(PMPrefetchRequest adRequest)
    {
        if (adRequest.getImpressions().size() == 0) {
            PMLogger.logEvent("No impressions found for Header Bidding Request.", PMLogger.PMLogLevel.Error);
            getPrefetchListener().onBidsFailed("No impressions found for Header Bidding Request.");
            return false;
        }

        return true;
    }

    /**
     * Provide the rendered adView from PubMatic cached creative.
     * This creative is the header bidding winner for the provided impressionId.
     *
     * @param impressionId  the winning impressionId
     */
    public void loadBannerAd(String impressionId, PMAdRendered pmAdRendered) {

        if(pmAdRendered!=null)
            pmAdRendered.renderPrefetchedAd(impressionId, this);

        if(publisherHBResponse!=null)
            publisherHBResponse.remove(impressionId);

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

        if(publisherHBResponse!=null)
            publisherHBResponse.remove(impressionId);

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

    private Map<String, PMBid> formatHBResponse(HttpResponse response) {

        Map<String, PMBid> bidsMap = new HashMap<>();

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

                            bidsMap.put(bid.getImpressionId(), bid);

                        } catch (JSONException e) {
                            PMLogger.logEvent("Error parsing bid " + e.getMessage(), PMLogger.PMLogLevel.Debug);
                        }
                    }
                }
                else
                {
                    PMLogger.logEvent("Parsing error : No bids found", PMLogger.PMLogLevel.Debug);
                }
            }
            else
            {
                PMLogger.logEvent("Parsing error : No seatbid found", PMLogger.PMLogLevel.Debug);
            }
        } catch (JSONException e) {
            PMLogger.logEvent("Invalid Json response received for Header Bidding. " + e.getMessage(), PMLogger.PMLogLevel.Debug);
        }

        return bidsMap;
    }

    private HttpHandler.HttpRequestListener networkListener = new HttpHandler.HttpRequestListener() {

        @Override
        public void onRequestComplete(HttpResponse response, String requestURL) {
            // parse request to populate the hbCreatives map.
            publisherHBResponse = formatHBResponse(response);
            if (pmPreFetchListener == null)
                return;

            if (publisherHBResponse != null && publisherHBResponse.size() > 0)
                pmPreFetchListener.onBidsFetched(publisherHBResponse);
            else
                pmPreFetchListener.onBidsFailed("No Bids available");
        }

        @Override
        public void onErrorOccured(int errorType, int errorCode, String requestURL) {
            String message = "Error Occurred while sending HB request.  " + " Code : " + errorCode + " requestURL " + requestURL;
            PMLogger.logEvent(message, PMLogger.PMLogLevel.Debug);
            if (pmPreFetchListener != null)
                pmPreFetchListener.onBidsFailed(message);
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
        if(publisherHBResponse!=null)
            publisherHBResponse.clear();

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

        if(publisherHBResponse != null)
        {
            PMBid pmBid = publisherHBResponse.get(impressionId);
            return pmBid.getTrackingUrl();
        }

        return null;
    }

    @Override
    public String getClickTrackingUrl(String impressionId) {

        if(publisherHBResponse != null)
        {
            PMBid pmBid = publisherHBResponse.get(impressionId);
            return pmBid.getClickTrackingUrl();
        }

        return null;
    }

    @Override
    public String getCreative(String impressionId) {

        if(publisherHBResponse != null)
        {
            PMBid pmBid = publisherHBResponse.get(impressionId);
            if(pmBid!=null)
                return pmBid.getCreative();
        }

        return null;

    }

    @Override
    public Double getPrice(String impressionId) {

        if(publisherHBResponse != null)
        {
            PMBid pmBid = publisherHBResponse.get(impressionId);
            return pmBid.getPrice();
        }

        return null;

    }
}
