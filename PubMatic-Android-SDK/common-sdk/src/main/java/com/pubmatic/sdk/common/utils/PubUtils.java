package com.pubmatic.sdk.common.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

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

}
