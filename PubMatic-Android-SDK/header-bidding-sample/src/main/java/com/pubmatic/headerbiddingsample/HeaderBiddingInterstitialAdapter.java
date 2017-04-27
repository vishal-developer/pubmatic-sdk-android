package com.pubmatic.headerbiddingsample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.pubmatic.sdk.banner.PMInterstitialAdView;
import com.pubmatic.sdk.headerbidding.PMAdSize;
import com.pubmatic.sdk.headerbidding.PMBannerPrefetchRequest;
import com.pubmatic.sdk.headerbidding.PMBid;
import com.pubmatic.sdk.headerbidding.PMBannerImpression;
import com.pubmatic.sdk.headerbidding.PMPrefetchManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.WINDOW_SERVICE;

/**
 *
 */

public class HeaderBiddingInterstitialAdapter {

    public static final class AdSlotInfo {

        private String           slotName;
        private List<PMAdSize>   adSizes;
        private PublisherInterstitialAd  adView;

        public AdSlotInfo(String slotName, List<PMAdSize> adSizes, PublisherInterstitialAd adView) {
            this.slotName = slotName;
            this.adSizes  = adSizes;
            this.adView   = adView;
        }

        public String getSlotName() {
            return slotName;
        }

        public List<PMAdSize> getAdSizes() {
            return adSizes;
        }

        public PublisherInterstitialAd getAdView() {
            return adView;
        }
    }

    private static final String BID                 = "bid";
    private static final String BID_ID              = "bidid";
    private static final String WDEAL_ID            = "wdeal";
    private static final String BID_STATUS          = "bidstatus";
    private static final String PUBMATIC_WIN_KEY    = "pubmaticdm";

    private Context                   mContext;
    private PMPrefetchManager         pmPrefetchManager;
    private List<AdSlotInfo>          adSlotInfoList;

    private static final String       TAG = "HBInterstitialAdapter";

    public HeaderBiddingInterstitialAdapter(Context context, List<HeaderBiddingInterstitialAdapter.AdSlotInfo> adSlotInfoList) {
        this.mContext        = context;
        this.adSlotInfoList = adSlotInfoList;
    }

    public void execute()
    {
        //Normal ad Events listener for DFP calls.
        registerAdListener(adSlotInfoList);
        registerListenerForPublisherAdView(adSlotInfoList);

        requestPubMaticHeaderBidding();
    }

    private void requestPubMaticHeaderBidding()
    {
        PMPrefetchManager.PMPrefetchListener listener = new PMPrefetchManager.PMPrefetchListener() {
            @Override
            public void onBidsFetched(Map<String, PMBid> hBResponse) {
                Log.d(TAG, "onBidsFetched");

                // Header bidding completed. Now send the custom data to DFP.
                requestDFPAd(hBResponse);
            }

            @Override
            public void onBidsFailed(String errorMessage) {
                Log.d(TAG, "Header Bidding failed. " + errorMessage);

                // Get on with requesting DFP for ads without HB data.
                requestDFPAd(null);
            }
        };

        // Create instance of PMPrefetchManager and set listener for bidding status.
        pmPrefetchManager = new PMPrefetchManager(mContext, listener);

        //Create Pubmatic adRequest for header bidding call with single impression or a Set of impressions.
        PMBannerPrefetchRequest interstitialHeaderBiddingAdRequest = getHeaderBiddingInterstitialAdRequest1();

        /*
        Set any targeting params on the adRequest instance.
        */
        pmPrefetchManager.prefetchCreatives(interstitialHeaderBiddingAdRequest);
    }

    /**
     * Returns the unique id against the PublisherAdView. It is being used as a ImpressionId.
     * @param adView
     * @return Returns id of hash value against given PublisherAdView
     */
    private String getUniqueIdForView(final PublisherInterstitialAd adView) {
        return String.valueOf(System.identityHashCode(adView));
    }

