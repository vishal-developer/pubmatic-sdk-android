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
package com.pubmatic.sdk.common;

@SuppressWarnings("unused")
public class CommonConstants {

	public static final String SDK_VERSION 			= "5.3.3";

	// This is used if the WebView's returned value is empty.
	public static final String DEFAULT_USER_AGENT 	= "PubMaticAdSDK/"
			+ SDK_VERSION + " (Android)";


	public enum CONTENT_TYPE {
		JSON, XML, URL_ENCODED, INVALID
	}

	public enum Method {
		GET, POST
	}

	// All type of network request
	public enum AD_REQUEST_TYPE {


		PUB_BANNER, PUB_NATIVE, PUB_INTERSTITIAL, PUB_RICH_MEDIA,
		PUB_PRIMARY_VIDEO, PUB_WRAPPER_VIDEO, PUB_PASSBACK_VIDEO,

		PUB_TRACKER,

	}

	public enum CHANNEL {
		NA, PUBMATIC
	}

	public static final String ENCODING_UTF_8  						= "UTF-8";

	// Constants used in XML inflation
	public static final String AD_WIDTH  							= "adWidth";
	public static final String AD_HEIGHT  							= "adHeight";

	// Common parameters for all platforms
	public static final String xml_layout_attribute_logLevel 		= "logLevel";
	public static final String xml_layout_attribute_channel 		= "channel";
	public static final String xml_layout_attribute_update_interval = "updateInterval";
	public static final String AMPERSAND 							= "&";
	public static final String EQUAL 								= "=";


	// Common parameters

	public static final String REQUESTPARAM_UA = "ua";
	public static final String REQUESTPARAM_SDK_VERSION = "version";
	public static final String REQUESTPARAM_COUNT = "count";
	public static final String REQUESTPARAM_KEY = "key";
	public static final String REQUESTPARAM_TYPE = "type";
	public static final String REQUESTPARAM_ZONE = "zone";
	public static final String REQUESTPARAM_IP = "ip";
	public static final String REQUESTPARAM_TEST = "test";
	public static final String REQUESTPARAM_LATITUDE = "lat";
	public static final String REQUESTPARAM_LONGITUDE = "long";
	public static final String REQUESTPARAM_AREA = "area";
	public static final String REQUESTPARAM_CITY = "city_name";
	public static final String REQUESTPARAM_ZIP = "zip";
	public static final String REQUESTPARAM_DMA = "dma";
	public static final String REQUESTPARAM_ISO_REGION = "iso_region";
	public static final String REQUESTPARAM_LANGUAGE = "language";
	public static final String REQUEST_HEADER_USER_AGENT = "User-Agent";
	public static final String REQUEST_HEADER_CONNECTION = "Connection";
	public static final String REQUEST_HEADER_CONNECTION_VALUE_CLOSE = "close";
	public static final String REQUEST_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String REQUEST_HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded;charset=UTF-8";
	public static final String REQUESTPARAM_ANDROID_AID = "androidaid";
	public static final String REQUESTPARAM_ANDROID_ID = "androidid";

	public static final String REQUEST_NATIVE_EQ_WRAPPER = "native";
	public static final String REQUEST_VER = "ver";
	public static final String REQUEST_VER_VALUE_1 = "1";
	public static final String REQUEST_REQUIRED = "required";
	public static final String REQUEST_LEN = "len";
	public static final String REQUEST_TITLE = "title";
	public static final String REQUEST_TYPE = "type";
	public static final String REQUEST_IMG = "img";
	public static final String REQUEST_MIMES = "mimes";
	public static final String REQUEST_DATA = "data";
	public static final String NATIVE_ASSETS_STRING = "assets";

	public static final String NEWLINE = "\n";
	public static final String QUESTIONMARK = "?";

	public static final String RESPONSE_HEADER_CONTENT_TYPE_JSON = "application/json";


	public static final String REQUESTPARAM_ANDROID_ID_SHA1 = "androidid_sha1";
	public static final String RESPONSE_NATIVE_STRING 		= "native";
	public static final String RESPONSE_URL 				= "url";
	public static final String NATIVE_IMAGE_W 				= "w";
	public static final String NATIVE_IMAGE_H		 		= "h";

	// Common params
	public static final String GENDER_PARAM 				= "gender";
	public static final String USER_ETHNICITY 				= "enthnicity";
	public static final String SIZE_X_PARAM 				= "size_x";
	public static final String SIZE_Y_PARAM 				= "size_y";

	public static final String HTTP_REQ_HEADER_USER_AGENT 	= "User-Agent";
	public static final String HTTP_REQ_HEADER_CONNECTION 	= "Connection";
	public static final String HTTP_REQ_HEADER_CONNECTION_CLOSE = "close";

	// HTTP request body parameters
	public static final String CONTENT_TYPE 				= "Content-Type";
	public static final String CONTENT_LENGTH  				= "Content-Length";
	public static final String CONTENT_MD5  				= "Content-MD5";
	public static final String ACCEPT  						= "Accept";
	public static final String ACCEPT_CHARSET  				= "Accept-Charset";
	public static final String ACCEPT_DATETIME  			= "Accept-Datetime";
	public static final String CACHE_CONTROL  				= "Cache-Control";
	public static final String CONNECTION  					= "Connection";
	public static final String DATE  						= "Date";

	public static final String CONTENT_LANGUAGE  			= "Content-Language";
	public static final String HOST 						= "Host";
	public static final String ACCEPT_LANGUAGE  			= "Accept-Language";
	public static final String USER_AGENT  					= "User-Agent";
	public static final String RLNCLIENT_IP_ADDR  			= "RLNClientIpAddr";
	public static final String HTTPMETHODPOST  				= "POST";
	public static final String HTTPMETHODGET  				= "GET";
	public static final String URL_ENCODING  				= "UTF-8";
	public static final String REQUEST_CONTENT_TYPE  		= "text/plain";
	public static final String REQUEST_CONTENT_LANG_EN 		= "en";

	public static final String PUBMATIC_AD_NETWORK_URL 		= "http://showads.pubmatic.com/AdServer/AdServerServlet";

	public static final int INVALID_INT 			= -999;
	public static final int AD_TAG_TYPE_VALUE 		= 13;
	public static final int MAX_SOCKET_TIME 		= 5000;
	public static final int MAX_TRACKER_TIMEOUT 	= 10000;
	public static final int NETWORK_TIMEOUT_SECONDS = 5;

	public static final String RESPONSE_IMG 		= "img";
	public static final String RESPONSE_TITLE 		= "title";
	public static final String RESPONSE_TEXT 		= "text";
	public static final String RESPONSE_DATA 		= "data";
	public static final String RESPONSE_VALUE 		= "value";
	public static final String RESPONSE_CLICKTRACKERS = "clicktrackers";
	public static final String RESPONSE_IMPTRACKERS = "imptrackers";
	public static final String ID_STRING 			= "id";
	public static final String RESPONSE_VER 		= "ver";
	public static final String RESPONSE_LINK 		= "link";
	public static final String RESPONSE_FALLBACK 	= "fallback";
	public static final String RESPONSE_JSTRACKER 	= "jstracker";
	public static final String RESPONSE_ERROR 		= "error";



	// 10 mins in ms
	public static final int LOCATION_DETECTION_MINTIME 		= 10 * 60 * 1000;
	public static final int LOCATION_DETECTION_MINDISTANCE 	= 20; // Meters

	/**
	 * Enum to define the supported Mediation network.
	 */
	public enum MediationNetwork {
		FACEBOOK_AUDIENCE_NETWORK, MOPUB
	}
}
