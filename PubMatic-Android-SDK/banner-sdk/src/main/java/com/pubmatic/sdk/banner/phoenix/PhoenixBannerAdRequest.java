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
import com.pubmatic.sdk.common.phoenix.PhoenixAdRequest;
import com.pubmatic.sdk.common.phoenix.PhoenixConstants;
import com.pubmatic.sdk.common.pubmatic.PubMaticConstants;

public class PhoenixBannerAdRequest extends PhoenixAdRequest {

	private int				mAdWidth = -1;
	private int				mAdHeight = -1;

	private PhoenixBannerAdRequest(Context context) {
		super(context);
	}
	
	public static PhoenixBannerAdRequest createPhoenixBannerAdRequest(Context context, String adUnitId, String impressionId) {

		PhoenixBannerAdRequest bannerAdRequest = new PhoenixBannerAdRequest(context);
		bannerAdRequest.setAdUnitId(adUnitId);
		bannerAdRequest.setImpressionId(impressionId);
		return bannerAdRequest;
	}

	/**
	 * Returns the base/host name URL
	 * @return
	 */
	public String getAdServerURL()
	{
		return TextUtils.isEmpty(mBaseUrl) ? CommonConstants.PHOENIX_AD_NETWORK_URL : mBaseUrl;
	}

	@Override
	public boolean checkMandatoryParams() {
		// TODO Auto-generated method stub
		return !TextUtils.isEmpty(mAdUnitId) && !TextUtils.isEmpty(mImpressionId);
	}

	@Override
	protected void initializeDefaultParams(Context context) {
		super.initializeDefaultParams(context);
		putPostData("o", 		"1");
		putPostData(PhoenixConstants.RESPONSE_FORMAT_PARAM, "2");//1 - VAST, 2 - JSON, 3 - Native

	}

	@Override
	protected void setupPostData() {
		super.setupPostData();
		if (mPostData == null)
			mPostData = new StringBuffer();

		// Set the Ad size
		if (mAdHeight > 0 && mAdWidth > 0) {//Need to confirm AD_HEIGHT_PARAM or SIZE_Y_PARAM
			putPostData(PhoenixConstants.AD_SIZE_PARAM, String.valueOf(mAdWidth)+"x"+String.valueOf(mAdHeight));
		} else if(getWidth()>0 && getAdHeight()>0){
			putPostData(PhoenixConstants.AD_SIZE_PARAM, String.valueOf(getWidth())+"x"+String.valueOf(getAdHeight()));
		}
		putPostData(PhoenixConstants.REQUEST_TYPE_PARAM, String.valueOf(REQUEST_TYPE.IMAGE|REQUEST_TYPE.TEXT|REQUEST_TYPE.RICH_MEDIA));
	}

	@Override
	public void copyRequestParams(AdRequest adRequestParams) {

	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.phoenix.PhoenixBannerRRFormatter";
	}


	public void setAttributes(AttributeSet attr) {

		if(attr==null)
			return;
		try {
			mImpressionId = attr.getAttributeValue(null,
					CommonConstants.REQUESTPARAM_IMPRESSION_ID);

			mAdWidth = Integer.parseInt(attr.getAttributeValue(null,
					CommonConstants.REQUESTPARAM_AD_WIDTH));

			mAdHeight = Integer.parseInt(attr.getAttributeValue(null,
					CommonConstants.REQUESTPARAM_AD_HEIGHT));

			mAdUnitId = attr.getAttributeValue(null,
					CommonConstants.REQUESTPARAM_AD_UNIT_ID);

			String width = attr.getAttributeValue(null,
					CommonConstants.AD_WIDTH);

			if (!TextUtils.isEmpty(width))
				setWidth(Integer.parseInt(width));

			String height = attr.getAttributeValue(null,
					CommonConstants.AD_HEIGHT);

			if (!TextUtils.isEmpty(height))
				setHeight(Integer.parseInt(height));


		} catch (Exception ex) {

		}
	}


	public int getAdWidth() {
		return mAdWidth;
	}

	public void setAdWidth(int mAdWidth) {
		this.mAdWidth = mAdWidth;
	}

	public int getAdHeight() {
		return mAdHeight;
	}

	public void setAdHeight(int mAdHeight) {
		this.mAdHeight = mAdHeight;
	}
}
