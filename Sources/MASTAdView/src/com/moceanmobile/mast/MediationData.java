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
package com.moceanmobile.mast;

/**
 * In case if third party response is received from the server (with
 * source=mediation), this class contains the details of third party network.<br>
 * Please note that MediationData will be null when mediation source is direct.
 */
public class MediationData {
	private String mediationNetworkId;
	private String mediationNetworkName;
	private String mediationSource;
	private String mediationAdId;
	private String mediationAdFormat;

	public MediationData(String mediationNetworkId,
			String mediationNetworkName, String mediationSource,
			String mediationAdId, String mediationAdFormat) {
		this.mediationNetworkId = mediationNetworkId;
		this.mediationNetworkName = mediationNetworkName;
		this.mediationAdFormat = mediationAdFormat;
		this.mediationSource = mediationSource;
		this.mediationAdId = mediationAdId;
	}

	public MediationData() {
		// Default constructor.
	}

	/**
	 * @return the mediationAdFormat
	 */
	public String getMediationAdFormat() {
		return mediationAdFormat;
	}

	/**
	 * @param mediationAdFormat
	 *            the mediationAdFormat to set
	 */
	public void setMediationAdFormat(String mediationAdFormat) {
		this.mediationAdFormat = mediationAdFormat;
	}

	/**
	 * @return the mediationNetworkId
	 */
	public String getMediationNetworkId() {
		return mediationNetworkId;
	}

	/**
	 * @param mediationNetworkId
	 *            the mediationNetworkId to set
	 */
	public void setMediationNetworkId(String mediationNetworkId) {
		this.mediationNetworkId = mediationNetworkId;
	}

	/**
	 * @return the mediationNetworkName
	 */
	public String getMediationNetworkName() {
		return mediationNetworkName;
	}

	/**
	 * @param mediationNetworkName
	 *            the mediationNetworkName to set
	 */
	public void setMediationNetworkName(String mediationNetworkName) {
		this.mediationNetworkName = mediationNetworkName;
	}

	/**
	 * @return the mediationSource
	 */
	public String getMediationSource() {
		return mediationSource;
	}

	/**
	 * @param mediationSource
	 *            the mediationSource to set
	 */
	public void setMediationSource(String mediationSource) {
		this.mediationSource = mediationSource;
	}

	/**
	 * @return the mediationAdId
	 */
	public String getMediationAdId() {
		return mediationAdId;
	}

	/**
	 * @param mediationAdId
	 *            the mediationAdId to set
	 */
	public void setMediationAdId(String mediationAdId) {
		this.mediationAdId = mediationAdId;
	}

	@Override
	public String toString() {
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append("MediationData:{");
		strBuffer.append("ID:");
		strBuffer.append(this.getMediationNetworkId());
		strBuffer.append(", Name:");
		strBuffer.append(this.getMediationNetworkName());
		strBuffer.append(", Source:");
		strBuffer.append(this.getMediationSource());
		strBuffer.append(", AdFormat:");
		strBuffer.append(this.getMediationAdFormat());
		strBuffer.append(", AdId:");
		strBuffer.append(this.getMediationAdId());
		strBuffer.append("}");
		return strBuffer.toString();
	}

}