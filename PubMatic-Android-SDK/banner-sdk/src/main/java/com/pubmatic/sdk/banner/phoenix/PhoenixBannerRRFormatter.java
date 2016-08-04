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
import java.net.URLEncoder;
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
	private static final String kid_tag             = "id";
	private static final String krefresh_time_tag   = "at";
	private static final String kprefetch_data_tag  = "pd";
	private static final String kcreative_width_tag = "w";
	private static final String kcreative_height_tag= "h";
	private static final String kcreative_id_tag    = "Creative ID";
	private static final String korder_id_tag       = "Order ID";
	private static final String kline_item_tag      = "LineItem ID";
	private static final String kadunit_id_tag      = "AdUnit ID";
	private static final String kcreative_type_tag  = "crTy";

	@Override
	public HttpRequest formatRequest(AdRequest request) {
		mRequest = request;
		PhoenixBannerAdRequest adRequest = (PhoenixBannerAdRequest) request;
		HttpRequest httpRequest = new HttpRequest(CONTENT_TYPE.URL_ENCODED);

		httpRequest.setUserAgent(adRequest.getUserAgent());
		httpRequest.setConnection("close");
		httpRequest.setRequestUrl(request.getAdServerURL());
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
						String creative= URLDecoder.decode(object.getString(kcreative_tag), PubMaticConstants.URL_ENCODING);
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
						//JSONObject tracker = clickTrackingArray.getJSONObject(i);
						//clickTrackers.add(tracker.getString(String.valueOf(j)));
					}

					// Setting landing_page if not null
					//if (!object.isNull(klanding_page)) {
					//    adInfo.put("url", object.getString(klanding_page));
					//}

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

    @Override
	public AdResponse formatHeaderBiddingResponse(JSONObject response) {
		return null;
	}
}
