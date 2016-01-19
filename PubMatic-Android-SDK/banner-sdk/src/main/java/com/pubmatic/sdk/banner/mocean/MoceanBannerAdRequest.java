package com.pubmatic.sdk.banner.mocean;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.mocean.MoceanAdRequest;
import com.pubmatic.sdk.common.CommonConstants;

public class MoceanBannerAdRequest extends MoceanAdRequest {

	private boolean test = false;

	private MoceanBannerAdRequest(Context context) {
		super(context);
	}

	public static MoceanBannerAdRequest createMoceanBannerAdRequest(
			Context context, String zone) {
		MoceanBannerAdRequest bannerAdRequest = new MoceanBannerAdRequest(context);
		bannerAdRequest.setZoneId(zone);
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

	public void setWidth(int width) {
		super.setWidth(width);
	}

	public void setHeight(int height) {
		super.setHeight(height);
	}

	@Override
	public boolean checkMandatoryParams() {
		return !TextUtils.isEmpty(mZoneId);
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

		super.setupPostData();
		if(getWidth()>0)
			putPostData(CommonConstants.SIZE_X_PARAM, String.valueOf(getWidth()));
		if(getHeight()>0)
			putPostData(CommonConstants.SIZE_Y_PARAM, String.valueOf(getHeight()));
	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.mocean.MoceanBannerRRFormatter";
	}

	public void setAttributes(AttributeSet attr) {
		try{
			mZoneId = attr.getAttributeValue(null,
					CommonConstants.REQUESTPARAM_ZONE);
		} catch(Exception ex) {

		}
	}

}
