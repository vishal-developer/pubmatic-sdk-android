package com.pubmatic.sdk.banner.pubmatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.pubmatic.sdk.banner.BannerAdDescriptor;
import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.AdResponse;
import com.pubmatic.sdk.common.RRFormatter;
import com.pubmatic.sdk.common.network.HttpRequest;
import com.pubmatic.sdk.common.network.HttpResponse;
import com.pubmatic.sdk.common.CommonConstants;
import com.pubmatic.sdk.common.CommonConstants.CONTENT_TYPE;

public class PubMaticBannerRRFormatter implements RRFormatter {

    private final static String kPubMatic_BidTag = "PubMatic_Bid";
    private static final String kecpm = "ecpm";
    private static final String kcreative_tag = "creative_tag";
    private static final String ktracking_url = "tracking_url";
    private static final String klanding_page = "landing_page";
    private static final String kclick_tracking_url = "click_tracking_url";
    private static final String kerror_code = "error_code";
    private static final String kerror_message = "error_string";

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
            JSONObject object = jsonObject.getJSONObject(kPubMatic_BidTag);

            // If there is an error from the server which happens when provided
            // wrong ad parameters, return the error with error code and error
            // message.

            if (!object.isNull(kerror_code) && !(object.getString(kerror_code)
                                                       .equalsIgnoreCase(""))) {

                pubResponse.setErrorCode(object.getString(kerror_code));
                pubResponse.setErrorMessage(object.getString(kerror_message));

                return pubResponse;
            }

            // Check if json contains the creative_tag and tracking_url.
            // If these are missing then the ad is invalid. Return null else
            // return valid adInfo object.
            if (object.isNull(kcreative_tag) == false && !(object.getString(kcreative_tag)
                                                                 .equalsIgnoreCase("")) && object.isNull(
                    ktracking_url) == false && !(object.getString(ktracking_url)
                                                       .equalsIgnoreCase(""))) {

                adInfo.put("content", object.getString(kcreative_tag));
                impressionTrackers.add(object.getString(ktracking_url));
                //adInfo.put("url", "http://searchbusinessanalytics.techtarget.com/definition/big-data-analytics");
                //clickTrackers.add("http://www.google.com");

                // Setting ecpm if not null
                if (!object.isNull(kecpm)) {
                    adInfo.put("ecpm", object.getString(kecpm));
                }
                // Setting click_tracking_url if not null
                if (!object.isNull(kclick_tracking_url)) {
                    clickTrackers.add(object.getString(kclick_tracking_url));
                }
                // Setting landing_page if not null
                if (!object.isNull(klanding_page)) {
                    adInfo.put("url", object.getString(klanding_page));
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
