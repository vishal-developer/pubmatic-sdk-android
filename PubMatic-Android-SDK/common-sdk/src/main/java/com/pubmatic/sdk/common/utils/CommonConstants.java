package com.pubmatic.sdk.common.utils;

public class CommonConstants {

	public static final String SDK_VERSION = "5.0.0.1";
	
	public enum CONTENT_TYPE{
		JSON,
		XML,
		URL_ENCODED,
		INVALID
	}

	public static enum Method {GET , POST};
	
	//All type of network request
	public static enum AD_REQUEST_TYPE{
		
		PUB_BANNER,
		PUB_NATIVE,
		PUB_INTERSTITIAL,
		PUB_RICH_MEDIA,
		PUB_PRIMARY_VIDEO,
		PUB_WRAPPER_VIDEO,
		PUB_PASSBACK_VIDEO,
		PUB_TRACKER,
		
		MOCEAN_BANNER,
		MOCEAN_NATIVE,
		MOCEAN_INTERSTITIAL,
		MOCEAN_RICH_MEDIA,
		MOCEAN_PRIMARY_VIDEO,
		MOCEAN_WRAPPER_VIDEO,
		MOCEAN_PASSBACK_VIDEO,
		MOCEAN_TRACKER, 
		
		PHOENIX_BANNER,
		PHOENIX_NATIVE,
		PHOENIX_INTERSTITIAL,
		PHOENIX_RICH_MEDIA,
		PHOENIX_PRIMARY_VIDEO,
		PHOENIX_WRAPPER_VIDEO,
		PHOENIX_PASSBACK_VIDEO,
		PHOENIX_TRACKER
	}
	
	public enum CHANNEL {
		NA,
		PUBMATIC,
		MOCEAN,
		PHOENIX
	}

	public static final String ENCODING_UTF_8 = "UTF-8";
	
	//Constants used in XML inflation
	public static final String AD_WIDTH 				= "adWidth";
	public static final String AD_HEIGHT 				= "adHeight";
	
	//Common parameters for all platforms
	public static final String xml_layout_attribute_logLevel = "logLevel";
	public static final String xml_layout_attribute_channel = "channel";

	//Mocean specific common parameters
	public static final String ZONE_ID_PARAM = "zone";

	//Mocean specific banner ad parameters
	

	//PubMatic specific common parameters for GET Parameters
	public static final String PUB_ID_PARAM 			= "pubId";
	public static final String SITE_ID_PARAM 			= "siteId";
	public static final String AD_ID_PARAM 				= "adId";
	public static final String SIZE_X_PARAM 			= "size_x";
	public static final String SIZE_Y_PARAM 			= "size_y";
	public static final String AD_WIDTH_PARAM 			= "kadwidth";
	public static final String AD_HEIGHT_PARAM 			= "kadheight";
	public static final String PAGE_URL_PARAM 			= "pageURL";
	public static final String FRAME_NAME_PARAM 		= "frameName";
	public static final String LTSTAMP_PARAM 			= "kltstamp";
	public static final String RAN_REQ_PARAM 			= "ranreq";
	public static final String TIMEZONE_PARAM 			= "timezone";
	public static final String SCREEN_RESOLUTION_PARAM 	= "screenResolution";
	public static final String IN_IFRAME_PARAM 			= "inIframe";
	public static final String AD_VISIBILITY_PARAM 		= "adVisibility";
	public static final String AD_POSITION_PARAM 		= "adPosition";
	
	
	//PubMatic specific common parameters for POST Parameters
	public static final String DID_PARAM 				= "did=";
	public static final String DPID_PARAM 				= "\ndpid=";
	public static final String LANGUAGE 				= "\nlang=";
	public static final String COUNTRY_PARAM 			= "\ncountry=";
	public static final String CARRIER_PARAM 			= "\ncarrier=";
	public static final String MAKE_PARAM 				= "\nmake=";
	public static final String MODEL_PARAM 				= "\nmodel=";
	public static final String OS_PARAM 				= "\nos=";
	public static final String OSV_PARAM 				= "\nosv=";
	public static final String JS_PARAM 				= "\njs=";
	public static final String LOC_PARAM 				= "\nloc=";
	public static final String VER_PARAM 				= "\nver=";
	public static final String BUNDLE_PARAM 			= "\nbundle=";
	public static final String AD_ORIENTATION_PARAM 	= "\nadOrientation=";
	public static final String DEVICE_ORIENTATION_PARAM = "\ndeviceOrientation=";
	public static final String AD_REFRESH_RATE_PARAM 	= "\nadRefreshRate=";
	public static final String YOB_PARAM 				= "\nyob=";
	public static final String GENDER_PARAM 			= "\ngender=";
	public static final String ZIP_PARAM 				= "\nzip=";
	public static final String KEYWORDS_PARAM 			= "\nkeywords=";
	public static final String AREACODE 				= "\nareaCode=";
	public static final String USERINCOME 				= "\nuserIncome=";
	public static final String USERETHNICITY 			= "\nuserEnthnicity=";
	public static final String SDK_ID_PARAM 			= "\nmsdkId=";
	public static final String SDK_VER_PARAM 			= "\nmsdkVersion=";
	public static final String NETWORK_TYPE_PARAM 		= "\nnettype=";
	

