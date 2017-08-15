package com.pubmatic.sdk.banner.pubmatic;

import android.content.Context;

/**
 *
 */
public class PMInterstitialAdRequest extends PubMaticBannerAdRequest{

    protected PMInterstitialAdRequest(Context context) {
        super(context);
        mIsInterstitial = true;
    }

    /**
     *
     * @param context
     * @param pubId
     * @param siteId
     * @param adId
     * @return
     */
    public static PMInterstitialAdRequest createPubMaticInterstitialAdRequest(Context context, String pubId, String siteId, String adId) {
        PMInterstitialAdRequest adRequest = new PMInterstitialAdRequest(context);
        adRequest.setPubId(pubId);
        adRequest.setSiteId(siteId);
        adRequest.setAdId(adId);
        return adRequest;
    }
}
