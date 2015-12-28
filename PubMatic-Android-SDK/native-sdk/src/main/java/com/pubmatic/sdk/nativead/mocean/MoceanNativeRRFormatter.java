package com.pubmatic.sdk.nativead.mocean;

import static com.pubmatic.sdk.common.utils.CommonConstants.ID_STRING;
import static com.pubmatic.sdk.common.utils.CommonConstants.NATIVE_ASSETS_STRING;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_ADS;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_CLICKTRACKERS;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_CREATIVEID;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_DATA;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_ERROR;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_FALLBACK;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_FEEDID;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_IMG;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_IMPTRACKERS;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_JSTRACKER;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_LINK;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_MEDIATION;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_MEDIATION_ADID;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_MEDIATION_DATA;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_MEDIATION_NAME;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_MEDIATION_SOURCE;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_NATIVE_STRING;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_SUBTYPE;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_TEXT;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_THIRDPARTY_STRING;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_TITLE;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_TYPE;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_URL;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_VALUE;
import static com.pubmatic.sdk.common.utils.CommonConstants.RESPONSE_VER;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.network.ProtocolConstants;
import com.pubmatic.sdk.common.utils.CommonConstants.AD_REQUEST_TYPE;
import com.pubmatic.sdk.common.utils.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.nativead.NativeAdDescriptor;
import com.pubmatic.sdk.nativead.PMNativeAd.Image;
import com.pubmatic.sdk.nativead.bean.PMAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMDataAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMImageAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetResponse;

public class MoceanNativeRRFormatter implements RRFormatter {


	private AdRequest mRequest;

