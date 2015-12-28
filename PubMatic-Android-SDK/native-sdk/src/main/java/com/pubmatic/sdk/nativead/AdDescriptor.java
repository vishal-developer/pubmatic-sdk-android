/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2014
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

package com.pubmatic.sdk.nativead;

import java.util.ArrayList;
import java.util.Map;

/**
 * This class is used for parsing ad response JSON/XML from Mocean ad server.
 */
public class AdDescriptor {

	private final Map<String, String> adInfo;
	private ArrayList<String> mImpressionTrackers = new ArrayList<String>();
	private ArrayList<String> mClickTrackers = new ArrayList<String>();
	private MediationData mMediationData = null;
	/*
	 * This parameter will be used for Native Ads. It will be set when error
	 * occurs while serving the Native ad and server returns a json with error.
	 */
	private String errorMessage = null;

	AdDescriptor() {
		this.adInfo = null;
	}

	public AdDescriptor(Map<String, String> adInfo) {
		this.adInfo = adInfo;
	}

	public String getType() {
		String value = adInfo.get("type");
		return value;
	}

	public String getWidth() {
		String value = adInfo.get("width");
		return value;
	}

	public String getHeight() {
		String value = adInfo.get("height");
		return value;
	}

	public String getSubType() {
		String value = adInfo.get("subtype");
		return value;
	}

	public String getURL() {
		String value = adInfo.get("url");
		return value;
	}

	public String getTrack() {
		String value = adInfo.get("track");
		return value;
	}

	public String getImage() {
		String value = adInfo.get("img");
		return value;
	}

	public String getImageType() {
		String value = adInfo.get("imgType");
		return value;
	}

	public String getText() {
		String value = adInfo.get("text");
		return value;
	}

	public String getContent() {
		String value = adInfo.get("content");
		return value;
	}

	public String getAdCreativeId() {
		String value = adInfo.get("creativeid");
		return value;
	}

	public ArrayList<String> getImpressionTrackers() {
		return mImpressionTrackers;
	}

	public void setImpressionTrackers(ArrayList<String> mImpressionTrackers) {
		this.mImpressionTrackers.clear();
		this.mImpressionTrackers = mImpressionTrackers;
	}

	/**
	 * Get click trackers list is received from server
	 * 
	 * @return List of click tracker URL's
	 */
	public ArrayList<String> getClickTrackers() {
		return mClickTrackers;
	}

	/**
	 * Set the list of click tracker url's
	 * 
	 * @param clickTrackers
	 */
	public void setClickTrackers(ArrayList<String> clickTrackers) {
		if (this.mClickTrackers != null) {
			this.mClickTrackers.clear();
		}
		this.mClickTrackers = clickTrackers;
	}

	/**
	 * Get the mediation data received 																																																													in case of third-party mediation response
	 */
	public MediationData getMediationData() {
		return mMediationData;
	}

	/**
	 * Set the mediation data received in case of third-party mediation response
	 */
	public void setMediationData(MediationData mediationData) {
		this.mMediationData = mediationData;
	}

	/**
	 * Returns the error message if an error occurs in case of Native Ad.
	 * 
	 * @return - errorMessage
	 */
	String getErrroMessage() {
		return errorMessage;
	}
}