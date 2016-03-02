/*
 * PubMatic Inc. ("PubMatic") CONFIDENTIAL Unpublished Copyright (c) 2006-2014
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

package com.moceanmobile.mast;

import static com.moceanmobile.mast.MASTNativeAdConstants.ID_STRING;
import static com.moceanmobile.mast.MASTNativeAdConstants.NATIVE_ASSETS_STRING;
import static com.moceanmobile.mast.MASTNativeAdConstants.NEWLINE;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_ADS;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_CLICKTRACKERS;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_CREATIVEID;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_DATA;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_ERROR;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_FALLBACK;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_FEEDID;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_IMG;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_IMPTRACKERS;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_JSTRACKER;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_LINK;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_MEDIATION;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_MEDIATION_ADID;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_MEDIATION_DATA;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_MEDIATION_NAME;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_MEDIATION_SOURCE;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_NATIVE_STRING;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_SUBTYPE;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_TEXT;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_THIRDPARTY_STRING;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_TITLE;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_TYPE;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_URL;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_VALUE;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_VER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;
import android.util.Log;

import com.moceanmobile.mast.MASTNativeAd.Image;
import com.moceanmobile.mast.bean.AssetResponse;
import com.moceanmobile.mast.bean.DataAssetResponse;
import com.moceanmobile.mast.bean.ImageAssetResponse;
import com.moceanmobile.mast.bean.TitleAssetResponse;

/**
 * This class is used for parsing ad response JSON/XML from Mocean ad server.
 */
public class AdDescriptor {

