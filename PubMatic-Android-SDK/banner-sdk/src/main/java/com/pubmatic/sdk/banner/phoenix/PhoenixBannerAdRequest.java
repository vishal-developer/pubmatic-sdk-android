package com.pubmatic.sdk.banner.phoenix;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;

import com.pubmatic.sdk.banner.BannerAdRequest;
import com.pubmatic.sdk.common.utils.CommonConstants;
import com.pubmatic.sdk.common.utils.CommonConstants.CHANNEL;

public class PhoenixBannerAdRequest extends BannerAdRequest {

	private String 			mPHAdId;

	@Override
	public String getAdServerURL() {
		return null;
	}

	private PhoenixBannerAdRequest() {
		super(CHANNEL.PHOENIX);
	}
	
	public static PhoenixBannerAdRequest createPhoenixBannerAdRequest(String adId) {

		PhoenixBannerAdRequest bannerAdRequest = new PhoenixBannerAdRequest();
		bannerAdRequest.setPHAdId(adId);
		return bannerAdRequest;
	}

	public String getPHAdId() {
		return mPHAdId;
	}

	public void setPHAdId(String mPHAdId) {
		this.mPHAdId = mPHAdId;
	}

	@Override
	public boolean checkMandatoryParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void initializeDefaultParams(Context context) {

	}

	@Override
	protected void setupPostData() {

	}

	@Override
	public void setCustomParams(Map<String, List<String>> customParams) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyRequestParams(BannerAdRequest adRequest) {
		if(adRequest!=null && adRequest instanceof PhoenixBannerAdRequest) {

		}
	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.phoenix.PhoenixBannerRRFormatter";
	}
	
	@Override
	public void setAttributes(AttributeSet attr) {

	}
}
