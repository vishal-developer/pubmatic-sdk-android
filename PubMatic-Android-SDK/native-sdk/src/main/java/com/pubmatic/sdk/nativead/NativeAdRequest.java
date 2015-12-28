package com.pubmatic.sdk.nativead;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.utils.CommonConstants;
import com.pubmatic.sdk.common.utils.CommonConstants.CHANNEL;

public abstract class NativeAdRequest extends AdRequest {

	private int mWidth;
	private int mHeight;
	private String mUserAgent;
	protected CHANNEL mChannel;
	private int timeout = CommonConstants.NETWORK_TIMEOUT_SECONDS;

	protected NativeAdRequest(CHANNEL channel) {
		mChannel = channel;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(int mWidth) {
		this.mWidth = mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(int mHeight) {
		this.mHeight = mHeight;
	}

	public String getUserAgent() {
		return mUserAgent;
	}

	public void setUserAgent(String mUserAgent) {
		this.mUserAgent = mUserAgent;
	}

	/**
	 * This method must take the Adserver URL, default params and custom params
	 * to form a requestURL. All platform specific inputs will be taken from
	 * respective subclasses. 
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("rawtypes")
	//It is being called from internalUpdate.
	public void createRequest(Context context) {
		mPostData		= null;
		initializeDefaultParams(context);
		setupPostData();
	}

	/**
	 * This method must take the Adserver URL, default params and custom params
	 * to form a requestURL. All platform specific inputs will be taken from
	 * respective subclasses.
	 *
	 * @throws UnsupportedEncodingException
	 */
	@Override
	protected void setupPostData() {

		// Append the Custom params to URL
		if (mCustomParams.size() > 0) {
			Set entrySet = mCustomParams.entrySet();
			Iterator it = entrySet.iterator();
			List list;

			while (it.hasNext()) {
				Map.Entry mapEntry = (Map.Entry) it.next();
				list = (List) mCustomParams.get(mapEntry.getKey());
				for (int j = 0; j < list.size(); j++) {
					putPostData(mapEntry.getKey().toString(), list.get(j).toString());
				}
			}
			entrySet.clear();
		}
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public abstract void setAttributes(AttributeSet attr);
	
	public abstract void copyRequestParams(NativeAdRequest adRequest);
	
	public abstract String getFormatter();

	public CHANNEL getChannel() {
		return mChannel;
	}

	public void setChannel(CHANNEL mChannel) {
		this.mChannel = mChannel;
	}
}
