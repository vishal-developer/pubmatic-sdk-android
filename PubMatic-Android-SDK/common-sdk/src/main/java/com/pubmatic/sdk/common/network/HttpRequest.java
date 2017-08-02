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
package com.pubmatic.sdk.common.network;

import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.AD_REQUEST_TYPE;
import com.pubmatic.sdk.common.CommonConstants.CHANNEL;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;

public class HttpRequest {


    public static HttpRequest getTrackingRequest(final String requestUrl,
            final CommonConstants.CHANNEL channel) {

        HttpRequest request = new HttpRequest();
        request.setRequestUrl(requestUrl);
        request.setRequestType(channel == CHANNEL.PUBMATIC ? AD_REQUEST_TYPE.PUB_TRACKER : AD_REQUEST_TYPE.PHOENIX_TRACKER);
        request.setRequestMethod(CommonConstants.HTTPMETHODGET);
        return request;
    }
    
	private AD_REQUEST_TYPE mRequestType= null;
	private String 			mRequestUrl = null;
	private String			mPostData	= null;
	
	// Headers
	String mContentLanguage = "en";
	String mAcceptCharset 	= "utf-8";
	String mConnection 		= "close";
	String mCacheControl 	= "no-cache";
	String mAccept 			= "text/plain";
	String mContentTypeHeader= null;
	String mContentLength 	= null;
	String mContentMd5 		= null;
	String mHost 			= null;
	String mAcceptLangauge 	= null;
	String mAcceptDateTime 	= null;
	String mDate 			= null;
	
	private String mRequestMethod 		= null;//GET/POST
//	private String mRLNClientIPAddress 	= null;
	private CONTENT_TYPE 	mContentType = CONTENT_TYPE.INVALID;
	private String mUserAgent 		= null;
	
	public HttpRequest() {
		
	}
	
	public HttpRequest(CONTENT_TYPE contentType)
	{
		this.mContentType 	= contentType;
	}

	public String getConnection() {
		return mConnection;
	}

	public void setConnection(String mConnection) {
		this.mConnection = mConnection;
	}
	
	public String getUserAgent() {
		return mUserAgent;
	}

	public void setUserAgent(String mUserAgent) {
		this.mUserAgent = mUserAgent;
	}

	public String getRequestUrl() {
		return mRequestUrl;
	}
	
	public void setRequestUrl(String mRequestUrl) {
		this.mRequestUrl = mRequestUrl;
	}

	public void appendRequestUrl(String mRequestUrl) {
		if(mRequestUrl==null)
			this.mRequestUrl = mRequestUrl;
		else
			this.mRequestUrl += mRequestUrl;
	}
	
//	public StringBuffer getPOSTData() {
//		return mPOSTData;
//	}
//	
//	public void setPOSTData(StringBuffer mPOSTData) {
//		this.mPOSTData = mPOSTData;
//	}
//	
//	public void appendAdParams(final String adRequestParams) {
//		if(adRequestParams!=null) {
//			if(mPOSTData==null)
//				mPOSTData = new StringBuffer(adRequestParams);
//			else
//				mPOSTData.append(adRequestParams);
//		}
//	}
//	
//	public Method getMethod() {
//		return mMethod;
//	}
//	
//	public void setMethod(Method method) {
//		this.mMethod = method;
//	}
//	
//	public JSONObject getJsonBody() {
//		return mJsonBody;
//	}
//
//	public void setJsonBody(JSONObject jsonBody) {
//		this.mJsonBody = jsonBody;
//	}
//
//	public Map<String, String> getParams() {
//		return mParams;
//	}
//
//	public void setParam(String key, String value) {
//		if(this.mParams==null)
//			this.mParams = new HashMap<String, String>();
//		this.mParams.put(key, value);
//	}
//	
//	public void setParams(Map<String, String> params) {
//		this.mParams = params;
//	}
	
	public CONTENT_TYPE getContentType() {
		return mContentType;
	}

	public void setContentType(CONTENT_TYPE responseType) {
		this.mContentType = responseType;
	}

	public AD_REQUEST_TYPE getRequestType() {
		return mRequestType;
	}

	public void setRequestType(AD_REQUEST_TYPE mRequestType) {
		this.mRequestType = mRequestType;
	}

	/**
	 * @return the mPostData
	 */
	public String getPostData() {
		return mPostData;
	}

	/**
	 * @param mPostData the mPostData to set
	 */
	public void setPostData(String mPostData) {
		this.mPostData = mPostData;
	}

	public String getRequestMethod() {
		return mRequestMethod;
	}

	public void setRequestMethod(String mRequestMethod) {
		this.mRequestMethod = mRequestMethod;
	}

//	public String getRLNClientIPAddress() {
//		return mRLNClientIPAddress;
//	}
//
//	public void setRLNClientIPAddress(String mRLNClientIPAddress) {
//		this.mRLNClientIPAddress = mRLNClientIPAddress;
//	}
}