	// @formatter:off
	/**
	 * Parses the native response. The common native response is as follows:-
	 * 
	 * In case of receiving ad : { "type": "thirdparty", "subtype": "native",
	 * "creativeid": 1, "feedid": 1115, "native": { "ver": 1, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 0 \ / 1 " , "fallback": "http:\/\/example.com\/fallback", "
	 * clicktrackers": [
	 * "http:\/\/clicktracker.com\/main\/9bde02d0-6017-11e4-9df7-005056967c35" ]
	 * }, "assets": [ { "id": 2, "img": { "url": "http:\/\/example_320x50.png",
	 * "w": 320, "h": 50 }, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 1 \ / 1 " , "fallback": "http:\/\/example.com\/custom_fallback
	 * ", "clicktrackers ": [
	 * "http:\/\/clicktracker.com\/custom\/9bde02d0-6017-11e4-9df7-005056967c35"
	 * ] } }, { "id": 3, "title": { "text": "Native title" }, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 2 \ / 1 " } }, { "id": 4, "data": { "value": "Native description
	 * " }, "link": { "url":
	 * "http:\/\/ads.moceanads.com\/1\/redir\/9bde02d0-6017-11e4-9df7-005056967c
	 * 3 5 \ / 3 \ / 1 " } }, { "id": 6, "data": { "value": "5" }, "link": { "
	 * url":
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
	 * @param inputStream
	 *            Input Stream data to parse
	 * @return AdDescriptor containing response for NativeAd
	 * 
	 */
	// @formatter:on
	public static AdDescriptor parseNativeResponse(InputStream inputStream) {
		NativeAdDescriptor nativeAdDescriptor = null;

		BufferedReader reader = null;
		String response = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			StringBuffer buff = null;
			while ((line = reader.readLine()) != null) {

				if (buff == null) {
					buff = new StringBuffer();
				}

				buff.append(line);
				buff.append(NEWLINE);
			}

			if (buff != null) {
				ArrayList<AssetResponse> nativeAssetList = new ArrayList<AssetResponse>();
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

				response = buff.toString();
				JSONObject responseObj = new JSONObject(response);
				JSONArray ads = responseObj.getJSONArray(RESPONSE_ADS);

				if (ads != null && (ad = ads.optJSONObject(0)) != null && ad.has(RESPONSE_ERROR)) {
					// Check whether there is an error. If the error format is:
					/* {"ads":[{ "error": "<error message>"}]} */
					// @formatter:on
					errorMessage = ad.optString(RESPONSE_ERROR);

					if (!TextUtils.isEmpty(errorMessage)) {
						AdDescriptor adDescriptor = new AdDescriptor();
						adDescriptor.errorMessage = errorMessage;
						return adDescriptor;
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
						mediationPartnerName = mediationObj.optString(RESPONSE_MEDIATION_NAME);
						mediationSource = mediationObj.optString(RESPONSE_MEDIATION_SOURCE);

						// Parse id for third party mediation
						int mediationNetworkId = mediationObj.optInt(ID_STRING, -1);
						if (mediationNetworkId != -1) {
							mediationId = String.valueOf(mediationNetworkId);
						}
						mediationData = mediationObj.optJSONObject(RESPONSE_MEDIATION_DATA);
						if (mediationData != null) {
							adUnitId = mediationData.optString(RESPONSE_MEDIATION_ADID);
						}

						/* Parse impression trackers : for mediation response */
						JSONArray imptracker = mediationObj.optJSONArray(RESPONSE_IMPTRACKERS);
						mediationObj.remove(RESPONSE_IMPTRACKERS);
						for (int i = 0; imptracker != null && i < imptracker.length(); i++) {
							String url = imptracker.optString(i);
							if (impressionTrackerStringArray == null) {
								impressionTrackerStringArray = new String[imptracker.length()];
							}

							if (url != null) {
								impressionTrackerStringArray[i] = url;
							}
						}
						/* Parse impression trackers Ends */

						// Parse jsTracker
						jsTrackerString = mediationObj.optString(RESPONSE_JSTRACKER);

						/* Parse click trackers : for mediation response */
						JSONArray clktrackerArray = mediationObj.optJSONArray(RESPONSE_CLICKTRACKERS);
						mediationObj.remove(RESPONSE_CLICKTRACKERS);
						for (int i = 0; clktrackerArray != null && i < clktrackerArray.length(); i++) {
							String url = clktrackerArray.optString(i);
							if (clickTrackersStringArray == null) {
								clickTrackersStringArray = new String[clktrackerArray.length()];
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
							|| ((TextUtils.equals(RESPONSE_THIRDPARTY_STRING, type))
									&& (TextUtils.equals(RESPONSE_NATIVE_STRING, subType)))) {
						/* Get the native object */
						nativeObj = ad.getJSONObject(RESPONSE_NATIVE_STRING);

						if (nativeObj != null) {
							nativeVersion = nativeObj.optInt(RESPONSE_VER);

							/* Parse impression trackers starts */
							JSONArray imptracker = nativeObj.optJSONArray(RESPONSE_IMPTRACKERS);
							nativeObj.remove(RESPONSE_IMPTRACKERS);
							for (int i = 0; imptracker != null && i < imptracker.length(); i++) {
								String url = imptracker.optString(i);
								if (impressionTrackerStringArray == null) {
									impressionTrackerStringArray = new String[imptracker.length()];
								}

								if (url != null) {
									impressionTrackerStringArray[i] = url;
								}
							}
							/* Parse impression trackers Ends */

							// Parse jsTracker
							jsTrackerString = nativeObj.optString(RESPONSE_JSTRACKER);

							/* Parse link object and contents */
							JSONObject linkObj = nativeObj.optJSONObject(RESPONSE_LINK);
							if (linkObj != null) {
								clickUrl = linkObj.optString(RESPONSE_URL);
								fallbackUrl = linkObj.optString(RESPONSE_FALLBACK);

								/* Parse click trackers */
								JSONArray clktrackerArray = linkObj.optJSONArray(RESPONSE_CLICKTRACKERS);
								linkObj.remove(RESPONSE_CLICKTRACKERS);
								for (int i = 0; clktrackerArray != null && i < clktrackerArray.length(); i++) {
									String clickTrackUrl = clktrackerArray.optString(i);
									if (clickTrackersStringArray == null) {
										clickTrackersStringArray = new String[clktrackerArray.length()];
									}

									if (clickTrackUrl != null) {
										clickTrackersStringArray[i] = clickTrackUrl;
									}
								}
								/* Parse click trackers Ends */
							}

							// Parse assets.
							JSONArray assets = nativeObj.optJSONArray(NATIVE_ASSETS_STRING);
							if (assets != null && assets.length() > 0) {
								JSONObject asset = null;
								int assetId = -1;
								for (int i = 0; i < assets.length(); i++) {
									asset = assets.optJSONObject(i);
									assetId = asset.optInt(ID_STRING, -1);

									if (!asset.isNull(RESPONSE_IMG)) {
										JSONObject imageAssetObj = asset.optJSONObject(RESPONSE_IMG);
										ImageAssetResponse imageAsset = new ImageAssetResponse();
										imageAsset.assetId = assetId;
										imageAsset.setImage(Image.getImage(imageAssetObj));
										if (!TextUtils.isEmpty(imageAsset.getImage().url)) {
											nativeAssetList.add(imageAsset);
										}
										continue;
									} else if (!asset.isNull(RESPONSE_TITLE)) {
										JSONObject titleAssetObj = asset.optJSONObject(RESPONSE_TITLE);
										TitleAssetResponse titleAsset = new TitleAssetResponse();
										titleAsset.assetId = assetId;
										titleAsset.titleText = titleAssetObj.optString(RESPONSE_TEXT);
										if (!TextUtils.isEmpty(titleAsset.titleText)) {
											nativeAssetList.add(titleAsset);
										}
										continue;
									} else if (!asset.isNull(RESPONSE_DATA)) {
										JSONObject dataAssetObj = asset.optJSONObject(RESPONSE_DATA);
										DataAssetResponse dataAsset = new DataAssetResponse();
										dataAsset.assetId = assetId;
										dataAsset.value = dataAssetObj.optString(RESPONSE_VALUE);
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
				if ((TextUtils.equals(RESPONSE_NATIVE_STRING, type)
						|| (TextUtils.equals(RESPONSE_THIRDPARTY_STRING, type)
								&& TextUtils.equals(RESPONSE_NATIVE_STRING, subType)))
						&& !TextUtils.isEmpty(clickUrl) && nativeAssetList != null && nativeAssetList.size() > 0) {
					nativeAdDescriptor = new NativeAdDescriptor(type, nativeVersion, clickUrl, fallbackUrl,
							impressionTrackerStringArray, clickTrackersStringArray, jsTrackerString, nativeAssetList);

					nativeAdDescriptor.setNativeAdJSON(response);
				} else if ((TextUtils.equals(RESPONSE_THIRDPARTY_STRING, type)
						&& TextUtils.equals(RESPONSE_MEDIATION, subType)) && !TextUtils.isEmpty(mediationPartnerName)
						&& (!TextUtils.isEmpty(mediationId) || !TextUtils.isEmpty(creativeId))
						&& !TextUtils.isEmpty(adUnitId) && !TextUtils.isEmpty(mediationSource)) {
					nativeAdDescriptor = new NativeAdDescriptor(type, creativeId, mediationPartnerName, mediationId,
							adUnitId, mediationSource, impressionTrackerStringArray, clickTrackersStringArray,
							jsTrackerString, feedId);
					nativeAdDescriptor.setNativeAdJSON(response);
				}
				// @formatter:on
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			try {
				// Check whether there is an error. If the error format is
				/* { "error": "No ads available" } */
				JSONObject errorResponse = new JSONObject(response);
				String errorMessage = errorResponse.optString(RESPONSE_ERROR);
				if (!TextUtils.isEmpty(errorMessage)) {
					AdDescriptor adDescriptor = new AdDescriptor();
					adDescriptor.errorMessage = errorMessage;
					return adDescriptor;
				}
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return nativeAdDescriptor;
	}

	/***
	 * Parses an ad descriptor from a pull parser that is parked on the "ad"
	 * start element. Returns the parser on the "ad" end element.
	 * 
	 * If the result is null or an exception is thrown the parser may be parked
	 * nested in the ad tag.
	 * 
	 * @param parser
	 * @return Parsed AdDescriptor or null if an error was encountered.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static AdDescriptor parseDescriptor(XmlPullParser parser) throws XmlPullParserException, IOException {
		Map<String, String> adInfo = new HashMap<String, String>();
		ArrayList<String> impressionTrackers = new ArrayList<String>();
		ArrayList<String> clickTrackers = new ArrayList<String>();
		MediationData mediationData = null;

		String adType = parser.getAttributeValue(null, "type");
		adInfo.put("type", adType);

		String subAdType = parser.getAttributeValue(null, "subtype");
		adInfo.put("subtype", subAdType);

		String width = parser.getAttributeValue(null, "width");
		String height = parser.getAttributeValue(null, "height");
		setImageWidthAndHeight(adInfo, width, height);

		// read past start tag
		parser.next();

		// read and populate ad info
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String name = parser.getName();
			String value = null;

			if ((eventType == XmlPullParser.END_TAG) && ("ad".equals(name))) {
				// done with the ad descriptor
				break;
			} else if (eventType == XmlPullParser.START_TAG) {
				String subType = parser.getAttributeValue(null, "type");
				if (TextUtils.isEmpty(subType) == false) {
					adInfo.put(name + "Type", subType);
				}
				if (name.equalsIgnoreCase("img") && (adInfo.get("width") == null || adInfo.get("height") == null)) {
					String imageWidth = parser.getAttributeValue(null, "width");
					String imageHeight = parser.getAttributeValue(null, "height");
					setImageWidthAndHeight(adInfo, imageWidth, imageHeight);
				}
				parser.next();
				XmlPullParser mParser = parser;
				int newEventType = mParser.getEventType();
				if (name.equals("impressiontrackers")) {
					String impT = mParser.getName();
					if (impT.equalsIgnoreCase("impressiontracker")) {
						while (newEventType != XmlPullParser.END_DOCUMENT) {
							String valueIT = "";
							String newName = mParser.getName();
							if (newEventType == XmlPullParser.END_TAG && "impressiontrackers".equals(newName)) {
								break;
							} else if (eventType == XmlPullParser.START_TAG) {
								if (mParser.getEventType() == XmlPullParser.TEXT) {
									valueIT = mParser.getText();
								}
								if (TextUtils.isEmpty(valueIT) == false) {
									impressionTrackers.add(valueIT);
								}
								mParser.next();
								newEventType = mParser.getEventType();
							}
						}
					}
				} else if (name.equals("clicktrackers")) {
					String clkT = mParser.getName();
					if (("clicktracker").equalsIgnoreCase(clkT)) {
						newEventType = mParser.getEventType();
						while (newEventType != XmlPullParser.END_DOCUMENT) {
							String valueCT = "";
							String newName = parser.getName();
							if (newEventType == XmlPullParser.END_TAG && "clicktrackers".equals(newName)) {
								break;
							} else if (eventType == XmlPullParser.START_TAG) {
								if (mParser.getEventType() == XmlPullParser.TEXT) {
									valueCT = mParser.getText();
								}
								if (TextUtils.isEmpty(valueCT) == false) {
									clickTrackers.add(valueCT);
								}
								mParser.next();
								newEventType = mParser.getEventType();
							}
						}
					}
				} else if (name.equals("mediation")) {
					mediationData = parseMediation(mParser);
				} else {
					if (parser.getEventType() == XmlPullParser.TEXT) {
						value = parser.getText();
					}
					if (TextUtils.isEmpty(value) == false) {
						adInfo.put(name, value);
					}
				}
			}

			parser.next();
			eventType = parser.getEventType();
		}

		AdDescriptor adDescriptor = new AdDescriptor(adInfo);
		adDescriptor.setImpressionTrackers(impressionTrackers);
		adDescriptor.setClickTrackers(clickTrackers);
		// Mediation data will be available only in case of third-party
		// mediation response
		adDescriptor.setMediationData(mediationData);
		return adDescriptor;
	}

	private static void setImageWidthAndHeight(Map<String, String> adInfo, String imageWidth, String imageHeight) {
		if (TextUtils.isEmpty(imageWidth) == false && imageWidth != null) {
			Log.d("Test", "Image in view widht" + imageWidth);
			adInfo.put("width", imageWidth);
		}
		if (TextUtils.isEmpty(imageHeight) == false && imageHeight != null) {
			Log.d("Test", "Image in view height" + imageHeight);
			adInfo.put("height", imageHeight);
		}
	}

	private static MediationData parseMediation(XmlPullParser mParser) throws XmlPullParserException, IOException {

		MediationData mediationData = new MediationData();
		int eventType = mParser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {

			String fieldName = mParser.getName();
			if (mParser.getEventType() == XmlPullParser.START_TAG) {

				String field = mParser.getName();
				if ("id".equals(field)) {
					mediationData.setMediationNetworkId(getXmlValue(mParser));
				} else if ("name".equals(field)) {
					mediationData.setMediationNetworkName(getXmlValue(mParser));
				} else if ("source".equals(field)) {
					mediationData.setMediationSource(getXmlValue(mParser));
				} else if ("adformat".equals(field)) {
					mediationData.setMediationAdFormat(getXmlValue(mParser));
				} else if ("data".equals(field)) {

					while (mParser.next() != XmlPullParser.END_TAG) {
						if (mParser.getEventType() == XmlPullParser.START_TAG) {
							String id = mParser.getName();
							if ("adid".equals(id)) {
								mediationData.setMediationAdId(getXmlValue(mParser));
							}
						} else if (mParser.getEventType() == XmlPullParser.END_TAG && "adid".equals(fieldName)) {
							break;
						}
					}

				} else {
					seekToCurrentEndTag(mParser);
				}
			} else if (mParser.getEventType() == XmlPullParser.END_TAG && "mediation".equals(fieldName)) {
				break;
			}

			mParser.next();
			eventType = mParser.getEventType();
		}
		return mediationData;
	}

	/**
	 * Returns the value of the current XML tag.
	 * 
	 * @param parser
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private static String getXmlValue(XmlPullParser parser) throws IOException, XmlPullParserException {
		if (parser.next() == XmlPullParser.TEXT) {
			String result = parser.getText();
			parser.nextTag();
			return result != null ? result.trim() : null;
		}
		return null;
	}

	/**
	 * Caller should only call this method to point to the end tag of current
	 * level of START_TAG and ignoring the next upcoming xml tags. It throws
	 * IllegalStateException if the current event type is not START_TAG
	 * 
	 * @param parser
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static void seekToCurrentEndTag(XmlPullParser parser) throws XmlPullParserException, IOException {

		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException("Current event of parser is not pointing to XmlPullParser.START_TAG");
		}
		int remainingTag = 1;
		while (remainingTag != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				remainingTag--;
				break;
			case XmlPullParser.START_TAG:
				remainingTag++;
				break;
			}
		}

	}

	private final Map<String, String> adInfo;
	private ArrayList<String> mImpressionTrackers = new ArrayList<String>();
	private ArrayList<String> mClickTrackers = new ArrayList<String>();
	private MediationData mMediationData = null;
	/*
	 * This parameter will be used for Native Ads. It will be set when error
	 * occurs while serving the Native ad and server returns a json with error.
	 */
	private String errorMessage = null;

	AdDescriptor() {
		this.adInfo = null;
	}

	public AdDescriptor(Map<String, String> adInfo) {
		this.adInfo = adInfo;
	}

	public String getType() {
		String value = adInfo.get("type");
		return value;
	}

	public String getWidth() {
		String value = adInfo.get("width");
		return value;
	}

	public String getHeight() {
		String value = adInfo.get("height");
		return value;
	}

	public String getSubType() {
		String value = adInfo.get("subtype");
		return value;
	}

	public String getURL() {
		String value = adInfo.get("url");
		return value;
	}

	public String getTrack() {
		String value = adInfo.get("track");
		return value;
	}

	public String getImage() {
		String value = adInfo.get("img");
		return value;
	}

	public String getImageType() {
		String value = adInfo.get("imgType");
		return value;
	}

	public String getText() {
		String value = adInfo.get("text");
		return value;
	}

	public String getContent() {
		String value = adInfo.get("content");
		return value;
	}

	public String getAdCreativeId() {
		String value = adInfo.get("creativeid");
		return value;
	}

	public ArrayList<String> getImpressionTrackers() {
		return mImpressionTrackers;
	}

	public void setImpressionTrackers(ArrayList<String> mImpressionTrackers) {
		this.mImpressionTrackers.clear();
		this.mImpressionTrackers = mImpressionTrackers;
	}

	/**
	 * Get click trackers list is received from server
	 * 
	 * @return List of click tracker URL's
	 */
	public ArrayList<String> getClickTrackers() {
		return mClickTrackers;
	}

	/**
	 * Set the list of click tracker url's
	 * 
	 * @param clickTrackers
	 */
	public void setClickTrackers(ArrayList<String> clickTrackers) {
		if (this.mClickTrackers != null) {
			this.mClickTrackers.clear();
		}
		this.mClickTrackers = clickTrackers;
	}

	/**
	 * Get the mediation data received in case of third-party mediation response
	 */
	public MediationData getMediationData() {
		return mMediationData;
	}

	/**
	 * Set the mediation data received in case of third-party mediation response
	 */
	public void setMediationData(MediationData mediationData) {
		this.mMediationData = mediationData;
	}

	/**
	 * Returns the error message if an error occurs in case of Native Ad.
	 * 
	 * @return - errorMessage
	 */
	String getErrroMessage() {
		return errorMessage;
	}
}