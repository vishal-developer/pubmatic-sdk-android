/*

 * PubMatic Inc. ("PubMatic") CONFIDENTIAL

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
package com.pubmatic.sdk.common.network;

public interface ProtocolConstants {
	
	// HTTP request body parameters
	public static String OPER_ID_VALUE 		= "201";

	public static String CONTENT_LANGUAGE 	= "Content-Language";
	public static String HOST 				= "Host";
	public static String ACCEPT_LANGUAGE 	= "Accept-Language";
	public static String USER_AGENT 		= "User-Agent";
	public static String RLNCLIENT_IP_ADDR 	= "RLNClientIpAddr";
	public static String HTTPMETHODPOST 	= "POST";
	public static String HTTPMETHODGET 		= "GET";
	public static String URL_ENCODING 		= "UTF-8";
	public static String REQUEST_CONTENT_TYPE = "text/plain";
	public static String REQUEST_CONTENT_LANG_EN = "en";
	public static String AMPERSAND 			= "&";

	// PUBRequestFormatterConstants
	// Request parameters
	public static String OPER_ID            = "operId=";
	public static String PUB_ID_PARAM 		= "pubId=";
	public static String SITE_ID_PARAM 		= "siteId=";
	public static String AD_ID_PARAM 		= "adId=";
	public static String AD_WIDTH_PARAM 	= "kadwidth=";
	public static String AD_HEIGHT_PARAM 	= "kadheight=";
	public static String PAGE_URL_PARAM 	= "pageURL=";
	public static String FRAME_NAME_PARAM 	= "frameName=";
	public static String LTSTAMP_PARAM 		= "kltstamp=";
	public static String RAN_REQ_PARAM 		= "ranreq=";
	public static String TIMEZONE_PARAM 	= "timezone=";
	public static String SCREEN_RESOLUTION_PARAM = "screenResolution=";
	public static String IN_IFRAME_PARAM 	= "inIframe=";
	public static String AD_VISIBILITY_PARAM= "adVisibility=";
	public static String AD_POSITION_PARAM 	= "adPosition=";
	public static String UDID_PARAM 		= "udid=";
	public static String UDID_HASH_PARAM 	= "udidhash=";
	public static String UDID_TYPE_PARAM 	= "udidtype=";
	public static String LANGUAGE 			= "lang=";
	public static String COUNTRY_PARAM 		= "country=";
	public static String STATE_PARAM 		= "state=";
	public static String CITY_PARAM 		= "city=";
	public static String DESIGNATED_MARKET_AREA_PARAM = "dma=";
	public static String LOC_PARAM 			= "loc=";
	public static String LOC_SOURCE_PARAM 	= "loc_source=";
	public static String VER_PARAM 			= "ver=";
	public static String JS_PARAM 			= "js=";
	public static String API_PARAM 			= "api=";
	public static String NET_TYPE_PARAM 	= "nettype=";
	public static String CARRIER_PARAM 		= "carrier=";
	public static String APPLICATION_NAME_PARAM = "name=";
	public static String BUNDLE_PARAM 		= "bundle=";
	public static String AD_ORIENTATION_PARAM = "adOrientation=";
	public static String DEVICE_ORIENTATION_PARAM = "deviceOrientation=";
	public static String AD_REFRESH_RATE_PARAM= "adRefreshRate=";
	public static String YOB_PARAM 			= "yob=";
	public static String GENDER_PARAM 		= "gender=";
	public static String ZIP_PARAM 			= "zip=";
	public static String KEYWORDS_PARAM 	= "keywords=";
	public static String AREACODE 			= "areaCode=";
	public static String USERINCOME 		= "inc=";
	public static String USERETHNICITY 		= "ethn=";
	public static String SDK_ID_PARAM 		= "msdkId=";
	public static String SDK_VER_PARAM 		= "msdkVersion=";
	public static String CUSTOM_PARAM 		= "pmcust=";
	// For default ad network
	public static String AD_NETWORK_PARAM 	= "kadNetwork=";
	// For interstitial
	public static String INTERSTITIAL_PARAM = "interstitial=";
	
	
	public static String AD_TAG_TYPE 		= "adtype=";//** "adtype";
	
	public static String DNT_KEY 			= "dnt=";//** "dnt";

	public static String COPPA_KEY 			= "coppa=";//** "coppa";
	
	public static String VIDEO_WIDTH_KEY 	= "kadwidth=";//Width (in pixels) of the video ad.
	
	public static String VIDEO_HEIGHT_KEY 	= "kadheight=";//Height (in pixels) of the video ad.

	public static String VIDEO_TYPE 		= "vtype=";
	
	public static String VIDEO_POSITION_KEY = "vpos=";//Position of the video in the content.0 - Any, 1 - Pre, 2 - Mid, 3 - Post
	
	public static String VIDEO_MINIMUM_LENGTH = "vminl=";
	
	public static String VIDEO_MAXIMUM_LENGTH = "vmaxl=";//** : Double start indicates Mandatory fields
	
	public static String COMPANION_REQUESTED_KEY = "vcom=";//Indicates whether a companion ad is requested or not. 0 means false.
	
	public static String MIN_BIT_RATE_KEY 	= "vminbtr=";//Minimum bitrate (in kbps) allowed for the video stream.
	
	public static String MAX_BIT_RATE_KEY 	= "vmaxbtr=";//Maximum bitrate (in kbps) allowed for the video stream.
	
	public static String VIDEO_STREAM_FORMAT_KEY = "vfmt=";//Acceptable video streaming formats. Defalt: 0 means ANY.
	
	public static String COMPANION_WIDTH_KEY = "vcomw=";//Width (in pixels) of the companion ad. Default 300.
	
	public static String COMPANION_HEIGHT_KEY = "vcomh=";//Height (in pixels) of the companion ad. Default 250.
	
	public static String VIDEO_AD_FORMAT 	= "vadFmt=";
	
	public static String VPAID_VERSION_KEY = "vapi=";//VPAID version
	
	public static String VIDEO_CONTEXTUAL_INFO_KEY = "vcont=";//Contextual information 
	
	public static String VIDEO_PLAYABLE_KEY = "vplay=";//Check feasibility for Mobile platform.

	public static String VIDEO_SKIPABLE 	= "vskip=";//Is video ad skippable

	public static String VIDEO_SKIP_DELAY 	= "vskipdelay=";//Skip delay

	public static String VIDEO_NO_SKIP_AD_LEN = "vnoskipadlen=";//No skip ad length 
	
	public static String MAKE_PARAM 		= "make=";
	public static String MODEL_PARAM 		= "model=";
	public static String OS_PARAM 			= "os=";
	public static String OS_VERSION_PARAM 	= "osv=";

	// PUBError error messages
	// If no ad to fill
	public static String PUB_ERROR_MESSAGE_NO_AD_FOUND 	= "ERROR: No Ad Found";
	// If request is timed out
	public static String PUB_ERROR_MESSAGE_TIMEOUT 		= "ERROR: Request timed out";
	// If url is incorrect
	public static String PUB_ERROR_MESSAGE_INVALID_AD 	= "ERROR: Ad url is incorrect, please check the url";
	// If there is no network
	public static String PUB_ERROR_MESSAGE_NO_NETWORK 	= "ERROR: The Internet connection appears to be offline.";
	
	
	//----------------------- Mocean API parameters Starts --------------------------
	public static String M_ZONE 		= "zone=";
	public static String M_IP 			= "ip=";
	public static String M_USER_AGENT 	= "ua=";
	public static String M_URL			= "url=";
	public static String M_COUNT		= "count=";
	public static String M_TEST			= "test=";
	public static String M_TIMEOUT		= "timeout=";
	public static String M_IMAGE		= "image=";
	public static String M_OVER18		= "Over_18=";
	public static String M_AGE			= "age=";
	public static String M_BIRTHDAY		= "birthday=";
	public static String M_GENDER		= "gender=";
	public static String M_ETHENTICITY	= "ethnicity=";
	public static String M_LANGUAGE		= "language=";
	public static String M_KEYWORDS		= "keywords=";
	public static String M_CARRIER		= "carrier=";
	public static String M_ISP			= "isp=";
	public static String M_MNC			= "mnc=";
	public static String M_TRACK		= "track=";
	public static String M_NO_EXTERNAL	= "no_external=";
	public static String M_IMAGE_STYLE	= "imgstyle=";		//Not added in parameters
	public static String M_ENCODING		= "encoding=";
	public static String M_TARGET		= "target=";		//Not added in parameters
	public static String M_KEY			= "key=";
	public static String M_TYPE			= "type=";
	public static String M_ADS_TYPE		= "adstype=";
	public static String M_JS_VARIABLE	= "jsvar=";			//Not added in parameters
	public static String M_CALLBACK		= "callback=";
	public static String M_UDID			= "udid=";
	public static String M_ANDROIDAID	= "androidaid=";
	public static String M_ANDROIDID	= "androidid=";
	public static String M_IDFA			= "idfa=";
	public static String M_IDFV			= "idfv=";
	public static String M_IDTE			= "idte=";
	public static String M_MACADDRESS	= "macaddress=";
	public static String M_ODIN1		= "odin1=";
	public static String M_SECURE_UDID	= "secureudid=";
	public static String M_OPEN_UDID	= "openudid=";
	public static String M_WUID			= "wuid=";
	public static String M_WAID			= "waid=";
	public static String M_AD_TRUTH		= "adtruth=";
	public static String M_BKID			= "bkid=";
	public static String M_DRAW_BRIDGE	= "drawbridge=";
	public static String M_CREATIVES	= "creatives=";
	public static String M_EXCREATIVES	= "excreatives=";
	public static String M_LINE_ITEMS	= "lineitems=";
	public static String M_ORDER		= "order=";
	public static String M_LATITUDE		= "lat=";
	public static String M_LONGITUDE	= "long=";
	public static String M_COUNTRY		= "country=";
	public static String M_MCC			= "mcc=";
	public static String M_REGION		= "region=";
	public static String M_CITY			= "city=";
	public static String M_AREA			= "area=";
	public static String M_DMA			= "dma=";
	public static String M_ZIP			= "zip=";
	public static String M_EXLINE_ITEMS	= "exlineitems=";
	public static String M_PUBMATIC_EXFEEDS = "pubmatic_exfeeds=";
	public static String M_VIDEO_PROTOCOL= "video_protocol=";
	//----------------------- Mocean API parameters Ends ----------------------------
}