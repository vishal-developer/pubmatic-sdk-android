package com.pubmatic.sdk.banner.pubmatic;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;

import com.pubmatic.sdk.banner.BannerAdRequest;
import com.pubmatic.sdk.common.PMUserInfo;
import com.pubmatic.sdk.common.utils.CommonConstants;
import com.pubmatic.sdk.common.utils.CommonConstants.CHANNEL;

public class PubMaticBannerAdRequest extends BannerAdRequest {

	private String 	mPublisherId;
	private String 	mSiteId;
	private String 	mAdTagId;
	private String 	mNetworkType 		= null;
	private int 	mAdOrientation 		= CommonConstants.INVALID_INT;
	private int 	mDeviceOrientation 	= CommonConstants.INVALID_INT;
	private int 	mTimeoutInterval 	= CommonConstants.INVALID_INT;
	private int 	mAdRefreshRate 		= 0;
	private float 	mLatitude;
	private float 	mLongitude;

	private PUBAdSize mPubAdSize 		= null;
	private PMUserInfo mPMUserInfo = null;

	//Should not exposed to Publisher, Handled by SDK.
	private int 	mDefaultedAdNetworkId;
	private int 	mDefaultedCampaignId;

	private Context mContext;
	private PubMaticBannerAdRequest() {
		super(CHANNEL.PUBMATIC);
	}

	private PubMaticBannerAdRequest(Context context) {
		super(CHANNEL.PUBMATIC);
		mContext = context;

	}
	public static PubMaticBannerAdRequest createPubMaticBannerAdRequest(Context context, String pubId, String siteId, String adId) {
		PubMaticBannerAdRequest bannerAdRequest = new PubMaticBannerAdRequest(context);
		bannerAdRequest.setPublisherId(pubId);
		bannerAdRequest.setSiteId(siteId);
		bannerAdRequest.setAdTagId(adId);
		return bannerAdRequest;
	}

