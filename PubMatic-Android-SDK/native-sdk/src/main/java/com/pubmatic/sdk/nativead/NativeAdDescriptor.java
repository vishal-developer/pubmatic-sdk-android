/*
 * 
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL
 * 
 * Unpublished Copyright (c) 2006-2017 PubMatic, All Rights Reserved.
 * 
 * 
 * 
 * NOTICE: All information contained herein is, and remains the property of
 * PubMatic. The intellectual and technical concepts contained
 * 
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign
 * Patents, patents in process, and are protected by trade secret or copyright
 * law.
 * 
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained
 * 
 * from PubMatic. Access to the source code contained herein is hereby forbidden
 * to anyone except current PubMatic employees, managers or contractors who have
 * executed
 * 
 * Confidentiality and Non-disclosure agreements explicitly covering such
 * access.
 * 
 * 
 * 
 * The copyright notice above does not evidence any actual or intended
 * publication or disclosure of this source code, which includes
 * 
 * information that is confidential and/or proprietary, and is a trade secret,
 * of PubMatic. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC
 * PERFORMANCE,
 * 
 * OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS
 * WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE
 * 
 * LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE
 * CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * 
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR
 * SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */

package com.pubmatic.sdk.nativead;

import java.util.Arrays;
import java.util.List;

import com.pubmatic.sdk.common.AdResponse.Renderable;
import com.pubmatic.sdk.nativead.bean.PMAssetResponse;

public final class NativeAdDescriptor extends AdDescriptor implements Renderable {

    private String click = null;
    private String[] impressionTrackers = null;
    private String jsTracker = null;
    private String[] clickTrackers = null;
    private String nativeAdJSON = null;
    private int nativeVersion = 0;
    private String mediation = null; // mediation partner name
    private String mediationId = null; // id of the mediation partner
    private String fallbackUrl = null;
    private List<PMAssetResponse> nativeAssetList = null;

    /**
     * Denotes whether the response received is of mediation or native
     */
    private boolean typeMediation = false;

    /**
     * Source of the advertisement, can be direct or mediation.
     */
    private String source = null;

    /**
     * @param clickUrl
     * @param fallbackUrl
     * @param impressionTrackers
     * @param clickTrackers
     * @param jsTrackerString
     * @param nativeAssetList
     */
    public NativeAdDescriptor(int nativeVersion2, String clickUrl, String fallbackUrl,
            String[] impressionTrackers, String[] clickTrackers, String jsTrackerString,
            List<PMAssetResponse> nativeAssetList) {
        this.click = clickUrl;
        this.impressionTrackers = impressionTrackers;
        this.clickTrackers = clickTrackers;
        this.jsTracker = jsTrackerString;
        this.fallbackUrl = fallbackUrl;
        this.nativeAssetList = nativeAssetList;
        this.nativeVersion = nativeVersion2;
        typeMediation = false;
    }

    /**
     * @param mediation
     * @param mediation
     * @param mediationId
     * @param impressionTrackers
     * @param clickTrackers
     * @param jsTrackerString
     */
    public NativeAdDescriptor(String mediation, String mediationId,
            String source, String[] impressionTrackers, String[] clickTrackers, String jsTrackerString) {
        super();
        this.mediation = mediation;
        this.mediationId = mediationId;
        this.source = source;
        this.impressionTrackers = impressionTrackers;
        this.clickTrackers = clickTrackers;
        this.jsTracker = jsTrackerString;

        typeMediation = true;
    }

    /**
     * Returns the click url
     * 
     * @return the click
     */
    public String getClick() {
        return click;
    }

    /**
     * Set the click url
     * 
     * @param click
     */
    public void setClick(String click) {
        this.click = click;
    }

    public void setFallbackUrl(String fallbackUrl) {
        this.fallbackUrl = fallbackUrl;
    }

    /**
     * @return the fallbackUrl to be used if click url deep-link does not work
     *         on device
     */
    public String getFallbackUrl() {
        return fallbackUrl;
    }

    /**
     * @return the impressionTrackers
     */
    public String[] getNativeAdImpressionTrackers() {
        return impressionTrackers;
    }

    /**
     * @return the clickTrackers
     */
    public String[] getNativeAdClickTrackers() {
        return clickTrackers;
    }

    /**
     * @return the jsTracker received in native json response.
     */
    public String getJsTracker() {
        return jsTracker;
    }

    /**
     * Setter for jsTracer
     * 
     * @param jsTracker
     */
    public void setJsTracker(String jsTracker) {
        this.jsTracker = jsTracker;
    }

    /**
     * @return the nativeAdJSON
     */
    public String getNativeAdJSON() {
        return nativeAdJSON;
    }

    /**
     * 
     * @param nativeAdJSON
     */
    public void setNativeAdJSON(String nativeAdJSON) {
        this.nativeAdJSON = nativeAdJSON;
    }

    /**
     * @return the mediation
     */
    String getMediation() {
        return mediation;
    }

    /**
     * @param mediation
     *            the mediation to set
     */
    void setMediation(String mediation) {
        this.mediation = mediation;
    }

    /**
     * @return the mediationId
     */
    String getMediationId() {
        return mediationId;
    }

    /**
     * @param mediationId
     *            the feedId to set
     */
    void setMediationId(String mediationId) {
        this.mediationId = mediationId;
    }

    /**
     * @return the source
     */
    String getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    void setSource(String source) {
        this.source = source;
    }

    /**
     * Denotes whether response received is of type mediation or native.
     * 
     * @return
     */
    boolean isTypeMediation() {
        return typeMediation;
    }

    public List<PMAssetResponse> getNativeAssetList() {
        return nativeAssetList;
    }

    public void setNativeAssetList(List<PMAssetResponse> nativeAssetList) {
        this.nativeAssetList = nativeAssetList;
    }

    public int getNativeVersion() {
        return nativeVersion;
    }

    public void setNativeVersion(int nativeVersion) {
        this.nativeVersion = nativeVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NativeAdDescriptor [click=" + click + ", impressionTrackers=" + Arrays.toString(impressionTrackers)
                + ", clickTrackers=" + Arrays.toString(clickTrackers) + ", nativeAdJSON=" + nativeAdJSON + ", mediation=" + mediation + ", mediationId=" + mediationId
                + " nativeVersion=" + nativeVersion + " ]";
    }

	@Override
	public Object getRenderable() {
		// TODO Auto-generated method stub
		return null;
	}

}
