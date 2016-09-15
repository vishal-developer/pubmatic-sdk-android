package com.pubmatic.headerbiddingsample;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.pubmatic.sdk.banner.PMBannerAdView;
import com.pubmatic.sdk.banner.PMInterstitialAdView;
import com.pubmatic.sdk.headerbidding.PubMaticHBBannerRequest;
import com.pubmatic.sdk.headerbidding.PubMaticPrefetchManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class InterstitialDemoScreen extends AppCompatActivity {

    private static final String TAG = "InterstitialDemoScreen";
    private static final String ATTHERATE = "@";
    private static final String BYSYMBOL = "x";
    private static final String BID = "bid";
    private static final String BID_ID = "bidid";
    private static final String BID_STATUS = "bidstatus";
    private static final String ECPM = "ecpm";
    private static final String PUBMATIC_WIN_KEY = "pubmaticdm";
    private PubMaticPrefetchManager headerBiddingManager;

    // To track all adViews on this page.
    private Set<PublisherInterstitialAd> adViews = new HashSet<>();
    private WeakHashMap<String, PublisherInterstitialAd> adSlotAdViewMap = new WeakHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        PublisherInterstitialAd adView = new PublisherInterstitialAd(this);
        adView.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));

        //Create Listener for PubMatic HB call
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
        adSlotAdViewMap = generateAdSlotsForInterstitialViews(adView);

        //Normal ad Events listener for DFP calls.
        registerAdListener(adView);
        registerApEventListener(adView);

        //Create Pubmatic adRequest for header bidding call with single adSlotId or a Set of adSlotIds.
        PubMaticHBBannerRequest adRequest = PubMaticHBBannerRequest.initHBRequestForAdSlotIds(this, adSlotAdViewMap.keySet());

        /*
        Set any targeting params on the adRequest instance.
         */

        headerBiddingManager.executeHeaderBiddingRequest(this, adRequest);
    }

    /**
     * It generates the ad-slots for all PublisherInterstitialAd. Logic for creation of ad-slots may vary per publisher.
     * @param publisherAdViews
     * @return
     */
    private WeakHashMap<String, PublisherInterstitialAd> generateAdSlotsForInterstitialViews(PublisherInterstitialAd... publisherAdViews) {

        //Get Width & Height for creating of adSlots
        int measuredWidth = 0;
        int measuredHeight = 0;
        Point size = new Point();
        WindowManager w = getWindowManager();
        float density  = getResources().getDisplayMetrics().density;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)    {
            w.getDefaultDisplay().getSize(size);
            measuredWidth = Math.round(size.x/density);
            measuredHeight = Math.round(size.y/density);
        }else{
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
            measuredWidth   = Math.round(displayMetrics.widthPixels/density);
            measuredHeight  = Math.round(displayMetrics.heightPixels/density);
        }

        Map<String, Integer> adSlotsCounts = new HashMap<>();
        WeakHashMap<String, PublisherInterstitialAd> adSlotAdViewMap = new WeakHashMap<>();

        for (PublisherInterstitialAd publisherAdView : publisherAdViews) {

            String pubMaticAdUnit = publisherAdView.getAdUnitId();
            pubMaticAdUnit = pubMaticAdUnit.substring(pubMaticAdUnit.lastIndexOf("/")).substring(1);
            String tag = pubMaticAdUnit + ATTHERATE +
                    measuredWidth + BYSYMBOL + measuredHeight;

            if (adSlotsCounts.get(tag) == null) {
                adSlotsCounts.put(tag, 0);
            } else {
                adSlotsCounts.put(tag, adSlotsCounts.get(tag) + 1);
            }
            if (adSlotsCounts.get(tag) != 0)
                tag = tag + ":" + adSlotsCounts.get(tag);

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
                              PublisherInterstitialAd publisherAdView;
                              Set<PublisherInterstitialAd> adViewsSet = new HashSet<>();
                              adViewsSet.addAll(adViews);

                              if (hBResponse != null) {
                                  // Loop over all those publisherAdViews that participated in Header Bidding i.e. have a valid adSlotId mapped to them.
                                  for (Map.Entry<String, PublisherInterstitialAd> entry : adSlotAdViewMap.entrySet()) {
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
                              for (PublisherInterstitialAd ad : adViewsSet) {
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
    private void registerAdListener(PublisherInterstitialAd... publisherAdViews) {
        for (PublisherInterstitialAd adView : publisherAdViews)
            if (adView != null)
                adView.setAdListener(new DfpAdListener());
    }

    /**
     * Register AppEventListener for all PublisherInterstitialAd.
     * Listens if PubMatic has won via header bidding.
     */
    private void registerApEventListener(PublisherInterstitialAd... publisherAdViews) {

        for (final PublisherInterstitialAd publisherAdView : publisherAdViews) {
            publisherAdView.setAppEventListener(new AppEventListener() {
                @Override
                public void onAppEvent(String key, final String adSlotId) {
                    Log.d(TAG, "onAppEvent() Key: " + key + " AdSlotId: " + adSlotId);

                    if (TextUtils.equals(key, PUBMATIC_WIN_KEY)) {
                        //Display PubMatic Cached Ad

                        PMInterstitialAdView pmView = headerBiddingManager.getRenderedPMInterstitialAd(InterstitialDemoScreen.this, adSlotId);

                        //Replace view with pubmatic Adview.
                        ViewGroup parent = (ViewGroup) findViewById(R.id.parent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        headerBiddingManager.destroy();
        headerBiddingManager = null;
        adSlotAdViewMap.clear();
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

