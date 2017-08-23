package com.pubmatic.sdk.banner.pubmatic;

import android.content.Context;
import android.text.TextUtils;

/**
 *
 */
public class PMInterstitialAdRequest extends PMBannerAdRequest {

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
    public static PMInterstitialAdRequest createPMInterstitialAdRequest(Context context, String pubId, String siteId, String adId) {
        PMInterstitialAdRequest adRequest = new PMInterstitialAdRequest(context);
        adRequest.setPubId(pubId);
        adRequest.setSiteId(siteId);
        adRequest.setAdId(adId);
        return adRequest;
    }

    /**
     * It returns true if pubID, siteID and adID are set else returns false.
     * @return
     */
    @Override
    public boolean checkMandatoryParams() {
        return !TextUtils.isEmpty(mPubId) && !TextUtils.isEmpty(mSiteId) && !TextUtils.isEmpty(mAdId);
    }
}
