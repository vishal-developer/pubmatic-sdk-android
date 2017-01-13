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
        }
        else if(settingsHeader.equals(PMConstants.SETTINGS_HEADING_TARGETTING))
        {
            if(setting.equals(PMConstants.SETTINGS_TARGETTING_CITY))
                id = R.id.targetting_city;
            else if(setting.equals(PMConstants.SETTINGS_TARGETTING_ZIP))
                id = R.id.targetting_zip;
        }

        return id;
    }
}
