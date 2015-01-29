/*
 * PubMatic Inc. (PubMatic) CONFIDENTIAL
 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained
 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed 
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes  
 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, 
 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE 
 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.                
 */

package com.moceanmobile.mast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.text.TextUtils;

import com.moceanmobile.mast.MASTNativeAd.Image;

/**
 * This class is used for parsing ad response JSON/XML from Mocean ad server.
 */
public class AdDescriptor {

	// @formatter:off
	/**	Parses the native response. The common native response is as follows:- 
	 * 
	* In case of receiving ad : 
	* {
	*  "ads": [
	*    {
	*      "type": "thirdparty",
	*      "subtype": "native",
	*      "creativeid": 1,
	*      "feedid": 4321,
	*      "link": {
	*        "url": "<ad url>",
	*        "fallback": "<fallback url>",
	*        "clicktrackers": [
	*          "<clicktracker 1>",
	*          "<clicktracker 2>"
	*        ]
	*      },
	*      "assets": [
	*        {
	*          "id": 2,
	*          "type": "img",
	*          "entity": {
	*            "subtype": 3,
	*            "url": "<main image url>",
	*            "w":1200,
	*            "h":627
	*          },
	*          "link": {
	*            "url": "<ad url>",
	*            "fallback": "<fallback url>",
	*            "clicktrackers": [
	*              "<clicktracker 1>"
	*            ]
	*          }
	*        },
	*        {
	*          "id": 1,
	*          "type": "img",
	*          "entity": {
	*            "subtype": 1,
	*            "url": "<icon image url>",
	*            "w":80,
	*            "h":80
	*          },
	*          "link": {
	*            "url": "<ad url>",
	*            "fallback": "<fallback url>",
	*            "clicktrackers": [
	*              "<clicktracker 1>"
	*            ]
	*          }
	*        },
	*        {
	*          "id": 7,
	*          "type": "img",
	*          "entity": {
	*            "subtype": 2,
	*            "url": "<logo image url>",
	*            "w":80,
	*            "h":80
	*          },
	*          "link": {
	*            "url": "<ad url>",
	*            "fallback": "<fallback url>",
	*            "clicktrackers": [
	*              "<clicktracker 1>"
	*            ]
	*          }
	*        },
	*        {
	*          "id": 3,
	*          "type": "title",
	*          "entity": {
	*            "text": "Native Test title"
	*          },
	*          "link": {
	*            "url": "<ad url>"
	*          }
	*        },
	*        {
	*          "id": 4,
	*          "type": "data",
	*          "entity": {
	*            "subtype": 2,
	*            "value": "Native test description"
	*          },
	*          "link": {
	*            "url": "<ad url>"
	*          }
	*        },
	*        {
	*          "id": 6,
	*          "type": "data",
	*          "entity": {
	*            "subtype": 3,
	*            "value": "3.5"
	*          },
	*          "link": {
	*            "url": "<ad url>"
	*          }
	*        },
	*        {
	*          "id": 8,
	*          "type": "video",
	*          "entity": {
	*            "vasttag": "<base64 encoded json vast tag>"
	*          }
	*        }
	*      ],
	*      "impressiontrackers": [
	*        "<impression tracker 1>",
	*        "<impression tracker 2>"
	*      ]
	*    }
	*  ]
	*}
	* 
    *  In case of third party :		
	*{
	*  "ads": [
	*    {
	*      "type": "thirdparty",
	*      "subtype": "mediation",
	*      "creativeid": 551453,
	*      "feedid": 1234,
	*      "mediation": {
	*        "id": <mediation id>,
	*        "name": "<mediation network name>",
	*        "source": "mediation",
	*        "data": {
	*          "adid": "<mediation ad unit it>"
	*        }
	*      },
	*      "impressiontrackers": [
	*        "<impression tracker 1>",
	*        "<impression tracker 2>"
	*      ],
	*      "clicktrackers": [
	*        "<click tracker 1>",
	*        "<click tracker 2>"
	*      ]
	*    }
	*  ]
	*}    
	*
	* @param inputStream Input Stream data to parse
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
				buff.append("\n");
			}

			if (buff != null) {
				String title = null;
				String description = null;
				String clickUrl = null;
				String fallbackUrl = null;
				String callToActionText = null;
				String creativeId = null;
				String feedId = null;
				String type = null;
				String subType = null;
				Image mainImage = null;
				Image logoImage = null;
				Image iconImage = null;
				JSONObject mediationObj = null;
				String mediationPartnerName = null;
				String mediationId = null;
				JSONObject mediationData = null;
				String adUnitId = null;
				String errorMessage = null;
				String mediationSource = null;

				String[] clickTrackersStringArray = null;
				String[] impressionTrackers = null;
				JSONObject ad = null;
				float rating = -1f;
				long downloads = 0;
				String vastTag = null;

				response = buff.toString();

				JSONObject responseObj = new JSONObject(response);
				JSONArray ads = responseObj.getJSONArray("ads");

				if (ads != null && (ad = ads.optJSONObject(0)) != null
						&& ad.has("error")) {
					// Check whether there is an error. If the error format is:
					/* {"ads":[{ "error": "<error message>"}]} */
					// @formatter:on
					errorMessage = ad.optString("error");

