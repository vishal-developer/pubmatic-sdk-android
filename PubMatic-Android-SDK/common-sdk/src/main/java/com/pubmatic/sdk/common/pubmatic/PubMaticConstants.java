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
package com.pubmatic.sdk.common.pubmatic;

public class PubMaticConstants {

    public static final String HASHING_RAW = "1";
    public static final String HASHING_SHA1= "2";
    public static final String HASHING_MD5 = "3";

    public static final String ADVERTISEMENT_ID = "9";
    public static final String ANDROID_ID = "3";

    public static final String LOCATION_SOURCE_UNKNOWN = "0";
    public static final String LOCATION_SOURCE_GPS_LOCATION_SERVICES = "1";
    public static final String LOCATION_SOURCE_IP_ADDRESS = "2";
    public static final String LOCATION_SOURCE_USER_PROVIDED = "3";

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

    //PubMatic specific common parameters for GET Parameters
    public static final String PUB_ID_PARAM 			= "pubId";
    public static final String SITE_ID_PARAM 			= "siteId";
    public static final String AD_ID_PARAM 				= "adId";
    public static final String OPER_ID_PARAM 			= "operId";
    public static final String SIZE_X_PARAM 			= "size_x";
    public static final String SIZE_Y_PARAM 			= "size_y";
    public static final String AD_WIDTH_PARAM 			= "kadwidth";
    public static final String AD_HEIGHT_PARAM 			= "kadheight";
    public static final String PAGE_URL_PARAM 			= "pageURL";
    public static final String AD_TYPE_PARAM 			= "adtype";
    public static final String FRAME_NAME_PARAM 		= "frameName";
    public static final String DNT_PARAM 				= "dnt";
    public static final String COPPA_PARAM 				= "coppa";
    public static final String AWT_PARAM 				= "awt";
    public static final String APP_NAME_PARAM 			= "name";
    public static final String APP_VERSION_PARAM 		= "ver";
    public static final String APP_ID_PARAM 			= "aid";
    public static final String APP_CATEGORY_PARAM 		= "cat";
    public static final String APP_DOMAIN_PARAM 		= "appdomain";
    public static final String APP_BUNDLE_PARAM 		= "bundle";
    public static final String STORE_URL_PARAM 			= "storeurl";
    public static final String AD_POSITION_PARAM 		= "adPosition";
    public static final String PAID_PARAM 				= "paid";
    public static final String ORMMA_COMPLAINCE_PARAM 	= "ormma";
    public static final String LTSTAMP_PARAM 			= "kltstamp";
    public static final String RAN_REQ_PARAM 			= "ranreq";
    public static final String TIMEZONE_PARAM 			= "timezone";
    public static final String SCREEN_RESOLUTION_PARAM 	= "screenResolution";
    public static final String IN_IFRAME_PARAM 			= "inIframe";
    public static final String AD_VISIBILITY_PARAM 		= "adVisibility";
    public static final String NETWORK_TYPE_PARAM 		= "nettype";
    public static final String UDID_PARAM 				= "udid";
    public static final String UDID_TYPE_PARAM 			= "udidtype";
    public static final String UDID_HASH_PARAM 			= "udidhash";
    public static final String IAB_CATEGORY             = "iabcat";
    public static final String DMA                      = "dma";


    //PubMatic specific common parameters for POST Parameters
    public static final String DID_PARAM 				= "did";
    public static final String DPID_PARAM 				= "dpid";
    public static final String LANGUAGE 				= "lang";
    public static final String COUNTRY_PARAM 			= "country";
    public static final String CARRIER_PARAM 			= "carrier";
    public static final String MAKE_PARAM 				= "make";
    public static final String MODEL_PARAM 				= "model";
    public static final String OS_PARAM 				= "os";
    public static final String OSV_PARAM 				= "osv";
    public static final String JS_PARAM 				= "js";
    public static final String LOC_PARAM 				= "loc";
    public static final String LOC_SOURCE_PARAM 		= "loc_source";
    public static final String VER_PARAM 				= "ver";
    public static final String BUNDLE_PARAM 			= "bundle";
    public static final String AD_ORIENTATION_PARAM 	= "adOrientation";
    public static final String DEVICE_ORIENTATION_PARAM = "deviceOrientation";
    public static final String AD_REFRESH_RATE_PARAM 	= "adRefreshRate";
    public static final String YOB_PARAM 				= "yob";
    public static final String GENDER_PARAM 			= "gender";
    public static final String ZIP_PARAM 				= "zip";
    public static final String KEYWORDS_PARAM 			= "keywords";
    public static final String AREACODE 				= "areaCode";
    public static final String USER_INCOME 				= "inc";
    public static final String USER_ETHNICITY 			= "ethn";
    public static final String USER_CITY 				= "city";
    public static final String USER_STATE 				= "state";
    public static final String SDK_ID_PARAM 			= "msdkId";
    public static final String SDK_VER_PARAM 			= "msdkVersion";
    public static final String MULTI_SIZE_PARAM 		= "multisize";

}
