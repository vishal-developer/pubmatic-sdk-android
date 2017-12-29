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
package com.pubmatic.sdk.common;

import java.io.IOException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

public final class AdvertisingIdClient {

    private final static String PM_LIMITED_TRACKING_AD_KEY 	= "limited_tracking_ad_key";
    private final static String PM_AID_STORAGE 				= "aid_shared_preference";
    private final static String PM_AID_KEY 					= "aid_key";

    public static final class AdInfo {
        private final String advertisingId;
        private final boolean limitAdTrackingEnabled;

        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.advertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.limitAdTrackingEnabled;
        }
    }

    /**
     * Refresh the advertising info saved in local storage asynchronously.
     * @param context
     * @return Returns the advertising info saved in local storage before refresh.
     */
    public static AdInfo refreshAdvertisingInfo(final Context context) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    com.google.android.gms.ads.identifier.AdvertisingIdClient.Info adInfo = com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(context);
                    String adId = adInfo != null ? adInfo.getId() : null;

                    Log.d("AdvertisingIdClient", "AdvertisingIdClient :: id = "+adId+", isLimitAdTrackingEnabled = "+adInfo.isLimitAdTrackingEnabled());

                    saveAndroidAid(context, adId);
                    saveLimitedAdTrackingState(context, adInfo.isLimitAdTrackingEnabled());
                } catch (IOException | GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException exception) {
                    // Error handling if needed
                }
            }
        });

        AdInfo adInfo = new AdInfo(AdvertisingIdClient.getAndroidAid(context, null),
                AdvertisingIdClient.getLimitedAdTrackingState(context, true));
        return adInfo;
    }

    /**
     * Save the Android advertisement id in local storage for further use.
     */
    public static void saveAndroidAid(final Context context, String androidAid) {
        //Save the android_aid in local storage & use for next ad request
        SharedPreferences storage = context.getSharedPreferences(PM_AID_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        if(editor!=null) {
            editor.putString(PM_AID_KEY, androidAid);
            editor.apply();
        }
    }

    /**
     * Returns the Android advertisement id if saved in local storage else returns given androidAid.
     */
    public static String getAndroidAid(Context context, String androidAid) {

        if(context!=null) {
            //Save the android_aid in local storage & use for next ad request
            SharedPreferences storage = context.getSharedPreferences(PM_AID_STORAGE, Context.MODE_PRIVATE);
            return storage.getString(PM_AID_KEY, androidAid);
        }
        return androidAid;
    }

    /**
     * Save the Android advertisement id in local storage for further use.
     */
    public static void saveLimitedAdTrackingState(final Context context, boolean state) {
        //Save the android_aid in local storage & use for next ad request
        SharedPreferences storage = context.getSharedPreferences(PM_AID_STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = storage.edit();
        if(editor!=null) {
            editor.putBoolean(PM_LIMITED_TRACKING_AD_KEY, state);
            editor.apply();
        }
    }

    /**
     * Returns the Android advertisement id if saved in local storage else returns given state.
     */
    public static boolean getLimitedAdTrackingState(Context context, boolean state) {

        if(context!=null) {
            //Save the android_aid in local storage & use for next ad request
            SharedPreferences storage = context.getSharedPreferences(PM_AID_STORAGE, Context.MODE_PRIVATE);
            return storage.getBoolean(PM_LIMITED_TRACKING_AD_KEY, state);
        }
        return state;
    }


}
