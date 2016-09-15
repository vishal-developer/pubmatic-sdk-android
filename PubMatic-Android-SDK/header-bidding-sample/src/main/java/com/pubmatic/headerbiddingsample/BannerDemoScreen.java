package com.pubmatic.headerbiddingsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.headerbidding.PubMaticHBBannerRequest;
import com.pubmatic.sdk.headerbidding.PubMaticPrefetchManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class BannerDemoScreen extends AppCompatActivity {

    private static final String TAG = "BannerDemoScreen";
    private static final String ATTHERATE = "@";
    private static final String BYSYMBOL = "x";
    private static final String BID = "bid";
    private static final String BID_ID = "bidid";
    private static final String BID_STATUS = "bidstatus";
    private static final String ECPM = "ecpm";
    private static final String PUBMATIC_WIN_KEY = "pubmaticdm";
    PubMaticPrefetchManager headerBiddingManager;

    // To track all adViews on this page.
    private Set<PublisherAdView> adViews = new HashSet<>();
    private WeakHashMap<String, PublisherAdView> adSlotAdViewMap = new WeakHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        PublisherAdView adView1 = (PublisherAdView) findViewById(R.id.publisherAdView1);
        PublisherAdView adView2 = (PublisherAdView) findViewById(R.id.publisherAdView2);
        adViews.add(adView1);
        adViews.add(adView2);

        PubMaticPrefetchManager.PrefetchListener listener = new PubMaticPrefetchManager.PrefetchListener() {
            @Override
            public void onBidsFetched(Map<String, JSONObject> hBResponse) {
                // Header bidding completed. Now send the custom data to DFP.
                Log.d(TAG, "onBidsFetched");
                requestDFPAd(hBResponse);
            }

            @Override
            public void onBidsFailed(String errorMessage) {
                Log.d(TAG, "Header Bidding failed. " + errorMessage);
                // Get on with requesting DFP for ads without HB data.
                requestDFPAd(null);
            }
        };

        // Create instance of PubMaticPrefetchManager and set listener for bidding status.
        headerBiddingManager = new PubMaticPrefetchManager(this);
        headerBiddingManager.setPrefetchListener(listener);
        adSlotAdViewMap = generateAdSlotsForViews(adView1, adView2);

        //Normal ad Events listener for DFP calls.
        registerAdListener(adView1, adView2);
        registerListenerForPublisherAdView(adView1, adView2);

        //Create Pubmatic adRequest for header bidding call with single adSlotId or a Set of adSlotIds.
        PubMaticHBBannerRequest adRequest = PubMaticHBBannerRequest.initHBRequestForAdSlotIds(this, adSlotAdViewMap.keySet());
        /*
        Set any targeting params on the adRequest instance.
         */

        headerBiddingManager.executeHeaderBiddingRequest(this, adRequest);
    }

    /**
     * Generate unique tag/adSlotId for every adView. In case of same dimension adView, append ":n"
     * where n is the number of identical size adtags seen so far.
     *
     * @param publisherAdViews Pass all PublisherAdViews on current page, comma-separated.
     */
    public WeakHashMap<String, PublisherAdView> generateAdSlotsForViews(PublisherAdView... publisherAdViews) {

        Map<String, Integer> adSlotsCounts = new HashMap<>();
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
        adSlotsCounts.clear();
        return adSlotAdViewMap;
    }

    /**
     * Send ad Request for all DFP adViews.
     */
    private void requestDFPAd(final Map<String, JSONObject> hBResponse) {

        runOnUiThread(new Runnable() {
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
                                          JSONObject pubResponse = hBResponse.get(adSlot);

                                          adRequest = new PublisherAdRequest.Builder().addCustomTargeting(BID_ID, adSlot)
                                                  .addCustomTargeting(BID_STATUS, pubResponse.getString(BID_STATUS))
                                                  .addCustomTargeting(BID, "17.12").build();

                                          publisherAdView.loadAd(adRequest);
                                          adViewsSet.remove(publisherAdView);
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

    /**
     * Register normal DFP callbacks on all adViews.
     *
     * @param publisherAdViews
     */
    private void registerAdListener(PublisherAdView... publisherAdViews) {
        for (PublisherAdView adView : publisherAdViews)
            if (adView != null)
                adView.setAdListener(new DfpAdListener());
    }

    /**
     * Register AppEventListener for all PublisherAdViews.
     * Listens if PubMatic has won via header bidding.
     */
    private void registerListenerForPublisherAdView(PublisherAdView... publisherAdViews) {

        for (final PublisherAdView publisherAdView : publisherAdViews) {
            publisherAdView.setAppEventListener(new AppEventListener() {
                @Override
                public void onAppEvent(String key, final String adSlotId) {
                    Log.d(TAG, "onAppEvent() Key: " + key + " AdSlotId: " + adSlotId);

                    if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {
                        //Display PubMatic Cached Ad

                        PMBannerAdView pmView = headerBiddingManager.getRenderedPubMaticAd(BannerDemoScreen.this, adSlotId, publisherAdView);

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

    /**
     * Get copy of the current set of all adViews.
     */
    public Set<PublisherAdView> getAdViewsSet() {
        Set<PublisherAdView> copySet = new HashSet<>();
        copySet.addAll(adViews);
        return copySet;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        headerBiddingManager.destroy();
        headerBiddingManager = null;
        adSlotAdViewMap.clear();

        for (PublisherAdView adView : adViews)
            adView.destroy();

        adViews.clear();
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

