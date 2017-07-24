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
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebView;

import com.pubmatic.sdk.common.CommonConstants;


public final class PUBDeviceInformation {

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
	//public String mDeviceIpAddress = null;
	public String mDeviceUserAgent = null;
	public String mCarrierName = null;
	public String mDeviceAcceptLanguage = null;
	public String mDeviceScreenResolution = null;

    public String mDeviceTimeStamp = "";
	public double mDeviceTimeZone = 0.0;
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
			//mPageURL = mApplicationName + "_" + mApplicationVersion;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			info = null;
			manager = null;
		}

		//mDeviceIpAddress = getDeviceIpAddress();

        mDeviceTimeStamp = getCurrentTimeStamp();
	}

	// Return the same instance
	public synchronized static PUBDeviceInformation getInstance(Context context) {
		if (instance == null) {
			instance = new PUBDeviceInformation(context);
		}
		return instance;
	}

	@Deprecated
	private synchronized static String getDeviceIpAddress() {
		/*WifiManager wifiManager = (WifiManager) mApplicationContext
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null) {
			try {
				//It requires ACCESS_WIFI_STATE permission
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (wifiInfo != null) {
					String strIpAdd = Formatter.formatIpAddress(wifiInfo
							.getIpAddress());
					return strIpAdd;
				}
			} catch (SecurityException e) {
				Log.e(TAG, "Unable to get IP address using WiFiManager.");
			}
			return null;
		}*/
		return null;
	}

	/**
	 *
	 * @return yyyy-MM-dd HH:mm:ss formate date as string
	 */
	private static String getCurrentTimeStamp(){
		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			String currentDateTime = dateFormat.format(new Date()); // Find todays date

			return currentDateTime;
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}
}
