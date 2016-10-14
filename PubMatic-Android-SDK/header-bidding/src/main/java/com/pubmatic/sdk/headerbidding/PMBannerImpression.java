package com.pubmatic.sdk.headerbidding;

import java.util.List;

/**
 * Created by Sagar on 10/5/2016.
 */

public class PMBannerImpression extends PMImpression {

    private String id;
    private List<PMAdSize> adSizes;
    private boolean interstitial;

    public PMBannerImpression(String id, String adSlotId, List adSizes, int adSlotIndex)
    {
        super(adSlotId, adSlotIndex);
        this.id = id;
        this.adSizes = adSizes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PMAdSize> getAdSizes() {
        return adSizes;
    }

    public void setAdSizes(List<PMAdSize> adSizes) {
        this.adSizes = adSizes;
    }

    public boolean isInterstitial() {
        return interstitial;
    }

    public void setInterstitial(boolean interstitial) {
        this.interstitial = interstitial;
    }
}
