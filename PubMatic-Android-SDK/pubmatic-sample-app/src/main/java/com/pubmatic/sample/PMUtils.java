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
package com.pubmatic.sample;

public class PMUtils {

    public static int getId(String settingsHeader, String setting)
    {
        int id = 0;

        if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_AD_TAG))
        {
            if(setting.equals(PMConstants.SETTINGS_AD_TAG_ZONE))
                id = R.id.ad_tag_zone;
            else if(setting.equals(PMConstants.SETTINGS_AD_TAG_PUB_ID))
                id = R.id.ad_tag_pub_id;
            else if(setting.equals(PMConstants.SETTINGS_AD_TAG_SITE_ID))
                id = R.id.ad_tag_site_id;
            else if(setting.equals(PMConstants.SETTINGS_AD_TAG_AD_ID))
                id = R.id.ad_tag_ad_id;
        }
        else if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_CONFIGURATION))
        {
            if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_WIDTH))
                id = R.id.configuration_width;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_HEIGHT))
                id = R.id.configuration_height;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED))
                id = R.id.configuration_android_aid_enabled;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_COPPA))
                id = R.id.configuration_coppa;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_TIME_OUT_INTERVAL))
                id = R.id.configuration_time_out_interval;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_AD_REFRESH_RATE))
                id = R.id.configuration_ad_refresh_rate;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_TEST))
                id = R.id.configuration_test;
        }
        else if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_TARGETTING))
        {
            if(setting.equals(PMConstants.SETTINGS_TARGETTING_LATITUDE))
                id = R.id.targetting_latitude;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_LONGITUDE))
                id = R.id.targetting_longitude;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_APP_CATEGORY))
                id = R.id.targetting_app_category;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_IAB_CATEGORY))
                id = R.id.targetting_iab_category;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_STORE_URL))
                id = R.id.targetting_store_url;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_APP_NAME))
                id = R.id.targetting_app_name;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_APP_DOMAIN))
                id = R.id.targetting_app_domain;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_CITY))
                id = R.id.targetting_city;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_ZIP))
                id = R.id.targetting_zip;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_COUNTRY))
                id = R.id.targetting_country;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_STATE))
                id = R.id.targetting_state;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH))
                id = R.id.targetting_year_of_birth;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_INCOME))
                id = R.id.targetting_income;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_ETHNICITY))
                id = R.id.targetting_ethnicity;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_PAID))
                id = R.id.targetting_paid;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_DMA))
                id = R.id.targetting_dma;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_LANGUAGE))
                id = R.id.targetting_language;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE))
                id = R.id.targetting_orma_compliance;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_IP))
                id = R.id.targetting_ip;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_AWT))
                id = R.id.targetting_awt;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_AGE))
                id = R.id.targetting_age;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_GENDER))
                id = R.id.targetting_gender;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_OVER_18))
                id = R.id.targetting_over_18;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_ISO_REGION))
                id = R.id.targetting_iso_region;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_BIRTHDAY))
                id = R.id.targetting_birthday;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_AREA))
                id = R.id.targetting_area;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_TIMEOUT))
                id = R.id.targetting_timeout;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_KEYWORDS))
                id = R.id.targetting_keywords;
        }

        return id;
    }
}
