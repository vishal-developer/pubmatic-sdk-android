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
package com.pubmatic.sdk.banner.pubmatic;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubmatic.sdk.banner.BannerAdDescriptor;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.pubmatic.PUBDeviceInformation;
import com.pubmatic.sdk.common.pubmatic.PubMaticAdRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.name;

public class PubMaticBannerRRFormatter implements RRFormatter {

    private final static String kPubMatic_BidTag = "PubMatic_Bid";
    private static final String kecpm = "ecpm";
    private static final String kcreative_tag = "creative_tag";
    private static final String ktracking_url = "tracking_url";
    private static final String klanding_page = "landing_page";
    private static final String kclick_tracking_url = "click_tracking_url";
    private static final String kerror_code = "error_code";
    private static final String kerror_message = "error_string";
    private static final String UTF8_CHARSET = "UTF-8";

    private AdRequest mRequest;

    @Override
    public HttpRequest formatRequest(AdRequest request) {
        mRequest = request;
        PubMaticBannerAdRequest adRequest = (PubMaticBannerAdRequest) request;
        HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);

        httpRequest.setUserAgent(adRequest.getUserAgent());
        httpRequest.setConnection("close");
        httpRequest.setRequestUrl(request.getAdServerURL());
        httpRequest.setRequestMethod(CommonConstants.HTTPMETHODPOST);
        httpRequest.setRequestType(CommonConstants.AD_REQUEST_TYPE.PUB_BANNER);
        httpRequest.setPostData(adRequest.getPostData());
        //httpRequest.setRLNClientIPAddress(adRequest.getIPAddress());
        return httpRequest;
    }

    @Override
    public AdResponse formatResponse(HttpResponse response) {

        // Check whether the adResponse is null or not. If it is null then
        // it is an invalid ad, so sending the null response.
        if (response == null) {
            return null;
        }

        // Parsing of the response.
        try {
            JSONObject jsonObject = new JSONObject(response.getResponseData());
            JSONObject object = jsonObject.getJSONObject(kPubMatic_BidTag);
            AdResponse adResponse = parseJSONResponse(object);
            adResponse.setRequest(mRequest);
            return adResponse;
        }catch(JSONException je){
            je.printStackTrace();
            return null;
        }
    }

    private AdResponse parseJSONResponse(JSONObject response){

        if(response == null){
            return null;
        }

        AdResponse pubResponse = new AdResponse();

        Map<String, String> adInfo = new HashMap<String, String>();
        ArrayList<String> impressionTrackers = new ArrayList<String>();
        ArrayList<String> clickTrackers = new ArrayList<String>();
        adInfo.put("type", "thirdparty");

        try {
            // If there is an error from the server which happens when provided
            // wrong ad parameters, return the error with error code and error
            // message.
            String errorCode;
            if (!TextUtils.isEmpty(errorCode = response.optString(kerror_code))) {

                pubResponse.setErrorCode(errorCode);
                pubResponse.setErrorMessage(response.getString(kerror_message));
                return pubResponse;
            }

            // Check if json contains the creative_tag and tracking_url.
            // If these are missing then the ad is invalid. Return null else
            // return valid adInfo object.
            if (!TextUtils.isEmpty(response.optString(kcreative_tag))) {

                adInfo.put("content", response.getString(kcreative_tag));
                impressionTrackers.add( URLDecoder.decode(response.optString(ktracking_url), UTF8_CHARSET));

                // Setting ecpm if not null
                if (!response.isNull(kecpm)) {
                    adInfo.put("ecpm", response.getString(kecpm));
                }
                // Setting click_tracking_url if not null
                if (!response.isNull(kclick_tracking_url)) {
                    clickTrackers.add(URLDecoder.decode(response.getString(kclick_tracking_url), UTF8_CHARSET));
                }
                // Setting landing_page if not null
                if (!response.isNull(klanding_page)) {
                    adInfo.put("url", URLDecoder.decode(response.getString(klanding_page), UTF8_CHARSET));
                }

                String width = response.optString("w");
                String height = response.optString("h");
                if (!TextUtils.isEmpty(width)) {
                    adInfo.put("width", width);
                }
                if (!TextUtils.isEmpty(height)) {
                    adInfo.put("height", height);
                }
            }

            BannerAdDescriptor adDescriptor = new BannerAdDescriptor(adInfo);
            adDescriptor.setImpressionTrackers(impressionTrackers);
            adDescriptor.setClickTrackers(clickTrackers);
            pubResponse.setRenderable(adDescriptor);

        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            //response = null;
        }

        return pubResponse;
    }
}
