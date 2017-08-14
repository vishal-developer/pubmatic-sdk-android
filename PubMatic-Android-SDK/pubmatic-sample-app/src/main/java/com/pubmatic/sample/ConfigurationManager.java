package com.pubmatic.sample;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *
 */
public final class ConfigurationManager {

    public enum PLATFORM { PUBMATIC };
    public enum AD_TYPE { BANNER, INTERSTITIAL, NATIVE };

    private JSONObject mSettingsJson;
    private Context mContext;

    private static ConfigurationManager instance = null;

    private ConfigurationManager(Context context) {
        mContext = context;
        mSettingsJson = readSettings(context);
    }

    public static synchronized ConfigurationManager getInstance(Context context) {

        if(instance == null)
            instance = new ConfigurationManager(context);

        return instance;
    }

    public LinkedHashMap<String, LinkedHashMap<String, String>> getSettings(PLATFORM platform, AD_TYPE adType)
    {
        LinkedHashMap<String, LinkedHashMap<String, String>> settings = null;

        JSONObject platformJsonObject = null;
        JSONObject adTypeJsonObject = null;

        String platformKey = null;
        String adTypeKey = null;

        if(platform == PLATFORM.PUBMATIC)
            platformKey = "PubMatic";

        if(adType == AD_TYPE.BANNER)
            adTypeKey = "Banner";
        else if(adType == AD_TYPE.INTERSTITIAL)
            adTypeKey = "Interstitial";
        else if(adType == AD_TYPE.NATIVE)
            adTypeKey = "Native";

        try
        {
            platformJsonObject = mSettingsJson.getJSONObject(platformKey);
            adTypeJsonObject = platformJsonObject.getJSONObject(adTypeKey);

            if(adTypeJsonObject != null)
            {
                settings = getSettingItems(adTypeJsonObject);
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        return settings;
    }

    private LinkedHashMap<String, LinkedHashMap<String, String>> getSettingItems(JSONObject adTypeJsonObject) {

        LinkedHashMap<String, LinkedHashMap<String, String>> settings = new LinkedHashMap<>();

        try {

            String settingHeader;
            JSONObject optionsJsonObject;

            Iterator<String> adTypeIterator = adTypeJsonObject.keys();

            while (adTypeIterator.hasNext()) {

                settingHeader = adTypeIterator.next();
                optionsJsonObject = adTypeJsonObject.getJSONObject(settingHeader);

                Iterator<String> optionsIterator = optionsJsonObject.keys();

                LinkedHashMap<String, String> options = new LinkedHashMap<>();

                while (optionsIterator.hasNext()) {
                    String key = optionsIterator.next();
                    try {
                        String value = optionsJsonObject.getString(key);

                        options.put(key, value);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                settings.put(settingHeader, options);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return settings;
    }

    private JSONObject readSettings(Context context)
    {
        BufferedReader reader = null;
        StringBuffer settings = new StringBuffer();
        JSONObject settingsJson = null;
        InputStream inputStream = null;
        String str;

        try
        {
            if (shouldImportSettings())
            {
                Log.i("Configuration Manager", "Reading from external storage");

                File myFile = new File("/storage/emulated/0/Automation/settings");
                inputStream = new FileInputStream(myFile);
            }
            else
            {
                Log.i("Configuration Manager", "Reading from internal storage");

                inputStream = context.getAssets().open("settings");
            }


            BufferedReader in= new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            while ((str = in.readLine()) != null) {
                settings.append(str);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        try {
            if(settings.toString() != null && !settings.toString().equals(""))
                settingsJson = new JSONObject(settings.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return settingsJson;
    }

    private boolean shouldImportSettings()
    {
        //File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/Automation", "settings" );
        File file = new File("/storage/emulated/0/Automation/settings");

        if (file.exists())
            return true;
        else
            return false;
    }
}
