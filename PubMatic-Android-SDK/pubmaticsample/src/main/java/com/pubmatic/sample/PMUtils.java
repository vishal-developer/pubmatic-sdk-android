package com.pubmatic.sample;

/**
 * Created by Sagar on 1/4/2017.
 */

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
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_DO_NOT_TRACK))
                id = R.id.configuration_do_not_track;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_ANDROID_AID_ENABLED))
                id = R.id.configuration_android_aid_enabled;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_COPPA))
                id = R.id.configuration_coppa;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_TIME_OUT_INTERVAL))
                id = R.id.configuration_time_out_interval;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_AD_REFRESH_RATE))
                id = R.id.configuration_ad_refresh_rate;
            else if(setting.equals(PMConstants.SETTINGS_CONFIGURATION_AD_SERVER_URL))
                id = R.id.configuration_ad_server_url;
        }
        else if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_TARGETTING))
        {
            if(setting.equals(PMConstants.SETTINGS_TARGETTING_USER_AGENT))
                id = R.id.targetting_user_agent;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_LOCATION))
                id = R.id.targetting_location;
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
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_YEAR_OF_BIRTH))
                id = R.id.targetting_year_of_birth;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_INCOME))
                id = R.id.targetting_income;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_DMA))
                id = R.id.targetting_dma;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_LANGUAGE))
                id = R.id.targetting_language;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_ORMA_COMPLIANCE))
                id = R.id.targetting_orma_compliance;
        }

        return id;
    }
}
