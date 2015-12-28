package com.pubmatic.sdk.common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.pubmatic.sdk.common.utils.CommonConstants;

public abstract class AdRequest {

	/**
	 * Publisher can set his own custom defined Ad request parameters via Map
	 */
	protected Map<String, List<String>> mCustomParams;
	/**
	 * This parameter will be used to save the base URL
	 */
	protected String					mBaseUrl;

	//It will be used only for parameter formation from internal update.
	//It should be reset on every ad request from same object.
	protected StringBuffer 				mPostData;

	/**
	 * Returns the base/host name URL
	 * @return
	 */
	public abstract String getAdServerURL();

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


	public AdRequest() {
		//mRequestParams = new HashMap<String, String>();
		mCustomParams = new HashMap<String, List<String>>();
	}

	public abstract boolean checkMandatoryParams();

	protected abstract void initializeDefaultParams(Context context);

	protected abstract void setupPostData();

	public abstract void setCustomParams(Map<String, List<String>> customParams);

	public String getPostData() {
		return mPostData!=null ? mPostData.toString() : null;
	}

	public void setPostData(String mPostData) {
		this.mPostData = new StringBuffer(mPostData);
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

}