	@Override
	public HttpRequest formatRequest(AdRequest request) {
		mRequest = request;
		MoceanNativeAdRequest adRequest = (MoceanNativeAdRequest) request;
		HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);
		httpRequest.setUserAgent(adRequest.getUserAgent());
		httpRequest.setConnection("close");
		httpRequest.setRequestUrl(request.getAdServerURL());
		httpRequest.setRequestType(AD_REQUEST_TYPE.MOCEAN_NATIVE);
		httpRequest.setRequestMethod(ProtocolConstants.HTTPMETHODPOST);
		httpRequest.setPostData(adRequest.getPostData());
		return httpRequest;
	}


	// @formatter:off
	/**
	 * Parses the native response. The common native response is as follows:-
	 * 
	 * In case of receiving ad : { "type": "thirdparty", "subtype": "native",
	 * "creativeid": 1, "feedid": 1115, "native": { "ver": 1, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 0 \ / 1
	 * " , "fallback": "http:\/\/example.com\/fallback", "clicktrackers": [
	 * "http:\/\/clicktracker.com\/main\/9bde02d0-6017-11e4-9df7-005056967c35" ]
	 * }, "assets": [ { "id": 2, "img": { "url": "http:\/\/example_320x50.png",
	 * "w": 320, "h": 50 }, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 1 \ / 1
	 * " , "fallback": "http:\/\/example.com\/custom_fallback", "clicktrackers
	 * ": [
	 * "http:\/\/clicktracker.com\/custom\/9bde02d0-6017-11e4-9df7-005056967c35"
	 * ] } }, { "id": 3, "title": { "text": "Native title" }, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 2 \ / 1 " } }, { "id": 4, "data": { "value": "Native
	 * description" }, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 3 \ / 1
	 * " } }, { "id": 6, "data": { "value": "5" }, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 4 \ / 1 " } } ], "imptrackers": [
	 * "http:\/\/ads.moceanads.com\/1\/img\/9bde02d0-6017-11e4-9df7-005056967c35
	 * " ] } }
	 * 
	 * In case of third party : { "type": "thirdparty", "subtype": "mediation",
	 * "creativeid": 1, "feedid": 1116, "mediation": { "id": 456, "name":
	 * "Mediation partner name as returned by the Ad Feed Partner", "source":
	 * "mediation", "data": { "adid": "123" }, "imptrackers": [
	 * "http:\/\/ads.moceanads.com\/1\/img\/9bde02d0-6017-11e4-9df7-005056967c35
	 * " ], "clicktrackers": [
	 * "http:\/\/ads.moceanads.com\/1\/click\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 1 " ] } }
	 * 
	 * @return AdDescriptor containing response for NativeAd
	 * 
	 */
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
				String type = null;
				String subType = null;
				JSONObject mediationObj = null;
				JSONObject nativeObj = null;
				int nativeVersion = 0;
				String mediationPartnerName = null;
				String mediationId = null;
				JSONObject mediationData = null;
				String adUnitId = null;
				String errorMessage = null;
				String mediationSource = null;
				String[] clickTrackersStringArray = null;
				String[] impressionTrackerStringArray = null;
				String jsTrackerString = null;
				JSONObject ad = null;

				JSONObject responseObj = new JSONObject(httpResponse.getResponseData());
				JSONArray ads = responseObj.getJSONArray(RESPONSE_ADS);

				if (ads != null && (ad = ads.optJSONObject(0)) != null
						&& ad.has(RESPONSE_ERROR)) {
					// Check whether there is an error. If the error format is:
					/* {"ads":[{ "error": "<error message>"}]} */
					// @formatter:on
					errorMessage = ad.optString(RESPONSE_ERROR);

					if (!TextUtils.isEmpty(errorMessage)) {
						adResponse.setErrorMessage(errorMessage);;
						return adResponse;
					}
				}
				if (ads != null && (ad = ads.optJSONObject(0)) != null) {
					type = ad.optString(RESPONSE_TYPE);
					subType = ad.optString(RESPONSE_SUBTYPE);

					int creativeIdInt = ad.optInt(RESPONSE_CREATIVEID, -1);
					if (creativeIdInt != -1) {
						creativeId = String.valueOf(creativeIdInt);
					}

					int feedIdInt = ad.optInt(RESPONSE_FEEDID, -1);
					if (feedIdInt != -1) {
						feedId = String.valueOf(feedIdInt);
					}

					// Parse third-party response
					mediationObj = ad.optJSONObject(RESPONSE_MEDIATION);
					if (mediationObj != null) {
						mediationPartnerName = mediationObj
								.optString(RESPONSE_MEDIATION_NAME);
						mediationSource = mediationObj
								.optString(RESPONSE_MEDIATION_SOURCE);

						// Parse id for third party mediation
						int mediationNetworkId = mediationObj.optInt(ID_STRING,
								-1);
						if (mediationNetworkId != -1) {
							mediationId = String.valueOf(mediationNetworkId);
						}
						mediationData = mediationObj
								.optJSONObject(RESPONSE_MEDIATION_DATA);
						if (mediationData != null) {
							adUnitId = mediationData
									.optString(RESPONSE_MEDIATION_ADID);
						}

						/* Parse impression trackers : for mediation response */
						JSONArray imptracker = mediationObj
								.optJSONArray(RESPONSE_IMPTRACKERS);
						mediationObj.remove(RESPONSE_IMPTRACKERS);
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
						jsTrackerString = mediationObj
								.optString(RESPONSE_JSTRACKER);

						/* Parse click trackers : for mediation response */
						JSONArray clktrackerArray = mediationObj
								.optJSONArray(RESPONSE_CLICKTRACKERS);
						mediationObj.remove(RESPONSE_CLICKTRACKERS);
						for (int i = 0; clktrackerArray != null
								&& i < clktrackerArray.length(); i++) {
							String url = clktrackerArray.optString(i);
							if (clickTrackersStringArray == null) {
								clickTrackersStringArray = new String[clktrackerArray
										.length()];
							}

							if (url != null) {
								clickTrackersStringArray[i] = url;
							}
						}
						/* Parse click trackers Ends */
					}
					// Parse mediation response ends.

					/*
					 * The "link" object is present in case of either
					 * "type=native" OR "type=thirdparty && subtype=native".
					 */
					if ((TextUtils.equals(RESPONSE_NATIVE_STRING, type))
							|| ((TextUtils.equals(RESPONSE_THIRDPARTY_STRING,
									type)) && (TextUtils.equals(
									RESPONSE_NATIVE_STRING, subType)))) {
						/* Get the native object */
						nativeObj = ad.getJSONObject(RESPONSE_NATIVE_STRING);

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
				}

				/**
				 * Valid native ad should contain click url, at least one asset
				 * element from the list (main image, icon image, logo image,
				 * title, description), optionally rating, zero or more
				 * impression and click trackers.
				 */
				// @formatter:off
				if ((TextUtils.equals(RESPONSE_NATIVE_STRING, type) || (TextUtils
						.equals(RESPONSE_THIRDPARTY_STRING, type) && TextUtils
						.equals(RESPONSE_NATIVE_STRING, subType)))
						&& !TextUtils.isEmpty(clickUrl)
						&& nativeAssetList != null
						&& nativeAssetList.size() > 0) {
					nativeAdDescriptor = new NativeAdDescriptor(type,
							nativeVersion, clickUrl, fallbackUrl,
							impressionTrackerStringArray,
							clickTrackersStringArray, jsTrackerString,
							nativeAssetList);

					nativeAdDescriptor.setNativeAdJSON(httpResponse.getResponseData());
				} else if ((TextUtils.equals(RESPONSE_THIRDPARTY_STRING, type) && TextUtils
						.equals(RESPONSE_MEDIATION, subType))
						&& !TextUtils.isEmpty(mediationPartnerName)
						&& (!TextUtils.isEmpty(mediationId) || !TextUtils
								.isEmpty(creativeId))
						&& !TextUtils.isEmpty(adUnitId)
						&& !TextUtils.isEmpty(mediationSource)) {
					nativeAdDescriptor = new NativeAdDescriptor(type,
							creativeId, mediationPartnerName, mediationId,
							adUnitId, mediationSource,
							impressionTrackerStringArray,
							clickTrackersStringArray, jsTrackerString, feedId);
					nativeAdDescriptor.setNativeAdJSON(httpResponse.getResponseData());
				}
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
