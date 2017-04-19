/*

 * PubMatic Inc. ("PubMatic") CONFIDENTIAL

 * Unpublished Copyright (c) 2006-2014 PubMatic, All Rights Reserved.

 *

 * NOTICE:  All information contained herein is, and remains the property of PubMatic. The intellectual and technical concepts contained

 * herein are proprietary to PubMatic and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.

 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained

 * from PubMatic.  Access to the source code contained herein is hereby forbidden to anyone except current PubMatic employees, managers or contractors who have executed 

 * Confidentiality and Non-disclosure agreements explicitly covering such access.

 *

 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code, which includes  

 * information that is confidential and/or proprietary, and is a trade secret, of  PubMatic.   ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, 

 * OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS  SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF PubMatic IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE 

 * LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS  

 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.                

 */
package com.pubmatic.sdk.common.pubmatic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.webkit.WebView;

import com.pubmatic.sdk.common.CommonConstants;

public final class PUBDeviceInformation implements LocationListener {

	private static Context mApplicationContext = null;

	public String mDeviceMake = null;
	public String mDeviceModel = null;
	public String mDeviceOSName = null;
	public String mDeviceOSVersion = null;

	public String mApplicationName = null;
	public String mApplicationVersion = null;
	public String mPackageName = null;

	public String mPageURL = null;

	public String mDeviceCountryCode = null;
	public String mDeviceIpAddress = null;
	public String mDeviceUserAgent = null;
	public String mCarrierName = null;
	public String mDeviceAcceptLanguage = null;
	public String mDeviceScreenResolution = null;

	public double mDeviceTimeZone = 0.0;
	public String mDeviceLocation = "";
	// Hard Coded Values
	public static int mJavaScriptSupport = DeviceConstants.mPubDeviceJavaScriptSupport;
	public static int mAdVisibility = DeviceConstants.mAdVisibility;
	public static int mInIframe = DeviceConstants.mInIframe;
	public static final String mAdPosition = DeviceConstants.mAdPosition;
	public static final String msdkVersion = CommonConstants.SDK_VERSION;
	private static PUBDeviceInformation instance = null;

	/**
	 * Constructor
	 */
	private PUBDeviceInformation(Context context) {
		// Get the application context
		mApplicationContext = context;

		// Get the device ODIN number
		mDeviceMake = Build.MANUFACTURER;
		mDeviceModel = Build.MODEL;
		mDeviceOSName = DeviceConstants.mDeviceOsName;
		mDeviceOSVersion = Build.VERSION.RELEASE;

		// Get the device screen resolution
		WindowManager window = (WindowManager) mApplicationContext
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = window.getDefaultDisplay();
		mDeviceScreenResolution = display.getWidth() + "x"
				+ display.getHeight();

		// Get the system time and time zone
		Calendar cal = Calendar.getInstance();
		mDeviceTimeZone = (double) cal.getTimeZone().getRawOffset() / 3600000;

		// Get the user agent string
		WebView webView = new WebView(mApplicationContext);
		mDeviceUserAgent = webView.getSettings().getUserAgentString();
		webView = null;

		// Get the device country code using local and accept language
		mDeviceAcceptLanguage = Locale.getDefault().toString();

		// Get the carrier name
		TelephonyManager telephonyManager = ((TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE));
		mCarrierName = telephonyManager.getNetworkOperatorName();
		if (telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
			String str = telephonyManager.getNetworkCountryIso();
			if (str.length() <= 0)
				str = telephonyManager.getSimCountryIso();

			Locale test = new Locale(Locale.getDefault().getLanguage(), str);
			mDeviceCountryCode = test.getISO3Country();
			test = null;
			str = null;
		}

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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			info = null;
			manager = null;
		}

		mDeviceIpAddress = getDeviceIpAddress();
	}

	// Return the same instance
	public synchronized static PUBDeviceInformation getInstance(Context context) {
		if (instance == null) {
			instance = new PUBDeviceInformation(context);
		}
		return instance;
	}

	// Returns the current system time
	public synchronized static String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				DeviceConstants.mDateTimeFormat);
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

	public synchronized static int getScreenOrientation() {
		WindowManager window = (WindowManager) mApplicationContext
				.getSystemService(Context.WINDOW_SERVICE);
		Display getOrient = window.getDefaultDisplay();
		int orientation = Configuration.ORIENTATION_UNDEFINED;
		if (getOrient.getWidth() == getOrient.getHeight()) {
			orientation = 0;
		} else {
			if (getOrient.getWidth() < getOrient.getHeight()) {
				orientation = 0;
			} else {
				orientation = 1;
			}
		}
		return orientation;
	}

	public synchronized int getDeviceOrientation() {
		// 1 for landscape
		// 0 for portrait

		Display display = ((WindowManager) mApplicationContext
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		int rotation = display.getRotation();

		// If a device has a naturally tall screen, and the user has turned it
		// on its side to go into a landscape orientation, the value returned
		// here may be either Surface.ROTATION_90 or Surface.ROTATION_270
		// depending on the direction it was turned.
		if (display.getWidth() == display.getHeight()) {
			if (rotation == Surface.ROTATION_90
					|| rotation == Surface.ROTATION_270) {
				rotation = 1;
			}
			else
			// Device will always remain in potrait mode
			rotation = 0;
		} else if (display.getWidth() < display.getHeight()) 
				{
						if (rotation == Surface.ROTATION_90
								|| rotation == Surface.ROTATION_270) 
						{
							rotation = 1;
						}else
						{
							// Device is in portrait mode
							rotation = 0;
						}
				} else 
				{
						if (rotation == Surface.ROTATION_90
										|| rotation == Surface.ROTATION_270)
						{
							rotation = 1;
						}else
						{
							// Device is in landscape mode
							rotation = 1;
						}
				}

		return rotation;
	}

	private synchronized static String getDeviceIpAddress() {
		WifiManager wifiManager = (WifiManager) mApplicationContext
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null) {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo != null) {
				String strIpAdd = Formatter.formatIpAddress(wifiInfo
						.getIpAddress());
				return strIpAdd;
			}
			return null;
		}
		return null;
	}

	public void onLocationChanged(Location location) {
		if (location != null) {
			mDeviceLocation = location.getLatitude() + ","
					+ location.getLongitude();
		}
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}
}