	//PubMatic specific banner parameters
	
	
	public static final String DEFAULTED_EXCREATIVES = "excreatives";
	public static final String DEFAULTED_PUBMATIC_EXFEEDS = "pubmatic_exfeeds";

	public static final String HTTP_REQ_HEADER_USER_AGENT = "User-Agent";
	public static final String HTTP_REQ_HEADER_CONNECTION = "Connection";
	public static final String HTTP_REQ_HEADER_CONNECTION_CLOSE = "close";
    
	// HTTP request body parameters
	public static final String CONTENT_TYPE 			= "Content-Type";
	public static final String CONTENT_LENGTH 			= "Content-Length";
	public static final String CONTENT_MD5 				= "Content-MD5";
	public static final String ACCEPT 					= "Accept";
	public static final String ACCEPT_CHARSET 			= "Accept-Charset";
	public static final String ACCEPT_DATETIME 			= "Accept-Datetime";
	public static final String CACHE_CONTROL 			= "Cache-Control";
	public static final String CONNECTION 				= "Connection";
	public static final String DATE 					= "Date";

	public static final String CONTENT_LANGUAGE 		= "Content-Language";
	public static final String HOST 					= "Host";
	public static final String ACCEPT_LANGUAGE 			= "Accept-Language";
	public static final String USER_AGENT 				= "User-Agent";
	public static final String RLNCLIENT_IP_ADDR 		= "RLNClientIpAddr";
	public static final String HTTPMETHODPOST 			= "POST";
	public static final String HTTPMETHODGET 			= "GET";
	public static final String URL_ENCODING 			= "UTF-8";
	public static final String REQUEST_CONTENT_TYPE 	= "text/plain";
	public static final String REQUEST_CONTENT_LANG_EN 	= "en";


	public static final String MOCEAN_AD_NETWORK_URL 	= "http://ads.moceanads.com/ad";
	public static final String PHOENIX_AD_NETWORK_URL 	= "http://ads.phoenix.com/ad";
	public static final String PUBMATIC_AD_NETWORK_URL 	= "http://showads.pubmatic.com/AdServer/AdServerServlet";
	public static final String PUBMATIC_NATIVE_TEST_NETWORK_URL 	= "http://showads1065.pubmatic.com/AdServer/AdServerServlet";

	
	public static final String PUB_CONFIG_URL 			= "http://172.16.4.36/ads/video/config.json";//172.16.4.65

	public static final int    INVALID_INT 				= -999;
	
	public static final int    AD_TAG_TYPE_VALUE 		= 13;

	public static final int    VAD_FORMAT_VALUE 		= 2; //VAST version i.e. 2
	
	public static final int    OPER_ID_VALUE 			= 102;
	
	public static final int    VMINIMUM_LENGTH_VALUE 	= 0;
	
	public static final int    VMAXIMUM_LENGTH_VALUE 	= 500;

	public static final int    MAX_SOCKET_TIME 			= 5000;
	
	public static final int    AD_NETWORK_243			= 243;

	public static final int NETWORK_TIMEOUT_SECONDS = 5;

