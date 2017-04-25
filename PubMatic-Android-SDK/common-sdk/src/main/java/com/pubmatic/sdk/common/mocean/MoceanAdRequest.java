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
package com.pubmatic.sdk.common.mocean;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMLogger;

public abstract class MoceanAdRequest extends AdRequest {

	public abstract void setAttributes(AttributeSet attr);

	protected MoceanAdRequest(Context context) {
		super(CommonConstants.CHANNEL.MOCEAN, context);
		mContext = context;

	}

	@Override
	public String getAdServerURL() {
		return TextUtils.isEmpty(mBaseUrl) ? CommonConstants.MOCEAN_AD_NETWORK_URL
				: mBaseUrl;
	}

	/**
	 *
	 * @param adRequest
     */
	@Override
	public void copyRequestParams(AdRequest adRequest) {
		if(adRequest!=null && adRequest instanceof MoceanAdRequest) {

			if (TextUtils.isEmpty(mZoneId))
				this.mZoneId = ((MoceanAdRequest)adRequest).mZoneId;
			if (TextUtils.isEmpty(mIp))
				this.mIp = ((MoceanAdRequest)adRequest).mIp;
			if (TextUtils.isEmpty(mUserAgent))
				this.mUserAgent = ((MoceanAdRequest)adRequest).mUserAgent;
		}
	}

	@Override
	protected void setupPostData() {

		super.setupPostData();

		try {

			putPostData(CommonConstants.REQUESTPARAM_ZONE, mZoneId);

			// Set IP address
			if(!TextUtils.isEmpty(mIp))
				putPostData(CommonConstants.REQUESTPARAM_IP, mIp);

			// Set user-agent
			if (!TextUtils.isEmpty(mUserAgent))
				putPostData(CommonConstants.REQUESTPARAM_UA, mUserAgent);

			//Append custom parameters
			if(mCustomParams!=null && !mCustomParams.isEmpty()) {
				Set<String> set = mCustomParams.keySet();
				Iterator<String> iterator = set.iterator();
				while(iterator.hasNext()) {
					String key = iterator.next();
					List<String> valueList = mCustomParams.get(key);
					for(String s : valueList) {
						putPostData(key,s);
					}

				}
			}
			//======== Setting user specific information ========

			// Setting Gender of user
			if (!TextUtils.isEmpty(mGender))
				putPostData(CommonConstants.GENDER_PARAM, URLEncoder.encode(
						mGender, CommonConstants.URL_ENCODING));

			// Setting the user ethnicity
			if (!TextUtils.isEmpty(mEthnicity)) {
				putPostData(CommonConstants.USER_ETHNICITY, URLEncoder.encode(
						mEthnicity, CommonConstants.URL_ENCODING));
			}

			// Setting the income
			if (!TextUtils.isEmpty(mAge)) {
				putPostData(CommonConstants.REQUESTPARAM_AGE,
						URLEncoder.encode(mAge, CommonConstants.URL_ENCODING));
			}

			// Setting zip code of user
			if (!TextUtils.isEmpty(mBirthDay)) {
				putPostData(CommonConstants.REQUESTPARAM_BIRTHDAY,
						URLEncoder.encode(mBirthDay,
								CommonConstants.URL_ENCODING));
			}

			// Setting over_18 value
			switch (mOver18) {
			case DENY:
				putPostData(CommonConstants.REQUESTPARAM_OVER_18,
						String.valueOf(0));
				break;
			case ONLY_OVER_18:
				putPostData(CommonConstants.REQUESTPARAM_OVER_18,
						String.valueOf(2));
				break;
			case ALLOW_ALL:
				putPostData(CommonConstants.REQUESTPARAM_OVER_18,
						String.valueOf(3));
				break;
			case NA:
				break;
			}

			// Setting the area
			if (!TextUtils.isEmpty(mAreaCode)) {
				putPostData(CommonConstants.REQUESTPARAM_AREA,
						URLEncoder.encode(mAreaCode,
								CommonConstants.URL_ENCODING));
			}
			if (!TextUtils.isEmpty(mCity)) {
				putPostData(CommonConstants.REQUESTPARAM_CITY,
						URLEncoder.encode(mCity, CommonConstants.URL_ENCODING));
			}

			if (!TextUtils.isEmpty(mDMA)) {
				putPostData(CommonConstants.REQUESTPARAM_DMA,
						URLEncoder.encode(mDMA, CommonConstants.URL_ENCODING));
			}
			if (!TextUtils.isEmpty(mZip)) {
				putPostData(CommonConstants.REQUESTPARAM_ZIP,
						URLEncoder.encode(mZip, CommonConstants.URL_ENCODING));
			}

			if (!TextUtils.isEmpty(mIsoRegion)) {
				putPostData(CommonConstants.REQUESTPARAM_ISO_REGION,
						URLEncoder.encode(mIsoRegion,
								CommonConstants.URL_ENCODING));
			}

			// Send Advertisement ID
			if (!TextUtils.isEmpty(mUDID)) {
				putPostData(CommonConstants.REQUESTPARAM_ANDROID_AID, mUDID);// Android
																				// Advertising
																				// ID
			} else if (mContext != null) {
				// Send Android ID
				putPostData(CommonConstants.REQUESTPARAM_ANDROID_ID,
						getUdidFromContext(mContext));
			}

			// Set the location
			if (mLocation != null) {
				putPostData(CommonConstants.REQUESTPARAM_LATITUDE,
						String.valueOf(mLocation.getLatitude()));
				putPostData(CommonConstants.REQUESTPARAM_LONGITUDE,
						String.valueOf(mLocation.getLongitude()));
			}
		} catch (Exception e) {
            PMLogger.logEvent("MoceanAdRequest: Error while setting request post data."+(e==null?"":e.getMessage()),
                              PMLogger.LogLevel.Error);
        }
	}

