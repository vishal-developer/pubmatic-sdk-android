package com.pubmatic.sdk.headerbidding;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

/**
 * Created by Sagar on 10/4/2016.
 */

public class PMImpression {

    private String adSlotId;
    private int adSlotIndex;
    private Map<String, JSONArray> keyValue;

    public PMImpression(String adSlotId, int adSlotIndex)
    {
        this.adSlotId = adSlotId;
        this.adSlotIndex = adSlotIndex;
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
}