    /**
     * Send ad Request for all DFP adViews.
     */
    private void requestDFPAd(final Map<String, PMBid> hBResponse) {

        ((Activity)mContext).runOnUiThread(new Runnable() {
                                               @Override
                                               public void run() {
                                                   PublisherAdRequest adRequest;
                                                   PublisherInterstitialAd publisherAdView;
                                                   Set<PublisherInterstitialAd> adViewsSet = getAdViewsSet();

                                                   if (hBResponse != null) {
                                                       // Loop over all those publisherAdViews that participated in Header Bidding i.e. have a valid impressionId mapped to them.
                                                       for (AdSlotInfo adSlotInfo : adSlotInfoList) {
                                                           publisherAdView = adSlotInfo.adView;

                                                           try {
                                                               String impressionId = getUniqueIdForView(adSlotInfo.adView);

                                                               //Fetch the winning bid details for the impressionId & send to DFP
                                                               PMBid pubResponse = hBResponse.get(impressionId);

                                                               if(pubResponse != null) {

                                                                   PublisherAdRequest.Builder requestBuilder = new PublisherAdRequest.Builder();

                                                                   if(!TextUtils.isEmpty(pubResponse.getDealId())) {
                                                                       requestBuilder.addCustomTargeting(WDEAL_ID, pubResponse.getDealId());
                                                                       Log.d(TAG, "DFP custom param [" + WDEAL_ID + "] = " + pubResponse.getDealId());
                                                                   }

                                                                   if(!TextUtils.isEmpty(pubResponse.getImpressionId())) {
                                                                       requestBuilder.addCustomTargeting(BID_ID, pubResponse.getImpressionId());
                                                                       Log.d(TAG, "DFP custom param [" + BID_ID + "] = " + pubResponse.getImpressionId());
                                                                   }

                                                                   String price = String.valueOf(pubResponse.getPrice());

                                                                   if(!TextUtils.isEmpty(price)) {
                                                                       double bidPrice = Double.valueOf(price);

                                                                       if(bidPrice > 0.0d) {
                                                                           requestBuilder.addCustomTargeting(BID, price);
                                                                           requestBuilder.addCustomTargeting(BID_STATUS, "1");
                                                                           Log.d(TAG, "DFP custom param [" + BID + "] = " + price + " & [" + BID_STATUS + "] = 1");
                                                                       } else {
                                                                           requestBuilder.addCustomTargeting(BID_STATUS, "0");
                                                                           Log.d(TAG, "DFP custom param [" + BID_STATUS + "] = 0");
                                                                       }
                                                                   }

                                                                   adRequest = requestBuilder.build();

                                                                   publisherAdView.loadAd(adRequest);
                                                                   adViewsSet.remove(publisherAdView);
                                                               }
                                                               else
                                                               {
                                                                   adRequest = new PublisherAdRequest.Builder().build();

                                                                   publisherAdView.loadAd(adRequest);
                                                                   adViewsSet.remove(publisherAdView);
                                                               }
                                                           } catch (Exception ex) {
                                                               // Do nothing. This view will send normal adRequest later.
                                                               Log.e(TAG, "Error while sending request to DFP :: " + ex.toString());
                                                           }
                                                       }
                                                   }

                                                   // Send Ad Request for the remaining adViews.
                                                   for (PublisherInterstitialAd ad : adViewsSet) {
                                                       adRequest = new PublisherAdRequest.Builder().build();
                                                       ad.loadAd(adRequest);
                                                   }
                                               }
                                           }
        );
    }

    private Set<PublisherInterstitialAd> getAdViewsSet() {

        Set<PublisherInterstitialAd> adViewSet = new HashSet<>();

        if(adSlotInfoList != null)
        {
            for (HeaderBiddingInterstitialAdapter.AdSlotInfo adSlotInfo : adSlotInfoList)
            {
                adViewSet.add(adSlotInfo.adView);
            }
        }

        return adViewSet;
    }

    private void registerAdListener(List<HeaderBiddingInterstitialAdapter.AdSlotInfo> adSlotInfoList) {

        for(HeaderBiddingInterstitialAdapter.AdSlotInfo adSlotInfo : adSlotInfoList)
        {
            if (adSlotInfo!=null && adSlotInfo.adView != null)
                adSlotInfo.adView.setAdListener(new HeaderBiddingInterstitialAdapter.DfpAdListener());
        }
    }