	// This is used if the WebView's value returned is empty.
	public static final String USER_AGENT_VALUE = "MASTAdView/" + SDK_VERSION
			+ " (Android)";

    public static final String RESPONSE_IMG = "img";
    public static final String RESPONSE_TITLE = "title";
    public static final String RESPONSE_TEXT = "text";
    public static final String RESPONSE_DATA = "data";
    public static final String RESPONSE_VALUE = "value";


	public static final String TELEPHONY_MCC = "mcc";
	public static final String TELEPHONY_MNC = "mnc";
	public static final String REQUESTPARAM_UA = "ua";
	public static final String REQUESTPARAM_SDK_VERSION = "version";
	public static final String REQUESTPARAM_COUNT = "count";
	public static final String REQUESTPARAM_KEY = "key";
	public static final String REQUESTPARAM_TYPE = "type";
	public static final String REQUESTPARAM_ZONE = "zone";
	public static final String REQUESTPARAM_TEST = "test";
	public static final String REQUESTPARAM_LATITUDE = "lat";
	public static final String REQUESTPARAM_LONGITUDE = "long";
	public static final String REQUEST_HEADER_USER_AGENT = "User-Agent";
	public static final String REQUEST_HEADER_CONNECTION = "Connection";
	public static final String REQUEST_HEADER_CONNECTION_VALUE_CLOSE = "close";
	public static final String REQUEST_HEADER_CONTENT_TYPE = "Content-Type";
	public static final String REQUEST_HEADER_CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded;charset=UTF-8";
	public static final String REQUESTPARAM_ANDROID_ID_SHA1 = "androidid_sha1";

	public static final String REQUEST_NATIVE_EQ_WRAPPER = "native";
	public static final String REQUEST_VER = "ver";
	public static final String REQUEST_VER_VALUE_1 = "1";
	public static final String REQUEST_REQUIRED = "required";
	public static final String REQUEST_LEN = "len";
	public static final String REQUEST_TITLE = "title";
	public static final String REQUEST_TYPE = "type";
	public static final String REQUEST_IMG = "img";
	public static final String REQUEST_DATA = "data";
	public static final String NATIVE_ASSETS_STRING = "assets";
	public static final String NATIVE_IMAGE_W = "w";
	public static final String NATIVE_IMAGE_H = "h";

	public static final String NEWLINE = "\n";
	public static final String QUESTIONMARK = "?";
	public static final String AMPERSAND = "&";
	public static final String EQUAL = "=";

	public static final String RESPONSE_HEADER_CONTENT_TYPE_JSON = "application/json";

	public static final String RESPONSE_ADS = "ads";
	public static final String RESPONSE_ERROR = "error";
	public static final String RESPONSE_ERROR_CODE = "code";
	public static final String RESPONSE_TYPE = "type";
	public static final String RESPONSE_SUBTYPE = "subtype";
	public static final String RESPONSE_CREATIVEID = "creativeid";
	public static final String RESPONSE_FEEDID = "feedid";
	public static final String RESPONSE_MEDIATION = "mediation";
	public static final String RESPONSE_MEDIATION_NAME = "name";
	public static final String RESPONSE_MEDIATION_SOURCE = "source";
	public static final String ID_STRING = "id";
	public static final String RESPONSE_MEDIATION_DATA = "data";
	public static final String RESPONSE_MEDIATION_ADID = "adid";
	public static final String RESPONSE_IMPTRACKERS = "imptrackers";
	public static final String RESPONSE_JSTRACKER = "jstracker";
	public static final String RESPONSE_CLICKTRACKERS = "clicktrackers";
	public static final String RESPONSE_VER = "ver";
	public static final String RESPONSE_NATIVE_STRING = "native";
	public static final String RESPONSE_THIRDPARTY_STRING = "thirdparty";
	public static final String RESPONSE_DIRECT_STRING = "direct";
	public static final String RESPONSE_LINK = "link";
	public static final String RESPONSE_URL = "url";
	public static final String RESPONSE_FALLBACK = "fallback";

	// 10 mins in ms
	public static final int LOCATION_DETECTION_MINTIME = 10 * 60 * 1000;
	public static final int LOCATION_DETECTION_MINDISTANCE = 20; // Meters
}
