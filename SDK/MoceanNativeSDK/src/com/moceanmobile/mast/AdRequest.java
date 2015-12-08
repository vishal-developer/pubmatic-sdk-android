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

import static com.moceanmobile.mast.MASTNativeAdConstants.AMPERSAND;
import static com.moceanmobile.mast.MASTNativeAdConstants.EQUAL;
import static com.moceanmobile.mast.MASTNativeAdConstants.ID_STRING;
import static com.moceanmobile.mast.MASTNativeAdConstants.NATIVE_ASSETS_STRING;
import static com.moceanmobile.mast.MASTNativeAdConstants.NATIVE_IMAGE_H;
import static com.moceanmobile.mast.MASTNativeAdConstants.NATIVE_IMAGE_W;
import static com.moceanmobile.mast.MASTNativeAdConstants.QUESTIONMARK;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_DATA;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_HEADER_CONTENT_TYPE;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_HEADER_CONTENT_TYPE_VALUE;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_IMG;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_LEN;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_NATIVE_EQ_WRAPPER;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_REQUIRED;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_TITLE;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_TYPE;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_VER;
import static com.moceanmobile.mast.MASTNativeAdConstants.REQUEST_VER_VALUE_1;
import static com.moceanmobile.mast.MASTNativeAdConstants.RESPONSE_HEADER_CONTENT_TYPE_JSON;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.moceanmobile.mast.bean.AssetRequest;
import com.moceanmobile.mast.bean.DataAssetRequest;
import com.moceanmobile.mast.bean.ImageAssetRequest;
import com.moceanmobile.mast.bean.TitleAssetRequest;

public class AdRequest {

	private final int timeout;
	private final String requestUrl;
	private final String userAgent;
	private Handler handler = null;
	private boolean isNative = false;
	private List<AssetRequest> requestedAssetsList = null;

	/**
	 * This method will create and object of {@link AdRequest}. It is used for
	 * the implementations of {@link MASTNativeAd}
	 * 
	 * @param timeout
	 * @param adServerUrl
	 * @param userAgent
	 * @param parameters
	 * @param handler
	 * @param isNative
	 * @return {@link AdRequest} instance
	 * @throws UnsupportedEncodingException
	 */
	public static AdRequest create(int timeout, String adServerUrl,
			String userAgent, Map<String, String> parameters,
			List<AssetRequest> requestedAssets, Handler handler,
			boolean isNative) throws UnsupportedEncodingException {
		AdRequest adRequest = new AdRequest(timeout, adServerUrl, userAgent,
				parameters, requestedAssets, handler, isNative);

		adRequest.start();
		return adRequest;
	}

	private AdRequest(int timeout, String adServerUrl, String userAgent,
			Map<String, String> parameters, List<AssetRequest> requestedAssets,
			Handler handler, boolean isNative)
			throws UnsupportedEncodingException {
		this.isNative = isNative;
		this.timeout = timeout;
		this.userAgent = userAgent;
		this.handler = handler;
		this.requestedAssetsList = requestedAssets;

		StringBuilder sb = new StringBuilder(128);
		sb.append(adServerUrl);
		if (sb.indexOf(QUESTIONMARK) > 0) {
			sb.append(AMPERSAND);
		} else {
			sb.append(QUESTIONMARK);
		}
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			if (entry != null && !TextUtils.isEmpty(entry.getKey())) {
				sb.append(URLEncoder.encode(entry.getKey(),
						Defaults.ENCODING_UTF_8));
				sb.append(EQUAL);
				if (!TextUtils.isEmpty(entry.getValue())) {
					sb.append(URLEncoder.encode(entry.getValue(),
							Defaults.ENCODING_UTF_8));
				}
				sb.append(AMPERSAND);
			}
		}
		sb.setLength(sb.length() - 1);

		requestUrl = sb.toString();
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void cancel() {
		this.handler = null;
	}

	private void start() {
		RequestProcessor processor = new RequestProcessor();

		Background.getExecutor().execute(processor);
	}

