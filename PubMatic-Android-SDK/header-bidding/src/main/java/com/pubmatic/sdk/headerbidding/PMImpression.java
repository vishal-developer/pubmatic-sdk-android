package com.pubmatic.sdk.headerbidding;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;

/**
 * Created by Sagar on 10/4/2016.
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
        if(id == null || id.equals("")) {
            return false;
        }

        if(adSlotId == null || adSlotId.equals("")) {
            return false;
        }

        return true;
    }
}
