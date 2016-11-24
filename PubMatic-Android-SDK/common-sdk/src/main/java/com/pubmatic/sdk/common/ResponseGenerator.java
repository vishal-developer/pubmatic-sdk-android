package com.pubmatic.sdk.common;

/**
 * Created by Sagar on 11/15/2016.
 */

public interface ResponseGenerator {

    public String getTrackingUrl(String impressionId);

    public String getCreative(String impressionId);

    public Double getPrice(String impressionId);

}
