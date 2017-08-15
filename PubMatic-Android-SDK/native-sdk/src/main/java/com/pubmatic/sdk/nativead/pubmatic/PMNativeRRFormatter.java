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
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_CLICKTRACKERS;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_DATA;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_ERROR;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_FALLBACK;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_IMG;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_IMPTRACKERS;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_JSTRACKER;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_LINK;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_NATIVE_STRING;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_TEXT;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_TITLE;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_URL;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_VALUE;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_VER;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.CommonConstants.AD_REQUEST_TYPE;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.nativead.NativeAdDescriptor;
import com.pubmatic.sdk.nativead.PMNativeAd.Image;
import com.pubmatic.sdk.nativead.bean.PMAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMDataAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMImageAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetResponse;

public class PMNativeRRFormatter implements RRFormatter {

	private final static String kPubMatic_BidTag = "PubMatic_Bid";
	private static final String kcreative_tag = "creative_tag";
	private static final String kerror_code = "error_code";
	private static final String kerror_message = "error_string";

	private AdRequest mRequest;

	@Override
	public HttpRequest formatRequest(AdRequest request) {
		mRequest = request;
		PMNativeAdRequest adRequest = (PMNativeAdRequest) request;
		adRequest.createRequest();

		HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);
		httpRequest.setUserAgent(adRequest.getUserAgent());
		httpRequest.setConnection("close");
		httpRequest.setRequestUrl(request.getRequestUrl());
		httpRequest.setRequestType(AD_REQUEST_TYPE.PUB_NATIVE);
		httpRequest.setRequestMethod(CommonConstants.HTTPMETHODPOST);
		httpRequest.setPostData(adRequest.getPostData());
		return httpRequest;
	}

	@Override
	public AdResponse formatResponse(HttpResponse httpResponse) {
		AdResponse adResponse = new AdResponse();
		// adResponse.setStatusCode(response.getStatusCode());
		adResponse.setRequest(mRequest);

		NativeAdDescriptor nativeAdDescriptor = null;

		try {
			if (httpResponse != null && httpResponse.getResponseData()!=null) {
				ArrayList<PMAssetResponse> nativeAssetList = new ArrayList<PMAssetResponse>();
				String clickUrl = null;
				String fallbackUrl = null;
				String creativeId = null;
				String feedId = null;
				String type = "native";
				String subType = null;
				JSONObject nativeObj = null;
				int nativeVersion = 0;
				String[] clickTrackersStringArray = null;
				String[] impressionTrackerStringArray = null;
				String jsTrackerString = null;

				JSONObject responseObj = new JSONObject(httpResponse.getResponseData());
				JSONObject object = responseObj.getJSONObject(kPubMatic_BidTag);

				// If there is an error from the server which happens when provided
				// wrong ad parameters, return the error with error code and error
				// message.

				if (!object.isNull(kerror_code)
						&& !(object.getString(kerror_code).equalsIgnoreCase(""))) {

					adResponse.setErrorCode(object.getString(kerror_code));
					adResponse.setErrorMessage(object.getString(kerror_message));

					return adResponse;
				}

				if (object.isNull(kcreative_tag) == false) {
					JSONObject creative_tag = object.getJSONObject(kcreative_tag);

					/* Get the native object */
					nativeObj = creative_tag.getJSONObject(RESPONSE_NATIVE_STRING);

					if (nativeObj != null) {
						nativeVersion = nativeObj.optInt(RESPONSE_VER);

						/* Parse impression trackers starts */
						JSONArray imptracker = nativeObj
								.optJSONArray(RESPONSE_IMPTRACKERS);
						nativeObj.remove(RESPONSE_IMPTRACKERS);
						for (int i = 0; imptracker != null
								&& i < imptracker.length(); i++) {
							String url = imptracker.optString(i);
							if (impressionTrackerStringArray == null) {
								impressionTrackerStringArray = new String[imptracker
										.length()];
							}

							if (url != null) {
								impressionTrackerStringArray[i] = url;
							}
						}
						/* Parse impression trackers Ends */

						// Parse jsTracker
						jsTrackerString = nativeObj
								.optString(RESPONSE_JSTRACKER);

						/* Parse link object and contents */
						JSONObject linkObj = nativeObj
								.optJSONObject(RESPONSE_LINK);
						if (linkObj != null) {
							clickUrl = linkObj.optString(RESPONSE_URL);
							fallbackUrl = linkObj
									.optString(RESPONSE_FALLBACK);

							/* Parse click trackers */
							JSONArray clktrackerArray = linkObj
									.optJSONArray(RESPONSE_CLICKTRACKERS);
							linkObj.remove(RESPONSE_CLICKTRACKERS);
							for (int i = 0; clktrackerArray != null
									&& i < clktrackerArray.length(); i++) {
								String clickTrackUrl = clktrackerArray
										.optString(i);
								if (clickTrackersStringArray == null) {
									clickTrackersStringArray = new String[clktrackerArray
											.length()];
								}

								if (clickTrackUrl != null) {
									clickTrackersStringArray[i] = clickTrackUrl;
								}
							}
							/* Parse click trackers Ends */
						}

						// Parse assets.
						JSONArray assets = nativeObj
								.optJSONArray(NATIVE_ASSETS_STRING);
						if (assets != null && assets.length() > 0) {
							JSONObject asset = null;
							int assetId = -1;
							for (int i = 0; i < assets.length(); i++) {
								asset = assets.optJSONObject(i);
								assetId = asset.optInt(ID_STRING, -1);

								if (!asset.isNull(RESPONSE_IMG)) {
									JSONObject imageAssetObj = asset
											.optJSONObject(RESPONSE_IMG);
									PMImageAssetResponse imageAsset = new PMImageAssetResponse();
									imageAsset.assetId = assetId;
									imageAsset.setImage(Image
											.getImage(imageAssetObj));
									if (!TextUtils.isEmpty(imageAsset
											.getImage().url)) {
										nativeAssetList.add(imageAsset);
									}
									continue;
								} else if (!asset.isNull(RESPONSE_TITLE)) {
									JSONObject titleAssetObj = asset
											.optJSONObject(RESPONSE_TITLE);
									PMTitleAssetResponse titleAsset = new PMTitleAssetResponse();
									titleAsset.assetId = assetId;
									titleAsset.titleText = titleAssetObj
											.optString(RESPONSE_TEXT);
									if (!TextUtils
											.isEmpty(titleAsset.titleText)) {
										nativeAssetList.add(titleAsset);
									}
									continue;
								} else if (!asset.isNull(RESPONSE_DATA)) {
									JSONObject dataAssetObj = asset
											.optJSONObject(RESPONSE_DATA);
									PMDataAssetResponse dataAsset = new PMDataAssetResponse();
									dataAsset.assetId = assetId;
									dataAsset.value = dataAssetObj
											.optString(RESPONSE_VALUE);
									if (!TextUtils.isEmpty(dataAsset.value)) {
										nativeAssetList.add(dataAsset);
									}
								}
							}
						}
					}

				}
				//Native parsing ends

				nativeAdDescriptor = new NativeAdDescriptor(
						nativeVersion, clickUrl, fallbackUrl,
						impressionTrackerStringArray,
						clickTrackersStringArray, jsTrackerString,
						nativeAssetList);

				nativeAdDescriptor.setNativeAdJSON(httpResponse.getResponseData());
				// @formatter:on
			}
		} catch (JSONException e) {
			try {
				// Check whether there is an error. If the error format is
				/* { "error": "No ads available" } */
				JSONObject errorResponse = new JSONObject(httpResponse.getResponseData());
				String errorMessage = errorResponse.optString(RESPONSE_ERROR);
				if (!TextUtils.isEmpty(errorMessage)) {
					adResponse.setErrorMessage(errorMessage);
				}
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		adResponse.setRenderable(nativeAdDescriptor);
		return adResponse;
	}

	public AdRequest getAdRequest() {
		return mRequest;
	}

	public void setAdRequest(AdRequest mRequest) {
		this.mRequest = mRequest;
	}

	public AdRequest getRequest() {
		return mRequest;
	}

	public void setRequest(AdRequest mRequest) {
		this.mRequest = mRequest;
	}

}