	private class RequestProcessor implements Runnable {
		@Override
		public void run() {
			HttpURLConnection httpURLConnection = null;
			InputStream inputStream = null;
			String contentType = null;
			int responseCode = 0;

			try {
				httpURLConnection = (HttpURLConnection) new URL(requestUrl).openConnection();

				httpURLConnection.setRequestProperty(MASTNativeAdConstants.REQUEST_HEADER_CONNECTION, MASTNativeAdConstants.REQUEST_HEADER_CONNECTION_VALUE_CLOSE);
				httpURLConnection.setRequestProperty(MASTNativeAdConstants.REQUEST_HEADER_USER_AGENT, userAgent);
				httpURLConnection.setConnectTimeout(timeout * 1000);
				
				if (!isNative) {
					httpURLConnection.setRequestMethod(Defaults.HTTP_METHOD_GET);
				} else {

					httpURLConnection.setRequestMethod(Defaults.HTTP_METHOD_POST);
					httpURLConnection.setRequestProperty(REQUEST_HEADER_CONTENT_TYPE, REQUEST_HEADER_CONTENT_TYPE_VALUE);
					
					// Set the length of the data
					String jsonBody = generateNativeAssetRequestJson();
					httpURLConnection
							.setFixedLengthStreamingMode(jsonBody.getBytes().length);

					DataOutputStream dataOutputStream = new DataOutputStream(
							httpURLConnection.getOutputStream());
					if (dataOutputStream != null) {
						// Write the content
						dataOutputStream.writeBytes(jsonBody);
						dataOutputStream.flush();
						dataOutputStream.close();
					}
				}
				responseCode = httpURLConnection.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					if (handler != null) {
						handler.adRequestFailed(AdRequest.this, null);
					}
					return;
				}
				contentType = httpURLConnection.getHeaderField("Content-Type");
				inputStream = httpURLConnection.getInputStream();
				if(inputStream!=null && contentType!=null) {
					
				}
				
				Log.d("AdRequest", "Response Content-Type = " + contentType);

				// If this is the native ad request then parse the native
				// response, else the flow will be same.
				if (contentType != null
						&& contentType
								.contains(RESPONSE_HEADER_CONTENT_TYPE_JSON)) {

					AdDescriptor adDescriptor = AdDescriptor
							.parseNativeResponse(inputStream);
					if (handler != null) {
						// If received the response, send back to the nativeAd
						if (adDescriptor != null) {
							String errorMessage = null;
							if ((errorMessage = adDescriptor.getErrroMessage()) != null) {
								handler.adRequestError(AdRequest.this, null,
										errorMessage);
							} else {
								handler.adRequestCompleted(AdRequest.this,
										adDescriptor);
							}
						} else {
							handler.adRequestFailed(
									AdRequest.this,
									new Exception("Invalid Response Received.."));
						}
					}
					return;
				}

			} catch (Exception ex) {
				if (handler != null) {
					handler.adRequestFailed(AdRequest.this, ex);
				}
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public interface Handler {
		/*
		 * Deprecating these methods as these methods are public and may be
		 * accidentally called by the user causing unexpected behavior.
		 * 
		 * The access modifier will be changed to protected or default in the
		 * future so that the user will not be able to see these methods and
		 * will not be able to invoke these.
		 */
		@Deprecated
		public void adRequestFailed(AdRequest request, Exception exception);

		@Deprecated
		public void adRequestError(AdRequest request, String errorCode,
				String errorMessage);

		@Deprecated
		public void adRequestCompleted(AdRequest request,
				AdDescriptor adDescriptor);
	}

	/**
	 * Generates JSON string for Native request.
	 * 
	 * @return generated JSON string.
	 * @throws JSONException
	 */
	private String generateNativeAssetRequestJson() throws JSONException {
		StringBuffer nativeRequestPostData = new StringBuffer(
				REQUEST_NATIVE_EQ_WRAPPER);
		JSONObject nativeObj = new JSONObject();
		nativeObj.put(REQUEST_VER, REQUEST_VER_VALUE_1);
		JSONArray assetsArray = new JSONArray();
		JSONObject assetObj;
		JSONObject titleObj;
		JSONObject imageObj;
		JSONObject dataObj;
		for (AssetRequest assetRequest : requestedAssetsList) {
			if (assetRequest != null) {
				assetObj = new JSONObject();
				assetObj.put(ID_STRING, assetRequest.assetId);
				assetObj.put(REQUEST_REQUIRED,
						(assetRequest.isRequired ? 1 : 0));
				if (assetRequest instanceof TitleAssetRequest) {
					// length is mandatory for title asset
					if (((TitleAssetRequest) assetRequest).length > 0) {
						titleObj = new JSONObject();
						titleObj.put(REQUEST_LEN,
								((TitleAssetRequest) assetRequest).length);
						assetObj.putOpt(REQUEST_TITLE, titleObj);
					} else {
						assetObj = null;
						Log.w("AdRequest",
								"'length' parameter is mandatory for title asset");
					}
				} else if (assetRequest instanceof ImageAssetRequest) {
					imageObj = new JSONObject();
					if (((ImageAssetRequest) assetRequest).imageType != null) {
						imageObj.put(REQUEST_TYPE,
								((ImageAssetRequest) assetRequest).imageType
										.getTypeId());
					}
					if (((ImageAssetRequest) assetRequest).width > 0) {
						imageObj.put(NATIVE_IMAGE_W,
								((ImageAssetRequest) assetRequest).width);
					}
					if (((ImageAssetRequest) assetRequest).height > 0) {
						imageObj.put(NATIVE_IMAGE_H,
								((ImageAssetRequest) assetRequest).height);
					}
					assetObj.putOpt(REQUEST_IMG, imageObj);
				} else if (assetRequest instanceof DataAssetRequest) {
					dataObj = new JSONObject();
					if (((DataAssetRequest) assetRequest).dataAssetType != null) {
						dataObj.put(REQUEST_TYPE,
								((DataAssetRequest) assetRequest).dataAssetType
										.getTypeId());

						if (((DataAssetRequest) assetRequest).length > 0) {
							dataObj.put(REQUEST_LEN,
									((DataAssetRequest) assetRequest).length);
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

		nativeRequestPostData.append(nativeObj.toString());

		return nativeRequestPostData.toString();
	}
}
