package com.pubmatic.sdk.nativead.mocean;

import static com.pubmatic.sdk.common.utils.CommonConstants.ID_STRING;
import static com.pubmatic.sdk.common.utils.CommonConstants.NATIVE_ASSETS_STRING;
import static com.pubmatic.sdk.common.utils.CommonConstants.NATIVE_IMAGE_H;
import static com.pubmatic.sdk.common.utils.CommonConstants.NATIVE_IMAGE_W;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_DATA;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_IMG;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_LEN;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_NATIVE_EQ_WRAPPER;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_REQUIRED;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_TITLE;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_TYPE;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_VER;
import static com.pubmatic.sdk.common.utils.CommonConstants.REQUEST_VER_VALUE_1;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import com.pubmatic.sdk.common.AdRequest;
import com.pubmatic.sdk.common.utils.CommonConstants;
import com.pubmatic.sdk.common.utils.CommonConstants.CHANNEL;
import com.pubmatic.sdk.nativead.NativeAdRequest;
import com.pubmatic.sdk.nativead.NativeConstants;
import com.pubmatic.sdk.nativead.PMNativeAd;
import com.pubmatic.sdk.nativead.bean.PMAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMDataAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMImageAssetRequest;
import com.pubmatic.sdk.nativead.bean.PMTitleAssetRequest;

public class MoceanNativeAdRequest extends NativeAdRequest {

	private List<PMAssetRequest> requestedAssetsList = null;
	
	private String zoneId;
	private boolean test = false;
	private final String requestUrl;

	public static MoceanNativeAdRequest createMoceanNativeAdRequest(
			Context context, String zone) {
		return createMoceanNativeAdRequest(context, zone, null);
	}
	/**
	 * This method will create and object of {@link AdRequest}. It is used for
	 * the implementations of {@link PMNativeAd}
	 * 
	 */
	public static MoceanNativeAdRequest createMoceanNativeAdRequest(Context context, String zone, List<PMAssetRequest> requestedAssets){

		WebView webView = new WebView(context);
		String userAgent = webView.getSettings().getUserAgentString();

		MoceanNativeAdRequest adRequest = new MoceanNativeAdRequest(NativeConstants.NETWORK_TIMEOUT_SECONDS,
				CommonConstants.MOCEAN_AD_NETWORK_URL, userAgent, null, requestedAssets);
		adRequest.setMoceanZoneId(zone);
		return adRequest;
	}

	private MoceanNativeAdRequest() {
		super(CHANNEL.MOCEAN);
		this.requestUrl = null;
	}
	