	@Override
	public void createRequest(Context context) {
		mPostData = null;
		initializeDefaultParams(context);
		setupPostData();
	}

	// PubMatic specific enums
	public enum AD_TYPE {
		TEXT, IMAGE, IMAGE_TEXT, RICHMEDIA, NATIVE, VIDEO, AUDIO
	}

	public enum AD_VISIBILITY {
		CAN_NOT_DETERMINE, ABOVE_FOLD, BELOW_FOLD, PARTIAL
	}

	public enum ETHNICITY {
		HISPANIC, AFRICAN_AMERICAN, CAUCASIAN, ASIAN_AMERICAN, OTHER
	}

	// Mocean specific enums
	public enum FORMAT_KEY {
		HTML, XML, JSON, JSONP, GENERIC, VAST, DAAST, OFFLINE_XML
	}

	protected Context mContext;
	protected String mZoneId;
	protected String mIp;

	public enum OVER_18 {
		NA, DENY, // 0 or 1
		ONLY_OVER_18, // 2
		ALLOW_ALL // 3
	}

	public static final String GENDER_MALE = "M";
	public static final String GENDER_FEMALE = "F";
	public static final String GENDER_OTHER = "O";

	// Mocean USer info params
	private String mAge = null;
	private String mGender = null;
	private String mAreaCode = null;
	private OVER_18 mOver18 = OVER_18.NA;
	private String mBirthDay = null;
	private String mIsoRegion = null;

	// Common for Mocean & PubMatic User info params
	private String mCity = null;
	private String mZip = null;
	private String mDMA = null;
	private String mEthnicity = null;

	// PubMatic User info
	/*
	 * private String mCountry = null; private String mState = null; private
	 * String mYearOfBirth = null; private String mIncome = null; private
	 * ArrayList<String> mKeywordsList = null;
	 */

	public String getIp() {
		return mIp;
	}

	public void setIp(String mIp) {
		this.mIp = mIp;
	}

	public String getIsoRegion() {
		return mIsoRegion;
	}

	public String getAge() {
		return mAge;
	}

	public void setAge(String mAge) {
		this.mAge = mAge;
	}

	public OVER_18 getOver18() {
		return mOver18;
	}

	public void setOver18(OVER_18 mOver18) {
		this.mOver18 = mOver18;
	}

	public void setIsoRegion(String isoRegion) {
		mIsoRegion = isoRegion;
	}

	/**
	 * Set the gender of the user.
	 * 
	 * @param gender
	 *            gender of the user
	 */
	public void setGender(final String gender) {

		if (gender == null || gender.trim().equals("")) {
			mGender = null;
			return;
		}

		if (gender.equalsIgnoreCase(GENDER_MALE)
				|| gender.equalsIgnoreCase(GENDER_FEMALE)
				|| gender.equalsIgnoreCase(GENDER_OTHER)) {
			mGender = gender.toUpperCase();
		} else {
			mGender = null;
		}
	}

	/**
	 *
	 * @param mBirthDay
	 */
	public void setBirthDay(String mBirthDay) {
		this.mBirthDay = mBirthDay;
	}

	/**
	 *
	 * @return
	 */
	public String getBirthDay() {
		return mBirthDay;
	}

	/**
	 * Returns the gender of the user.
	 * 
	 * @return the gender
	 */
	public String getGender() {
		return mGender;
	}

	/**
	 * Returns the area code of the user.
	 * 
	 * @return the area code
	 */
	public String getAreaCode() {
		return mAreaCode;
	}

	/**
	 * Set the area code of the user.
	 * 
	 * @param areaCode
	 *            Area code of the user
	 */
	public void setAreaCode(final String areaCode) {
		mAreaCode = areaCode;
	}

	/**
	 * Returns the zip of the user.
	 * 
	 * @return the zip
	 */
	public String getZip() {
		return mZip;
	}

	/**
	 * Returns the ethnicity of the user.
	 * 
	 * @return the ethnicity of user
	 */
	public String getEthnicity() {
		return mEthnicity;
	}

	/**
	 * Sets the ethnicity parameter
	 * @param mEthnicity
	 */
	public void setEthnicity(String mEthnicity) {
		this.mEthnicity = mEthnicity;
	}
	/**
	 * Set the zip of the user.
	 * 
	 * @param zip
	 *            zip of the user
	 */
	public void setZip(final String zip) {
		mZip = zip;
	}

	public String getDMA() {
		return mDMA;
	}

	public void setDMA(String mDMA) {
		this.mDMA = mDMA;
	}

	/**
	 *
	 * @return
	 */
	public String getCity() {
		return mCity;
	}

	/**
	 *
	 * @param city
	 */
	public void setCity(String city) {
		this.mCity = city;
	}

	public String getZoneId() {
		return mZoneId;
	}

	public void setZoneId(String mZoneId) {
		this.mZoneId = mZoneId;
	}

	private static String getUdidFromContext(Context context) {
		String deviceId = Settings.Secure.getString(
				context.getContentResolver(), Settings.Secure.ANDROID_ID);
		deviceId = (deviceId == null) ? "" : sha1(deviceId);
		return deviceId;

	}

	@SuppressLint("DefaultLocale")
	private static String sha1(String string) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = string.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);
			bytes = digest.digest();

			for (final byte b : bytes) {
				stringBuilder.append(String.format("%02X", b));
			}

			return stringBuilder.toString().toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}

}
