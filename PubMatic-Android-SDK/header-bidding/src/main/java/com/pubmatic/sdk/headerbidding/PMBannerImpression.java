/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2016
 * PubMatic, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained herein are
 * proprietary to PubMatic and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from PubMatic.
 * Access to the source code contained herein is hereby forbidden to anyone
 * except current PubMatic employees, managers or contractors who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 *
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes information
 * that is confidential and/or proprietary, and is a trade secret, of PubMatic.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC
 * DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN
 * CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO
 * REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

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
