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
package com.pubmatic.sdk.banner.phoenix;

import android.text.TextUtils;

import com.pubmatic.sdk.banner.BannerAdDescriptor;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.pubmatic.PubMaticConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PhoenixBannerRRFormatter  implements RRFormatter {

	private AdRequest mRequest;

	private final static String kBid_Tag            = "bids";
	private static final String kecpm               = "ecpm";
	private static final String kcreative_tag       = "ct";
	private static final String ktracking_url       = "tr";
	private static final String kclick_tracking_url = "cltr";
	private static final String kcreative_type_tag  = "crTy";

	@Override
	public HttpRequest formatRequest(AdRequest request) {
		mRequest = request;
		PhoenixBannerAdRequest adRequest = (PhoenixBannerAdRequest) request;
		adRequest.createRequest();

		HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);

		httpRequest.setUserAgent(adRequest.getUserAgent());
		httpRequest.setConnection("close");
		httpRequest.setRequestUrl(request.getRequestUrl());
		httpRequest.setRequestMethod(CommonConstants.HTTPMETHODGET);
		httpRequest.setRequestType(CommonConstants.AD_REQUEST_TYPE.PHOENIX_BANNER);
		httpRequest.setPostData(adRequest.getPostData());
		return httpRequest;
	}

	@Override
	public AdResponse formatResponse(HttpResponse response) {

		AdResponse pubResponse = new AdResponse();
		pubResponse.setRequest(mRequest);

		Map<String, String> adInfo = new HashMap<String, String>();
		ArrayList<String> impressionTrackers = new ArrayList<String>();
		ArrayList<String> clickTrackers = new ArrayList<String>();
		adInfo.put("type", "thirdparty");

		try {
			// Check whether the adResponse is null or not. If it is null then
			// it is
			// an invalid ad, so sending the null response.
			if (response == null) {
				return null;
			}

			// Parsing of the response.
			JSONObject jsonObject = new JSONObject(response.getResponseData().toString());
			JSONArray bidArray = jsonObject.getJSONArray(kBid_Tag);

			// If there is an error from the server which happens when provided
			// wrong ad parameters, return the error with error code and error
			// message.

			if (bidArray==null || bidArray.isNull(0) || (TextUtils.isEmpty(bidArray.getString(0)))) {

				pubResponse.setErrorCode("-1");
				pubResponse.setErrorMessage(null);

				return pubResponse;
			}

			// Check if json contains the creative_tag and tracking_url.
			// If these are missing then the ad is invalid. Return null else
			// return valid adInfo object.
			for (int i = 0; i < bidArray.length(); i++) {
				JSONObject object = bidArray.getJSONObject(i);

				if (object.isNull(kcreative_tag) == true || !TextUtils.isEmpty(object.getString(kcreative_tag))
						|| object.isNull(
						ktracking_url) == true || !TextUtils.isEmpty(object.getString(ktracking_url))) {

					// Setting ecpm if not null
					if (!object.isNull(kecpm)) {
						adInfo.put("ecpm", object.getString(kecpm));
					}

					try {
						String creative= URLDecoder.decode(object.getString(kcreative_tag), CommonConstants.URL_ENCODING);
						adInfo.put("content", creative);
					} catch (UnsupportedEncodingException e) {

					}
					// parse tracking url
					JSONArray trackingArray = object.getJSONArray(ktracking_url);
					for (int j = 0; j < trackingArray.length(); j++) {
						if(!TextUtils.isEmpty(trackingArray.getString(j)))
							impressionTrackers.add(trackingArray.getString(j));
					}

					// Setting click_tracking_url if not null
					JSONArray clickTrackingArray = object.getJSONArray(kclick_tracking_url);
					for (int j = 0; j < clickTrackingArray.length(); j++) {
						if(!TextUtils.isEmpty(clickTrackingArray.getString(j)))
							clickTrackers.add(clickTrackingArray.getString(j));
					}

					// Setting creative_type if not null
					if (!object.isNull(kcreative_type_tag)) {
						adInfo.put("url", object.getString(kcreative_type_tag));
					}
				}
			}

			BannerAdDescriptor adDescriptor = new BannerAdDescriptor(adInfo);
			adDescriptor.setImpressionTrackers(impressionTrackers);
			adDescriptor.setClickTrackers(clickTrackers);
			pubResponse.setRenderable(adDescriptor);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			response = null;
		}
		return pubResponse;
	}

}
