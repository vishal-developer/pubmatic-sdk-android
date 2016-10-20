package com.pubmatic.sdk.headerbidding;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.pubmatic.sdk.banner.BannerAdDescriptor;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMInterstitialAdView;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.PMLogger;
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


public class PubMaticPrefetchManager {

    private Context mContext;
    private String userAgent;
    private static final String UTF8_CHARSET = "UTF-8";

    /**
     * Listener to channel result events of a header bidding request to the publisher app.
     */
    public interface PrefetchListener {
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

    private List<WeakReference<PMBannerAdView>> pubmaticAdViews;
    private List<WeakReference<PMInterstitialAdView>> pubmaticInterstitialAdViews;

    private PrefetchListener preFetchListener;

    public PubMaticPrefetchManager(Context context) {
        mContext = context;
        userAgent = new WebView(context).getSettings().getUserAgentString();
    }

    public void setPrefetchListener(PrefetchListener prefetchListener) {
        this.preFetchListener = prefetchListener;
    }

    public PrefetchListener getPrefetchListener() {
        return preFetchListener;
    }

    public void executeHeaderBiddingRequest(Context context, PubMaticBannerPrefetchRequest adRequest) {

        // Sanitise request. Remove any ad tag detail.
        adRequest.setSiteId("");
        adRequest.setAdId("");

        if(validateHeaderBiddingRequest(adRequest))
        {
            adRequest.createRequest(context);

            HttpRequest httpRequest = formatHeaderBiddingRequest(adRequest);
            PMLogger.logEvent("Request Url : " + httpRequest.getRequestUrl() + "\n body : " + httpRequest.getPostData(),
                    PMLogger.LogLevel.Debug);

            HttpHandler requestProcessor = new HttpHandler(networkListener, httpRequest);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(requestProcessor);
        }
    }

    private boolean validateHeaderBiddingRequest(PubMaticBannerPrefetchRequest adRequest)
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
     * This creative is the header bidding winner for the provided adSlotId.
     *
     * @param context   Activity context
     * @param adSlotId  the winning adSlotId
     * @param dfpAdView the PublisherAdView in which the new creative from PubMatic is to be displayed.
     */
    public PMBannerAdView getRenderedPubMaticAd(Context context, String adSlotId, PublisherAdView dfpAdView) {

        PMBannerAdView adView = new PMBannerAdView(context);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(dfpAdView.getLayoutParams());
        layoutParams.width = dfpAdView.getMeasuredWidth();
        layoutParams.height = dfpAdView.getMeasuredHeight();
        adView.setLayoutParams(layoutParams);
        adView.setUseInternalBrowser(true);

        AdResponse adData = formatHeaderBiddingResponse(publisherHBResponse.get(adSlotId));
        publisherHBResponse.remove(adSlotId);
        adView.renderHeaderBiddingCreative(adData);

        // Save a weak reference to this view. To be used in destroy method later.
        if (pubmaticAdViews == null)
            pubmaticAdViews = new ArrayList<>();
        WeakReference<PMBannerAdView> weakRefAdView = new WeakReference<>(adView);
        pubmaticAdViews.add(weakRefAdView);

        return adView;
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
            if (bid != null && !TextUtils.isEmpty(bid.getCreative())
                    && !TextUtils.isEmpty(bid.getTrackingUrl())) {

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
     * @param context   Activity context
     * @param adSlotId  the winning adSlotId
     */
    public PMInterstitialAdView getRenderedPMInterstitialAd(Context context, String adSlotId) {

        PMInterstitialAdView adView = new PMInterstitialAdView(context);
        adView.setUseInternalBrowser(true);

        AdResponse adData = formatHeaderBiddingResponse(publisherHBResponse.get(adSlotId));
        publisherHBResponse.remove(adSlotId);
        adView.renderHeaderBiddingCreative(adData);

        // Save a weak reference to this view. To be used in destroy method later.
        if (pubmaticInterstitialAdViews == null)
            pubmaticInterstitialAdViews = new ArrayList<>();
        WeakReference<PMInterstitialAdView> weakRefAdView = new WeakReference<>(adView);
        pubmaticInterstitialAdViews.add(weakRefAdView);

        return adView;
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
        JSONArray setBidJsonArray;
        JSONObject bidParentJsonObject;
        JSONArray bidJsonArray;
        JSONObject bidJsonObject;
        JSONObject extJsonObject;
        JSONObject extensionJsonObject;

        try
        {
            headerBiddingJsonObject = new JSONObject(response.getResponseData());

            setBidJsonArray = headerBiddingJsonObject.getJSONArray("seatbid");

            bidParentJsonObject = setBidJsonArray.getJSONObject(0);

            bidJsonArray = bidParentJsonObject.getJSONArray("bid");

            PMBid bid;

            for(int i = 0 ; i < bidJsonArray.length(); i++)
            {
                bidJsonObject = bidJsonArray.getJSONObject(i);
                bid = new PMBid();

                try
                {
                    bid.setImpressionId(bidJsonObject.getString("impid"));
                    bid.setPrice(bidJsonObject.getDouble("price"));
                    bid.setCreative(bidJsonObject.getString("adm"));
                    bid.setWidth(bidJsonObject.getInt("w"));
                    bid.setHeight(bidJsonObject.getInt("h"));

                    extJsonObject = bidJsonObject.getJSONObject("ext");
                    extensionJsonObject = extJsonObject.getJSONObject("extension");

                    bid.setTrackingUrl(extensionJsonObject.getString("trackingUrl"));
                    bid.setSlotName(extensionJsonObject.getString("slotname"));
                    bid.setSlotIndex(extensionJsonObject.getInt("slotindex"));
                    bid.setGaId(extensionJsonObject.getInt("gaId"));

                    bidsMap.put(bid.getImpressionId(), bid);
                } catch (JSONException e) {
                    PMLogger.logEvent("Error parsing bid " + e.getMessage(), PMLogger.LogLevel.Debug);
                }
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
            if (preFetchListener == null)
                return;

            if (publisherHBResponse != null && publisherHBResponse.size() > 0)
                preFetchListener.onBidsFetched(publisherHBResponse);
            else
                preFetchListener.onBidsFailed("Error in parsing Header Bidding response.");
        }

        @Override
        public void onErrorOccured(int errorType, int errorCode, String requestURL) {
            String message = "Error Occurred while sending HB request.  " + " Code : " + errorCode + " requestURL " + requestURL;
            PMLogger.logEvent(message, PMLogger.LogLevel.Debug);
            if (preFetchListener != null)
                preFetchListener.onBidsFailed(message);
        }

        @Override
        public boolean overrideRedirection() {
            return false;
        }
    };

    /**
     * Release resources, clear maps and reset the adViews used.
     */
    public void destroy() {
        publisherHBResponse.clear();

        // Reset all PMBannerAdViews.
        if (pubmaticAdViews != null && pubmaticAdViews.size() != 0) {
            for (WeakReference adView : pubmaticAdViews) {
                if (adView.get() != null)
                    ((PMBannerAdView) adView.get()).reset();
            }
            pubmaticAdViews.clear();
        }

        // Reset all PMInterstitialAdView.
        if (pubmaticInterstitialAdViews != null && pubmaticInterstitialAdViews.size() != 0) {
            for (WeakReference adView : pubmaticInterstitialAdViews) {
                if (adView.get() != null)
                    ((PMInterstitialAdView) adView.get()).reset();
            }
            pubmaticInterstitialAdViews.clear();
        }
        preFetchListener = null;
    }

}