					if (!TextUtils.isEmpty(errorMessage)) {
						AdDescriptor adDescriptor = new AdDescriptor();
						adDescriptor.errorMessage = errorMessage;
						return adDescriptor;
					}
				}
				if (ads != null && (ad = ads.optJSONObject(0)) != null) {
					type = ad.optString("type");
					subType = ad.optString("subtype");

					int creativeIdInt = ad.optInt("creativeid", -1);
					if (creativeIdInt != -1) {
						creativeId = String.valueOf(creativeIdInt);
					}

					int feedIdInt = ad.optInt("feedid", -1);
					if (feedIdInt != -1) {
						feedId = String.valueOf(feedIdInt);
					}

					// Parse third-party response
					mediationObj = ad.optJSONObject("mediation");
					if (mediationObj != null) {
						mediationPartnerName = mediationObj.optString("name");
						mediationSource = mediationObj.optString("source");

						// Parse id for third party mediation
						int mediationNetworkId = mediationObj.optInt("id", -1);
						if (mediationNetworkId != -1) {
							mediationId = String.valueOf(mediationNetworkId);
						}
						mediationData = mediationObj.optJSONObject("data");
						if (mediationData != null) {
							adUnitId = mediationData.optString("adid");
						}
					}

					/* Parse impression trackers starts */
					JSONArray imptracker = ad
							.optJSONArray("impressiontrackers");
					ad.remove("impressiontrackers");
					for (int i = 0; imptracker != null
							&& i < imptracker.length(); i++) {
						String url = imptracker.optString(i);
						if (impressionTrackers == null) {
							impressionTrackers = new String[imptracker.length()];
						}

						if (url != null) {
							impressionTrackers[i] = url;
						}
					}
					/* Parse impression trackers Ends */

					/*
					 * The "link" object is present in case of either
					 * "type=native" OR "type=thirdparty && subtype=native". In
					 * all other cases the link object will not be present and
					 * the clicktrackers array will be outside the link object
					 */
					if ((TextUtils.equals("native", type))
							|| ((TextUtils.equals("thirdparty", type)) && (TextUtils
									.equals("native", subType)))) {
						/* Parse link object and contents */
						JSONObject linkObj = ad.optJSONObject("link");
						if (linkObj != null) {
							clickUrl = linkObj.optString("url");
							fallbackUrl = linkObj.optString("fallback");

							/* Parse click trackers */
							JSONArray clktrackerArray = linkObj
									.optJSONArray("clicktrackers");
							linkObj.remove("clicktrackers");
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
					} else {
						/* Parse click trackers */
						JSONArray clktrackerArray = ad
								.optJSONArray("clicktrackers");
						ad.remove("clicktrackers");
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

					// @formatter:off
					// Parse assets.
					JSONArray assets = ad.optJSONArray("assets");
					if (assets != null) {
						for (int i = 0; i < assets.length(); i++) {
							JSONObject asset = assets.optJSONObject(i);

							int assetId = asset.optInt("id", -1);
							@SuppressWarnings(value = { "unused" })
							String assetType = asset.optString("type", "");
							@SuppressWarnings(value = { "unused" })
							int assetSubtype = asset.getJSONObject("entity")
									.optInt("subtype", -1);

							if (assetId == NativeAssetIdTypeMap.TITLE) {
								title = asset.getJSONObject("entity")
										.optString("text", "");
								continue;
							} else if (assetId == NativeAssetIdTypeMap.DATA_DESCRIPTION) {
								description = asset.getJSONObject("entity")
										.optString("value", "");
								continue;
							} else if (assetId == NativeAssetIdTypeMap.DATA_RATING) {
								rating = (float) asset.getJSONObject("entity")
										.optDouble("value", -1f);
								continue;
							} else if (assetId == NativeAssetIdTypeMap.IMAGE_ICON) {
								iconImage = Image.getImage(asset
										.optJSONObject("entity"));
								continue;
							} else if (assetId == NativeAssetIdTypeMap.IMAGE_MAIN) {
								mainImage = Image.getImage(asset
										.optJSONObject("entity"));
								continue;
							} else if (assetId == NativeAssetIdTypeMap.IMAGE_LOGO) {
								logoImage = Image.getImage(asset
										.optJSONObject("entity"));
								continue;
							} 
							else if (assetId == NativeAssetIdTypeMap.VIDEO) {
								vastTag = asset.getJSONObject("entity")
										.optString("vasttag", "");
							}
						}
					}
				}

				/**
				 * Valid native ad should contain click url, at least one asset
				 * element from the list (main image, icon image, logo image, title,
				 * description), optionally  rating, zero or
				 * more impression and click trackers.
				 */
				// @formatter:off
				if ((TextUtils.equals("native", type) || (TextUtils.equals(
						"thirdparty", type) && TextUtils.equals("native",
						subType)))
						&& !TextUtils.isEmpty(clickUrl)
						&& (!TextUtils.isEmpty(description)
								|| !TextUtils.isEmpty(title)
								|| mainImage != null 
								|| iconImage != null 
								|| logoImage != null)) {
					nativeAdDescriptor = new NativeAdDescriptor(type, title,
							description, mainImage, iconImage, logoImage, clickUrl,
							callToActionText, rating, downloads, vastTag,
							impressionTrackers, clickTrackersStringArray, fallbackUrl);

					nativeAdDescriptor.setNativeAdJSON(response);
				} else if ((TextUtils.equals("thirdparty", type) && TextUtils.equals("mediation", subType))
							&& !TextUtils.isEmpty(mediationPartnerName)
							&& (!TextUtils.isEmpty(mediationId) || !TextUtils.isEmpty(creativeId))
							&& !TextUtils.isEmpty(adUnitId)
							&& !TextUtils.isEmpty(mediationSource)) {
					nativeAdDescriptor = new NativeAdDescriptor(type,
							creativeId, mediationPartnerName, mediationId, adUnitId,
							mediationSource, impressionTrackers, clickTrackersStringArray, feedId);
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
				String errorMessage = errorResponse.optString("error");
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
	public static AdDescriptor parseDescriptor(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		Map<String, String> adInfo = new HashMap<String, String>();

		String adType = parser.getAttributeValue(null, "type");
		adInfo.put("type", adType);

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

				parser.next();
				if (parser.getEventType() == XmlPullParser.TEXT) {
					value = parser.getText();
				}

				if (TextUtils.isEmpty(value) == false) {
					adInfo.put(name, value);
				}
			}

			parser.next();
			eventType = parser.getEventType();
		}

		AdDescriptor adDescriptor = new AdDescriptor(adInfo);
		return adDescriptor;
	}

	private final Map<String, String> adInfo;

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

	/**
	 * Returns the error message if an error occurs in case of Native Ad.
	 * 
	 * @return - errorMessage
	 */
	String getErrroMessage() {
		return errorMessage;
	}

	interface NativeAssetIdTypeMap {
		int IMAGE_ICON = 1;
		int IMAGE_MAIN = 2;
		int IMAGE_LOGO = 7;
		int TITLE = 3;
		int DATA_DESCRIPTION = 4;
		int DATA_RATING = 6;
		int VIDEO = 8;
	}
}
