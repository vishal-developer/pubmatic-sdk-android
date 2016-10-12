package com.pubmatic.sdk.headerbidding;

/**
 * Created by Sagar on 10/6/2016.
 */

public class Bid {

    private String slotName;
    private int slotIndex;
    private int gaId;
    private String impressionId;
    private double price;
    private String creative;
    private long width;
    private long height;
    private String trackingUrl;

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public int getGaId() {
        return gaId;
    }

    public void setGaId(int gaId) {
        this.gaId = gaId;
    }

    public String getImpressionId() {
        return impressionId;
    }

    public void setImpressionId(String impressionId) {
        this.impressionId = impressionId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public String getTrackingUrl() {
        return trackingUrl;
    }

    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }

    public String getCreative() {
        return creative;
    }

    public void setCreative(String creative) {
        this.creative = creative;
    }
}
