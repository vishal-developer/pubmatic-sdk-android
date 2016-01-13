package com.pubmatic.sdk.banner.phoenix;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.CommonConstants.CHANNEL;

public class PhoenixBannerAdRequest extends AdRequest {

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
	public void addCustomParams(Map<String, List<String>> customParams) {

	}

	@Override
	public void createRequest(Context context) {

	}

	@Override
	protected void setupPostData() {

	}

	@Override
	public void copyRequestParams(AdRequest adRequestParams) {

	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.phoenix.PhoenixBannerRRFormatter";
	}

	public void setAttributes(AttributeSet attr) {

	}
}
