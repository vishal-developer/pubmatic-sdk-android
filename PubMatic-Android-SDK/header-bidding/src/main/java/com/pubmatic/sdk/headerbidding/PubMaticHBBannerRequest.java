package com.pubmatic.sdk.headerbidding;

import android.content.Context;

import com.pubmatic.sdk.banner.pubmatic.PubMaticBannerAdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.pubmatic.PubMaticConstants;

import java.util.HashSet;
import java.util.Set;

public class PubMaticHBBannerRequest extends PubMaticBannerAdRequest {

    private PubMaticHBBannerRequest(Context context) {
        super(context);
    }

    private Set<String> adSlotIdsHB;

    /**
     * Create a Banner Ad Request for Header Bidding using single adSlotId.
     *
     * @param context
     * @param adSlotId
     * @return
     */
    public static PubMaticHBBannerRequest initHBRequestForAdSlotId(Context context, String adSlotId) {
        PubMaticHBBannerRequest adRequest = createHeaderBiddingRequest(context);
        adRequest.adSlotIdsHB.add(adSlotId);
        return adRequest;
    }

    /**
     * Create a Banner Ad Request for Header Bidding  using a set of adSlotIds
     *
     * @param context   Activity context
     * @param adSlotIds Set of adSlotIds
     * @return
     */
    public static PubMaticHBBannerRequest initHBRequestForAdSlotIds(Context context, Set<String> adSlotIds) {
        PubMaticHBBannerRequest adRequest = createHeaderBiddingRequest(context);
        adRequest.adSlotIdsHB.addAll(adSlotIds);
        return adRequest;
    }

    private static PubMaticHBBannerRequest createHeaderBiddingRequest(Context context) {

        PubMaticHBBannerRequest adRequest = new PubMaticHBBannerRequest(context);
        adRequest.setAdServerURL(CommonConstants.HEADER_BIDDING_HASO_URL);
        adRequest.adSlotIdsHB = new HashSet<>();
        return adRequest;
    }

    /**
     * Returns a new Set containing all the current adSlotIds participating in header bidding.
     */
    public Set<String> getAdSlotIdsHB() {
        HashSet<String> adSlotIdsCopySet = new HashSet<String>();
        if (adSlotIdsHB == null)
            adSlotIdsHB = new HashSet<String>();

        adSlotIdsCopySet.addAll(adSlotIdsHB);
        return adSlotIdsCopySet;
    }

    /**
     * Add a new adSlotId to compete for header bidding via PubMatic.
     *
     * @param adSlotId
     */
    public void addAdSlotIdsForHeaderBidding(String adSlotId) {
        adSlotIdsHB.add(adSlotId);
    }

    /**
     * Update the adSlotIds participating in Header bidding.
     * Note : This will remove all previous adSlotIds registered on this AdRequest instance.
     *
     * @param adSlotIds
     */
    private void setAdSlotIdsForHeaderBidding(Set<String> adSlotIds) {
        adSlotIdsHB.clear();
        adSlotIdsHB.addAll(adSlotIds);

    }

    /**
     * Unregister all adSlotIds participating in Header bidding.
     */
    public void clearAdSlotIdsForHeaderBidding() {
        adSlotIdsHB.clear();
    }


    public void createRequest(Context context) {
        super.createRequest(context);
        putPostData(PubMaticConstants.SA_VERSION, "1000");
        putPostData(PubMaticConstants.GRS, "4");

        // Build a comma separated list of adSlotIds
        StringBuilder sb = new StringBuilder();
        for (String id : adSlotIdsHB)
            sb.append(id + ",");
        String adSlotIds = "[" + sb.substring(0, sb.length() - 1) + "]";

        putPostData(PubMaticConstants.AD_SLOT_IDS, adSlotIds);
    }

}
