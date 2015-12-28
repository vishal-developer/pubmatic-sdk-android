package com.pubmatic.sdk.banner;

import android.content.Context;
import android.util.AttributeSet;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.utils.CommonConstants;
import com.pubmatic.sdk.common.utils.CommonConstants.CHANNEL;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BannerAdRequest extends AdRequest {


    private int mWidth;
    private int mHeight;
    private String mUserAgent;
    protected CHANNEL mChannel;
    private int timeout = CommonConstants.NETWORK_TIMEOUT_SECONDS;

    protected BannerAdRequest(CHANNEL channel) {
        mChannel = channel;
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
     * This method must take the Adserver URL, default params and custom params
     * to form a requestURL. All platform specific inputs will be taken from
     * respective subclasses.
     *
     * @throws UnsupportedEncodingException
     */
    public void createRequest(Context context) {
        mPostData		= null;
        initializeDefaultParams(context);
        setupPostData();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public abstract void setAttributes(AttributeSet attr);

    public abstract void copyRequestParams(BannerAdRequest adRequest);

    public abstract String getFormatter();

    public CHANNEL getChannel() {
        return mChannel;
    }

    public void setChannel(CHANNEL mChannel) {
        this.mChannel = mChannel;
    }

}
