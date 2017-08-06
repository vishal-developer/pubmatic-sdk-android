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

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.PMLogger;
import com.pubmatic.sdk.common.phoenix.PhoenixAdRequest;
import com.pubmatic.sdk.common.phoenix.PhoenixConstants;
import com.pubmatic.sdk.nativead.PMNativeAd;
import com.pubmatic.sdk.nativead.bean.PMAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMDataAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMImageAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.pubmatic.sdk.common.CommonConstants.ID_STRING;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_IMAGE_H;
import static com.pubmatic.sdk.common.CommonConstants.NATIVE_IMAGE_W;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_DATA;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_IMG;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_LEN;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_MIMES;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_REQUIRED;
import static com.pubmatic.sdk.common.CommonConstants.REQUEST_TITLE;

/**
 *
 */

public class PhoenixNativeAdRequest extends PhoenixAdRequest {

    private String mNativeTemplateID;

    private List<PMAssetRequest> requestedAssetsList;

    public static PhoenixNativeAdRequest createPhoenixNativeAdRequest(
            Context context, String adUnitId) {
        return createPhoenixNativeAdRequest(context, adUnitId, new ArrayList<PMAssetRequest>(0));
    }

    private PhoenixNativeAdRequest(Context context, int timeout, String adServerUrl, String userAgent, List<PMAssetRequest> requestedAssets) {
        super(context);

        this.requestedAssetsList = requestedAssets;
        this.mNativeTemplateID = "";

        StringBuilder sb = new StringBuilder();
        sb.append(adServerUrl);
        if (sb.indexOf(CommonConstants.QUESTIONMARK) > 0) {
            sb.append(CommonConstants.AMPERSAND);
        } else {
            sb.append(CommonConstants.QUESTIONMARK);
        }
    }

    private PhoenixNativeAdRequest(Context context, int timeout, String adServerUrl, String userAgent, String nativeTemplateID) {
        super(context);

        this.mNativeTemplateID = nativeTemplateID;
        this.requestedAssetsList = null;

        StringBuilder sb = new StringBuilder();
        sb.append(adServerUrl);
        if (sb.indexOf(CommonConstants.QUESTIONMARK) > 0) {
            sb.append(CommonConstants.AMPERSAND);
        } else {
            sb.append(CommonConstants.QUESTIONMARK);
        }
    }

    /**
     * This method will create and object of {@link AdRequest}. It is used for
     * the implementations of {@link PMNativeAd}
     *
     */
    public static PhoenixNativeAdRequest createPhoenixNativeAdRequest(Context context, String adUnitId, List<PMAssetRequest> requestedAssets){

        WebView webView = new WebView(context);
        String userAgent = webView.getSettings().getUserAgentString();

        PhoenixNativeAdRequest adRequest = new PhoenixNativeAdRequest(context, CommonConstants.NETWORK_TIMEOUT_SECONDS,
                CommonConstants.PHOENIX_AD_NETWORK_URL, userAgent, requestedAssets);
        adRequest.setAdUnitId(adUnitId);
        return adRequest;
    }

    /**
     * This method will create and object of {@link AdRequest}. It is used for
     * the implementations of {@link PMNativeAd}
     *
     */
    public static PhoenixNativeAdRequest createPhoenixNativeAdRequest(Context context, String adUnitId, String nativeTemplateID){

        WebView webView = new WebView(context);
        String userAgent = webView.getSettings().getUserAgentString();

        PhoenixNativeAdRequest adRequest = new PhoenixNativeAdRequest(context, CommonConstants.NETWORK_TIMEOUT_SECONDS,
                CommonConstants.PHOENIX_AD_NETWORK_URL, userAgent, nativeTemplateID);
        adRequest.setAdUnitId(adUnitId);
        adRequest.setNativeTemplateID(nativeTemplateID);
        return adRequest;
    }

    @Override
    public String getFormatter() {
        return "com.pubmatic.sdk.nativead.phoenix.PhoenixNativeRRFormatter";
    }

    void createRequest() {
        mPostData		= null;
        initializeDefaultParams();
        setupPostData();
        setUpUrlParams();
    }

    @Override
    protected void setUpUrlParams() {
        super.setUpUrlParams();

        if(!mNativeTemplateID.equals(""))
            addUrlParam(PhoenixConstants.NATIVE_TEMPLATE_ID, mNativeTemplateID);

        if(requestedAssetsList != null && requestedAssetsList.size() > 0)
            addUrlParam(PhoenixConstants.NATIVE_INPUT, getNativeAssetInputData(requestedAssetsList));
    }

