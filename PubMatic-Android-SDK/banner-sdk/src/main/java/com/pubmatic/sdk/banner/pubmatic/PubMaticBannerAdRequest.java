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

import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMAdSize;
import com.pubmatic.sdk.common.pubmatic.PUBAdSize;
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;
import com.pubmatic.sdk.common.pubmatic.PubMaticConstants;

/**
 *
 */
public class PubMaticBannerAdRequest extends PubMaticAdRequest {

	protected PMAdSize[] mMultiAdSizes	 = null;
	protected boolean	 mIsInterstitial = false;

	//	//Should not exposed to Publisher, Handled by SDK.
	//	private int mDefaultedAdNetworkId;
	//	private int mDefaultedCampaignId;

	//---------------- Object creational methods ------------------
	protected PubMaticBannerAdRequest(Context context) {
		super(context);
	}

	public static PubMaticBannerAdRequest createPubMaticBannerAdRequest(Context context, String pubId, String siteId, String adId) {
		PubMaticBannerAdRequest bannerAdRequest = new PubMaticBannerAdRequest(context);
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
			putPostData(PubMaticConstants.AD_HEIGHT_PARAM, String.valueOf(mPMAdSize.getAdHeight()));
			putPostData(PubMaticConstants.AD_WIDTH_PARAM, String.valueOf(mPMAdSize.getAdWidth()));
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

			putPostData(PubMaticConstants.MULTI_SIZE_PARAM, multisize.toString());
		}

		//Set interstitial flag
		if(mIsInterstitial== true)
			putPostData(PubMaticConstants.INSTERSTITIAL_KEY, "1");
	}

	//---------------- public methods can be called externally ------------------

	@Override
	public boolean checkMandatoryParams() {
		return super.checkMandatoryParams();
	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.pubmatic.PubMaticBannerRRFormatter";
	}

//	protected void setAttributes(AttributeSet attr) {
//		if (attr == null)
//			return;
//		try {
//			mPubId = attr.getAttributeValue(null,
//					PubMaticConstants.PUB_ID_PARAM);
//
//			mSiteId = attr.getAttributeValue(null,
//					PubMaticConstants.SITE_ID_PARAM);
//
//			mAdId = attr.getAttributeValue(null,
//					PubMaticConstants.AD_ID_PARAM);
//
//			String width = attr.getAttributeValue(null,
//					CommonConstants.AD_WIDTH);
//
//            String height = attr.getAttributeValue(null,
//                    CommonConstants.AD_HEIGHT);
//			if (!TextUtils.isEmpty(width) && !TextUtils.isEmpty(height)) {
//                int widthInt = Integer.parseInt(width);
//                int heightInt = Integer.parseInt(height);
//                setAdSize(new PUBAdSize(widthInt, heightInt));
//            }
//
//
//		} catch (Exception ex) {
//
//		}
//	}
//
//	public int getDefaultedAdNetworkId() {
//		return mDefaultedAdNetworkId;
//	}
//
//	public void setDefaultedAdNetworkId(int mDefaultedAdNetworkId) {
//		this.mDefaultedAdNetworkId = mDefaultedAdNetworkId;
//	}
//
//	public int getDefaultedCampaignId() {
//		return mDefaultedCampaignId;
//	}
//
//	public void setDefaultedCampaignId(int mDefaultedCampaignId) {
//		this.mDefaultedCampaignId = mDefaultedCampaignId;
//	}

    /**
	 * Returns the Ad size array for banner ad
	 * @return
	 */
	public PMAdSize[] getOptionalAdSizes() {
		return mMultiAdSizes;
	}

	/**
	 * Set the multisize keyword with provided pair of ad sizes. Compatible
	 * creative would be returned based on DSP auctioning. Maximum first 4
	 * sizes would be considered at server.
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
	 * Sets the banner ad size in ad request with provided size. It is mandatory to set ad size in PubMaticBannerAdRequest.
	 * @return Size of banner ad
	 */
	public void setAdSize(PMAdSize adSize) {
		mPMAdSize = adSize;
	}

	public boolean isInterstitial() {
		return mIsInterstitial;
	}

	/**
	 * Set this flag to true for interstitial ad format.
	 * @param isInterstitial
	 */
	public void setInterstitial(boolean isInterstitial) {
		this.mIsInterstitial = isInterstitial;
	}

}
