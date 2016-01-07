package com.pubmatic.sdk.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.DisplayMetrics;

import java.security.MessageDigest;

public class PubUtils {
	

	public static String getUseragent() {
		return "Android 2.3";
	}
	
	public static int dpToPx(int dp) {
		DisplayMetrics displayMetrics = Resources.getSystem()
				.getDisplayMetrics();
		int px = (int) (dp * displayMetrics.density + .5f);
		return px;
	}

	public static int pxToDp(float px) {
		DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
		int dp = (int) (px / displayMetrics.density + .5f);
		return dp;
	}

	public static String getNetworkType(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo= cm.getActiveNetworkInfo();
		switch (networkInfo.getType()) {
			case ConnectivityManager.TYPE_MOBILE:
				return "cellular";
			case ConnectivityManager.TYPE_WIFI:
				return  "wifi";
			default:
				return null;
		}
	}

	public static String getUdidFromContext(Context context) {
		String deviceId = Settings.Secure.getString(
				context.getContentResolver(), Settings.Secure.ANDROID_ID);
		deviceId = (deviceId == null) ? "" : sha1(deviceId);
		return deviceId;

	}

	@SuppressLint("DefaultLocale")
	public static String sha1(String string) {
		StringBuilder stringBuilder = new StringBuilder();

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = string.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);
			bytes = digest.digest();

			for (final byte b : bytes) {
				stringBuilder.append(String.format("%02X", b));
			}

			return stringBuilder.toString().toLowerCase();
		} catch (Exception e) {
			return "";
		}
	}

}
