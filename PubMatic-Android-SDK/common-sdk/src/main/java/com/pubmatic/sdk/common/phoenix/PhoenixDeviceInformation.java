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
package com.pubmatic.sdk.common.phoenix;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 */
public class PhoenixDeviceInformation {

    private static PhoenixDeviceInformation instance    = null;
    private static Context mApplicationContext          = null;


    public String mApplicationName                      = null;
    public String mApplicationVersion                   = null;
    public String mPackageName                          = null;
    public String mPageURL                              = null;
    public String mDeviceScreenResolution               = null;
    public String mCarrierName = null;

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

            // Get the carrier name
            TelephonyManager telephonyManager = ((TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE));
            mCarrierName = telephonyManager.getNetworkOperatorName();

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

            double minute = Double.valueOf(localTime.substring(3, 5));
            String decimal = String.valueOf(minute/60);
            decimal = decimal.substring(decimal.indexOf("."));

            return localTime.substring(0, 3) + decimal;//"."+ localTime.substring(3, 5);
        } catch (Exception e) {

        }
        return localTime;
    }
}