    @Override
    public boolean checkMandatoryParams() {
        return !TextUtils.isEmpty(mAdUnitId);
    }

    @Override
    protected void initializeDefaultParams() {
        setRequestType(REQUEST_TYPE.NATIVE);
        setResponseFormat(RESPONSE_TYPE.JSON);
    }

    public String getNativeTemplateID() {
        return mNativeTemplateID;
    }

    public void setNativeTemplateID(String nativeTemplateID) {
        mNativeTemplateID = nativeTemplateID;
    }

    private String getNativeAssetInputData(List<PMAssetRequest> requestedAssets)
    {
        String nativeInput = null;
        JSONObject nativeInputJsonObject = new JSONObject();
        JSONArray nativeInputJsonArray = new JSONArray();
        JSONObject nativeParentJsonObject = new JSONObject();
        JSONObject nativeJsonObject = new JSONObject();
        JSONArray assetsJsonArray = new JSONArray();

        try
        {
            if(requestedAssets != null && requestedAssets.size() > 0)
            {
                for(PMAssetRequest assetRequest : requestedAssets)
                {
                    if(assetRequest != null)
                    {
                        JSONObject assetObj = new JSONObject();
                        assetObj.put(ID_STRING, assetRequest.assetId);
                        assetObj.put(REQUEST_REQUIRED, (assetRequest.isRequired ? 1 : 0));

                        if(assetRequest instanceof PMTitleAssetRequest)
                        {
                            // length is mandatory for title asset
                            if (((PMTitleAssetRequest) assetRequest).length > 0)
                            {
                                JSONObject titleObj = new JSONObject();
                                titleObj.put(REQUEST_LEN, ((PMTitleAssetRequest) assetRequest).length);

                                assetObj.putOpt(REQUEST_TITLE, titleObj);
                            }
                            else
                            {
                                assetObj = null;
                                PMLogger.logEvent("PM-NativeAdRequest: 'length' parameter is mandatory for title asset", PMLogger.LogLevel.Debug);
                            }
                        }
                        else if (assetRequest instanceof PMImageAssetRequest)
                        {
                            JSONObject imageObj = new JSONObject();

                            if (((PMImageAssetRequest) assetRequest).imageType != null)
                            {
                                imageObj.put(CommonConstants.REQUEST_TYPE,((PMImageAssetRequest) assetRequest).imageType.getTypeId());
                            }

                            if (((PMImageAssetRequest) assetRequest).width > 0)
                            {
                                imageObj.put(NATIVE_IMAGE_W, ((PMImageAssetRequest) assetRequest).width);
                            }

                            if (((PMImageAssetRequest) assetRequest).height > 0)
                            {
                                imageObj.put(NATIVE_IMAGE_H, ((PMImageAssetRequest) assetRequest).height);
                            }

                            if (((PMImageAssetRequest) assetRequest).getMimeTypes().size() > 0)
                            {
                                JSONArray mimes = new JSONArray();

                                for(String mime : ((PMImageAssetRequest) assetRequest).getMimeTypes())
                                    mimes.put(mime);

                                imageObj.put(REQUEST_MIMES, mimes);
                            }

                            assetObj.putOpt(REQUEST_IMG, imageObj);
                        }
                        else if (assetRequest instanceof PMDataAssetRequest)
                        {
                            JSONObject dataObj = new JSONObject();

                            if (((PMDataAssetRequest) assetRequest).dataAssetType != null)
                            {
                                dataObj.put(CommonConstants.REQUEST_TYPE, ((PMDataAssetRequest) assetRequest).dataAssetType.getTypeId());

                                if (((PMDataAssetRequest) assetRequest).length > 0)
                                {
                                    dataObj.put(REQUEST_LEN, ((PMDataAssetRequest) assetRequest).length);
                                }

                                assetObj.putOpt(REQUEST_DATA, dataObj);
                            }
                            else
                            {
                                assetObj = null;
                                PMLogger.logEvent("PM-NativeAdRequest: 'type' parameter is mandatory for data asset", PMLogger.LogLevel.Debug);
                            }
                        }

                        if (assetObj != null) {
                            assetsJsonArray.put(assetObj);
                        }
                    }
                }

                nativeJsonObject.put("layout", 1);
                nativeJsonObject.put("assets",assetsJsonArray);

                nativeParentJsonObject.put("native", nativeJsonObject);

                nativeInputJsonArray.put(nativeParentJsonObject);

                nativeInputJsonObject.put("ntI", nativeInputJsonArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            nativeInput = URLEncoder.encode(nativeInputJsonObject.toString(),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return nativeInput.toString();
    }
}
