package com.pubmatic.sdk.headerbidding;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerRRFormatter;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.PMLogger;
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
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PubMaticPrefetchManager {

    Context mContext;
    private static final String ATTHERATE = "@";
    private static final String BYSYMBOL = "x";

    /**
     * Listener to channel result events of a header bidding request to the publisher app.
     */
    public interface PrefetchListener {
        /**
         * Success callback for Header Bidding request on PubMatic Server.
         *
         * @param publisherHBResponse mapping of adslot to corresponding prefetched creative.
         */
        void onBidsFetched(Map<String, JSONObject> publisherHBResponse);

        /**
         * Failure callback for header bidding. Publisher must go ahead with normal DFP calls.
         *
         * @param errorMessage Failure message.
         */
        void onBidsFailed(String errorMessage);
    }

    public PubMaticPrefetchManager(Context context) {
        mContext = context;
    }

    private PrefetchListener preFetchListener;
    private Map<String, JSONObject> hbCreatives = new HashMap<>();
    private Map<String, Integer> adSlotsCounts = new HashMap<>();

    private List<WeakReference<PMBannerAdView>> pubmaticAdViews;

    public void setPrefetchListener(PrefetchListener prefetchListener) {
        this.preFetchListener = prefetchListener;
    }

    public PrefetchListener getPrefetchListener() {
        return preFetchListener;
    }

    /**
     * Generate unique tag/adSlotId for every adView. In case of same dimension adView, append ":n"
     * where n is the number of identical size adtags seen so far.
     *
     * @param publisherAdViews Pass all PublisherAdViews on current page, comma-separated.
     */
    public WeakHashMap<String, PublisherAdView> generateAdSlotsForViews(PublisherAdView... publisherAdViews) {

        WeakHashMap<String, PublisherAdView> adSlotAdViewMap = new WeakHashMap<>();

        for (PublisherAdView publisherAdView : publisherAdViews) {

            String pubMaticAdUnit = publisherAdView.getAdUnitId();
            pubMaticAdUnit = pubMaticAdUnit.substring(pubMaticAdUnit.lastIndexOf("/")).substring(1);
            String tag = pubMaticAdUnit + ATTHERATE +
                    publisherAdView.getAdSize().getWidth() +
                    BYSYMBOL + publisherAdView.getAdSize().getHeight();

            if (adSlotsCounts.get(tag) == null) {
                adSlotsCounts.put(tag, 0);
            } else {
                adSlotsCounts.put(tag, adSlotsCounts.get(tag) + 1);
            }
            if (adSlotsCounts.get(tag) != 0)
                tag = tag + ":" + adSlotsCounts.get(tag);

            publisherAdView.setTag(tag);
            adSlotAdViewMap.put(tag, publisherAdView);
        }
        return adSlotAdViewMap;
    }

    /**
     * Returns the copy of header bidding creative mapped to the provided adSlotId.
     */
    public JSONObject getPrefetchCreativeForAdSlotId(String adSlotId) {

        JSONObject creative = null;
        try {
            String bidJson = String.valueOf(hbCreatives.get(adSlotId));
            if (bidJson != null) {
                creative = new JSONObject(bidJson);
                creative.remove("creative_tag");
                creative.remove("tracking_url");
            }
        } catch (JSONException e) {
            PMLogger.logEvent("Request Url : ", PMLogger.LogLevel.Debug);

        }
        return creative;
    }

    public void executeHeaderBiddingRequest(Context context, PubMaticHBBannerRequest adRequest) {

        // Sanitise request. Remove any ad tag detail.
        adRequest.setPubId("");
        adRequest.setSiteId("");
        adRequest.setAdId("");

        if (adRequest.getAdSlotIdsHB().size() == 0) {
            PMLogger.logEvent("Aborting. No adSlotIds set for Header Bidding ad Request. " +
                            "\nCheck if \"PubMaticPrefetchManager.generateAdSlotsForViews()\" method has been called. ",
                    PMLogger.LogLevel.Error);
            getPrefetchListener().onBidsFailed("No AdSlotId found for Header Bidding Request.");
            return;
        }

        adRequest.createRequest(context);

        HttpRequest httpRequest = formatHBRequest(adRequest);
        PMLogger.logEvent("Request Url : " + httpRequest.getRequestUrl() + "\n body : " + httpRequest.getPostData(),
                PMLogger.LogLevel.Debug);

        HttpHandler requestProcessor = new HttpHandler(networkListener, httpRequest);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(requestProcessor);
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

        PubMaticBannerRRFormatter rrFormatter = new PubMaticBannerRRFormatter();
        AdResponse adData = rrFormatter.formatHeaderBiddingResponse(hbCreatives.get(adSlotId));
        hbCreatives.remove(adSlotId);
        adView.renderHeaderBiddingCreative(adData);

        // Save a weak reference to this view. To be used in destroy method later.
        if (pubmaticAdViews == null)
            pubmaticAdViews = new ArrayList<>();
        WeakReference<PMBannerAdView> weakRefAdView = new WeakReference<>(adView);
        pubmaticAdViews.add(weakRefAdView);

        return adView;
    }

    private HttpRequest formatHBRequest(PubMaticBannerAdRequest adRequest) {
        HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);
        httpRequest.setUserAgent(adRequest.getUserAgent());
        httpRequest.setConnection("close");
        httpRequest.setRequestUrl(adRequest.getAdServerURL());
        httpRequest.setRequestMethod(CommonConstants.HTTPMETHODGET);
        httpRequest.setRequestType(CommonConstants.AD_REQUEST_TYPE.PUB_BANNER);
        httpRequest.setPostData(adRequest.getPostData());
        return httpRequest;
    }

    private Map<String, JSONObject> formatHBResponse(HttpResponse response) {

        // parse the json array and populate hbCreatives map.
        JSONArray bidsArray;
        JSONObject bidJson, pubJson;
        Map<String, JSONObject> publisherHBResponse = null;
        try {
            bidsArray = new JSONArray(response.getResponseData());
            int i, len = bidsArray.length();
            String key;
            for (i = 0; i < len; i++) {
                bidJson = bidsArray.getJSONObject(i);
                if (!TextUtils.isEmpty(key = bidJson.optString("id"))) {
                    hbCreatives.put(key, bidJson);

                    // prepare map to return to publisher after removing "creative_tag" and "tracking_url".
                    if (publisherHBResponse == null)
                        publisherHBResponse = new HashMap<>();

                    String bid = String.valueOf(bidJson);
                    pubJson = new JSONObject(bid);
                    pubJson.remove("creative_tag");
                    pubJson.remove("tracking_url");
                    publisherHBResponse.put(key, pubJson);
                }
            }
        } catch (JSONException e) {
            PMLogger.logEvent("Invalid Json response received for Header Bidding. " + e.getMessage(), PMLogger.LogLevel.Debug);
        }
        return publisherHBResponse;
    }

    private HttpHandler.HttpRequestListener networkListener = new HttpHandler.HttpRequestListener() {

        @Override
        public void onRequestComplete(HttpResponse response, String requestURL) {
            // parse request to populate the hbCreatives map.
            Map<String, JSONObject> publisherHBResponse = formatHBResponse(response);
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
        hbCreatives.clear();
        adSlotsCounts.clear();

        // Reset all PMBannerAdViews.
        if (pubmaticAdViews != null && pubmaticAdViews.size() != 0) {
            for (WeakReference adView : pubmaticAdViews) {
                if (adView.get() != null)
                    ((PMBannerAdView) adView.get()).reset();
            }
            pubmaticAdViews.clear();
        }

        preFetchListener = null;
    }

}
