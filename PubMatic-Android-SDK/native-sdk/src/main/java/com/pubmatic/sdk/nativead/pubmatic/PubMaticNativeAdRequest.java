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
package com.pubmatic.sdk.nativead.pubmatic;

import static com.pubmatic.sdk.common.CommonConstants.ID_STRING;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_ASSETS_STRING;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_IMAGE_H;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_IMAGE_W;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_DATA;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_IMG;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_LEN;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_NATIVE_EQ_WRAPPER;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_REQUIRED;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_TITLE;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_TYPE;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;
import com.pubmatic.sdk.nativead.PMNativeAd;
import com.pubmatic.sdk.nativead.bean.PMAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMDataAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMImageAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetRequest;

public class PubMaticNativeAdRequest  extends PubMaticAdRequest {

	private List<PMAssetRequest> requestedAssetsList = null;

	private Context context;
	private boolean test = false;

	/**
	 * This method will create and object of {@link AdRequest}. It is used for
	 * the implementations of {@link PMNativeAd}
	 *
	 * @return {@link AdRequest} instance
	 */
	public static PubMaticNativeAdRequest createPubMaticNativeAdRequest(Context context, String pubId, String siteId, String adId, List<PMAssetRequest> requestedAssets){

		PubMaticNativeAdRequest adRequest = new PubMaticNativeAdRequest(context,
				CommonConstants.PUBMATIC_AD_NETWORK_URL, requestedAssets);
		adRequest.setPubId(pubId);
		adRequest.setSiteId(siteId);
		adRequest.setAdId(adId);
		return adRequest;
	}

	private PubMaticNativeAdRequest(Context context, String adServerUrl,
									List<PMAssetRequest> requestedAssets) {
		super(context);
		this.context = context;
		this.requestedAssetsList = requestedAssets;

		StringBuilder sb = new StringBuilder();
		sb.append(adServerUrl);
		if (sb.indexOf(CommonConstants.QUESTIONMARK) > 0) {
			sb.append(CommonConstants.AMPERSAND);
		} else {
			sb.append(CommonConstants.QUESTIONMARK);
		}
	}

	void createRequest() {
		initializeDefaultParams();
		setUpUrlParams();
		setUpPostParams();
	}

	/**
	 * This method will initialize all the static parameters which SDK need to set.
	 */
	protected void initializeDefaultParams() {
		setOperId(OPERID.JSON_MOBILE);
		setAdType(AD_TYPE.NATIVE);
	}

//	public void setUserAgent(String userAgent) {
//		super.setUserAgent(userAgent);
//	}

	@Override
	public boolean checkMandatoryParams() {
		return !TextUtils.isEmpty(mPubId) && !TextUtils.isEmpty(mSiteId) && !TextUtils.isEmpty(mAdId);
	}

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
	protected void setUpPostParams() {
		super.setUpPostParams();

		//attach the Native asset request data
		setupAssetData();

	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.nativead.pubmatic.PubMaticNativeRRFormatter";
	}

	private void setupAssetData() {
		try {
			JSONObject nativeObj = new JSONObject();
			JSONArray assetsArray = new JSONArray();
			JSONObject assetObj;
			JSONObject titleObj;
			JSONObject imageObj;
			JSONObject dataObj;
			for (PMAssetRequest assetRequest : requestedAssetsList) {
				if (assetRequest != null) {
					assetObj = new JSONObject();
					assetObj.put(ID_STRING, assetRequest.getAssetId());
					assetObj.put(REQUEST_REQUIRED,
							(assetRequest.isRequired ? 1 : 0));
					if (assetRequest instanceof PMTitleAssetRequest) {
						// length is mandatory for title asset
						if (((PMTitleAssetRequest) assetRequest).length > 0) {
							titleObj = new JSONObject();
							titleObj.put(REQUEST_LEN,
									((PMTitleAssetRequest) assetRequest).length);
							assetObj.putOpt(REQUEST_TITLE, titleObj);
						} else {
							assetObj = null;
							Log.w("AdRequest",
									"'length' parameter is mandatory for title asset");
						}
					} else if (assetRequest instanceof PMImageAssetRequest) {
						imageObj = new JSONObject();
						if (((PMImageAssetRequest) assetRequest).imageType != null) {
							imageObj.put(REQUEST_TYPE,
									((PMImageAssetRequest) assetRequest).imageType
											.getTypeId());
						}
						if (((PMImageAssetRequest) assetRequest).width > 0) {
							imageObj.put(NATIVE_IMAGE_W,
									((PMImageAssetRequest) assetRequest).width);
						}
						if (((PMImageAssetRequest) assetRequest).height > 0) {
							imageObj.put(NATIVE_IMAGE_H,
									((PMImageAssetRequest) assetRequest).height);
						}
						assetObj.putOpt(REQUEST_IMG, imageObj);
					} else if (assetRequest instanceof PMDataAssetRequest) {
						dataObj = new JSONObject();
						if (((PMDataAssetRequest) assetRequest).dataAssetType != null) {
							dataObj.put(REQUEST_TYPE,
									((PMDataAssetRequest) assetRequest).dataAssetType
											.getTypeId());

							if (((PMDataAssetRequest) assetRequest).length > 0) {
								dataObj.put(REQUEST_LEN,
										((PMDataAssetRequest) assetRequest).length);
							}
							assetObj.putOpt(REQUEST_DATA, dataObj);
						} else {
							assetObj = null;
							Log.w("AdRequest",
									"'type' parameter is mandatory for data asset");
						}
					}
					if (assetObj != null) {
						assetsArray.put(assetObj);
					}
				}
			}
			nativeObj.putOpt(NATIVE_ASSETS_STRING, assetsArray);

			putPostData(REQUEST_NATIVE_EQ_WRAPPER, nativeObj.toString());
		} catch(JSONException e) {
		}
	}
}