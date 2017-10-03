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
package com.pubmatic.sdk.banner.pubmatic;

import android.content.Context;

import com.pubmatic.sdk.common.PMAdSize;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.pubmatic.PMConstants;
import com.pubmatic.sdk.common.pubmatic.PUBAdSize;
import com.pubmatic.sdk.common.pubmatic.PMAdRequest;

/**
 * PMBannerAdRequest class provides parameters for banner ad request. To request Banner Ad, you need to pass valid PMBannerAdRequest instance to PMBannerAdView’s loadRequest() method.
 */
public class PMBannerAdRequest extends PMAdRequest {

	protected PMAdSize[] mMultiAdSizes	 = null;
	protected boolean	 mIsInterstitial = false;

	//---------------- Object creational methods ------------------
	protected PMBannerAdRequest(Context context) {
		super(context);
	}

	/**
	 * Creates Ad request with Publisher credentials i.e Publisher Id, Site Id, AdTag Id
	 * @param context
	 * @param pubId
	 * @param siteId
	 * @param adId
	 * @return
	 */
	public static PMBannerAdRequest createPMBannerAdRequest(Context context, String pubId, String siteId, String adId) {
		PMBannerAdRequest bannerAdRequest = new PMBannerAdRequest(context);
		bannerAdRequest.setPubId(pubId);
		bannerAdRequest.setSiteId(siteId);
		bannerAdRequest.setAdId(adId);
		return bannerAdRequest;
	}

	// Default method: Called from PMBannerRRFormatter i.e. within same package
	void createRequest() {
		initializeDefaultParams();
		setUpUrlParams();
		setUpPostParams();
	}

	/**
	 * This method will initialize all the parameters which SDK need to fetch.
	 */
	protected void initializeDefaultParams() {
		setOperId(OPERID.JSON_MOBILE);
		setAdType(AD_TYPE.BANNER);
		setAWT(AWT_OPTION.DEFAULT);
	}

	@Override
	protected void setUpPostParams() {
		super.setUpPostParams();

		// Set the Ad size
		if (mPMAdSize != null) {
			putPostData(PMConstants.AD_HEIGHT_PARAM, String.valueOf(mPMAdSize.getAdHeight()));
			putPostData(PMConstants.AD_WIDTH_PARAM, String.valueOf(mPMAdSize.getAdWidth()));
		}

		// Send multisize parameter seperated by comma. Max 4 sizes would be considered at server
		if(mMultiAdSizes!=null && mMultiAdSizes.length>0) {
			StringBuffer multisize = new StringBuffer();
			int length = 0;

			while(length<mMultiAdSizes.length) {
				PMAdSize size = mMultiAdSizes[length];
				if(size!=null) {
					multisize.append(size.getAdWidth()+"x"+size.getAdHeight());
					length++;
				}
				if(length!=mMultiAdSizes.length)
					multisize.append(",");
			}

			putPostData(PMConstants.MULTI_SIZE_PARAM, multisize.toString());
		}

		//Set interstitial flag
		if(mIsInterstitial== true)
			putPostData(PMConstants.INSTERSTITIAL_KEY, "1");
	}

	//---------------- public methods can be called externally ------------------

	@Override
	public boolean checkMandatoryParams() {
		boolean result = super.checkMandatoryParams();

		//size is mandatory for Banner
		if(result) {
			if (mPMAdSize != null)
				result = (mPMAdSize.getAdWidth() > 0 && mPMAdSize.getAdHeight() > 0);
			else
				result = false;
		}
		return result;
	}

	@Override
	public RRFormatter getFormatter() {
		if(mRRFormatter==null)
			mRRFormatter = new PMBannerRRFormatter();
		return mRRFormatter;
	}

    /**
	 * Returns the Ad size array for banner ad
	 * @return
	 */
	public PMAdSize[] getOptionalAdSizes() {
		return mMultiAdSizes;
	}

	/**
	 * Set the array of ad sizes. Maximum first 4 sizes would be considered at server for DSP auctioning.
	 * @param mMultiAdSizes
	 */
	public void setOptionalAdSizes(PMAdSize[] mMultiAdSizes) {
		this.mMultiAdSizes = mMultiAdSizes;
	}

	/**
	 * Set the multisize keyword with provided pair of ad sizes. Compatible
	 * creative would be returned based on DSP auctioning. Maximum first 4
	 * sizes would be considered at server.
	 *
	 * PUBAdSise is deprecated, use PMAdSize class instead.
	 * @param mMultiAdSizes
	 */
	@Deprecated
	public void setOptionalAdSizes(PUBAdSize[] mMultiAdSizes) {

		if(mMultiAdSizes!=null && mMultiAdSizes.length>0) {
			this.mMultiAdSizes = new PMAdSize[mMultiAdSizes.length];

			for(int index = 0; index<mMultiAdSizes.length; index++) {

				PMAdSize adSize = new PMAdSize(mMultiAdSizes[index].getAdWidth(), mMultiAdSizes[index].getAdHeight());
				this.mMultiAdSizes[index] = adSize;
			}
		}
	}

	/**
	 * Returns the ad size set from setAdSize()
	 * @return size of banner ad
	 */
	public PMAdSize getAdSize() {
		return mPMAdSize;
	}

	/**
	 * Sets the banner ad size in ad request with provided size. It is mandatory to set ad size in PMBannerAdRequest.
	 * @return Size of banner ad
	 */
	public void setAdSize(PMAdSize adSize) {
		mPMAdSize = adSize;
	}
}
