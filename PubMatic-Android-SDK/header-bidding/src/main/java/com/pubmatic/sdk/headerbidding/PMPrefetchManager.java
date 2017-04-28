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
import android.text.TextUtils;
import android.webkit.WebView;

import com.pubmatic.sdk.banner.BannerAdDescriptor;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMInterstitialAdView;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.PMAdRendered;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.ResponseGenerator;
import com.pubmatic.sdk.common.network.HttpHandler;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.pubmatic.PUBDeviceInformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
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
    private static final String UTF8_CHARSET = "UTF-8";

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

    public PMPrefetchManager(Context context, PMPrefetchListener pmPrefetchListener) {
        mContext = context;
        this.pmPreFetchListener = pmPrefetchListener;

        userAgent = new WebView(context).getSettings().getUserAgentString();
    }

    public PMPrefetchListener getPrefetchListener() {
        return pmPreFetchListener;
    }

    public void prefetchCreatives(PMBannerPrefetchRequest adRequest) {

        // Sanitise request. Remove any ad tag detail.
        adRequest.setSiteId("");
        adRequest.setAdId("");

        if(validateHeaderBiddingRequest(adRequest))
        {
            adRequest.createRequest(mContext);

            HttpRequest httpRequest = formatHeaderBiddingRequest(adRequest);
            PMLogger.logEvent("Request Url : " + httpRequest.getRequestUrl() + "\n body : " + httpRequest.getPostData(),
                    PMLogger.LogLevel.Debug);

            HttpHandler requestProcessor = new HttpHandler(networkListener, httpRequest);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(requestProcessor);
        }
    }

    private boolean validateHeaderBiddingRequest(PMBannerPrefetchRequest adRequest)
    {
        if (adRequest.getImpressions().size() == 0) {
            PMLogger.logEvent("No impressions found for Header Bidding Request.", PMLogger.LogLevel.Error);
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
    public void renderPubMaticAd(String impressionId, PMAdRendered pmAdRendered) {

        //AdResponse adData = formatHeaderBiddingResponse(publisherHBResponse.get(impressionId));
        //adView.renderHeaderBiddingCreative(adData);

        pmAdRendered.renderPrefetchedAd(impressionId, this);
        publisherHBResponse.remove(impressionId);

        // Save a weak reference to this view. To be used in destroy method later.
        if (pubmaticAdViews == null)
            pubmaticAdViews = new ArrayList<>();
        WeakReference<PMAdRendered> weakRefAdView = new WeakReference<>(pmAdRendered);
        pubmaticAdViews.add(weakRefAdView);
    }

    public void reset() {
        if(pubmaticAdViews!=null) {
            if(pubmaticAdViews.size()>0) {
                for(WeakReference<PMAdRendered> adRendered : pubmaticAdViews) {
                    ((PMBannerAdView)adRendered.get()).destroy();
                }
            }
            pubmaticAdViews = null;
        }
    }

    private AdResponse formatHeaderBiddingResponse(PMBid bid)
    {
        AdResponse pubResponse = new AdResponse();

        Map<String, String> adInfo = new HashMap<String, String>();
        ArrayList<String> impressionTrackers = new ArrayList<String>();
        ArrayList<String> clickTrackers = new ArrayList<String>();
        adInfo.put("type", "thirdparty");

        try {
            // If there is an error from the server which happens when provided
            // wrong ad parameters, return the error with error code and error
            // message.
            String errorCode;
            /*if (!TextUtils.isEmpty(errorCode = response.optString(kerror_code))) {

                pubResponse.setErrorCode(errorCode);
                pubResponse.setErrorMessage(response.getString(kerror_message));
                return pubResponse;
            }*/

            // Check if json contains the creative_tag and tracking_url.
            // If these are missing then the ad is invalid. Return null else
            // return valid adInfo object.
            if (bid != null && !TextUtils.isEmpty(bid.getCreative())) {

                adInfo.put("content", URLDecoder.decode(bid.getCreative(), UTF8_CHARSET));
                impressionTrackers.add( URLDecoder.decode(bid.getTrackingUrl(), UTF8_CHARSET));

                // Setting ecpm if not null
                if (bid.getPrice() != 0) {
                    adInfo.put("ecpm", String.valueOf(bid.getPrice()));
                }
                // Setting click_tracking_url if not null
                if (bid.getTrackingUrl() != null && bid.getTrackingUrl() != "") {
                    // clickTrackers.add(URLDecoder.decode(bid.getTrackingUrl(), UTF8_CHARSET));
                }
                // Setting landing_page if not null
                /*if (!response.isNull(klanding_page)) {
                    adInfo.put("url", URLDecoder.decode(response.getString(klanding_page), UTF8_CHARSET));
                }*/
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

        return pubResponse;
    }

    /**
     * Provide the rendered adView from PubMatic cached creative.
     * This creative is the header bidding winner for the provided adSlotId.
     *
     */
    public void renderedPMInterstitialAd(String impressionId, PMAdRendered pmAdRendered) {

        pmAdRendered.renderPrefetchedAd(impressionId, this);
        publisherHBResponse.remove(impressionId);

        // Save a weak reference to this view. To be used in destroy method later.
        if (pubmaticInterstitialAdViews == null)
            pubmaticInterstitialAdViews = new ArrayList<>();
        WeakReference<PMAdRendered> weakRefAdView = new WeakReference<>(pmAdRendered);
        pubmaticInterstitialAdViews.add(weakRefAdView);

    }

    private HttpRequest formatHeaderBiddingRequest(PubMaticBannerAdRequest adRequest) {
        HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);
        httpRequest.setUserAgent(adRequest.getUserAgent());
        httpRequest.setConnection("close");
        httpRequest.setRequestMethod(CommonConstants.HTTPMETHODPOST);
        httpRequest.setRequestType(CommonConstants.AD_REQUEST_TYPE.PUB_BANNER);
        httpRequest.setRequestUrl(adRequest.getRequestUrl());
        httpRequest.setPostData(adRequest.getPostData());
        PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation.getInstance(mContext);
        httpRequest.setRLNClientIPAddress(pubDeviceInformation.mDeviceIpAddress);
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
                            PMLogger.logEvent("Error parsing bid " + e.getMessage(), PMLogger.LogLevel.Debug);
                        }
                    }
                }
                else
                {
                    PMLogger.logEvent("Parsing error : No bids found", PMLogger.LogLevel.Debug);
                }
            }
            else
            {
                PMLogger.logEvent("Parsing error : No seatbid found", PMLogger.LogLevel.Debug);
            }
        } catch (JSONException e) {
            PMLogger.logEvent("Invalid Json response received for Header Bidding. " + e.getMessage(), PMLogger.LogLevel.Debug);
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
            PMLogger.logEvent(message, PMLogger.LogLevel.Debug);
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
        publisherHBResponse.clear();

        // Reset all PMBannerAdViews.
        if (pubmaticAdViews != null && pubmaticAdViews.size() != 0) {
            for (WeakReference adView : pubmaticAdViews) {
                if (adView.get() != null)
                    ((PMBannerAdView) adView.get()).destroy();
            }
            pubmaticAdViews.clear();
        }

        // Reset all PMInterstitialAdView.
        if (pubmaticInterstitialAdViews != null && pubmaticInterstitialAdViews.size() != 0) {
            for (WeakReference adView : pubmaticInterstitialAdViews) {
                if (adView.get() != null)
                    ((PMInterstitialAdView) adView.get()).destroy();
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
