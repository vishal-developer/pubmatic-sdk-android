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

public abstract class AdRequest {

	/**
	 * This parameter will be used to save the base URL
	 */
	protected String					mBaseUrl;
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
	protected int 						mWidth;
	/**
	 *
	 */
	protected int 						mHeight;
	/**
	 *
	 */
	protected int 						mTimeout = CommonConstants.NETWORK_TIMEOUT_SECONDS;
	/**
	 *
	 */
	protected String 					mUserAgent;
	/**
	 *
	 */
	protected CommonConstants.CHANNEL 	mChannel = CommonConstants.CHANNEL.MOCEAN;

	protected static String 			mUDID;

	/**
	 * Publisher can set his own custom defined Ad request parameters via Map
	 */
	protected Map<String, List<String>> mCustomParams;
	/**
	 *
	 * @return
     */
	public abstract String getFormatter();

	/**
	 * Returns the base/host name URL
	 * @return
	 */
	public abstract String getAdServerURL();

	/**
	 *
	 * @return
     */
	public abstract boolean checkMandatoryParams();

	/**
	 *
	 */
	protected void setupPostData() {

	}

	/**
	 *
	 * @param adRequestParams
     */
	public abstract void copyRequestParams(AdRequest adRequestParams);
	/**
	 *
	 * @param context
     */
	protected abstract void initializeDefaultParams(Context context);

	/**
	 *
	 * @param customParams
     */
	public void setCustomParams(Map<String, List<String>> customParams) {
		mCustomParams = customParams;
	}

	/**
	 * It sets the list of multiple values for same key. Set values will be send
	 * with comma separation in post body data like: interest=cricket,football,tennis
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
	 *
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
	 *
	 */
	public abstract void createRequest(Context context);

	protected AdRequest(CommonConstants.CHANNEL channel, Context context) {
		mChannel = channel;
		retrieveAndroidAid(context);
	}

	// androidAid
	private boolean isAndroidAidEnabled;

	/**
	 * add androidaid as request param.
	 *
	 * @param isAndroidAidEnabled
	 */
	public void setAndroidAidEnabled(boolean isAndroidAidEnabled) {
		this.isAndroidAidEnabled = isAndroidAidEnabled;
	}

	public boolean isAndoridAidEnabled() {
		return isAndroidAidEnabled;
	}

	/**
	 * add androidaid as request param.
	 *
	 * @param context
	 */
	public void retrieveAndroidAid(final Context context) {

		new Thread(new Runnable() {
			public void run() {
				try {
					AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
					mUDID = adInfo.getId();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(int mHeight) {
		this.mHeight = mHeight;
	}

	public void setWidth(int mWidth) {
		this.mWidth = mWidth;
	}

	public String getUserAgent() {
		return mUserAgent;
	}

	public void setUserAgent(String mUserAgent) {
		this.mUserAgent = mUserAgent;
	}

	/**
	 * Sets the base/host name URL
	 * @param baseUrl
	 */
	public void setAdServerURL(String baseUrl) {
		if(!TextUtils.isEmpty(baseUrl)) {
			if(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))
				this.mBaseUrl = baseUrl;
			else if(baseUrl.startsWith("//"))
				this.mBaseUrl = "http:"+baseUrl;
			else
				this.mBaseUrl = "http://"+baseUrl;
		}
	}

	public String getPostData() {
		return mPostData!=null ? mPostData.toString() : null;
	}

	public void putPostData(String key, String value) {
		if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
			try {
				//append & before 1st parameter else append &
				if (mPostData == null) {
					mPostData = new StringBuffer();

				} else
					mPostData.append(CommonConstants.AMPERSAND);

				//append key=value
				mPostData.append(URLEncoder.encode(key,
						CommonConstants.ENCODING_UTF_8));
				mPostData.append(CommonConstants.EQUAL);
				mPostData.append(URLEncoder.encode(value,
						CommonConstants.ENCODING_UTF_8));

			} catch (UnsupportedEncodingException e) {
			}

		}
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

	public void setChannel(CommonConstants.CHANNEL mChannel) {
		this.mChannel = mChannel;
	}

}
