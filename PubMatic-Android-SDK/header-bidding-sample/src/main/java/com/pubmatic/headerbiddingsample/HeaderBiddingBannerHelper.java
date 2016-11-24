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
import com.pubmatic.sdk.headerbidding.PMBannerPrefetchRequest;
import com.pubmatic.sdk.headerbidding.PMBid;
import com.pubmatic.sdk.headerbidding.PMBannerImpression;
import com.pubmatic.sdk.headerbidding.PMPrefetchManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Sagar on 10/12/2016.
 */
public class HeaderBiddingBannerHelper {

    public static final class AdSlotInfo {

        private String           slotName;
        private List<PMAdSize>   adSizes;
        private PublisherAdView  adView;

        public AdSlotInfo(String slotName, List<PMAdSize> adSizes, PublisherAdView adView) {
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

        public PublisherAdView getAdView() {
            return adView;
        }
    }

    private static final String BID                 = "bid";
    private static final String BID_ID              = "bidid";
    private static final String BID_STATUS          = "bidstatus";
    private static final String PUBMATIC_WIN_KEY    = "pubmaticdm";

    private Context                   mContext;
    private PMPrefetchManager         pmPrefetchManager;
    private List<AdSlotInfo>          adSlotInfoList;

    private static final String       TAG = "HeaderBiddingBannerHelper";

    public HeaderBiddingBannerHelper(Context context, List<AdSlotInfo> adSlotInfoList) {
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
        PMBannerPrefetchRequest bannerHeaderBiddingAdRequest = getHeaderBiddingBannerAdRequest();

        //Set any targeting params on the adRequest instance.
        pmPrefetchManager.prefetchCreatives(bannerHeaderBiddingAdRequest);
    }

    /**
     * Returns the unique id against the PublisherAdView. It is being used as a ImpressionId.
     * @param adView
     * @return Returns id of hash value against given PublisherAdView
     */
    private String getUniqueIdForView(final PublisherAdView adView) {
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
                              PublisherAdView publisherAdView;
                              Set<PublisherAdView> adViewsSet = getAdViewsSet();

                              if (hBResponse != null) {
                                  // Loop over all those publisherAdViews that participated in Header Bidding i.e. have a valid impressionId mapped to them.
                                  for (AdSlotInfo adSlotInfo : adSlotInfoList) {
                                      publisherAdView = adSlotInfo.adView;

                                      try {
                                          String impressionId = getUniqueIdForView(adSlotInfo.adView);

                                          //Fetch the winning bid details for the impressionId & send to DFP
                                          PMBid pubResponse = hBResponse.get(impressionId);

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
                                          Log.e(TAG, "Error while sending request to DFP :: " + ex.toString());
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

    private Set<PublisherAdView> getAdViewsSet() {

        Set<PublisherAdView> adViewSet = new HashSet<>();

        if(adSlotInfoList != null)
        {
            for (AdSlotInfo adSlotInfo : adSlotInfoList)
            {
                adViewSet.add(adSlotInfo.adView);
            }
        }

        return adViewSet;
    }

    private void registerAdListener(List<AdSlotInfo> adSlotInfoList) {

        for(AdSlotInfo adSlotInfo : adSlotInfoList)
        {
            if (adSlotInfo!=null && adSlotInfo.adView != null)
                adSlotInfo.adView.setAdListener(new DfpAdListener());
        }
    }

    private void registerListenerForPublisherAdView(final List<AdSlotInfo> adSlotInfoList) {

        for(final AdSlotInfo adSlotInfo : adSlotInfoList)
        {
            if (adSlotInfo!=null && adSlotInfo.adView != null)
            {
                adSlotInfo.adView.setAppEventListener(new AppEventListener() {
                    @Override
                    public void onAppEvent(String key, final String impressionId) {
                        Log.d(TAG, "AppEvent Key:" + key + " AdSlotId:" + impressionId);

                        if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {

                            PMBannerAdView adView = new PMBannerAdView(mContext);

                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(adSlotInfo.adView.getLayoutParams());
                            layoutParams.width = adSlotInfo.adView.getMeasuredWidth();
                            layoutParams.height = adSlotInfo.adView.getMeasuredHeight();
                            adView.setLayoutParams(layoutParams);
                            adView.setUseInternalBrowser(true);

                            //Display PubMatic Cached Ad
                            pmPrefetchManager.renderPubMaticAd(impressionId, adView);

                            //Replace view with pubmatic Adview.
                            ViewGroup parent = (ViewGroup) adSlotInfo.adView.getParent();
                            if (parent != null) {
                                parent.removeView(adSlotInfo.adView);
                                parent.addView(adView, adSlotInfo.adView.getLayoutParams());
                            }
                        }
                    }
                });
            }
        }
    }

    private PMBannerPrefetchRequest getHeaderBiddingBannerAdRequest()
    {
        PMBannerPrefetchRequest adRequest;

        List<PMBannerImpression> bannerImpressions = new ArrayList<>();
        for(AdSlotInfo adSlotInfo : adSlotInfoList) {
            bannerImpressions.add(new PMBannerImpression(getUniqueIdForView(adSlotInfo.adView), adSlotInfo.slotName, adSlotInfo.adSizes, 1));
        }

        adRequest = PMBannerPrefetchRequest.initHBRequestForImpression(mContext, "31400", bannerImpressions);

        adRequest.setStoreURL("http://www.financialexpress.com");
        adRequest.setAppDomain("www.financialexpress.com");
        adRequest.setApplicationPaid(false);
        adRequest.setAWT(PubMaticAdRequest.AWT_OPTION.WRAPPED_IN_IFRAME);
        adRequest.setPMZoneId("1");
        adRequest.addKeyword("entertainment");
        adRequest.addKeyword("sports");
        adRequest.setEthnicity(PubMaticAdRequest.ETHNICITY.ASIAN_AMERICAN);
        adRequest.setIncome("income");

        adRequest.setYearOfBirth("1989");
        adRequest.setGender("M");

        adRequest.setCity("Pune");
        adRequest.setZip("411011");
        adRequest.setCoppa(true);
        adRequest.setOrmmaComplianceLevel(1);

        adRequest.setIABCategory("IAB1-1,IAB1-7");
        adRequest.setAppCategory("Entertainment");

        return adRequest;
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

}