	private MoceanNativeAdRequest(int timeout, String adServerUrl, String userAgent,
			Map<String, String> parameters, List<PMAssetRequest> requestedAssets) {
		super(CHANNEL.MOCEAN);
		this.requestedAssetsList = requestedAssets;

		StringBuilder sb = new StringBuilder();
		sb.append(adServerUrl);
		if (sb.indexOf(CommonConstants.QUESTIONMARK) > 0) {
			sb.append(CommonConstants.AMPERSAND);
		} else {
			sb.append(CommonConstants.QUESTIONMARK);
		}
		if(parameters!=null) {
			try {
				for (Map.Entry<String, String> entry : parameters.entrySet()) {
					if (entry != null && !TextUtils.isEmpty(entry.getKey())) {
						sb.append(URLEncoder.encode(entry.getKey(),
								CommonConstants.ENCODING_UTF_8));
						sb.append(CommonConstants.EQUAL);
						if (!TextUtils.isEmpty(entry.getValue())) {
							sb.append(URLEncoder.encode(entry.getValue(),
									CommonConstants.ENCODING_UTF_8));
						}
						sb.append(CommonConstants.AMPERSAND);
					}
				}
			} catch(UnsupportedEncodingException e) {
				
			}
		}
		sb.setLength(sb.length() - 1);

		requestUrl = sb.toString();
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	/**
	 * Returns the base/host name URL
	 * @return
	 */
	public String getAdServerURL()
	{
		return TextUtils.isEmpty(mBaseUrl) ? CommonConstants.MOCEAN_AD_NETWORK_URL : mBaseUrl;
	}

	/**
	 * This method will initialize all the static parameters which SDK need to set.
	 * @param context
	 */
	protected void initializeDefaultParams(Context context) {

		putPostData("count", "1");
		putPostData("key", "8");
		putPostData("type", "8");
		putPostData("version", CommonConstants.SDK_VERSION);
		if (this.test) {
			putPostData("test", "1");
		}
		// Network related params reqd by Mocean
		try {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			String networkOperator = tm.getNetworkOperator();

			if ((networkOperator != null) && (networkOperator.length() > 3)) {
				String mcc = networkOperator.substring(0, 3);
				String mnc = networkOperator.substring(3);
				putPostData("mcc", String.valueOf(mcc));
				putPostData("mnc", String.valueOf(mnc));
			}
		} catch (Exception ex) {
			System.out.println("Unable to obtain mcc and mnc. Exception:" + ex);
		}
	}

	public String getMoceanZoneId() {
		return zoneId;
	}

	public void setMoceanZoneId(String zone) {
		this.zoneId = zone;
	}

	public void setWidth(int width) {
		super.setWidth(width);
	}

	public void setHeight(int height) {
		super.setHeight(height);
	}

	public void setUserAgent(String userAgent) {
		super.setUserAgent(userAgent);
	}

	@Override
	public boolean checkMandatoryParams() {
		return !TextUtils.isEmpty(zoneId);
	}

	@Override
	public void setCustomParams(Map<String, List<String>> customParams) {
		mCustomParams = customParams;
	}

	/**
	 * Sets the instance test mode. If set to test mode the instance will
	 * request test ads for the configured zone.
	 * <p>
	 * Warning: This should never be enabled for application releases.
	 * 
	 * @param test
	 *            true to set test mode, false to disable test mode.
	 */
	public void setTest(boolean test) {
		this.test = test;
	}

	/**
	 * Access for test mode state of the instance.
	 * 
	 * @return true if the instance is set to test mode, false if test mode is
	 *         disabled.
	 */
	public boolean isTest() {
		return test;
	}


	@Override
	protected void setupPostData() {

		putPostData(CommonConstants.ZONE_ID_PARAM, String.valueOf(this.zoneId));
		if(getWidth()>0)
			putPostData(CommonConstants.SIZE_X_PARAM, String.valueOf(getWidth()));
		if(getHeight()>0)
			putPostData(CommonConstants.SIZE_Y_PARAM, String.valueOf(getHeight()));

		//attach the Native asset request data
		setupAssetData();
	}
	
	/**
	 * 
	 */
	public void copyRequestParams(NativeAdRequest adRequest) {
		if(adRequest!=null && adRequest instanceof MoceanNativeAdRequest && 
				(zoneId==null || TextUtils.isEmpty(zoneId))) {
			this.zoneId = ((MoceanNativeAdRequest)adRequest).zoneId;
		}
	}

	@Override
	public String getFormatter() {
		return "com.pubmatic.sdk.nativead.mocean.MoceanNativeRRFormatter";
	}
	
	@Override
	public void setAttributes(AttributeSet attr) {
		try{
			zoneId = attr.getAttributeValue(null,
					CommonConstants.ZONE_ID_PARAM);
		} catch(Exception ex) {
			
		}
	}
	private void setupAssetData() {
		try {
			JSONObject nativeObj = new JSONObject();
			nativeObj.put(REQUEST_VER, REQUEST_VER_VALUE_1);
			JSONArray assetsArray = new JSONArray();
			JSONObject assetObj;
			JSONObject titleObj;
			JSONObject imageObj;
			JSONObject dataObj;
			for (PMAssetRequest assetRequest : requestedAssetsList) {
				if (assetRequest != null) {
					assetObj = new JSONObject();
					assetObj.put(ID_STRING, assetRequest.assetId);
					assetObj.put(REQUEST_REQUIRED,
							(assetRequest.isRequired ? 1 : 0));
					if (assetRequest instanceof PMTitleAssetRequest) {
						// length is mandatory for title asset
						if (((PMTitleAssetRequest) assetRequest).length > 0) {
							titleObj = new JSONObject();
							titleObj.put(REQUEST_LEN,
									((PMTitleAssetRequest) assetRequest).length);
							assetObj.putOpt(REQUEST_TITLE, titleObj);
						} else {
							assetObj = null;
							Log.w("AdRequest",
									"'length' parameter is mandatory for title asset");
						}
					} else if (assetRequest instanceof PMImageAssetRequest) {
						imageObj = new JSONObject();
						if (((PMImageAssetRequest) assetRequest).imageType != null) {
							imageObj.put(REQUEST_TYPE,
									((PMImageAssetRequest) assetRequest).imageType
											.getTypeId());
						}
						if (((PMImageAssetRequest) assetRequest).width > 0) {
							imageObj.put(NATIVE_IMAGE_W,
									((PMImageAssetRequest) assetRequest).width);
						}
						if (((PMImageAssetRequest) assetRequest).height > 0) {
							imageObj.put(NATIVE_IMAGE_H,
									((PMImageAssetRequest) assetRequest).height);
						}
						assetObj.putOpt(REQUEST_IMG, imageObj);
					} else if (assetRequest instanceof PMDataAssetRequest) {
						dataObj = new JSONObject();
						if (((PMDataAssetRequest) assetRequest).dataAssetType != null) {
							dataObj.put(REQUEST_TYPE,
									((PMDataAssetRequest) assetRequest).dataAssetType
											.getTypeId());
	
							if (((PMDataAssetRequest) assetRequest).length > 0) {
								dataObj.put(REQUEST_LEN,
										((PMDataAssetRequest) assetRequest).length);
							}
							assetObj.putOpt(REQUEST_DATA, dataObj);
						} else {
							assetObj = null;
							Log.w("AdRequest",
									"'type' parameter is mandatory for data asset");
						}
					}
					if (assetObj != null) {
						assetsArray.put(assetObj);
					}
				}
			}
			nativeObj.putOpt(NATIVE_ASSETS_STRING, assetsArray);

			putPostData(REQUEST_NATIVE_EQ_WRAPPER, nativeObj.toString());
		} catch(JSONException e) {
		}
	}
}
