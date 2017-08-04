/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2017
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
package com.pubmatic.sdk.banner.phoenix;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CHANNEL;
import com.pubmatic.sdk.common.PMAdSize;
import com.pubmatic.sdk.common.phoenix.PhoenixAdRequest;
import com.pubmatic.sdk.common.phoenix.PhoenixConstants;
import com.pubmatic.sdk.common.pubmatic.PubMaticConstants;

public class PhoenixBannerAdRequest extends PhoenixAdRequest {

	private PhoenixBannerAdRequest(Context context) {
		super(context);
	}
	
	public static PhoenixBannerAdRequest createPhoenixBannerAdRequest(Context context, String adUnitId, String impressionId) {

		PhoenixBannerAdRequest bannerAdRequest = new PhoenixBannerAdRequest(context);
		bannerAdRequest.setAdUnitId(adUnitId);
		bannerAdRequest.setImpressionId(impressionId);
		return bannerAdRequest;
	}

	void createRequest() {
		mPostData		= null;
		initializeDefaultParams();
		setupPostData();
	}

	@Override
	public boolean checkMandatoryParams() {
		return !TextUtils.isEmpty(mAdUnitId) && !TextUtils.isEmpty(mImpressionId);
	}

	@Override
	protected void initializeDefaultParams() {
		putPostData("o", 		"1");
		putPostData(PhoenixConstants.RESPONSE_FORMAT_PARAM, "2");
	}

	@Override
	protected void setupPostData() {
		super.setupPostData();
		if (mPostData == null)
			mPostData = new StringBuffer();

		// Set the Ad size
		if (mPMAdSize !=null) {
			putPostData(PhoenixConstants.AD_SIZE_PARAM, String.valueOf(mPMAdSize.getAdWidth())+"x"+String.valueOf(mPMAdSize.getAdHeight()));
		}
		putPostData(PhoenixConstants.REQUEST_TYPE_PARAM, String.valueOf(REQUEST_TYPE.IMAGE|REQUEST_TYPE.TEXT|REQUEST_TYPE.RICH_MEDIA));
	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.phoenix.PhoenixBannerRRFormatter";
	}

}
