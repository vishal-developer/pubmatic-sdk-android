/*

 * PubMatic Inc. ("PubMatic") CONFIDENTIAL

 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.

 *

 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained

 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.

 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained

 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed 

 * Confidentiality and Non-disclosure agreements explicitly covering such access.

 *

 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes  

 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, 

 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE 

 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  

 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.                

 */

package com.moceanmobile.mast;

import java.util.Arrays;

import com.moceanmobile.mast.MASTNativeAd.Image;

public final class NativeAdDescriptor extends AdDescriptor {

	private String title = null;
	private String description = null;
	private String creativeId = null;
	private String feedId = null;
	private MASTNativeAd.Image iconImage = null;
	private MASTNativeAd.Image mainImage = null;
	private MASTNativeAd.Image logoImage = null;
	private String click = null;
	private String callToActionText = null;
	private String vastTag = null;
	private float rating = 0.0f;
	private long downloads = 0;
	private String[] impressionTrackers = null;
	private String[] clickTrackers = null;
	private String nativeAdJSON = null;
	private String subtype = "";
	private String mediation = null; // mediation partner name
	private String mediationId = null; // id of the mediation partner
	private String fallbackUrl = null;
	/**
	 * Denotes whether the response received is of mediation or native
	 */
	private boolean typeMediation = false;

	/*
	 * Source of the advertisement, can be direct or mediation.
	 */
	private String source = null;
	// @formatter:off
	// may be of in the form "mediationData":{ "adid":"<ad id>"} 
	private String adUnitId = null; 
	// @formatter:on

	/**
	 * @param subtype
	 * @param title
	 * @param description
	 * @param mainImage
	 * @param iconImage
	 * @param logoImage
	 * @param click
	 * @param callToActionText
	 * @param rating
	 * @param downloads
	 * @param vastTag
	 * @param impressionTrackers
	 * @param clickTrackers
	 * @param fallbackUrl
	 */
	NativeAdDescriptor(String subtype, String title, String description,
			Image mainImage, Image iconImage, Image logoImage, String click,
			String callToActionText, float rating, long downloads,
			String vastTag, String[] impressionTrackers,
			String[] clickTrackers, String fallbackUrl) {
		this.subtype = subtype;
		this.title = title;
		this.description = description;
		this.click = click;
		this.callToActionText = callToActionText;
		this.rating = rating;
		this.downloads = downloads;
		this.impressionTrackers = impressionTrackers;
		this.clickTrackers = clickTrackers;
		this.vastTag = vastTag;
		this.iconImage = iconImage;
		this.mainImage = mainImage;
		this.logoImage = logoImage;
		this.fallbackUrl = fallbackUrl;

		typeMediation = false;
	}

	/**
	 * @param subtype
	 * @param creativeId
	 * @param mediation
	 * @param mediation
	 * @param mediationId
	 * @param adUnitId
	 * @param impressionTrackers
	 * @param clickTrackers
	 * @param feedId
	 */
	NativeAdDescriptor(String subtype, String creativeId, String mediation,
			String mediationId, String adUnitId, String source,
			String[] impressionTrackers, String[] clickTrackers, String feedId) {
		super();
		this.subtype = subtype;
		this.creativeId = creativeId;
		this.mediation = mediation;
		this.mediationId = mediationId;
		this.adUnitId = adUnitId;
		this.source = source;
		this.impressionTrackers = impressionTrackers;
		this.clickTrackers = clickTrackers;
		this.feedId = feedId;

		typeMediation = true;
	}

	/**
	 * @return the title
	 */
	public String getNativeAdTitle() {
		return title;
	}

	public void setNativeAdTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the description
	 */
	public String getNativeAdText() {
		return description;
	}

	public void setNativeAdText(String text) {
		this.description = text;
	}

	/**
	 * @return the mainImageUrl
	 */
	public Image getMainImage() {
		return mainImage;
	}

	public void setMainImage(Image mainImage) {
		this.mainImage = mainImage;
	}

	/**
	 * @return the iconImageUrl
	 */
	public Image getIconImage() {
		return iconImage;
	}

	public void setIconImage(Image iconImage) {
		this.iconImage = iconImage;
	}

	public void setLogoImage(MASTNativeAd.Image logoImage) {
		this.logoImage = logoImage;
	}

	/**
	 * @return the logoImage
	 */
	public MASTNativeAd.Image getLogoImage() {
		return logoImage;
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
	 * @return the call to action description
	 */
	public String getNativeAdCallToAction() {
		return callToActionText;
	}

	public void setNativeAdCallToActionText(String callToActionText) {
		this.callToActionText = callToActionText;
	}

	/**
	 * @return the rating
	 */
	public float getNativeAdRating() {
		return rating;
	}

	/**
	 * Set the native rating
	 * 
	 * @param rating
	 */
	public void setNativeAdRating(float rating) {
		this.rating = rating;
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
	 * @return the vastTag
	 */
	public String getVastTag() {
		return vastTag;
	}

	/**
	 * @return the downloads
	 */
	public long getDownloads() {
		return downloads;
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
	void setNativeAdJSON(String nativeAdJSON) {
		this.nativeAdJSON = nativeAdJSON;
	}

	/**
	 * @return the subtype
	 */
	String getSubtype() {
		return subtype;
	}

	/**
	 * @param subtype
	 *            the subtype to set
	 */
	void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	/**
	 * @return the creativeId
	 */
	String getCreativeId() {
		return creativeId;
	}

	/**
	 * @param creativeId
	 *            the creativeId to set
	 */
	void setCreativeId(String creativeId) {
		this.creativeId = creativeId;
	}

	/**
	 * @param feedId
	 *            Ad feed partner identifier in Mocean
	 */
	public void setFeedId(String feedId) {
		this.feedId = feedId;
	}

	/**
	 * 
	 * @return feedId Ad feed partner identifier in Mocean
	 */
	public String getFeedId() {
		return feedId;
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
	 * 
	 * @return
	 */
	String getAdUnitId() {
		return adUnitId;
	}

	/**
	 * 
	 * @param adUnitId
	 */
	void setMediationData(String adUnitId) {
		this.adUnitId = adUnitId;
	}

	/**
	 * Denotes whether response received is of type mediation or native.
	 * 
	 * @return
	 */
	boolean isTypeMediation() {
		return typeMediation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NativeAdDescriptor [title=" + title + ", description="
				+ description + ", iconImage=" + iconImage + ", mainImage="
				+ mainImage + ", click=" + click + ", callToActionText="
				+ callToActionText + ", rating=" + rating
				+ ", impressionTrackers=" + Arrays.toString(impressionTrackers)
				+ ", clickTrackers=" + Arrays.toString(clickTrackers)
				+ ", nativeAdJSON=" + nativeAdJSON + ", subtype=" + subtype
				+ ", mediation=" + mediation + ", mediationId=" + mediationId
				+ ", adUnitId=" + adUnitId + "]";
	}

}
