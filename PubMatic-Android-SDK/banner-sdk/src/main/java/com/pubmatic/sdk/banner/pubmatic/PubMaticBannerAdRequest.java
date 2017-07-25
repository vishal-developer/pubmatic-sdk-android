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
import android.text.TextUtils;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.pubmatic.PUBAdSize;
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;
import com.pubmatic.sdk.common.pubmatic.PubMaticConstants;

public class PubMaticBannerAdRequest extends PubMaticAdRequest {

	private PUBAdSize mPubAdSize = null;
	private PUBAdSize[] mMultiAdSizes	 = null;
	private int mTimeoutInterval = CommonConstants.INVALID_INT;

	//Should not exposed to Publisher, Handled by SDK.
	private int mDefaultedAdNetworkId;
	private int mDefaultedCampaignId;

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

	/**
	 * Returns the base/host name URL
	 *
	 * @return
	 */
	public String getAdServerURL() {
		return TextUtils.isEmpty(mBaseUrl) ? CommonConstants.PUBMATIC_AD_NETWORK_URL : mBaseUrl;
	}

	/**
	 * This method will initialize all the parameters which SDK need to fetch.
	 *
	 * @param context
	 */
	protected void initializeDefaultParams(Context context) {
		super.initializeDefaultParams(context);
		setOperId(OPERID.JSON_MOBILE);
		setAdType(AD_TYPE.BANNER);
	}

	@Override
	protected void setUpPostParams() {
		super.setUpPostParams();

		// Set the Ad size
		if (mPubAdSize != null) {//Need to confirm AD_HEIGHT_PARAM or SIZE_Y_PARAM
			putPostData(PubMaticConstants.AD_HEIGHT_PARAM, String.valueOf(mPubAdSize.getAdHeight()));
			putPostData(PubMaticConstants.AD_WIDTH_PARAM, String.valueOf(mPubAdSize.getAdWidth()));
		} else {
			putPostData(PubMaticConstants.AD_WIDTH_PARAM, String.valueOf(getWidth()));
			putPostData(PubMaticConstants.AD_HEIGHT_PARAM, String.valueOf(getHeight()));
		}

		putPostData("api", "3::4::5");

		//Send multisize parameter seperated by comma. Max 4 sizes would be considered at server
		if(mMultiAdSizes!=null && mMultiAdSizes.length>0) {
			StringBuffer multisize = new StringBuffer();
			int length = 0;

			while(length<mMultiAdSizes.length) {
				PUBAdSize size = mMultiAdSizes[length];
				if(size!=null) {
					multisize.append(size.getAdWidth()+"x"+size.getAdHeight());
					length++;
				}
				if(length!=mMultiAdSizes.length)
					multisize.append(",");
			}

			putPostData(PubMaticConstants.MULTI_SIZE_PARAM, multisize.toString());
		}
	}

	/**
	 * Ad will be refreshed by given time interval in seconds. By default,
	 * Library will set to 0 second and Ad will be not refresh automatically.
	 * <p/>
	 * <p/>
	 * The adRefreshRa te should be in range on 12 to 120 second. If you set the
	 * ad refresh time interval value < 0 or >=1 && < 12, library will set it to
	 * 12 seconds If you set the ad refresh time interval >120 seconds, library
	 * will set it to 120 seconds
	 *
	 * @param adRefreshRate adRefreshRate to set.
	 */
	public void setAdRefreshRate(int adRefreshRate) {
		mAdRefreshRate = adRefreshRate;
	}

    /**
	 * @param pubAdSize the pubAdSize to set
	 */
	public void setAdSize(PUBAdSize pubAdSize) {
        this.mPubAdSize = pubAdSize;
	}

    /**
     * @Return mPubAdSize Returns ad size
     */
    public PUBAdSize getAdSize() {
        return this.mPubAdSize;
    }

	public int getDefaultedAdNetworkId() {
		return mDefaultedAdNetworkId;
	}

	public void setDefaultedAdNetworkId(int mDefaultedAdNetworkId) {
		this.mDefaultedAdNetworkId = mDefaultedAdNetworkId;
	}

	public int getDefaultedCampaignId() {
		return mDefaultedCampaignId;
	}

	public void setDefaultedCampaignId(int mDefaultedCampaignId) {
		this.mDefaultedCampaignId = mDefaultedCampaignId;
	}

	@Override
	public boolean checkMandatoryParams() {
		return super.checkMandatoryParams();
	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.pubmatic.PubMaticBannerRRFormatter";
	}

	public void setAttributes(AttributeSet attr) {
		if (attr == null)
			return;
		try {
			mPubId = attr.getAttributeValue(null,
					PubMaticConstants.PUB_ID_PARAM);

			mSiteId = attr.getAttributeValue(null,
					PubMaticConstants.SITE_ID_PARAM);

			mAdId = attr.getAttributeValue(null,
					PubMaticConstants.AD_ID_PARAM);

			String width = attr.getAttributeValue(null,
					CommonConstants.AD_WIDTH);

            String height = attr.getAttributeValue(null,
                    CommonConstants.AD_HEIGHT);
			if (!TextUtils.isEmpty(width) && !TextUtils.isEmpty(height)) {
                int widthInt = Integer.parseInt(width);
                int heightInt = Integer.parseInt(height);
                setAdSize(new PUBAdSize(widthInt, heightInt));
            }


		} catch (Exception ex) {

		}
	}

    public int getWidth() {
        if(mPubAdSize==null)
            return  super.getWidth();
        return mPubAdSize.getAdWidth();
    }

    public int getHeight() {
        if(mPubAdSize==null)
            return  super.getHeight();
        return mPubAdSize.getAdHeight();
    }

    /**
	 * Returns the Ad size array for banner ad
	 * @return
	 */
	public PUBAdSize[] getOptionalAdSizes() {
		return mMultiAdSizes;
	}

	/**
	 * Set the multisize keyword with provided pair of ad sizes. Compatible
	 * creative would be returned based on DSP auctioning. Maximum first 4
	 * sizes would be considered at server.
	 * @param mMultiAdSizes
	 */
	public void setOptionalAdSizes(PUBAdSize[] mMultiAdSizes) {
		this.mMultiAdSizes = mMultiAdSizes;
	}

	/**
	 * @return the mTimeoutInterval
	 */
	public int getTimeoutInterval() {
		return mTimeoutInterval;
	}

	/**
	 * @param mTimeoutInterval the mTimeoutInterval to set
	 */
	public void setTimeoutInterval(int mTimeoutInterval) {
		this.mTimeoutInterval = mTimeoutInterval;
	}
}
