package com.pubmatic.sdk.headerbidding;

import java.util.List;

/**
 * Created by Sagar on 10/5/2016.
 */

public class PMBannerImpression extends PMImpression {

    private List<PMAdSize> adSizes;
    private boolean interstitial;

    public PMBannerImpression(String id, String adSlotId, List adSizes, int adSlotIndex)
    {
        super(id, adSlotId, adSlotIndex);
        this.adSizes = adSizes;
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

    protected boolean validate()
    {
        if(super.validate()) {
            if (adSizes.size() == 0)
                return false;
        }
        else
            return false;

        return true;
    }
}
