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
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.pubmatic.sdk.banner.PMInterstitialAdView;
import com.pubmatic.sdk.headerbidding.PMAdSize;
import com.pubmatic.sdk.headerbidding.PMBannerPrefetchRequest;
import com.pubmatic.sdk.headerbidding.PMBid;
import com.pubmatic.sdk.headerbidding.PMBannerImpression;
import com.pubmatic.sdk.headerbidding.PMPrefetchManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Sagar on 10/12/2016.
 */

public class HeaderBiddingInterstitialAdapter {

    private static final String BID = "bid";
    private static final String BID_ID = "bidid";
    private static final String BID_STATUS = "bidstatus";
    private static final String PUBMATIC_WIN_KEY = "pubmaticdm";

    private Context mContext;
    private Set<PublisherAdView> adViews = new HashSet<>();
    private PMPrefetchManager headerBiddingManager;
    private HashMap<String, PublisherInterstitialAd> adSlotAdViewMap = new HashMap<>();

    private static final String TAG = "HeaderBiddingInterstitialAdapter";

    public HeaderBiddingInterstitialAdapter(Context context, HashMap<String, PublisherInterstitialAd> adSlotAdViewMap)
    {
        mContext = context;
        this.adSlotAdViewMap = adSlotAdViewMap;
    }

    public void execute()
    {
        //Normal ad Events listener for DFP calls.
        registerAdListener(adSlotAdViewMap);
        registerListenerForPublisherAdView(adSlotAdViewMap);

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
        headerBiddingManager = new PMPrefetchManager(mContext, listener);

        //Create Pubmatic adRequest for header bidding call with single impression or a Set of impressions.
        PMBannerPrefetchRequest interstitialHeaderBiddingAdRequest = getHeaderBiddingInterstitialAdRequest();

        /*
        Set any targeting params on the adRequest instance.
        */
        headerBiddingManager.prefetchCreatives(interstitialHeaderBiddingAdRequest);
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
                                                       // Loop over all those publisherAdViews that participated in Header Bidding i.e. have a valid adSlotId mapped to them.
                                                       for (Map.Entry<String, PublisherInterstitialAd> entry : adSlotAdViewMap.entrySet()) {
                                                           publisherAdView = entry.getValue();

                                                           try {
                                                               String adSlot = entry.getKey();
                                                               PMBid pubResponse = hBResponse.get(adSlot);

                                                               if(pubResponse != null) {
                                                                   adRequest = new PublisherAdRequest.Builder().addCustomTargeting(BID_ID, pubResponse.getImpressionId())
                                                                           .addCustomTargeting(BID_STATUS, "1")
                                                                           .addCustomTargeting(BID, String.valueOf(pubResponse.getPrice())).build();

                                                                   publisherAdView.loadAd(adRequest);
                                                                   adViewsSet.remove(publisherAdView);
                                                               }
                                                               else
                                                               {
                                                                   adRequest = new PublisherAdRequest.Builder().addCustomTargeting(BID_ID, pubResponse.getImpressionId())
                                                                           .addCustomTargeting(BID_STATUS, "0")
                                                                           .addCustomTargeting(BID, String.valueOf(pubResponse.getPrice())).build();

                                                                   publisherAdView.loadAd(adRequest);
                                                                   adViewsSet.remove(publisherAdView);
                                                               }
                                                           } catch (Exception ex) {
                                                               // Do nothing. This view will send normal adRequest later.
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

    public Set<PublisherInterstitialAd> getAdViewsSet() {

        Set<PublisherInterstitialAd> adViewSet = new HashSet<>();

        for(Map.Entry<String, PublisherInterstitialAd> entry : adSlotAdViewMap.entrySet())
        {
            PublisherInterstitialAd adView = entry.getValue();
            adViewSet.add(adView);
        }

        return adViewSet;
    }

    private void registerAdListener(HashMap<String, PublisherInterstitialAd> adSlotAdViewMap) {

        for(Map.Entry<String, PublisherInterstitialAd> entry : adSlotAdViewMap.entrySet())
        {
            PublisherInterstitialAd adView = entry.getValue();

            if (adView != null)
                adView.setAdListener(new DfpAdListener());
        }
    }

    class DfpAdListener extends AdListener {
        public void onAdLoaded() {
            Log.d(TAG, "onAdLoaded");
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

    private void registerListenerForPublisherAdView(final HashMap<String, PublisherInterstitialAd> adSlotAdViewMap) {

        for(Map.Entry<String, PublisherInterstitialAd> entry : adSlotAdViewMap.entrySet())
        {
            final PublisherInterstitialAd publisherAdView = entry.getValue();

            if (publisherAdView != null)
            {
                publisherAdView.setAppEventListener(new AppEventListener() {
                    @Override
                    public void onAppEvent(String key, final String adSlotId) {
                        Log.d(TAG, "onAppEvent() Key: " + key + " AdSlotId: " + adSlotId);

                        if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {

                            //Display PubMatic Cached Ad
                            PMInterstitialAdView pmView = headerBiddingManager.getRenderedPMInterstitialAd(mContext, adSlotId);

                            //Replace view with pubmatic Adview.
                            ViewGroup parent = (ViewGroup) ((Activity)mContext).findViewById(R.id.parent);
                            if (parent != null) {

                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT);

                                parent.addView(pmView, layoutParams);
                                pmView.showInterstitial();
                            }

                            adSlotAdViewMap.remove(adSlotId);
                        }
                    }
                });
            }
        }
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



}