	/**
	 * Returns the base/host name URL
	 * @return
	 */
	public String getAdServerURL()
	{
		return TextUtils.isEmpty(mBaseUrl) ? CommonConstants.PUBMATIC_AD_NETWORK_URL : mBaseUrl;
	}
	/**
	 * This method will initialize all the parameters which SDK need to fetch.
	 * @param context
	 */
	protected void initializeDefaultParams(Context context) {

		//TODO :: Need to verify all Banner parameters
		putPostData("operId", "201");
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

	@Override
	protected void setupPostData() {

		if(mPostData == null)
			mPostData = new StringBuffer();

		putPostData(CommonConstants.PUB_ID_PARAM, String.valueOf(this.mPublisherId));
		putPostData(CommonConstants.SITE_ID_PARAM, String.valueOf(this.mSiteId));
		putPostData(CommonConstants.AD_ID_PARAM, String.valueOf(this.mAdTagId));

		// Set the Ad size
		if (mPubAdSize != null) {//Need to confirm AD_HEIGHT_PARAM or SIZE_Y_PARAM
			putPostData(CommonConstants.AD_HEIGHT_PARAM, String.valueOf(mPubAdSize.getAdHeight()));
			putPostData(CommonConstants.AD_WIDTH_PARAM, String.valueOf(mPubAdSize.getAdWidth()));
		} else {
			putPostData(CommonConstants.AD_WIDTH_PARAM, String.valueOf(getWidth()));
			putPostData(CommonConstants.AD_HEIGHT_PARAM, String.valueOf(getHeight()));
		}
		PUBDeviceInformation pubDeviceInformation = PUBDeviceInformation
				.getInstance(mContext);

		try {

			// Setting the manadatory parameters
			if (pubDeviceInformation.mDeviceIdentifier != null) {
				// Appending did
				putPostData(CommonConstants.DID_PARAM, URLEncoder.encode(
						pubDeviceInformation.mDeviceIdentifier,
						CommonConstants.URL_ENCODING));

				// Appending dpid
				putPostData(CommonConstants.DPID_PARAM, URLEncoder.encode(
						pubDeviceInformation.mDeviceIdentifier,
						CommonConstants.URL_ENCODING));
			}

			if(pubDeviceInformation.mDeviceAcceptLanguage != null)
			{
				// Appending did
				putPostData(CommonConstants.LANGUAGE, URLEncoder.encode(
						pubDeviceInformation.mDeviceAcceptLanguage,
						CommonConstants.URL_ENCODING));
			}
			// Setting country
			if (pubDeviceInformation.mDeviceCountryCode != null) {
				putPostData(CommonConstants.COUNTRY_PARAM, URLEncoder.encode(
						pubDeviceInformation.mDeviceCountryCode,
						CommonConstants.URL_ENCODING));
			}

			// Setting carrier
			if (pubDeviceInformation.mCarrierName != null) {
				putPostData(CommonConstants.CARRIER_PARAM, URLEncoder.encode(
						pubDeviceInformation.mCarrierName,
						CommonConstants.URL_ENCODING));
			}

			// Setting make
			if (pubDeviceInformation.mDeviceMake != null) {
				putPostData(CommonConstants.MAKE_PARAM, URLEncoder.encode(
						pubDeviceInformation.mDeviceMake,
						CommonConstants.URL_ENCODING));
			}

			// Setting model
			if (pubDeviceInformation.mDeviceModel != null) {
				putPostData(CommonConstants.MODEL_PARAM, URLEncoder.encode(
						pubDeviceInformation.mDeviceModel,
						CommonConstants.URL_ENCODING));
			}

			// Setting os
			if (pubDeviceInformation.mDeviceOSName != null) {
				putPostData(CommonConstants.OS_PARAM, URLEncoder.encode(
						pubDeviceInformation.mDeviceOSName,
						CommonConstants.URL_ENCODING));
			}

			// Setting osv
			if (pubDeviceInformation.mDeviceOSVersion != null) {
				putPostData(CommonConstants.OSV_PARAM, URLEncoder.encode(
						pubDeviceInformation.mDeviceOSVersion,
						CommonConstants.URL_ENCODING));
			}

			// Setting js
			putPostData(CommonConstants.JS_PARAM, String.valueOf(PUBDeviceInformation.mJavaScriptSupport));

			// Setting ver
			if (pubDeviceInformation.mApplicationVersion != null) {
				putPostData(CommonConstants.VER_PARAM, URLEncoder.encode(
						pubDeviceInformation.mApplicationVersion,
						CommonConstants.URL_ENCODING));
			}

			// Setting bundle
			if (pubDeviceInformation.mApplicationName != null) {
				putPostData(CommonConstants.BUNDLE_PARAM, URLEncoder.encode(
						pubDeviceInformation.mApplicationName,
						CommonConstants.URL_ENCODING));
			}

			// Setting location
			if (mPMUserInfo != null) {
				Location location = mPMUserInfo.getLocation();
				if (location != null) {
					putPostData(CommonConstants.LOC_PARAM, URLEncoder.encode(
							location.getLatitude() + ","
									+ location.getLongitude(),
							CommonConstants.URL_ENCODING));
				} else {
					putPostData(CommonConstants.LOC_PARAM, URLEncoder.encode(
							pubDeviceInformation.mDeviceLocation,
							CommonConstants.URL_ENCODING));
				}
			} else {
				putPostData(CommonConstants.LOC_PARAM, URLEncoder.encode(
						pubDeviceInformation.mDeviceLocation,
						CommonConstants.URL_ENCODING));
			}

			// Setting user specific information
			if (mPMUserInfo != null) {
				// Setting year of birth
				if (mPMUserInfo.getYearOfBirth() != null) {
					putPostData(CommonConstants.YOB_PARAM, URLEncoder.encode(
							mPMUserInfo.getYearOfBirth(),
							CommonConstants.URL_ENCODING));
				}

				// Setting Gender of user
				putPostData(CommonConstants.GENDER_PARAM, URLEncoder.encode(
						mPMUserInfo.getGender(), CommonConstants.URL_ENCODING));

				// Setting zip code of user
				if (mPMUserInfo.getZip() != null) {
					putPostData(CommonConstants.ZIP_PARAM, URLEncoder.encode(
							mPMUserInfo.getZip(), CommonConstants.URL_ENCODING));
				}

				// Setting area code of user
				if (mPMUserInfo.getAreaCode() != null) {
					putPostData(CommonConstants.AREACODE, URLEncoder.encode(
							mPMUserInfo.getAreaCode(),
							CommonConstants.URL_ENCODING));
				}

				// Setting the income
				if (mPMUserInfo.getIncome() != null) {
					putPostData(CommonConstants.USERINCOME, URLEncoder.encode(
							mPMUserInfo.getIncome(),
							CommonConstants.URL_ENCODING));
				}

				// Setting the user entnicity
				if (mPMUserInfo.getEthnicity() != null) {
					putPostData(CommonConstants.USERETHNICITY , URLEncoder.encode(
							mPMUserInfo.getEthnicity(),
							CommonConstants.URL_ENCODING));
				}
				if (mPMUserInfo.getKeywordString() != null) {
					putPostData(CommonConstants.KEYWORDS_PARAM, URLEncoder.encode(
							mPMUserInfo.getKeywordString(),
							CommonConstants.URL_ENCODING));
				}
			}

			// Setting adOrientation
			putPostData(CommonConstants.AD_ORIENTATION_PARAM, String.valueOf(mAdOrientation));

			// Setting deviceOrientation
			putPostData(CommonConstants.DEVICE_ORIENTATION_PARAM, String.valueOf(mDeviceOrientation));

			// Setting adRefreshRate
			putPostData(CommonConstants.AD_REFRESH_RATE_PARAM, String.valueOf(mAdRefreshRate));

			// Setting sdk_id
			putPostData(CommonConstants.SDK_ID_PARAM, URLEncoder
					.encode(PUBDeviceInformation.msdkId,
							CommonConstants.URL_ENCODING));

			// Setting sdk_ver
			putPostData(CommonConstants.SDK_VER_PARAM, URLEncoder.encode(
					PUBDeviceInformation.msdkVersion,
					CommonConstants.URL_ENCODING));

			if (mNetworkType != null) {
				putPostData(CommonConstants.NETWORK_TYPE_PARAM, URLEncoder.encode(mNetworkType,
						CommonConstants.URL_ENCODING));
			}


		} catch (Exception e) {

		}
	}

	/**
	 * Ad will be refreshed by given time interval in seconds. By default,
	 * Library will set to 0 second and Ad will be not refresh automatically.
	 *
	 *
	 * The adRefreshRa te should be in range on 12 to 120 second. If you set the
	 * ad refresh time interval value < 0 or >=1 && < 12, library will set it to
	 * 12 seconds If you set the ad refresh time interval >120 seconds, library
	 * will set it to 120 seconds
	 *
	 * @param adRefreshRate
	 *            adRefreshRate to set.
	 *
	 */
	public void setAdRefreshRate(int adRefreshRate) {
		mAdRefreshRate = adRefreshRate;
	}

	/**
	 * AdTagId is associated with the Banner Ad Size. You have to create the
	 * AdTagId for recommended size.
	 *
	 * Before loading the Ad's you have to set the respective AdTagId for given
	 * Banner.
	 *
	 * @param adTagId
	 *            adTagId to set
	 */
	public void setAdTagId(String adTagId) {
		mAdTagId = adTagId;
	}

	/**
	 * You have to set the site id before making call to loadAd.
	 *
	 * @param siteId
	 *            siteId to set
	 */
	public void setSiteId(String siteId) {
		mSiteId = siteId;
	}

	/**
	 * You have to set the Publisher id before making a call to loadAd.
	 *
	 * @param publisherId
	 *            publisherId The publisher id provided by PubMatic.
	 */
	public void setPublisherId(String publisherId) {
		mPublisherId = publisherId;
	}

	/**
	 * @param pubAdSize
	 *            the pubAdSize to set
	 */
	public void setAdSize(PUBAdSize pubAdSize) {
		setPubAdSize(pubAdSize);
	}

	/**
	 * This is an optional API's to set for application user Information to
	 * target specific audience. You can set the user information with
	 * PMUserInfo
	 *
	 * @param userInfo
	 */
	public void setUserInformation(PMUserInfo userInfo) {
		setPubUserInformation(userInfo);
	}

	public String getAdTagId() {
		return mAdTagId;
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
		// TODO Auto-generated method stub
		return !TextUtils.isEmpty(mAdTagId) && !TextUtils.isEmpty(mSiteId) && !TextUtils.isEmpty(mPublisherId);
	}

	@Override
	public void setCustomParams(Map<String, List<String>> customParams) {
		mCustomParams = customParams;
	}

	@Override
	public void copyRequestParams(BannerAdRequest adRequest) {
		if(adRequest!=null && adRequest instanceof PubMaticBannerAdRequest) {
			if(TextUtils.isEmpty(mAdTagId))
				this.mAdTagId = ((PubMaticBannerAdRequest)adRequest).mAdTagId;
			if(TextUtils.isEmpty(mPublisherId))
				this.mPublisherId = ((PubMaticBannerAdRequest)adRequest).mPublisherId;
			if(TextUtils.isEmpty(mSiteId))
				this.mSiteId = ((PubMaticBannerAdRequest)adRequest).mSiteId;
			if(getWidth()<=0)
				setWidth(adRequest.getWidth());
			if(getHeight()<=0)
				setHeight(adRequest.getHeight());
		}
	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.banner.pubmatic.PubMaticBannerRRFormatter";
	}

	@Override
	public void setAttributes(AttributeSet attr) {
		try{
			mPublisherId = attr.getAttributeValue(null,
												  CommonConstants.PUB_ID_PARAM);

			mSiteId = attr.getAttributeValue(null,
											 CommonConstants.SITE_ID_PARAM);

			mAdTagId = attr.getAttributeValue(null,
											  CommonConstants.AD_ID_PARAM);

			String width = attr.getAttributeValue(null,
												  CommonConstants.AD_WIDTH);

			if(!TextUtils.isEmpty(width))
				setWidth(Integer.parseInt(width));

			String height = attr.getAttributeValue(null,
												   CommonConstants.AD_HEIGHT);

			if(!TextUtils.isEmpty(height))
				setHeight(Integer.parseInt(height));


		} catch(Exception ex) {

		}
	}

	/**
	 * @return the mAdRefreshRate
	 */
	public int getAdRefreshRate() {
		return mAdRefreshRate;
	}

	/**
	 * @return the mPMUserInfo
	 */
	public PMUserInfo getUserInfo() {
		return mPMUserInfo;
	}

	/**
	 * @param mPMUserInfo the mPMUserInfo to set
	 */
	public void setPubUserInformation(PMUserInfo mPMUserInfo) {
		this.mPMUserInfo = mPMUserInfo;
	}

	/**
	 * @return the mPubAdSize
	 */
	public PUBAdSize getPubAdSize() {
		return mPubAdSize;
	}

	/**
	 * @param mPubAdSize the mPubAdSize to set
	 */
	public void setPubAdSize(PUBAdSize mPubAdSize) {
		this.mPubAdSize = mPubAdSize;
	}

	/**
	 * @return the mNetworkType
	 */
	public String getNetworkType() {
		return mNetworkType;
	}

	/**
	 * @param mNetworkType the mNetworkType to set
	 */
	public void setNetworkType(String mNetworkType) {
		this.mNetworkType = mNetworkType;
	}

	/**
	 * @return the mAdOrientation
	 */
	public int getAdOrientation() {
		return mAdOrientation;
	}

	/**
	 * @param mAdOrientation the mAdOrientation to set
	 */
	public void setAdOrientation(int mAdOrientation) {
		this.mAdOrientation = mAdOrientation;
	}

	/**
	 * @return the mDeviceOrientation
	 */
	public int getDeviceOrientation(Context context) {
		int rotation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
		if(rotation==Surface.ROTATION_0 || rotation==Surface.ROTATION_180)
			mDeviceOrientation = 0;
		else if(rotation==Surface.ROTATION_90 || rotation==Surface.ROTATION_270)
			mDeviceOrientation = 1;
		return mDeviceOrientation;
	}

	/**
	 * @param mDeviceOrientation the mDeviceOrientation to set
	 */
	public void setDeviceOrientation(int mDeviceOrientation) {
		this.mDeviceOrientation = mDeviceOrientation;
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

	/**
	 * @return the mLatitude
	 */
	public float getLatitude() {
		return mLatitude;
	}

	/**
	 * @param mLatitude the mLatitude to set
	 */
	public void setLatitude(float mLatitude) {
		this.mLatitude = mLatitude;
	}

	/**
	 * @return the mLongitude
	 */
	public float getLongitude() {
		return mLongitude;
	}

	/**
	 * @param mLongitude the mLongitude to set
	 */
	public void setLongitude(float mLongitude) {
		this.mLongitude = mLongitude;
	}
}
