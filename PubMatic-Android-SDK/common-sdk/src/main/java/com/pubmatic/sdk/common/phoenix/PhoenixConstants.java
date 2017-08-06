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
package com.pubmatic.sdk.common.phoenix;

/**
 *
 */
public class PhoenixConstants {

    public static final String DATE_TIME_FORMAT         = "yyyy-MM-dd HH:mm:ss";
    public static final String AD_UNIT_PARAM 	        = "au";
    public static final String IMPRESSION_ID_PARAM 	    = "iid";
    public static final String AD_SIZE_PARAM 	        = "asz";
    public static final String PAGE_URL_PARAM 			= "purl";
    public static final String RANDOM_NUMBER_PARAM 	    = "rndn";
    public static final String TIME_STAMP_PARAM 	    = "kltstamp";
    public static final String R_URL_PARAM 			    = "rurl";
    public static final String IN_IFRAME_PARAM 			= "iifr";
    public static final String SCREEN_PARAM 			= "scrn";
    public static final String TIME_ZONE_PARAM 			= "tz";
    public static final String RESPONSE_FORMAT_PARAM    = "res_format";
    public static final String REQUEST_TYPE_PARAM       = "req_type";
    public static final String LATITUDE_PARAM           = "lat";
    public static final String LONGITUDE_PARAM          = "lon";
    public static final String LOCATION_SOURCE_PARAM    = "lsrc";
    public static final String SOURCE_PARAM             = "src";
    public static final String DEBUG_PARAM              = "d";
    public static final String VISIBLE_AD_POSITION_PARAM= "visi";
    public static final String GLOBAL_KEYWORD_PARAM     = "gkv";
    public static final String SDK_VERSION_PARAM     = "msdkVersion";

    public static final String URL_ENCODING 			= "UTF-8";

    //SSP related parameters;
    public static final String  IAP_CATEGORY_PARAM      = "iabcat"; //Publisher
    public static final String  APP_NAME_PARAM          = "aname";  //SDK
    public static final String  APP_ID_PARAM            = "aid";    //SDK
    public static final String  BUNDLE_PARAM            = "bundle"; //SDK
    public static final String  APP_CATEGORY_PARAM      = "acat";   //Publisher
    public static final String  APP_API_PARAM           = "api";    //Hardcoded
    public static final String  STORE_URL_PARAM         = "storeurl";//Publisher
    public static final String  APP_VERSION_PARAM       = "aver";   //SDK
    public static final String  APP_DOMAIN_PARAM        = "appdomain";//Publisher
    public static final String  APP_PAID_PARAM          = "apaid";  //Publisher
    public static final String  UDID_PARAM              = "udid";   //SDK
    public static final String  UDID_TYPE_PARAM         = "udidtype";//SDK
    public static final String  UDID_HASH_PARAM         = "udidhash";//SDK
    public static final String  NETWORK_TYPE_PARAM      = "nettype";//SDK
    public static final String  CARRIER_PARAM           = "carrier";//SDK
    public static final String  JS_PARAM                = "js";//SDK
    public static final String  AD_ORIENTATION_PARAM    = "adOrientation";
    public static final String  DEVICE_ORIENTATION_PARAM= "deviceOrientation";//SDK
    public static final String  AWT_PARAM               = "awt";
    public static final String  PM_ZONE_ID_PARAM        = "pmZoneId";
    public static final String  SITE_CODE_PARAM         = "sitecode";
    public static final String  INTERSTITIAL_FLAG_PARAM = "instl";     //SDK
    public static final String  SECURE_FLAG_PARAM       = "sec";
    public static final String  AD_TRUTH_PARAM          = "at_payload";
    public static final String  AD_FLOOR_PARAM          = "kadfloor";
    public static final String  BATTR_PARAM             = "battr";
    public static final String  BLK_DOMAIN_IDS_PARAM    = "blkdmns";//Publisher
    public static final String  BLK_ADV_IDS_PARAM       = "blkadvtids";//Publisher
    public static final String  BLK_ADV_DOMAIN_PARAM    = "blkadvtdmns";//Publisher
    public static final String  BLK_IAB_CATEG_PARAM     = "blkiabcats";//Publisher
    public static final String  DNT_PARAM               = "dnt";
    public static final String  COPPA_PARAM             = "coppa";

    // Native Specific constants
    public static final String  NATIVE_TEMPLATE_ID      = "ntid";
    public static final String  NATIVE_INPUT            = "ntI";

}
