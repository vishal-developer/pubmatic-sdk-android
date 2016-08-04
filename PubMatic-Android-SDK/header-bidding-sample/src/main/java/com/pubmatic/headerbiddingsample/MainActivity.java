package com.pubmatic.headerbiddingsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.pubmatic.sdk.headerbidding.PubMaticPrefetchManager;
import com.pubmatic.sdk.headerbidding.PubMaticHBBannerRequest;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String BID = "bid";
    private static final String BID_ID = "bidid";
    private static final String BID_STATUS = "bidstatus";
    private static final String ECPM = "ecpm";
    PubMaticPrefetchManager headerBiddingManager;

    // To track all adViews on this page.
    private Set<PublisherAdView> adViews = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PublisherAdView adView1 = (PublisherAdView) findViewById(R.id.publisherAdView1);
        PublisherAdView adView2 = (PublisherAdView) findViewById(R.id.publisherAdView2);
        adViews.add(adView1);
        adViews.add(adView2);

        PubMaticPrefetchManager.PrefetchListener listener = new PubMaticPrefetchManager.PrefetchListener() {
            @Override
            public void onBidsFetched() {
                // Header bidding completed. Now send the custom data to DFP.
                Log.d(TAG, "onBidsFetched");
                requestDFPAd(true);
            }

            @Override
            public void onBidsFailed(String errorMessage) {
                Log.d(TAG, "Header Bidding failed. " + errorMessage);
                // Get on with requesting DFP for ads without HB data.
                requestDFPAd(false);
            }
        };

        // Create instance of PubMaticPrefetchManager and set listener for bidding status.
        headerBiddingManager = new PubMaticPrefetchManager(this);
        headerBiddingManager.setPrefetchListener(listener);
        headerBiddingManager.generateAdSlotsForViews(adView1, adView2);

        //Normal ad Events listener for DFP calls.
        registerAdListener(adView1, adView2);

        //Create Pubmatic adRequest for header bidding call with single adSlotId or a Set of adSlotIds.
        PubMaticHBBannerRequest adRequest = PubMaticHBBannerRequest.initHBRequestForAdSlotIds(this, headerBiddingManager.getAdSlotAdViewMap().keySet());
        /*
        Set any targeting params on the adRequest instance.
         */

        headerBiddingManager.executeHeaderBiddingRequest(this, adRequest);
    }

    /**
     * Send ad Request for all DFP adViews.
     */
    private void requestDFPAd(final boolean headerBiddingSuccess) {

        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              PublisherAdRequest adRequest = null;
                              PublisherAdView publisherAdView;
                              Set<PublisherAdView> adViewsSet = getAdViewsSet();

                              if (headerBiddingSuccess) {
                                  // Loop over all those publisherAdViews that participated in Header Bidding i.e. have a valid adSlotId mapped to them.
                                  for (Map.Entry<String, PublisherAdView> entry : headerBiddingManager.getAdSlotAdViewMap().entrySet()) {
                                      publisherAdView = entry.getValue();

                                      try {
                                          String adSlot = entry.getKey();
                                          JSONObject pubResponse = headerBiddingManager.getPrefetchCreativeForAdSlotId(adSlot);

                                          adRequest = new PublisherAdRequest.Builder().addCustomTargeting(BID_ID, adSlot)
                                                  .addCustomTargeting(BID_STATUS, pubResponse.getString(BID_STATUS))
                                                  .addCustomTargeting(BID, pubResponse.getString(ECPM)).build();

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
     * Get copy of the current set of all adViews.
     *
     * @return
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

