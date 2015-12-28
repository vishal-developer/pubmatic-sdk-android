package com.pubmatic.sdk.banner.mocean;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.pubmatic.sdk.banner.BannerAdRequest;
import com.pubmatic.sdk.common.utils.CommonConstants;
import com.pubmatic.sdk.common.utils.CommonConstants.CHANNEL;

public class MoceanBannerAdRequest extends BannerAdRequest {

	private String zoneId;
	private boolean test = false;

	private MoceanBannerAdRequest() {
		super(CHANNEL.MOCEAN);
	}

	public static MoceanBannerAdRequest createMoceanBannerAdRequest(
			Context context, String zone) {
		MoceanBannerAdRequest bannerAdRequest = new MoceanBannerAdRequest();
		bannerAdRequest.setMoceanZoneId(zone);
		return bannerAdRequest;
	}

	/**
	 * Returns the base/host name URL
	 * @return
	 */
	public String getAdServerURL()
	{
		return TextUtils.isEmpty(mBaseUrl) ? CommonConstants.MOCEAN_AD_NETWORK_URL : mBaseUrl;
	}
	/**
	 * This method will initialize all the static parameters which SDK need to set.
	 * @param context
	 */
	protected void initializeDefaultParams(Context context) {

		putPostData("count", "1");
		putPostData("key", "3");
		putPostData("version", CommonConstants.SDK_VERSION);
		if (this.test) {
			putPostData("test", "1");
		}
		// Network related params reqd by Mocean
		try {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			String networkOperator = tm.getNetworkOperator();

			if ((networkOperator != null) && (networkOperator.length() > 3)) {
				String mcc = networkOperator.substring(0, 3);
				String mnc = networkOperator.substring(3);
				putPostData("mcc", String.valueOf(mcc));
				putPostData("mnc", String.valueOf(mnc));
			}
		} catch (Exception ex) {
			System.out.println("Unable to obtain mcc and mnc. Exception:" + ex);
		}
	}

	public String getMoceanZoneId() {
		return zoneId;
	}

	public void setMoceanZoneId(String zone) {
		this.zoneId = zone;
	}

	public void setWidth(int width) {
		super.setWidth(width);
	}

	public void setHeight(int height) {
		super.setHeight(height);
	}

	public void setUserAgent(String userAgent) {
		super.setUserAgent(userAgent);
	}

	@Override
	public boolean checkMandatoryParams() {
		return !TextUtils.isEmpty(zoneId);
	}

	@Override
	public void setCustomParams(Map<String, List<String>> customParams) {
		mCustomParams = customParams;
	}

	/**
	 * Sets the instance test mode. If set to test mode the instance will
	 * request test ads for the configured zone.
	 * <p>
	 * Warning: This should never be enabled for application releases.
	 *
	 * @param test
	 *            true to set test mode, false to disable test mode.
	 */
	public void setTest(boolean test) {
		this.test = test;
	}

	/**
	 * Access for test mode state of the instance.
	 *
	 * @return true if the instance is set to test mode, false if test mode is
	 *         disabled.
	 */
	public boolean isTest() {
		return test;
	}

	@Override
	protected void setupPostData() {

		putPostData(CommonConstants.ZONE_ID_PARAM, String.valueOf(this.zoneId));
		if(getWidth()>0)
			putPostData(CommonConstants.SIZE_X_PARAM, String.valueOf(getWidth()));
		if(getHeight()>0)
			putPostData(CommonConstants.SIZE_Y_PARAM, String.valueOf(getHeight()));
	}

	/**
	 *
	 */
	public void copyRequestParams(BannerAdRequest adRequest) {
		if(adRequest!=null && adRequest instanceof MoceanBannerAdRequest &&
				(zoneId==null || TextUtils.isEmpty(zoneId))) {
			this.zoneId = ((MoceanBannerAdRequest)adRequest).zoneId;
		}
	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.mocean.MoceanBannerRRFormatter";
	}

	@Override
	public void setAttributes(AttributeSet attr) {
		try{
			zoneId = attr.getAttributeValue(null,
					CommonConstants.ZONE_ID_PARAM);
		} catch(Exception ex) {

		}
	}

}
