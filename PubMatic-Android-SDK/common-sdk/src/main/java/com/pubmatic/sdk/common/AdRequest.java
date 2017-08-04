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
package com.pubmatic.sdk.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.pubmatic.sdk.common.pubmatic.PUBAdSize;

import static android.content.ContentValues.TAG;

public abstract class AdRequest {

	/**
	 *
	 */
	protected StringBuffer 				mPostData;
	/**
	 *
	 */
	protected Location 					mLocation = null;
	/**
	 *
	 */
	protected PMAdSize 					mPMAdSize = null;
	/**
	 *
	 */
	protected String 					mUserAgent;
	/**
	 *
	 */
	protected CommonConstants.CHANNEL 	mChannel = CommonConstants.CHANNEL.PUBMATIC;

	/**
	 * Request Url Params
	 */
	protected Map<String, String> 		mUrlParams;

	/**
	 * Publisher can set his own custom defined Ad request parameters via Map
	 */
	protected Map<String, List<String>> mCustomParams;

	// androidAid
	protected boolean isAndroidAidEnabled= true;

	protected AdRequest(CommonConstants.CHANNEL channel, Context context) {
		mChannel = channel;
		mUrlParams = new HashMap<>(0);
	}

	//------------------------- All Abstract methods -------------------------
	/**
	 *
	 * @return
     */
	public abstract String getFormatter();

	/**
	 * Returns the base/host name URL
	 * @return
	 */
	protected abstract String getAdServerURL();

	/**
	 *
	 */
	protected abstract void initializeDefaultParams();

	/**
	 *
	 * @return
     */
	public abstract boolean checkMandatoryParams();

	//------------------------- All protected methods -------------------------

    /**
     *
     */
    protected void setUpUrlParams() {

		if(mUrlParams!=null) {
			mUrlParams.clear();
		}
    }

	/**
	 *
	 */
	protected void setupPostData() {

		if(mPostData!=null)
			mPostData =null;
	}

	protected void setUrlParams(Map<String, String> urlParams) {
		mUrlParams = urlParams;
	}

	protected void addUrlParam(String key, String value) {
		if(value != null && !value.equals("")) {

			String encodedValue = null;
			try{
				encodedValue = URLEncoder.encode(
						value,
						CommonConstants.URL_ENCODING);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unable to encode ["+key+"]:[+"+value+"] in ad request URL");
			}
			if(encodedValue!=null)
				mUrlParams.put(key, encodedValue);
		}
	}

	protected void putPostData(String key, String value) {
		if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {

			if (mPostData == null) {
				mPostData = new StringBuffer();

			} else
				mPostData.append(CommonConstants.AMPERSAND);

			String encodedValue = null;
			try{
				encodedValue = URLEncoder.encode(
						value,
						CommonConstants.URL_ENCODING);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "Unable to encode ["+key+"]:[+"+value+"] in ad request");
			}

			if(encodedValue!=null) {
				mPostData.append(key);
				mPostData.append(CommonConstants.EQUAL);
				mPostData.append(encodedValue);
			}
		}
	}

	//------------------------- All public Getter/Setter -------------------------
	/**
	 * Set the list of custom key-value pair. There could be more than 1 possible value for 1 key.
	 * In this case values will be send with comma separated for same key like key=value1,value2,...valuen
	 * @param customParams
     */
	public void setCustomParams(Map<String, List<String>> customParams) {
		mCustomParams = customParams;
	}

	/**
	 * It sets the list of multiple values for same key. Set values will be send
	 * with comma separation in ad request like: interest=cricket,football,tennis
	 * @param key
	 * @param value
	 */
	public void addCustomParam(String key, List<String> value) {
		if(mCustomParams==null)
			mCustomParams = new HashMap<String, List<String>>();

		List<String> list =null;
		if(mCustomParams.containsKey(key)) {
			list = mCustomParams.get(key);
			list.addAll(value);
		} else
			mCustomParams.put(key, value);
	}

	/**
	 * It sets the key and value pair. Key value pair will be send
	 * in ad request like: interest=cricket
	 * @param key
	 * @param value
	 *
	 */
	public void addCustomParam(String key, String value) {
		if(mCustomParams==null)
			mCustomParams = new HashMap<String, List<String>>();

		List<String> list =null;
		if(mCustomParams.containsKey(key)) {
			list = mCustomParams.get(key);
			list.add(value);
		} else {
			list = new ArrayList<String>();
			list.add(value);
			mCustomParams.put(key, list);
		}
	}

	/**
	 * Set to true if Android Advertisement ID to be used for udid parameter instead set false if Android device ID needs to be used.
	 * If it is true and user opt-out for ad from device settings then user's decision will be honored and
	 * Android device ID would be send as a udid
	 *
	 * @param isAndroidAidEnabled
	 */
	public void setAndroidAidEnabled(boolean isAndroidAidEnabled) {
		this.isAndroidAidEnabled = isAndroidAidEnabled;
	}

	/**
	 *
	 * @return
	 */
	public boolean isAndoridAidEnabled() {
		return isAndroidAidEnabled;
	}

	/**
	 * Returns the ad size set from setAdSize()
	 * @return size of banner ad
	 */
	public PMAdSize getAdSize() {
		return mPMAdSize;
	}

	/**
	 * Sets the banner ad size in ad request with provided size.
	 * @return Size of banner ad
	 */
	public void setAdSize(PMAdSize adSize) {
		mPMAdSize = adSize;
	}

	/**
	 * PUBAdSize is deprecated, use PMAdSize class instead.
	 * @return
	 */
	@Deprecated
	public void setAdSize(PUBAdSize adSize) {
		mPMAdSize = new PMAdSize(adSize.getAdWidth(), adSize.getAdHeight());
	}

	public String getUserAgent() {
		return mUserAgent;
	}

	public void setUserAgent(String userAgent) {
		this.mUserAgent = userAgent;
	}

    public Map<String, String> getUrlParams() {
        return mUrlParams;
    }

    public final String getRequestUrl() {

        if(mUrlParams!=null && mUrlParams.size() != 0)
        {

			StringBuffer requestUrl = new StringBuffer(getAdServerURL());

			requestUrl.append(CommonConstants.QUESTIONMARK);

            for (Map.Entry param : mUrlParams.entrySet())
            {
                requestUrl.append(param.getKey() + CommonConstants.EQUAL + param.getValue() + CommonConstants.AMPERSAND);
            }

            requestUrl.setLength(requestUrl.length() - 1);

			return  requestUrl.toString();
        } else {
			return getAdServerURL();
		}

    }

	public String getPostData() {
		return mPostData!=null ? mPostData.toString() : null;
	}

	public Map<String, List<String>> getCustomParams() {
		return mCustomParams;
	}

	/**
	 * Set the location of the user.
	 *
	 * @param location
	 *            - Location of the user
	 */
	public void setLocation(final Location location) {
		mLocation = location;
	}

	/**
	 * Return the location of the user.
	 *
	 * @return the user location
	 */
	public Location getLocation() {
		return mLocation;
	}

	public CommonConstants.CHANNEL getChannel() {
		return mChannel;
	}

}
