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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.pubmatic.sdk.common.CommonConstants;

import java.util.Calendar;
import java.util.Locale;


public final class PUBDeviceInformation {

	public String mDeviceMake = null;
	public String mDeviceModel = null;
	public String mDeviceOSName = null;
	public String mDeviceOSVersion = null;

	public String mApplicationName = null;
	public String mApplicationVersion = null;
	public String mPackageName = null;

	public String mPageURL = null;

	public String mDeviceCountryCode = null;
	public String mCarrierName = null;
	public String mDeviceAcceptLanguage = null;
	public String mDeviceScreenResolution = null;

	public double mDeviceTimeZone = 0.0;
	// Hard Coded Values
	public int mJavaScriptSupport = 1;
	public int mAdVisibility = 0;
	public int mInIframe = 0;
	public final String mAdPosition = "-1x-1";
	public final String msdkVersion = CommonConstants.SDK_VERSION;
	private static PUBDeviceInformation instance = null;
	private static String sUserAgent=null;

	/**
	 * Constructor
	 */
	private PUBDeviceInformation(Context context) {
		// Get the application context


		// Get the device ODIN number
		mDeviceMake = Build.MANUFACTURER;
		mDeviceModel = Build.MODEL;
		mDeviceOSName = "Android";//DeviceConstants.mDeviceOsName;
		mDeviceOSVersion = Build.VERSION.RELEASE;

		// Get the device screen resolution
		WindowManager window = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = window.getDefaultDisplay();
		mDeviceScreenResolution = display.getWidth() + "x"
								+ display.getHeight();

		// Get the system time and time zone
		Calendar cal = Calendar.getInstance();
		mDeviceTimeZone = (double) cal.getTimeZone().getRawOffset() / 3600000;

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
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(context.getPackageName(),
					0);
			mApplicationName = info.applicationInfo.loadLabel(manager).toString();
			mPackageName = context.getPackageName();
			mApplicationVersion = info.versionName;
			//mPageURL = mApplicationName + "_" + mApplicationVersion;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			info = null;
			manager = null;
		}
	}

	// Return the same instance
	public synchronized static PUBDeviceInformation getInstance(Context context) {
		if (instance == null) {
			instance = new PUBDeviceInformation(context);
		}
		return instance;
	}

	public static String getUserAgent(final Context context) {

		if(sUserAgent==null) {

			// Return the user agent from system properties only for first time
			String ua;
			try {
				ua = System.getProperty("http.agent");
			} catch(Exception e) {
				ua = "PubMatic Android SDK v"+CommonConstants.SDK_VERSION;
			}
			// Delegate the UA in main thread from worker thread.
			// It will not block the UI thread on app launch.
			new Thread(new Runnable() {
				@Override
				public void run() {

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							// this will run in the main thread
							if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1) {
								sUserAgent = WebSettings.getDefaultUserAgent(context);
							} else {
								sUserAgent = (new WebView(context)).getSettings().getUserAgentString();
							}
						}
					});

				}
			}).start();
			return ua;
		}
		return sUserAgent;
	}
}
