package com.pubmatic.sdk.banner;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

public class BannerUtils {

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
}
