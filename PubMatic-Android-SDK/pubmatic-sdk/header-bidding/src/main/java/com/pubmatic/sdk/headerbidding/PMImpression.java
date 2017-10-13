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

import android.text.TextUtils;

import com.pubmatic.sdk.common.PMLogger;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

/**
 *
 */

public class PMImpression {

    private String id;
    private String adSlotId;
    private int adSlotIndex;
    private Map<String, JSONArray> keyValue;

    public PMImpression(String id, String adSlotId, int adSlotIndex)
    {
        this.id = id;
        this.adSlotId = adSlotId;
        this.adSlotIndex = adSlotIndex;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdSlotId() {
        return adSlotId;
    }

    public void setAdSlotId(String adSlotId) {
        this.adSlotId = adSlotId;
    }

    public int getAdSlotIndex() {
        return adSlotIndex;
    }

    public void setAdSlotIndex(int adSlotIndex) {
        this.adSlotIndex = adSlotIndex;
    }

    public Map<String, JSONArray> getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(Map<String, JSONArray> keyValue) {
        this.keyValue = keyValue;
    }

    protected boolean validate()
    {
        if(TextUtils.isEmpty(id)) {
            PMLogger.logEvent("Invalid Impression ID found for Impression", PMLogger.PMLogLevel.Debug);
            return false;
        } else if(TextUtils.isEmpty(adSlotId)) {
            PMLogger.logEvent("Invalid Slot ID found for Impression", PMLogger.PMLogLevel.Debug);
            return false;
        }
        return true;
    }
}
