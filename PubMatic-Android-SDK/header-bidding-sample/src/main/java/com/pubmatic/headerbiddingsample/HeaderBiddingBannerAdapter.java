package com.pubmatic.headerbiddingsample;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;
import com.pubmatic.sdk.headerbidding.PMAdSize;
import com.pubmatic.sdk.headerbidding.PMBid;
import com.pubmatic.sdk.headerbidding.PMBannerImpression;
import com.pubmatic.sdk.headerbidding.PubMaticBannerPrefetchRequest;
import com.pubmatic.sdk.headerbidding.PubMaticDecisionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Sagar on 10/12/2016.
 */
public class HeaderBiddingBannerAdapter {

    private static final String BID = "bid";
    private static final String BID_ID = "bidid";
    private static final String BID_STATUS = "bidstatus";
    private static final String PUBMATIC_WIN_KEY = "pubmaticdm";

    private Context mContext;
    private Set<PublisherAdView> adViews = new HashSet<>();
    private PubMaticDecisionManager headerBiddingManager;
    private HashMap<String, PublisherAdView> adSlotAdViewMap = new HashMap<>();

    private static final String TAG = "HeaderBiddingBannerAdapter";

    public HeaderBiddingBannerAdapter(Context context, HashMap<String, PublisherAdView> adSlotAdViewMap)
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
        PubMaticDecisionManager.PrefetchListener listener = new PubMaticDecisionManager.PrefetchListener() {
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

        // Create instance of PubMaticDecisionManager and set listener for bidding status.
        headerBiddingManager = new PubMaticDecisionManager(mContext);
        headerBiddingManager.setPrefetchListener(listener);

        //Create Pubmatic adRequest for header bidding call with single impression or a Set of impressions.
        PubMaticBannerPrefetchRequest bannerHeaderBiddingAdRequest = getHeaderBiddingBannerAdRequest();

        /*
        Set any targeting params on the adRequest instance.
        */
        headerBiddingManager.executeHeaderBiddingRequest(mContext, bannerHeaderBiddingAdRequest);
    }

    /**
     * Send ad Request for all DFP adViews.
     */
    private void requestDFPAd(final Map<String, PMBid> hBResponse) {

        ((Activity)mContext).runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              PublisherAdRequest adRequest;
                              PublisherAdView publisherAdView;
                              Set<PublisherAdView> adViewsSet = getAdViewsSet();

                              if (hBResponse != null) {
                                  // Loop over all those publisherAdViews that participated in Header Bidding i.e. have a valid adSlotId mapped to them.
                                  for (Map.Entry<String, PublisherAdView> entry : adSlotAdViewMap.entrySet()) {
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
                              for (PublisherAdView ad : adViewsSet) {
                                  adRequest = new PublisherAdRequest.Builder().build();
                                  ad.loadAd(adRequest);
                              }
                          }
                      }
        );
    }

    public Set<PublisherAdView> getAdViewsSet() {

        Set<PublisherAdView> adViewSet = new HashSet<>();

        for(Map.Entry<String, PublisherAdView> entry : adSlotAdViewMap.entrySet())
        {
            PublisherAdView adView = entry.getValue();
            adViewSet.add(adView);
        }

        return adViewSet;
    }

    private void registerAdListener(HashMap<String, PublisherAdView> adSlotAdViewMap) {

        for(Map.Entry<String, PublisherAdView> entry : adSlotAdViewMap.entrySet())
        {
            PublisherAdView adView = entry.getValue();

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

    private void registerListenerForPublisherAdView(final HashMap<String, PublisherAdView> adSlotAdViewMap) {

        for(Map.Entry<String, PublisherAdView> entry : adSlotAdViewMap.entrySet())
        {
            final PublisherAdView publisherAdView = entry.getValue();

            if (publisherAdView != null)
            {
                publisherAdView.setAppEventListener(new AppEventListener() {
                    @Override
                    public void onAppEvent(String key, final String adSlotId) {
                        Log.d(TAG, "onAppEvent() Key: " + key + " AdSlotId: " + adSlotId);

                        if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {

                            //Display PubMatic Cached Ad
                            PMBannerAdView pmView = headerBiddingManager.getRenderedPubMaticAd(mContext, adSlotId, publisherAdView);

                            //Replace view with pubmatic Adview.
                            ViewGroup parent = (ViewGroup) publisherAdView.getParent();
                            if (parent != null) {
                                parent.addView(pmView, publisherAdView.getLayoutParams());
                            }

                            adSlotAdViewMap.remove(adSlotId);
                        }
                    }
                });
            }
        }
    }

    private PubMaticBannerPrefetchRequest getHeaderBiddingBannerAdRequest()
    {
        PubMaticBannerPrefetchRequest adRequest;

        List<PMAdSize> adSizes = new ArrayList<>(1);
        adSizes.add(new PMAdSize(320, 50));

        PMBannerImpression pmBannerImpression = new PMBannerImpression("impression1", "DMDemo", adSizes, 1);

        List<PMAdSize> adSizes1 = new ArrayList<>(1);
        adSizes1.add(new PMAdSize(320, 50));

        PMBannerImpression pmBannerImpression1 = new PMBannerImpression("impression2", "DMDemo2", adSizes1, 1);

        List<PMAdSize> adSizes2 = new ArrayList<>(1);
        adSizes2.add(new PMAdSize(320, 50));

        PMBannerImpression pmBannerImpression2 = new PMBannerImpression("impression3", "WDemo", adSizes2, 1);

        List<PMBannerImpression> bannerImpressions = new ArrayList<>();
        bannerImpressions.add(pmBannerImpression);
        bannerImpressions.add(pmBannerImpression1);
        bannerImpressions.add(pmBannerImpression2);

        //adRequest = PubMaticBannerPrefetchRequest.initHBRequestForImpression(this, "5890", bannerImpressions);
        adRequest = PubMaticBannerPrefetchRequest.initHBRequestForImpression(mContext, "31400", bannerImpressions);

        adRequest.setStoreURL("http://www.financialexpress.com");
        adRequest.setAppDomain("www.financialexpress.com");
        adRequest.isApplicationPaid(true);
        adRequest.setAWT(PubMaticAdRequest.AWT_OPTION.WRAPPED_IN_IFRAME);
        adRequest.setPMZoneId("1");
        adRequest.addKeyword("entertainment");
        adRequest.addKeyword("sports");
        adRequest.setEthnicity("1");
        adRequest.setIncome("income");

        adRequest.setYearOfBirth("1989");
        adRequest.setGender("M");

        adRequest.setCity("Pune");
        adRequest.setZip("411011");
        adRequest.setCoppa(true);
        adRequest.setOrmmaComplianceLevel(1);

        adRequest.setIABCategory("IAB1-1,IAB1-7");

        return adRequest;
    }
}
