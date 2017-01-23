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
 * Created by Sagar on 12/28/2016.
 */
public final class ConfigurationManager {

    public enum PLATFORM { MOCEAN, PUBMATIC, PHEONIX };
    public enum AD_TYPE { BANNER, NATIVE };

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

        JSONArray platformJsonArray = null;
        JSONArray adTypeJsonArray = null;

        String platformKey = null;
        String adTypeKey = null;

        if(platform == PLATFORM.MOCEAN)
            platformKey = "Mocean";
        else if(platform == PLATFORM.PUBMATIC)
            platformKey = "PubMatic";
        else if(platform == PLATFORM.PHEONIX)
            platformKey = "Phoenix";

        if(adType == AD_TYPE.BANNER)
            adTypeKey = "Banner";
        else if(adType == AD_TYPE.NATIVE)
            adTypeKey = "Native";

        try
        {
            platformJsonArray = mSettingsJson.getJSONArray(platformKey);

            for(int i = 0 ; i < platformJsonArray.length() ; i++)
            {
                adTypeJsonArray = platformJsonArray.getJSONObject(i).optJSONArray(adTypeKey);

                if(adTypeJsonArray != null)
                    break;
            }

            if(adTypeJsonArray != null)
            {
                settings = getSettingItems(adTypeJsonArray);
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        return settings;
    }

    private LinkedHashMap<String, LinkedHashMap<String, String>> getSettingItems(JSONArray adTypeJsonArray) {

        LinkedHashMap<String, LinkedHashMap<String, String>> settings = new LinkedHashMap<>();

        try {

            for (int j = 0; j < adTypeJsonArray.length(); j++) {
                String settingHeader = adTypeJsonArray.getJSONObject(j).getString("name");
                JSONObject optionsJsonObject = adTypeJsonArray.getJSONObject(j).getJSONObject("options");

                Iterator<String> iter = optionsJsonObject.keys();

                LinkedHashMap<String, String> options = new LinkedHashMap<>();

                while (iter.hasNext()) {
                    String key = iter.next();
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
                File myFile = new File("/storage/emulated/0/Automation/settings");
                inputStream = new FileInputStream(myFile);
            }
            else
            {
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
