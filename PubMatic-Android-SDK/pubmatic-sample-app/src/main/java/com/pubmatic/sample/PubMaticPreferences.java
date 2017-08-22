package com.pubmatic.sample;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class PubMaticPreferences {

	private static String PREFERENCE_FILE_NAME = "PUBMATIC_PREFERENCE";

	public static String PREFERENCE_KEY_USE_INTERNAL_BROWSER = "KEY_USE_INTERNAL_BROWSER";
	public static String PREFERENCE_KEY_AUTO_LOCATION_DETECTION = "KEY_AUTO_LOCATION_DETECTION";
	
	public static void saveBooleanPreference(Activity context, String key, boolean value)
	{
		if(context!=null) {
			SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putBoolean(key, value);
			editor.commit();
		}
	}

    public static Boolean getBooleanPreference(Activity context, String key)
	{
		if(context!=null) {
			SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
			return sharedPref.getBoolean(key, false);
		} else
			return false;
	}

    public static void deletePreference(Activity context, String key)
	{
		if(context!=null) {
			SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
			sharedPref.edit().remove(key).commit();
		}
	}
}