    private void registerListenerForPublisherAdView(final List<HeaderBiddingInterstitialAdapter.AdSlotInfo> adSlotInfoList) {

        for(final HeaderBiddingInterstitialAdapter.AdSlotInfo adSlotInfo : adSlotInfoList)
        {
            if (adSlotInfo!=null && adSlotInfo.adView != null)
            {
                adSlotInfo.adView.setAppEventListener(new AppEventListener() {
                    @Override
                    public void onAppEvent(String key, final String impressionId) {
                        Log.d(TAG, "AppEvent Key:" + key + " AdSlotId:" + impressionId);

                        if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {

                            adSlotInfoList.get(0).adView = null;

                            PMInterstitialAdView adView = new PMInterstitialAdView(mContext);
                            adView.setUseInternalBrowser(true);

                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT);

                            //Display PubMatic Cached Ad
                            pmPrefetchManager.renderedPMInterstitialAd(impressionId, adView);

                            //Replace view with pubmatic Adview.
                            ViewGroup parent = (ViewGroup) ((Activity)mContext).findViewById(R.id.parent);
                            if (parent != null) {
                                parent.addView(adView, layoutParams);
                                adView.showInterstitial();
                            }
                        }
                    }
                });
            }
        }
    }

    private PMBannerPrefetchRequest getHeaderBiddingInterstitialAdRequest1()
    {
        PMBannerPrefetchRequest adRequest;

        List<PMBannerImpression> interstitialImpressions = new ArrayList<>();
        for(HeaderBiddingInterstitialAdapter.AdSlotInfo adSlotInfo : adSlotInfoList) {
            PMBannerImpression pmBannerImpression = new PMBannerImpression(getUniqueIdForView(adSlotInfo.adView), adSlotInfo.slotName, adSlotInfo.adSizes, 1);
            pmBannerImpression.setInterstitial(true);
            interstitialImpressions.add(pmBannerImpression);
        }

        adRequest = PMBannerPrefetchRequest.initHBRequestForImpression(mContext, "31400", interstitialImpressions);

        return adRequest;
    }

    private PMBannerPrefetchRequest getHeaderBiddingInterstitialAdRequest()
    {
        //Get Width & Height for creating of adSlots
        int measuredWidth = 0;
        int measuredHeight = 0;
        Point size = new Point();
        WindowManager w = ((Activity)mContext).getWindowManager();
        float density  = mContext.getResources().getDisplayMetrics().density;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)    {
            w.getDefaultDisplay().getSize(size);
            measuredWidth = Math.round(size.x/density);
            measuredHeight = Math.round(size.y/density);
        }else{
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowmanager = (WindowManager) mContext.getApplicationContext().getSystemService(WINDOW_SERVICE);
            windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
            measuredWidth   = Math.round(displayMetrics.widthPixels/density);
            measuredHeight  = Math.round(displayMetrics.heightPixels/density);
        }

        PMBannerPrefetchRequest adRequest;

        List<PMAdSize> adSizes = new ArrayList<>(1);
        //adSizes.add(new PMAdSize(measuredWidth, measuredHeight));
        adSizes.add(new PMAdSize(320, 480));

        PMBannerImpression pmBannerImpression = new PMBannerImpression("impression1", "DMDemo", adSizes, 1);
        pmBannerImpression.setInterstitial(true);

        List<PMBannerImpression> bannerImpressions = new ArrayList<>();
        bannerImpressions.add(pmBannerImpression);

        adRequest = PMBannerPrefetchRequest.initHBRequestForImpression(mContext, "31400", bannerImpressions);

        return adRequest;
    }

    class DfpAdListener extends AdListener {
        public void onAdLoaded() {
            Log.d(TAG, "onAdLoaded");

            if(adSlotInfoList.get(0).adView != null)
                adSlotInfoList.get(0).adView.show();
        }

        public void onAdFailedToLoad(int errorCode) {
            Log.d(TAG, "onAdFailedToLoad");
        }

        public void onAdOpened() {
            Log.d(TAG, "onAdOpened");
        }

        public void onAdClosed() {
            Log.d(TAG, "onAdClosed");
        }

        public void onAdLeftApplication() {
            Log.d(TAG, "onAdLeftApplication");
        }
    }


}
