package com.pubmatic.sdk.common.phoenix;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by shrawangupta on 24/02/16.
 */
public class PhoenixDeviceInformation {

    private static PhoenixDeviceInformation instance    = null;
    private static Context mApplicationContext          = null;


    public String mApplicationName                      = null;
    public String mApplicationVersion                   = null;
    public String mPackageName                          = null;
    public String mPageURL                              = null;
    public String mDeviceScreenResolution               = null;

    //Constant values for SDK
    public static int mInIframe                         = 0;

    /**
     * Constructor
     */
    private PhoenixDeviceInformation(Context context) {
        // Get the application context
        mApplicationContext = context;

        // Get the application name and version number
        PackageManager manager = mApplicationContext.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(mApplicationContext.getPackageName(),
                    0);
            mApplicationName = info.applicationInfo.loadLabel(manager).toString();
            mPackageName = mApplicationContext.getPackageName();
            mApplicationVersion = info.versionName;
            mPageURL = mApplicationName + "_" + mApplicationVersion;


            // Get the device screen resolution
            WindowManager window = (WindowManager) mApplicationContext
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = window.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            mDeviceScreenResolution = metrics.widthPixels + "x"
                    + metrics.heightPixels;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            info = null;
            manager = null;
        }
    }


    // Return the same instance
    public synchronized static PhoenixDeviceInformation getInstance(Context context) {
        if (instance == null) {
            instance = new PhoenixDeviceInformation(context);
        }
        return instance;
    }

    // Returns the current system time
    public synchronized static String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                PhoenixConstants.DATE_TIME_FORMAT);
        String systemTime = simpleDateFormat.format(cal.getTime());
        return systemTime;
    }

    // Generate the random number in between 0 to 1
    public synchronized static float getRandomNumber() {
        float randomNumber = (float) Math.random();
        while (randomNumber >= 1)
            randomNumber = randomNumber / 10;
        return randomNumber;
    }

    //
    public static String getTimeZoneOffset() {
        String localTime = null;
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                    Locale.getDefault());
            Date currentLocalTime = calendar.getTime();
            SimpleDateFormat date = new SimpleDateFormat("Z");
            localTime = date.format(currentLocalTime);
            return localTime.substring(0, 3) + "."+ localTime.substring(3, 5);
        } catch (Exception e) {

        }
        return localTime;
    }
}
