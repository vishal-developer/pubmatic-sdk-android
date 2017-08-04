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
package com.pubmatic.sdk.nativead.phoenix;

import android.text.TextUtils;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.nativead.NativeAdDescriptor;
import com.pubmatic.sdk.nativead.PMNativeAd;
import com.pubmatic.sdk.nativead.bean.PMAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMDataAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMImageAssetResponse;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import static com.pubmatic.sdk.common.CommonConstants.ID_STRING;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_ASSETS_STRING;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_CLICKTRACKERS;
import static com.pubmatic.sdk.common.CommonConstants.RESPONSE_DATA;
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

/**
 *
 */
public class PhoenixNativeRRFormatter implements RRFormatter {

    private AdRequest mRequest;

    @Override
    public HttpRequest formatRequest(AdRequest request) {
        mRequest = request;
        PhoenixNativeAdRequest adRequest = (PhoenixNativeAdRequest) request;
        adRequest.createRequest();

        HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);
        httpRequest.setUserAgent(adRequest.getUserAgent());
        httpRequest.setConnection("close");
        httpRequest.setRequestUrl(request.getRequestUrl());
        httpRequest.setRequestType(CommonConstants.AD_REQUEST_TYPE.PHOENIX_NATIVE);
        httpRequest.setRequestMethod(CommonConstants.HTTPMETHODGET);
        httpRequest.setPostData(adRequest.getPostData());
        return httpRequest;
    }

    @Override
    public AdResponse formatResponse(HttpResponse httpResponse) {

        AdResponse adResponse = new AdResponse();

        adResponse.setRequest(mRequest);

        NativeAdDescriptor nativeAdDescriptor = null;

        try
        {
            if (httpResponse != null && httpResponse.getResponseData()!=null)
            {
                ArrayList<PMAssetResponse> nativeAssetList = new ArrayList<PMAssetResponse>();

                String fallbackUrl = null;
                String feedId = null;
                String type = null;
                String subType = null;
                JSONObject mediationObj = null;
                String mediationPartnerName = null;
                String mediationId = null;
                JSONObject mediationData = null;
                String adUnitId = null;
                String errorMessage = null;
                String mediationSource = null;
                JSONObject ad = null;

                JSONArray bids;
                JSONObject bid;
                String creativeId = null;
                JSONObject creativeObject;
                JSONObject nativeObj = null;
                int nativeVersion = 0;
                String jsTrackerString = null;
                String[] clickTrackersStringArray = null;
                String[] impressionTrackerStringArray = null;
                List<String> impressionTrackerList = new ArrayList<>(0);
                List<String> clickTrackerList = new ArrayList<>(0);
                JSONArray impressionJsonArray;
                JSONArray clickJsonArray;
                JSONObject linkObject;
                String clickUrl = null;

                JSONObject responseObj = new JSONObject(httpResponse.getResponseData());

                bids = responseObj.optJSONArray("bids");

                if(bids != null)
                {
                    bid = bids.optJSONObject(0);

                    if(bid != null)
                    {
                        // Parse Creative Id
                        creativeId = bid.getString("creativeID");

                        // Parse Impression Trackers
                        JSONArray impressionTrackerJsonArray = bid.optJSONArray("tr");

                        if(impressionTrackerJsonArray != null)
                        {
                            for(int i = 0 ; i < impressionTrackerJsonArray.length(); i++)
                            {
                                impressionTrackerList.add(impressionTrackerJsonArray.getString(i));
                            }
                        }
                        else
                        {
                            // No Impression trackers found
                        }

                        // Parse Click Trackers
                        JSONArray clickTrackerJsonArray = bid.optJSONArray("cltr");

                        if(clickTrackerJsonArray != null)
                        {
                            for(int i = 0 ; i < clickTrackerJsonArray.length(); i++)
                            {
                                clickTrackerList.add(clickTrackerJsonArray.getString(i));
                            }
                        }
                        else
                        {
                            // No Click trackers found
                        }

                        // Parse Creative
                        String creativeStringEncoded = bid.optString("ct");

                        if(creativeStringEncoded != null)
                        {
                            final String creativeStringDeEncoded = URLDecoder.decode(creativeStringEncoded, "UTF-8");

                            creativeObject = new JSONObject(creativeStringDeEncoded);

                            // Parse Native Object
                            nativeObj = creativeObject.optJSONObject(RESPONSE_NATIVE_STRING);

                            if(nativeObj != null)
                            {
                                // Parse version
                                nativeVersion = nativeObj.optInt(RESPONSE_VER);

                                // Parse jsTracker
                                jsTrackerString = nativeObj.optString(RESPONSE_JSTRACKER);

                                // Parse impression trackers
                                impressionJsonArray = nativeObj.optJSONArray(RESPONSE_IMPTRACKERS);

                                if(impressionJsonArray != null)
                                {
                                    for(int i = 0 ; i < impressionJsonArray.length(); i++)
                                        impressionTrackerList.add(impressionJsonArray.getString(i));
                                }
                                else
                                {
                                    // Impression trackers not found
                                }

                                // Parse link object
                                linkObject = nativeObj.optJSONObject(RESPONSE_LINK);

                                if(linkObject != null)
                                {
                                    // Parse click url
                                    clickUrl = linkObject.optString(RESPONSE_URL);

                                    // Parse fallback url
                                    fallbackUrl = linkObject.optString(RESPONSE_FALLBACK);

                                    // Parse click trackers
                                    clickJsonArray = linkObject.optJSONArray(RESPONSE_CLICKTRACKERS);

                                    if(clickJsonArray != null)
                                    {
                                        for(int i = 0 ; i < clickJsonArray.length(); i++)
                                            clickTrackerList.add(clickJsonArray.getString(i));
                                    }
                                    else
                                    {
                                        // Click trackers not found
                                    }
                                }

                                // Parse assets
                                JSONArray assets = nativeObj.optJSONArray(NATIVE_ASSETS_STRING);

                                if (assets != null && assets.length() > 0)
                                {
                                    JSONObject asset = null;
                                    int assetId = -1;

                                    for (int i = 0; i < assets.length(); i++)
                                    {
                                        asset = assets.optJSONObject(i);
                                        assetId = asset.optInt(ID_STRING, -1);

                                        if (!asset.isNull(RESPONSE_IMG))
                                        {
                                            JSONObject imageAssetObj = asset.optJSONObject(RESPONSE_IMG);

                                            PMImageAssetResponse imageAsset = new PMImageAssetResponse();
                                            imageAsset.assetId = assetId;
                                            imageAsset.setImage(PMNativeAd.Image.getImage(imageAssetObj));

                                            if (!TextUtils.isEmpty(imageAsset.getImage().url)) {
                                                nativeAssetList.add(imageAsset);
                                            }
                                        }
                                        else if(!asset.isNull(RESPONSE_TITLE))
                                        {
                                            JSONObject titleAssetObj = asset.optJSONObject(RESPONSE_TITLE);

                                            PMTitleAssetResponse titleAsset = new PMTitleAssetResponse();
                                            titleAsset.assetId = assetId;
                                            titleAsset.titleText = titleAssetObj.optString(RESPONSE_TEXT);

                                            if (!TextUtils.isEmpty(titleAsset.titleText)) {
                                                nativeAssetList.add(titleAsset);
                                            }
                                        }
                                        else if(!asset.isNull(RESPONSE_DATA))
                                        {
                                            JSONObject dataAssetObj = asset.optJSONObject(RESPONSE_DATA);

                                            PMDataAssetResponse dataAsset = new PMDataAssetResponse();
                                            dataAsset.assetId = assetId;
                                            dataAsset.value = dataAssetObj.optString(RESPONSE_VALUE);

                                            if (!TextUtils.isEmpty(dataAsset.value)) {
                                                nativeAssetList.add(dataAsset);
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    // No Assets found
                                    adResponse.setErrorCode(CommonConstants.PARSING_ERROR);
                                    adResponse.setErrorMessage("No Assets found");

                                    Exception exception = new Exception("No Assets found");
                                    adResponse.setException(exception);

                                    return  adResponse;
                                }
                            }
                            else
                            {
                                // Native Object not foud
                                adResponse.setErrorCode(CommonConstants.PARSING_ERROR);
                                adResponse.setErrorMessage("No Bids array found");

                                Exception exception = new Exception("No Bids array found");
                                adResponse.setException(exception);

                                return  adResponse;
                            }
                        }
                        else
                        {
                            // Creative not found
                            adResponse.setErrorCode(CommonConstants.PARSING_ERROR);
                            adResponse.setErrorMessage("No Creative found");

                            Exception exception = new Exception("No Creative found");
                            adResponse.setException(exception);

                            return  adResponse;
                        }
                    }
                    else
                    {
                        // No Bid found
                        adResponse.setErrorCode(CommonConstants.PARSING_ERROR);
                        adResponse.setErrorMessage("No Bid found");

                        Exception exception = new Exception("No Bid found");
                        adResponse.setException(exception);

                        return  adResponse;
                    }
                }
                else
                {
                    // No Bids array found
                    adResponse.setErrorCode(CommonConstants.PARSING_ERROR);
                    adResponse.setErrorMessage("No Bids array found");

                    Exception exception = new Exception("No Bids array found");
                    adResponse.setException(exception);

                    return  adResponse;
                }

                if(impressionTrackerList!= null && impressionTrackerList.size() > 0)
                {
                    impressionTrackerStringArray = new String[impressionTrackerList.size()];

                    for(int i = 0 ; i < impressionTrackerList.size(); i++)
                    {
                        impressionTrackerStringArray[i] = impressionTrackerList.get(i);
                    }
                }

                if(clickTrackerList!= null && clickTrackerList.size() > 0)
                {
                    clickTrackersStringArray = new String[clickTrackerList.size()];

                    for(int i = 0 ; i < clickTrackerList.size(); i++)
                    {
                        clickTrackersStringArray[i] = clickTrackerList.get(i);
                    }
                }

                nativeAdDescriptor = new NativeAdDescriptor(
                        nativeVersion, clickUrl, fallbackUrl,
                        impressionTrackerStringArray,
                        clickTrackersStringArray, jsTrackerString,
                        nativeAssetList);

                nativeAdDescriptor.setNativeAdJSON(httpResponse.getResponseData());
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
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

    public AdResponse formatHeaderBiddingResponse(JSONObject response) { return null; }
}
